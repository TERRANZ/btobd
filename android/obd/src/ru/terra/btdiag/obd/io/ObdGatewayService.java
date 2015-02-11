package ru.terra.btdiag.obd.io;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.inject.Inject;
import org.acra.ACRA;
import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.enums.ObdProtocols;
import ru.terra.btdiag.MainActivity;
import ru.terra.btdiag.R;
import ru.terra.btdiag.activity.ChartActivity;
import ru.terra.btdiag.activity.ConfigActivity;
import ru.terra.btdiag.obd.commands.*;
import ru.terra.btdiag.core.constants.Constants;
import ru.terra.btdiag.core.Logger;
import ru.terra.btdiag.core.SettingsService;
import ru.terra.btdiag.net.core.OBDRest;
import ru.terra.btdiag.net.dto.OBDInfoDto;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

/**
 * This service is primarily responsible for establishing and maintaining a
 * permanent connection between the device where the application runs and a more
 * OBD Bluetooth interface.
 * <p/>
 * Secondarily, it will serve as a repository of ObdCommandJobs and at the same
 * time the application state-machine.
 */
public class ObdGatewayService extends AbstractGatewayService implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String TAG = ObdGatewayService.class.getName();
    /*
     * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
     * #createRfcommSocketToServiceRecord(java.util.UUID)
     *
     * "Hint: If you are connecting to a Bluetooth serial board then try using the
     * well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However if you
     * are connecting to an Android peer then please generate your own unique
     * UUID."
     */
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final IBinder binder = new ObdGatewayServiceBinder();
    @Inject
    SharedPreferences prefs;

    @Inject
    public OBDRest obdRest;

    @Inject
    public SettingsService settingsService;

    private BluetoothDevice dev = null;
    private BluetoothSocket sock = null;
    private BluetoothSocket sockFallback = null;
    private LocationClient mLocationClient;

    public boolean startService() {
        Logger.d(TAG, "Starting service..");

        // get the remote Bluetooth device
        String remoteDevice = prefs.getString(ConfigActivity.BLUETOOTH_LIST_KEY, null);
        if (remoteDevice == null || "".equals(remoteDevice) || remoteDevice == null) {
            Toast.makeText(ctx, "No Bluetooth device selected", Toast.LENGTH_LONG).show();

            // log error
            Logger.w(TAG, "No Bluetooth device has been selected.");

            // TODO kill this service gracefully
            stopService();
            return false;
        }

        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        dev = btAdapter.getRemoteDevice(remoteDevice);
        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();

    /*
     * TODO clean
     *
     * Get more preferences
     */
        ArrayList<ObdCommand> cmds = ConfigActivity.getObdCommands(prefs);

    /*
     * Establish Bluetooth connection
     *
     * Because discovery is a heavyweight procedure for the Bluetooth adapter,
     * this method should always be called before attempting to connect to a
     * remote device with connect(). Discovery is not managed by the Activity,
     * but is run as a system service, so an application should always call
     * cancel discovery even if it did not directly request a discovery, just to
     * be sure. If Bluetooth state is not STATE_ON, this API will return false.
     *
     * see
     * http://developer.android.com/reference/android/bluetooth/BluetoothAdapter
     * .html#cancelDiscovery()
     */
        Log.d(TAG, "Stopping Bluetooth discovery.");
        btAdapter.cancelDiscovery();

        showNotification("Tap to open OBD-Reader", "Starting OBD connection..", R.drawable.ic_launcher, true, true, false);

        try {
            startObdConnection();
        } catch (Exception e) {
            Logger.e(TAG, "There was an error while establishing connection. -> " + e.getMessage(), e);

            // in case of failure, stop this service.
            stopService();
            return false;
        }
        return true;
    }

    /**
     * Start and configure the connection to the OBD interface.
     * <p/>
     * See http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/18786701#18786701
     *
     * @throws java.io.IOException
     */
    private void startObdConnection() throws IOException {
        Logger.d(TAG, "Starting OBD connection..");
        sendStatus("Старт");
        try {
            // Instantiate a BluetoothSocket for the remote device and connect it.
            sock = dev.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            sock.connect();
            sendStatus("Подключено");
        } catch (Exception e1) {
            Logger.e(TAG, "There was an error while establishing Bluetooth connection. Falling back..", e1);
            Class<?> clazz = sock.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
            try {
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};
                sockFallback = (BluetoothSocket) m.invoke(sock.getRemoteDevice(), params);
                sockFallback.connect();
                sock = sockFallback;
            } catch (Exception e2) {
                Logger.e(TAG, "Couldn't fallback while establishing Bluetooth connection. Stopping app..", e2);
                sendStatus("Ошибка: " + e2.getMessage());
                stopService();

                return;
            }
        }

        // Let's configure the connection.
        Logger.d(TAG, "Queing jobs for connection configuration..");
        queueJob(new ObdCommandJob(new ObdResetFixCommand()));
        sendStatus("Сброс адаптера");
        queueJob(new ObdCommandJob(new EchoOffObdCommand()));

    /*
     * Will send second-time based on tests.
     *
     * TODO this can be done w/o having to queue jobs by just issuing
     * command.run(), command.getResult() and validate the result.
     */
        queueJob(new ObdCommandJob(new EchoOffObdCommand()));
        queueJob(new ObdCommandJob(new LineFeedOffCommand()));
        queueJob(new ObdCommandJob(new TimeoutObdCommand(62)));

        // For now set protocol to AUTO
        queueJob(new ObdCommandJob(
                        new SelectProtocolObdCommand(
                                ObdProtocols.valueOf(
                                        settingsService.getSetting(
                                                getString(R.string.obd_protocol),
                                                String.valueOf(ObdProtocols.AUTO.getValue())
                                        )
                                )
                        )
                )
        );
        sendStatus("Выставление протокола");

        // Job for returning dummy data
        queueJob(new ObdCommandJob(new AmbientAirTemperatureObdCommand()));

        queueCounter = 0L;
        Logger.d(TAG, "Initialization jobs queued.");
        sendStatus("В работе");

        isRunning = true;
    }

    /**
     * Runs the queue until the service is stopped
     */
    protected void executeQueue() {
        Logger.d(TAG, "Executing queue..");
        isQueueRunning = true;
        while (!jobsQueue.isEmpty()) {
            ObdCommandJob job = null;
            try {
                job = jobsQueue.take();

                // log job
                Logger.d(TAG, "Taking job[" + job.getId() + "] from queue..");

                if (job.getState().equals(ObdCommandJob.ObdCommandJobState.NEW)) {
                    Logger.d(TAG, "Job state is NEW. Run it..");
                    job.setState(ObdCommandJob.ObdCommandJobState.RUNNING);
                    job.getCommand().run(sock.getInputStream(), sock.getOutputStream());
                } else
                    // log not neww job
                    Logger.w(TAG,
                            "Job state was not new, so it shouldn't be in queue. BUG ALERT!");
            } catch (Exception e) {
                job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                Logger.e(TAG, "Failed to run command. -> " + e.getMessage(), e);
            }

            if (job != null) {
                final ObdCommandJob job2 = job;
                if (ctx instanceof ChartActivity)
                    ((ChartActivity) ctx).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ChartActivity) ctx).stateUpdate(job2);
                        }
                    });
                OBDInfoDto info = new OBDInfoDto();
                info.userId = Integer.parseInt(settingsService.getSetting(Constants.CONFIG_UID, "0"));
                info.command = job.getCommand().getName();
                info.result = job2.getCommand().getFormattedResult();
                info.deviceId = settingsService.getSetting(Constants.CONFIG_DEVID, "");
                if (mLocationClient != null) {
                    if (mLocationClient.isConnected()) {
                        Location lastLocation = mLocationClient.getLastLocation();
                        info.lat = lastLocation.getLatitude();
                        info.lon = lastLocation.getLongitude();
                    }
                }
//                startService(new Intent(this, SendInfoService.class).putExtra("info", info));
                try {
                    Logger.i("SendInfoService", "result = " + obdRest.sendInfo(info));
                } catch (Exception e) {
                    Logger.e(TAG, "Unable to send result to server", e);
                    ACRA.getErrorReporter().handleSilentException(e);
                }
            }
        }
        // will run next time a job is queued
        isQueueRunning = false;
    }

    /**
     * Stop OBD connection and queue processing.
     */
    public void stopService() {
        Logger.d(TAG, "Stopping service..");

        notificationManager.cancel(NOTIFICATION_ID);
        jobsQueue.removeAll(jobsQueue); // TODO is this safe?
        isRunning = false;

        if (sock != null)
            // close socket
            try {
                sock.close();
            } catch (IOException e) {
                Logger.e(TAG, e.getMessage(), e);
            }

        // kill service
        if (mLocationClient != null)
            mLocationClient.disconnect();
        stopSelf();
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class ObdGatewayServiceBinder extends Binder {
        public ObdGatewayService getService() {
            return ObdGatewayService.this;
        }
    }

    private void sendStatus(String text) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(MainActivity.STATUS_RECEIVER).putExtra("type", 0).putExtra("text", text));
    }
}
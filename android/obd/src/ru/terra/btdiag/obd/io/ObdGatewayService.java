package ru.terra.btdiag.obd.io;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.inject.Inject;
import org.acra.ACRA;
import pt.lighthouselabs.obd.enums.ObdProtocols;
import ru.terra.btdiag.R;
import ru.terra.btdiag.activity.ConfigActivity;
import ru.terra.btdiag.core.Logger;
import ru.terra.btdiag.core.SettingsService;
import ru.terra.btdiag.core.constants.Constants;
import ru.terra.btdiag.net.core.OBDRest;
import ru.terra.btdiag.net.dto.OBDInfoDto;
import ru.terra.btdiag.net.task.SendInfoAsyncTask;
import ru.terra.btdiag.obd.io.helper.BtObdConnectionHelper;
import ru.terra.btdiag.obd.io.helper.exception.BTOBDConnectionException;

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

    @Inject
    public OBDRest obdRest;
    @Inject
    public SettingsService settingsService;
    private LocationClient mLocationClient;
    @Inject
    public BtObdConnectionHelper connectionHelper;
    @Inject
    SharedPreferences prefs;

    public boolean startService() {
        try {
            connectionHelper.start(prefs.getString(ConfigActivity.BLUETOOTH_LIST_KEY, null));
        } catch (BTOBDConnectionException e) {
            Logger.e(TAG, "There was an error while starting connection", e);
//            ACRA.getErrorReporter().handleException(e);
            stopService();
            return false;
        }
        try {
            mLocationClient = new LocationClient(this, this, this);
            mLocationClient.connect();
        } catch (Exception e) {
            Logger.e(TAG, "Unable to establish location connect", e);
            ACRA.getErrorReporter().handleSilentException(e);
        }

        try {
            connectionHelper.connect();
        } catch (BTOBDConnectionException e) {
            Logger.e(TAG, "There was an error while establishing connection", e);
            ACRA.getErrorReporter().handleException(e);
            stopService();
            return false;
        }

        connectionHelper.doResetAdapter(ctx);

        ObdProtocols prot = ObdProtocols.valueOf(prefs.getString(getString(R.string.obd_protocol), String.valueOf(ObdProtocols.AUTO.getValue())));
        try {
            connectionHelper.doSelectProtocol(prot, ctx);
        } catch (BTOBDConnectionException e) {
            Logger.e(TAG, "There was an error while selecting protocol", e);
            ACRA.getErrorReporter().handleException(e);
            stopService();
            return false;
        }

        queueCounter = 0L;
        Logger.d(TAG, "Initialization jobs queued.");
        isRunning = true;
        return true;
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
//                    job.getCommand().run(sock.getInputStream(), sock.getOutputStream());
                    connectionHelper.executeCommand(job.getCommand(), ctx);
                } else
                    // log not new job
                    Logger.w(TAG, "Job state was not new, so it shouldn't be in queue. BUG ALERT!");
            } catch (Exception e) {
                job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                Logger.e(TAG, "Failed to run command", e);
            }

            if (job != null) {
                OBDInfoDto info = new OBDInfoDto();
                info.userId = Integer.parseInt(settingsService.getSetting(Constants.CONFIG_UID, "0"));
                info.command = job.getCommand().getName();
                info.result = job.getCommand().getFormattedResult();
                info.deviceId = settingsService.getSetting(Constants.CONFIG_DEVID, "");
                if (mLocationClient != null && mLocationClient.isConnected()) {
                    Location lastLocation = mLocationClient.getLastLocation();
                    info.lat = lastLocation.getLatitude();
                    info.lon = lastLocation.getLongitude();
                }
                new SendInfoAsyncTask(this, obdRest).execute(info);
//                try {
//
//                    Logger.i("SendInfoService", "result = " + obdRest.sendInfo(info));
//                } catch (Exception e) {
//                    Logger.e(TAG, "Unable to send result to server", e);
//                    ACRA.getErrorReporter().handleSilentException(e);
//                }
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
        jobsQueue.removeAll(jobsQueue);
        isRunning = false;

        connectionHelper.disconnect();

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

}
package ru.terra.btdiag.obd.io.helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.enums.ObdProtocols;
import pt.lighthouselabs.obd.exceptions.ObdResponseException;
import ru.terra.btdiag.MainActivity;
import ru.terra.btdiag.activity.ChartActivity;
import ru.terra.btdiag.core.InfoService;
import ru.terra.btdiag.core.Logger;
import ru.terra.btdiag.obd.commands.*;
import ru.terra.btdiag.obd.io.helper.exception.BTOBDConnectionException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Date: 12.02.15
 * Time: 21:20
 */
@Singleton
public class BtObdConnectionHelper {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = BtObdConnectionHelper.class.getName();
    private Context context;
    private BluetoothDevice dev = null;
    private BluetoothSocket sock = null;
    private BluetoothSocket sockFallback = null;
    private String remoteDevice;
    private ConnectionStatus connectionStatus = ConnectionStatus.NC;

    @Inject
    public BtObdConnectionHelper(Context context) {
        this.context = context;
    }

    public void start(String remoteDevice) throws BTOBDConnectionException {
        this.remoteDevice = remoteDevice;
        Logger.d(context, TAG, "Starting service..");
        if (remoteDevice == null || remoteDevice.isEmpty())
            throw new BTOBDConnectionException("No Bluetooth device has been selected.");
        connectionStatus = ConnectionStatus.DEV_SELECTED;
    }

    public void connect() throws BTOBDConnectionException {
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        dev = btAdapter.getRemoteDevice(remoteDevice);
        btAdapter.cancelDiscovery();
        Logger.d(context, TAG, "Starting OBD connection..");
        sendStatus("Старт");
        try {
            // Instantiate a BluetoothSocket for the remote device and connect it.
            sock = dev.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            sock.connect();
            sendStatus("Подключено");
            connectionStatus = ConnectionStatus.CONNECTED;
        } catch (Exception e1) {
            Logger.e(context, TAG, "There was an error while establishing Bluetooth connection. Falling back..", e1);
            Class<?> clazz = sock.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
            try {
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};
                sockFallback = (BluetoothSocket) m.invoke(sock.getRemoteDevice(), params);
                sockFallback.connect();
                sock = sockFallback;
                sendStatus("Подключено");
                connectionStatus = ConnectionStatus.CONNECTED;
            } catch (Exception e2) {
                Logger.e(context, TAG, "Couldn't fallback while establishing Bluetooth connection. Stopping app..", e2);
                sendStatus("Ошибка: " + e2.getMessage());
                disconnect();
                throw new BTOBDConnectionException("Ошибка: " + e2.getMessage());
            }
        }
    }

    public void disconnect() {
        if (sock != null)
            // close socket
            try {
                sock.close();
                connectionStatus = ConnectionStatus.DISCONNECTED;
                sendStatus("Отключено");
            } catch (IOException e) {
                Logger.e(context, TAG, e.getMessage(), e);
            }
        else
            connectionStatus = ConnectionStatus.NC;
    }

    public void doResetAdapter(Context runContext) throws ObdResponseException {
        Logger.d(context, TAG, "Queing jobs for connection configuration..");
        if (executeCommand(new ObdResetFixCommand(), runContext)) {
            sendStatus("Сброс адаптера");
            if (executeCommand(new EchoOffObdCommand(), runContext))
                if (executeCommand(new EchoOffObdCommand(), runContext))
                    if (executeCommand(new LineFeedOffCommand(), runContext))
                        if (executeCommand(new TimeoutObdCommand(62), runContext))
                            connectionStatus = ConnectionStatus.RESETTED;
        }

    }

    public void doSelectProtocol(ObdProtocols prot, Context runContext) throws BTOBDConnectionException {
        // For now set protocol to AUTO

        Logger.d(context, TAG, "Selecting protocol: " + prot.name());
        executeCommand(new SelectProtocolObdCommand(prot), runContext);
        sendStatus("Выставление протокола");
        connectionStatus = ConnectionStatus.PROTOCOL_SELECTED;
        // Job for returning dummy data
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!executeCommand(new AmbientAirTemperatureObdCommand(), runContext)) {
            Logger.w(context, TAG, "Unable to select protocol");
            throw new BTOBDConnectionException("Unable to select protocol");
        }
        sendStatus("В работе");
        connectionStatus = ConnectionStatus.INWORK;
    }

    public boolean executeCommand(final ObdCommand cmd, final Context runContext) throws ObdResponseException {
        try {
            cmd.run(sock.getInputStream(), sock.getOutputStream());
        } catch (Exception e) {
            Logger.e(context, TAG, "Unable to execute command", e);
//            if (e instanceof ObdResponseException)
//                throw (ObdResponseException) e;
            return false;
        }
        if (runContext instanceof ChartActivity)
            ((ChartActivity) runContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ChartActivity) runContext).stateUpdate(cmd);
                }
            });
        return true;
    }

    public BluetoothSocket getSock() {
        return sock;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    private void sendStatus(String text) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(MainActivity.STATUS_RECEIVER).putExtra("type", 0).putExtra("text", text));
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(InfoService.INFO_BROADCAST_ACTION).putExtra("text", text));
    }
}

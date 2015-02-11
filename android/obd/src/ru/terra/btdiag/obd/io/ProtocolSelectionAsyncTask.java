package ru.terra.btdiag.obd.io;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import org.acra.ACRA;
import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.enums.ObdProtocols;
import pt.lighthouselabs.obd.exceptions.MisunderstoodCommandException;
import ru.terra.btdiag.R;
import ru.terra.btdiag.activity.ConfigActivity;
import ru.terra.btdiag.core.AsyncTaskEx;
import ru.terra.btdiag.core.Logger;
import ru.terra.btdiag.core.SettingsService;
import ru.terra.btdiag.obd.commands.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Date: 05.01.15
 * Time: 17:42
 */
public class ProtocolSelectionAsyncTask extends AsyncTaskEx<Void, String, String> {

    private static final String TAG = ProtocolSelectionAsyncTask.class.getName();
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private SettingsService settingsService;
    private BluetoothDevice dev = null;
    private BluetoothSocket sock = null;
    private BluetoothSocket sockFallback = null;

    public ProtocolSelectionAsyncTask(Context a) {
        super(20000l, a);
        settingsService = new SettingsService(a);
        showDialog("Определение протокола", "Запуск...");
    }

    private void connect() {
        try {
            // Instantiate a BluetoothSocket for the remote device and connect it.
            sock = dev.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            sock.connect();
            publishProgress("Подключено");
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
                ACRA.getErrorReporter().handleSilentException(e2);
                Logger.e(TAG, "Couldn't fallback while establishing Bluetooth connection. Stopping app..", e2);
                publishProgress("Ошибка: " + e2.getMessage());
                stopTask();
            }
        }
    }


    @Override
    protected String doInBackground(Void... p) {
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        final String remoteDevice = settingsService.getSetting(ConfigActivity.BLUETOOTH_LIST_KEY, null);
        if (remoteDevice == null || remoteDevice.isEmpty()) {
            publishProgress("Не выбран bluetooth адаптер");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stopTask();
            return null;
        }
        dev = btAdapter.getRemoteDevice(remoteDevice);
        Logger.d(TAG, "Stopping Bluetooth discovery.");
        btAdapter.cancelDiscovery();
        Logger.d(TAG, "Starting OBD connection..");
        publishProgress("Старт");
        boolean found = false;
        int currentProtocol = ObdProtocols.values().length - 1;

        connect();

        int tryes = 0;

        while (!found || tryes < ObdProtocols.values().length + 5) {
            try {
                publishProgress("Сброс адаптера");
                try {
                    tryes++;
                    execCommand(new ObdResetFixCommand());
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (MisunderstoodCommandException e) {
                    Logger.e(TAG, "Controller responses ? on ATZ command, reconnect", e);
                    ACRA.getErrorReporter().handleSilentException(e);
                    stopTask();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e.printStackTrace();
                    }
                    connect();
                    continue;
                }
                execCommand(new EchoOffObdCommand());
                execCommand(new EchoOffObdCommand());
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                execCommand(new LineFeedOffCommand());
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                execCommand(new TimeoutObdCommand(62));

                ObdProtocols protocol = ObdProtocols.values()[currentProtocol];
                publishProgress("Пробуем протокол: " + protocol.name());
                Logger.d(TAG, "Trying protocol " + protocol.name());
                Thread.sleep(500);
                try {
                    execCommand(new TryProtocolCommand(protocol));
                    execCommand(new GetAvailPIDSCommand());
                    found = true;
                    publishProgress("Протокол " + protocol.name() + " подходит");
                    settingsService.saveSetting(context.getString(R.string.obd_protocol), protocol.name());
                } catch (Exception e) {
//                    ACRA.getErrorReporter().handleSilentException(e);
                    publishProgress("Протокол " + protocol.name() + " не подходит");
                    e.printStackTrace();
                }

            } catch (Exception e) {
                publishProgress("Ошибка: " + e.getMessage());
                ACRA.getErrorReporter().handleSilentException(e);
                e.printStackTrace();
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            currentProtocol--;
            if (currentProtocol < 0) {
                publishProgress("Ни один протокол не подошёл");
                found = true;
            }
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stopTask();

        return null;
    }

    private void stopTask() {
        if (sock != null)
            // close socket
            try {
                sock.close();
            } catch (IOException e) {
                Logger.e(TAG, e.getMessage(), e);
                ACRA.getErrorReporter().handleSilentException(e);
            }
    }

    @Override
    protected void onCancelled() {
        stopTask();
        dismissDialog();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        dlg.setMessage(values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        dismissDialog();
    }

    private void execCommand(ObdCommand cmd) throws IOException, InterruptedException {
        cmd.run(sock.getInputStream(), sock.getOutputStream());
        Logger.d(TAG, "Command " + cmd.getName() + " result = " + cmd.getFormattedResult());
    }
}

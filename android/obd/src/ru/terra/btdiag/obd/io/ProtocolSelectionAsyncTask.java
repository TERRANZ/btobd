package ru.terra.btdiag.obd.io;

import android.content.Context;
import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.enums.ObdProtocols;
import ru.terra.btdiag.R;
import ru.terra.btdiag.activity.ConfigActivity;
import ru.terra.btdiag.core.AsyncTaskEx;
import ru.terra.btdiag.core.Logger;
import ru.terra.btdiag.core.SettingsService;
import ru.terra.btdiag.obd.commands.GetAvailPIDSCommand;
import ru.terra.btdiag.obd.commands.TryProtocolCommand;
import ru.terra.btdiag.obd.io.helper.BtObdConnectionHelper;
import ru.terra.btdiag.obd.io.helper.exception.BTOBDConnectionException;

/**
 * Date: 05.01.15
 * Time: 17:42
 */
public class ProtocolSelectionAsyncTask extends AsyncTaskEx<Void, String, String> {

    private static final String TAG = ProtocolSelectionAsyncTask.class.getName();
    private SettingsService settingsService;
    private BtObdConnectionHelper connectionHelper;

    public ProtocolSelectionAsyncTask(Context a, BtObdConnectionHelper connectionHelper) {
        super(300000l, a);
        this.connectionHelper = connectionHelper;
        settingsService = new SettingsService(a);
        showDialog("Определение протокола", "Запуск...");
    }


    @Override
    protected String doInBackground(Void... p) {
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

        boolean found = false;
        int currentProtocol = ObdProtocols.values().length - 1;
        try {
            connectionHelper.start(remoteDevice);
        } catch (BTOBDConnectionException e) {
            publishProgress("Ошибка при подключении: " + e.getMessage());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
            stopTask();
        }

        connect(remoteDevice);

        int tryes = 0;

        while (!found) {
            if (tryes > ObdProtocols.values().length + 5) {
                Logger.w(TAG, "Too many tries");
                break;
            }
            try {
                publishProgress("Сброс адаптера");
                Logger.d(TAG, "Сброс адаптера");
                try {
                    tryes++;
                    connectionHelper.doResetAdapter(context);
                } catch (Exception e) {
                    Logger.e(TAG, "Controller unable to ATZ command, reconnect", e);
//                    ACRA.getErrorReporter().handleSilentException(e);
//                    stopTask();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e.printStackTrace();
                    }
                    connect(remoteDevice);
                    continue;
                }

                ObdProtocols protocol = ObdProtocols.values()[currentProtocol];
                publishProgress("Пробуем протокол: " + protocol.name());
                Logger.d(TAG, "Trying protocol " + protocol.name());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Logger.w(TAG, "Sleeping interrupted", e);
                }
                try {
                    if (execCommand(new TryProtocolCommand(protocol))) {
                        GetAvailPIDSCommand tryCmd = new GetAvailPIDSCommand();
                        if (execCommand(tryCmd)) {
                            Logger.d(TAG, "Try cmd result = " + tryCmd.getResult());
                            found = true;
                            Logger.i(TAG, "Протокол " + protocol.name() + " подходит");
                            publishProgress("Протокол " + protocol.name() + " подходит");
                            settingsService.saveSetting(context.getString(R.string.obd_protocol), protocol.name());
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
//                e.printStackTrace();
                            }
                        } else {
                            Logger.w(TAG, "Unable to get avail pids");
                            publishProgress("Протокол " + protocol.name() + " не подходит");
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
//                e.printStackTrace();
                            }

                        }
                    } else
                        Logger.w(TAG, "Unable to try protocol");

                } catch (Exception e) {
                    publishProgress("Протокол " + protocol.name() + " не подходит");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e2) {
//                e.printStackTrace();
                    }
//
//  e.printStackTrace();
                }

            } catch (Exception e) {
                publishProgress("Ошибка: " + e.getMessage());
//                ACRA.getErrorReporter().handleSilentException(e);
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
//                e.printStackTrace();
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
//            e.printStackTrace();
        }

        stopTask();

        return null;
    }

    private void connect(String remoteDevice) {
        connectionHelper.disconnect();
        try {
            connectionHelper.connect();
            connectionHelper.doResetAdapter(context);
        } catch (BTOBDConnectionException e) {
            publishProgress("Ошибка при подключении: " + e.getMessage());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
            stopTask();
        }
    }

    private boolean execCommand(ObdCommand cmd) {
        return connectionHelper.executeCommand(cmd, context);

    }

    private void stopTask() {
        connectionHelper.disconnect();
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
}

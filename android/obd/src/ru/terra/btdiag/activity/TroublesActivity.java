package ru.terra.btdiag.activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.google.inject.Inject;
import org.acra.ACRA;
import pt.lighthouselabs.obd.commands.control.TroubleCodesObdCommand;
import pt.lighthouselabs.obd.enums.ObdProtocols;
import roboguice.activity.RoboListActivity;
import roboguice.inject.ContentView;
import ru.terra.btdiag.R;
import ru.terra.btdiag.core.Logger;
import ru.terra.btdiag.obd.io.helper.BtObdConnectionHelper;
import ru.terra.btdiag.obd.io.helper.ConnectionStatus;
import ru.terra.btdiag.obd.io.helper.exception.BTOBDConnectionException;

/**
 * Date: 12.11.14
 * Time: 11:14
 */
@ContentView(R.layout.a_troubles)
public class TroublesActivity extends RoboListActivity {
    private static final String TAG = TroublesActivity.class.getName();
    private static final int NO_BLUETOOTH_DEVICE_SELECTED = 0;
    private static final int CANNOT_CONNECT_TO_DEVICE = 1;
    private static final int OBD_COMMAND_FAILURE = 2;
    private static final int NO_DATA = 3;
    private static final int DATA_OK = 4;

    private String remoteDevice;
    private GetTroubleCodesTask gtct;
    private ProgressDialog progressDialog;

    @Inject
    SharedPreferences prefs;
    @Inject
    public BtObdConnectionHelper connectionHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        remoteDevice = prefs.getString(ConfigActivity.BLUETOOTH_LIST_KEY, null);
        if (remoteDevice == null || "".equals(remoteDevice)) {
            Logger.w(TAG, "No Bluetooth device has been selected.");
            mHandler.obtainMessage(NO_BLUETOOTH_DEVICE_SELECTED).sendToTarget();
        } else {
            gtct = new GetTroubleCodesTask();
            gtct.execute(remoteDevice);
        }
    }

    public void makeToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Message received on handler");
            switch (msg.what) {
                case NO_BLUETOOTH_DEVICE_SELECTED:
                    makeToast("Не выбрано устройство bluetooth");
                    finish();
                    break;
                case CANNOT_CONNECT_TO_DEVICE:
                    makeToast("Невозможно подключиться к устройству");
                    finish();
                    break;
                case OBD_COMMAND_FAILURE:
                    makeToast("Ошибка OBD");
                    finish();
                    break;
                case NO_DATA:
                    makeToast("Данных об ошибках нет");
                    finish();
                    break;
                case DATA_OK:
                    dataOk((String) msg.obj);
                    break;

            }
        }
    };

    private void dataOk(String res) {
        if (!res.isEmpty()) {
            ArrayAdapter<String> myarrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, res.split("\n"));
            setListAdapter(myarrayAdapter);
        }
    }

    private class GetTroubleCodesTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            //Create a new progress dialog
            progressDialog = new ProgressDialog(TroublesActivity.this);
            //Set the progress dialog to display a horizontal progress bar
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //Set the dialog title to 'Loading...'
            progressDialog.setTitle("Загрузка...");
            //Set the dialog message to 'Loading application View, please wait...'
            progressDialog.setMessage("Загрузка приложения...");
            //This dialog can't be canceled by pressing the back key
            progressDialog.setCancelable(false);
            //This dialog isn't indeterminate
            progressDialog.setIndeterminate(false);
            //The maximum number of items is 100
            progressDialog.setMax(5);
            //Set the current progress to zero
            progressDialog.setProgress(0);
            //Display the progress dialog
            progressDialog.show();
        }


        @Override
        protected String doInBackground(String... params) {
            boolean needToDisconnect = false;
            try {
                if (connectionHelper.getConnectionStatus().equals(ConnectionStatus.NC)) {
                    onProgressUpdate(1);
                    connectionHelper.start(remoteDevice);
                    onProgressUpdate(2);
                    connectionHelper.connect();
                    onProgressUpdate(4);
                    connectionHelper.doResetAdapter(TroublesActivity.this);

                    onProgressUpdate(5);
                    ObdProtocols prot = ObdProtocols.valueOf(prefs.getString(getString(R.string.obd_protocol), String.valueOf(ObdProtocols.AUTO.getValue())));
                    connectionHelper.doSelectProtocol(prot, TroublesActivity.this);
                    needToDisconnect = true;
                }
                onProgressUpdate(6);
                TroubleCodesObdCommand tcoc = new TroubleCodesObdCommand();
                connectionHelper.executeCommand(tcoc, TroublesActivity.this);
                Logger.d(TAG, "Trouble command result: " + tcoc.getResult());
                Logger.d(TAG, "Trouble command formatted result: " + tcoc.getFormattedResult());
                return tcoc.getFormattedResult();
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.obtainMessage(OBD_COMMAND_FAILURE).sendToTarget();
                ACRA.getErrorReporter().handleException(e);
                return null;
            } catch (BTOBDConnectionException e) {
                e.printStackTrace();
                mHandler.obtainMessage(OBD_COMMAND_FAILURE).sendToTarget();
                ACRA.getErrorReporter().handleException(e);
                return null;
            } finally {
                if (needToDisconnect)
                    connectionHelper.disconnect();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
            }
            if (result == null) {
                return;
            }
            if (result.contains("NODATA")) {
                mHandler.obtainMessage(NO_DATA, result).sendToTarget();
            } else {
                mHandler.obtainMessage(DATA_OK, result).sendToTarget();
            }
        }
    }
}
package ru.terra.btdiag.net.task;

import android.content.Context;
import org.acra.ACRA;
import ru.terra.btdiag.core.AsyncTaskEx;
import ru.terra.btdiag.net.core.OBDRest;

/**
 * Date: 05.02.15
 * Time: 19:17
 */
public class SendTroubleAsyncTask extends AsyncTaskEx<String, Void, Void> {

    private OBDRest obdRest;

    public SendTroubleAsyncTask(Context context, OBDRest obdRest) {
        super(30000L, context);
        this.obdRest = obdRest;
    }

    @Override
    protected void onPreExecute() {
        showDialog("Отправка", "Отправка кода ошибки");
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            obdRest.sendTrouble(strings[0]);
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(e);
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        dismissDialog();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        dismissDialog();
    }
}

package ru.terra.btdiag.net.core;

import android.content.Context;
import android.widget.Toast;
import ru.terra.btdiag.core.constants.Constants;
import ru.terra.btdiag.core.AsyncTaskEx;
import ru.terra.btdiag.core.WorkIsDoneListener;
import ru.terra.btdiag.net.dto.LoginDTO;

/**
 * Date: 20.11.14
 * Time: 22:12
 */
public class LoginAsyncTask extends AsyncTaskEx<Void, Void, Boolean> {

    private OBDRest obd;
    private WorkIsDoneListener workIsDoneListener;
    private Context context;
    private String message = "";

    public LoginAsyncTask(OBDRest obd, WorkIsDoneListener workIsDoneListener, Context context) {
        super(20000l, context);
        this.obd = obd;
        this.workIsDoneListener = workIsDoneListener;
        this.context = context;
        showDialog("Вход", "Выполняется вход");
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        LoginDTO ret = null;
        try {
            ret = obd.login();
            message = ret.message;
            return ret.logged;
        } catch (Exception e) {
            exception = e;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dismissDialog();
        if (message.length() > 0)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        if (workIsDoneListener != null)
            workIsDoneListener.workIsDone(Constants.LOGIN_ACTION, exception, result.toString());
    }
    @Override
    protected void onCancelled() {
        dismissDialog();
        Toast.makeText(context, "Превышено время ожидания", Toast.LENGTH_SHORT).show();
    }
}

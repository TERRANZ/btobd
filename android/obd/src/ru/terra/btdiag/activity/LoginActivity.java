package ru.terra.btdiag.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import ru.terra.btdiag.R;
import ru.terra.btdiag.core.SettingsService;
import ru.terra.btdiag.core.WorkIsDoneListener;
import ru.terra.btdiag.net.core.LoginAsyncTask;
import ru.terra.btdiag.net.core.OBDRest;

/**
 * Date: 20.11.14
 * Time: 12:32
 */
@ContentView(R.layout.a_login)
public class LoginActivity extends RoboActivity {
    @Inject
    SharedPreferences prefs;
    @InjectView(R.id.edtLogin)
    private EditText edtLogin;
    @InjectView(R.id.edtPass)
    private EditText edtPass;

    @Inject
    private SettingsService settingsService;
    @Inject
    private OBDRest obdRest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        edtLogin.setText(settingsService.getSetting(getString(R.string.username), ""));
        edtPass.setText(settingsService.getSetting(getString(R.string.password), ""));
    }

    public void loginOk(View view) {
        settingsService.saveSetting(getString(R.string.username), edtLogin.getText().toString());
        settingsService.saveSetting(getString(R.string.password), edtPass.getText().toString());
        doLogin();
    }

    private void doLogin() {
        new LoginAsyncTask(obdRest, new WorkIsDoneListener() {

            @Override
            public void workIsDone(int action, Exception e, String... params) {
                Boolean result = Boolean.valueOf(params[0]);
                if (result) {
                    Toast.makeText(LoginActivity.this, "Вход успешен", Toast.LENGTH_SHORT).show();
                    settingsService.saveSetting(getString(R.string.logged_in), "true");
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Логин/пароль не опознаны", Toast.LENGTH_SHORT).show();
                }
            }
        }, this).execute();
    }
}

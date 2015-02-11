package ru.terra.btdiag.net.core;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import org.apache.http.message.BasicNameValuePair;
import roboguice.inject.ContextSingleton;
import ru.terra.btdiag.R;
import ru.terra.btdiag.constants.Constants;
import ru.terra.btdiag.constants.URLConstants;
import ru.terra.btdiag.core.SettingsService;
import ru.terra.btdiag.net.dto.CommonDTO;
import ru.terra.btdiag.net.dto.LoginDTO;
import ru.terra.btdiag.net.dto.OBDInfoDto;

import java.io.IOException;

/**
 * Date: 20.11.14
 * Time: 12:41
 */
@ContextSingleton
public class OBDRest {
    private Context context;

    @Inject
    private HttpRequestHelper httpRequestHelper;

    @Inject
    private SettingsService settingsService;

    @Inject
    public OBDRest(Context context) {
        this.context = context;
    }

    public LoginDTO login() throws IOException, UnableToLoginException {
        LoginDTO ret = httpRequestHelper.getForObject(
                URLConstants.DoJson.Login.LOGIN_DO_LOGIN_JSON,
                LoginDTO.class,
                new BasicNameValuePair(URLConstants.DoJson.Login.LOGIN_PARAM_USER, settingsService.getSetting(context.getString(R.string.username),
                        "")),
                new BasicNameValuePair(URLConstants.DoJson.Login.LOGIN_PARAM_PASS, settingsService.getSetting(context.getString(R.string.password),
                        "")));
        if (ret != null && ret.logged) {
            settingsService.saveSetting(Constants.CONFIG_SESSION, ret.session);
            settingsService.saveSetting(Constants.CONFIG_UID, ret.id.toString());
            final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            settingsService.saveSetting(Constants.CONFIG_DEVID, btAdapter.getAddress());
        }
        return ret;
    }

    public LoginDTO reg(String user, String pass, String cid, String captcha) throws IOException, UnableToLoginException {
        LoginDTO ret = httpRequestHelper.getForObject(URLConstants.DoJson.Login.LOGIN_DO_REGISTER_JSON, LoginDTO.class, new BasicNameValuePair(
                        URLConstants.DoJson.Login.LOGIN_PARAM_USER, user), new BasicNameValuePair(URLConstants.DoJson.Login.LOGIN_PARAM_PASS, pass),
                new BasicNameValuePair(URLConstants.DoJson.Login.LOGIN_PARAM_CAPTCHA, cid), new BasicNameValuePair(
                        URLConstants.DoJson.Login.LOGIN_PARAM_CAPVAL, captcha));
        return ret;
    }

    public boolean sendInfo(OBDInfoDto info) throws IOException, UnableToLoginException {
        OBDInfoDto ret = httpRequestHelper.putForObject(URLConstants.DoJson.Obd.ADD, OBDInfoDto.class, new Gson().toJson(info, new TypeToken<OBDInfoDto>() {
        }.getType()));
        return ret != null;
    }


    public boolean sendTrouble(String trouble) throws IOException, UnableToLoginException {
        CommonDTO ret = httpRequestHelper.getForObject(URLConstants.DoJson.Obd.DO_REPORT_TROUBLE, CommonDTO.class, new BasicNameValuePair("trouble", trouble));
        return ret.errorCode == 0;
    }

}
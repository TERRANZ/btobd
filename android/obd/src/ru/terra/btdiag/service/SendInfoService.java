package ru.terra.btdiag.service;

import android.content.Intent;
import com.google.inject.Inject;
import roboguice.service.RoboIntentService;
import ru.terra.btdiag.core.Logger;
import ru.terra.btdiag.net.core.OBDRest;
import ru.terra.btdiag.net.core.UnableToLoginException;

import java.io.IOException;

/**
 * Date: 20.11.14
 * Time: 13:02
 */
public class SendInfoService extends RoboIntentService {
    public SendInfoService() {
        super("Send info service");
    }

    @Inject
    public OBDRest obdRest;

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Logger.i("SendInfoService", "result = " + obdRest.sendInfo((ru.terra.btdiag.net.dto.OBDInfoDto) intent.getSerializableExtra("info")));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnableToLoginException e) {
            e.printStackTrace();
        }
    }
}

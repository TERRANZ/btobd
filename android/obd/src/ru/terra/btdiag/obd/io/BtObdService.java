package ru.terra.btdiag.obd.io;

import android.content.Intent;
import roboguice.service.RoboIntentService;

/**
 * Date: 11.02.15
 * Time: 13:58
 */
public class BtObdService extends RoboIntentService {


    public BtObdService() {
        super("BTDIAG bluetooth obd service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}

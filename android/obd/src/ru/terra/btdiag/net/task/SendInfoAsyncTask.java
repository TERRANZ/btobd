package ru.terra.btdiag.net.task;

import android.content.Context;
import ru.terra.btdiag.core.AsyncTaskEx;
import ru.terra.btdiag.core.Logger;
import ru.terra.btdiag.net.core.OBDRest;
import ru.terra.btdiag.net.core.UnableToLoginException;
import ru.terra.btdiag.net.dto.OBDInfoDto;

import java.io.IOException;

/**
 * Date: 11.02.15
 * Time: 13:46
 */
public class SendInfoAsyncTask extends AsyncTaskEx<OBDInfoDto, Void, Void> {
    private OBDRest obdRest;

    public SendInfoAsyncTask(Context a, OBDRest obdRest) {
        super(30000L, a);
        this.obdRest = obdRest;
    }

    @Override
    protected Void doInBackground(OBDInfoDto... infos) {
        try {
            Logger.i("SendInfoService", "result = " + obdRest.sendInfo(infos[0]));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnableToLoginException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCancelled() {

    }
}

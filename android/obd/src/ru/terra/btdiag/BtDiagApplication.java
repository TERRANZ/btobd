package ru.terra.btdiag;

import android.app.Application;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

/**
 * Date: 11.11.14
 * Time: 9:59
 */
@ReportsCrashes(formKey = "",
        formUri = "http://terranz.ath.cx/jbrss/errors/do.error.report/btdiag",
        httpMethod = HttpSender.Method.POST,
        mode = ReportingInteractionMode.TOAST, resToastText = R.string.error_caught)
public class BtDiagApplication extends Application {
    @Override
    public void onCreate() {
        ACRA.init(this);
        super.onCreate();
    }
}

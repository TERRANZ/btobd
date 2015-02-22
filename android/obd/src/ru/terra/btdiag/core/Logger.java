package ru.terra.btdiag.core;

import android.content.Context;
import android.util.Log;

public class Logger {
    public static void i(Context context, String tag, String msg) {
        if (tag != null && msg != null) {
            Log.i(tag, msg);
            try {
                new SettingsService(context).addLogString(msg);
            } catch (Exception e) {
            }
        }
    }

    public static void w(Context context, String tag, String msg) {
        if (tag != null && msg != null) {
            Log.w(tag, msg);
            try {
                new SettingsService(context).addLogString(msg);
            } catch (Exception e) {
            }
        }
    }

    public static void w(Context context, String tag, String msg, Throwable t) {
        if (tag != null && msg != null) {
            Log.w(tag, msg, t);
            try {
                new SettingsService(context).addLogString(msg);
            } catch (Exception e) {
            }
        }
    }

    public static void d(Context context, String tag, String msg) {
        if (tag != null && msg != null) {
            Log.d(tag, msg);
            try {
                new SettingsService(context).addLogString(msg);
            } catch (Exception e) {
            }
        }
    }

    public static void e(Context context, String tag, String msg, Throwable t) {
        if (tag != null && msg != null) {
            Log.e(tag, msg, t);
            try {
                new SettingsService(context).addLogString(msg);
            } catch (Exception e) {
            }
        }
    }
}

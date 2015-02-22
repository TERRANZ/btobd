package ru.terra.btdiag.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.google.inject.Inject;
import roboguice.inject.ContextSingleton;
import ru.terra.btdiag.R;

import java.util.HashSet;
import java.util.Set;

@ContextSingleton
public class SettingsService {

    private Context context;

    @Inject
    public SettingsService(Context context) {
        this.context = context;
    }

    public void saveSetting(String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getSetting(String key, String defVal) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defVal);
    }

    public Boolean getSettingBoolean(String key, Boolean defVal) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(key, defVal);
    }

    public void addLogString(String msg) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.log_strs);
        Set<String> messages = prefs.getStringSet(key, new HashSet<String>());
        Editor editor = prefs.edit();
        messages.add(msg);
        editor.putStringSet(key, messages);
        editor.commit();
    }

    public void clearLog() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.log_strs);
        Set<String> messages = prefs.getStringSet(key, new HashSet<String>());
        Editor editor = prefs.edit();
        editor.putStringSet(key, new HashSet<String>());
        editor.commit();
    }
}

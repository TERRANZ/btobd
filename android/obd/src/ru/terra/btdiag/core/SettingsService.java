package ru.terra.btdiag.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.google.inject.Inject;
import roboguice.inject.ContextSingleton;

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
}

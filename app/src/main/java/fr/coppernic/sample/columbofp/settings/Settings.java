package fr.coppernic.sample.columbofp.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import java.util.Locale;

import fr.coppernic.sample.columbofp.R;

/**
 * Created by michael on 30/01/18.
 */

public class Settings {
    private Context context;
    private SharedPreferences sharedPreferences;

    public Settings(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setReaderName(String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_reader_name), value);
        editor.apply();
    }

    public void setSerialNumber(String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_serial_number), value);
        editor.apply();
    }

    public void setFirmwareVersion(String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_firmware_version), value);
        editor.apply();
    }

    public void setProductionRevisionn(String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_production_revision), value);
        editor.apply();
    }

    public String getCaptureTimeout() {
        return sharedPreferences.getString(context.getString(R.string.pref_timeout), "10");
    }

    public String getPowerSaveMode() {
        boolean powerSaveMode = sharedPreferences.getBoolean(context.getString(R.string.pref_power_save_mode), false);
        return Boolean.toString(powerSaveMode).toUpperCase(Locale.US);
    }
}

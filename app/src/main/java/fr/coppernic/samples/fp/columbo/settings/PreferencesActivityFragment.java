package fr.coppernic.samples.fp.columbo.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import fr.coppernic.sample.columbofp.BuildConfig;
import fr.coppernic.sample.columbofp.R;


/**
 * A placeholder fragment containing a simple view.
 */
public class PreferencesActivityFragment extends PreferenceFragmentCompat {

    private static String key_version;

    public PreferencesActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        key_version = getString(R.string.pref_version);
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_reader_name)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sdk_version)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_serial_number)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_firmware_version)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_production_revision)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_timeout)));
        bindPreferenceSummaryToValue(findPreference(key_version));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringValue = newValue.toString();
                    if (preference instanceof ListPreference) {
                        // For list preferences, look up the correct display value in
                        // the preference's 'entries' list.
                        ListPreference listPreference = (ListPreference) preference;
                        int index = listPreference.findIndexOfValue(stringValue);

                        // Set the summary to reflect the new value.
                        preference.setSummary(
                                index >= 0
                                        ? listPreference.getEntries()[index]
                                        : null);
                    } else if (preference.getKey().equals(key_version)) {
                        preference.setSummary(BuildConfig.VERSION_NAME);
                    } else {
                        // For all other preferences, set the summary to the value's
                        // simple string representation.
                        preference.setSummary(stringValue);
                    }
                    return true;
                }
            };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(
                                preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}

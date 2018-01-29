package fr.coppernic.sample.columbofp.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import fr.coppernic.sample.columbofp.R;


/**
 * A placeholder fragment containing a simple view.
 */
public class PreferencesActivityFragment extends PreferenceFragment {

    public PreferencesActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs);
    }
}

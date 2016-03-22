package com.example.android.sunshine.app;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by zach on 3/21/2016.
 */
public class SettingsActivity extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {

    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "made it to the onCreate method in SettingsActivity.java");
        super.onCreate(savedInstanceState);
        //add preferences from XML.
        //addPreferencesFromResource is deprecated but is still best practice when targeting gingerbread per udacity
        addPreferencesFromResource(R.xml.pref_general);
        //for all preferences, attach an OnPreferenceChangeListener to update UI when prefs change
        //findPreference is deprecated but is still best practice when targeting gingerbread per udacity
        bindPrefSummaryToValue(findPreference(getString(R.string.pref_location_key)));
    }

    //Attach a listener so that summary is always updated with the preference value.
    //Fire the listener once to initialize the summary (before any values are changed).
    private void bindPrefSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(
                preference.getContext()).getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringVal = value.toString();
        //TODO what about CheckboxPreferences?
        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            int prefIndex = listPref.findIndexOfValue(stringVal);
            if (prefIndex >= 0) {
                preference.setSummary(listPref.getEntries()[prefIndex]);
            }
        } else {
            //for all other preferences (non list type prefs),
            //set the summary to use the value's string representation
            preference.setSummary(stringVal);
        }
        return true;
    }
}

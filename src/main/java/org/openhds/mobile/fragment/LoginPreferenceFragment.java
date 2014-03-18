package org.openhds.mobile.fragment;

import org.openhds.mobile.R;
import org.openhds.mobile.utilities.UrlUtils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class LoginPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updatePreference(getResources().getString(R.string.openhds_server_url));
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(key);
    }

    private void updatePreference(String key) {

        Preference preference = findPreference(key);
        if (key.equals(getResources().getString(R.string.openhds_server_url))) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            if (validateUrl(editTextPreference.getText())) {
                editTextPreference.setSummary(editTextPreference.getText());
            } else {
                editTextPreference.setSummary(R.string.openhds_server_url_default_summary);
            }
        }
    }

    private boolean validateUrl(String url) {
        if (null == url) {
            return false;
        }
        return UrlUtils.isValidUrl(url);
    }

}

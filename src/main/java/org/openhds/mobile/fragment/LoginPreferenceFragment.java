package org.openhds.mobile.fragment;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;
import static org.openhds.mobile.utilities.UrlUtils.isValidUrl;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.openhds.mobile.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class LoginPreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onResume() {
		super.onResume();

		PreferenceManager.getDefaultSharedPreferences(getActivity())
				.registerOnSharedPreferenceChangeListener(this);
		refreshPreferenceSummary(getResourceString(getActivity(),
				R.string.openhds_server_url_key));
	}

	@Override
	public void onPause() {
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(getActivity())
				.unregisterOnSharedPreferenceChangeListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updatePreference(key);
	}

	private void updatePreference(String key) {
		if (validatePreference(key)) {
			refreshPreferenceSummary(key);
		} else {
			showInvalidPreferenceSummary(key);
			showLongToast(getActivity(), R.string.preference_invalid_warning);
		}
	}

	private boolean validatePreference(String key) {
		Preference preference = findPreference(key);
		if (null == preference) {
			return false;
		}

		if (key.equals(getResourceString(getActivity(),
				(R.string.openhds_server_url_key)))) {
			String newUrl = getPreferenceString(getActivity(), key, "");
			return isValidUrl(newUrl);
		}

		return false;
	}

	private void refreshPreferenceSummary(String key) {
		Preference preference = findPreference(key);
		if (null == preference) {
			return;
		}

		if (key.equals(getResourceString(getActivity(),
				(R.string.openhds_server_url_key)))) {
			EditTextPreference editTextPreference = (EditTextPreference) preference;
			editTextPreference.setSummary(editTextPreference.getText());
		}
	}

	private void showInvalidPreferenceSummary(String key) {
		Preference preference = findPreference(key);
		if (null == preference) {
			return;
		}

		preference.setSummary(getResourceString(getActivity(),
				(R.string.preference_invalid_label)));
	}
}

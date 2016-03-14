package org.openhds.mobile.fragment;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;
import static org.openhds.mobile.utilities.UrlUtils.isValidUrl;

import org.openhds.mobile.OpenHDSApplication;
import org.openhds.mobile.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
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
		
		//Dynamically set default value of ListPreference according to settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultLang = getString(R.string.locale_lang);
        String lang = settings.getString(getString(R.string.locale_lang), defaultLang);
		ListPreference languagePreferenceList = (ListPreference) findPreference ("displayLanguage");
		languagePreferenceList.setValue(lang);
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
			
		if(key.equals("displayLanguage")){
			String language = sharedPreferences.getString("displayLanguage",getString(R.string.locale_lang));
			
			((OpenHDSApplication)getActivity().getApplicationContext()).changeLang(language);
		}
		else if((key.equals(getResourceString(getActivity(),
				(R.string.openhds_server_url_key))))){
			updatePreference(key);
		}
		
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
		else if(key.equals("displayLanguage")){
			return true;
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

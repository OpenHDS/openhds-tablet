package org.openhds.mobile.activity;

import java.net.MalformedURLException;
import java.net.URL;
import org.openhds.mobile.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public abstract class AbstractActivity extends Activity {

	public static final String USERNAME_PARAM = "username";
	public static final String PASSWORD_PARAM = "password";
	public static final String FORM_ID = "form_id";

	protected URL getServerUrl(String path) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String url = sp.getString(ServerPreferencesActivity.INTEROP_SERVER, getString(R.string.default_server_url));

		if (url.trim().length() == 0) {
			showToastWithText("No server URL has been set. Set server URL from preferences");
			return null;
		}

		URL parsedUrl = null;
		try {
			parsedUrl = new URL(url + path);
		} catch (MalformedURLException e) {
			showToastWithText("Bad Server URL");
		}

		return parsedUrl;
	}

	protected void showToastWithText(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	protected String getUsernameFromIntent() {
		return getIntent().getExtras().getString(USERNAME_PARAM);
	}

	protected String getPasswordFromIntent() {
		return getIntent().getExtras().getString(PASSWORD_PARAM);
	}
	
	protected void setUsernameOnIntent(Intent intent) {
		intent.putExtra(USERNAME_PARAM, getUsernameFromIntent());		
	}
	
	protected long getFormIdFromIntent() {
		return getIntent().getExtras().getLong(FORM_ID);
	}
}

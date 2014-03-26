package org.openhds.mobile.utilities;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;

import org.openhds.mobile.R;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;
import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;

public class UrlUtils {

	public static boolean isValidUrl(String url) {
		try {
			new URL(url);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}

	public static URL buildServerUrl(Context context, String path) {
		String openHdsBaseUrl = getPreferenceString(context,
				R.string.openhds_server_url_key, "");

		if (0 == openHdsBaseUrl.trim().length()) {
			showLongToast(context,
					"No server URL has been set. Set server URL from preferences");
			return null;
		}

		try {
			return new URL(openHdsBaseUrl + path);
		} catch (MalformedURLException e) {
			showLongToast(context, "Bad Server URL");
			return null;
		}
	}
}

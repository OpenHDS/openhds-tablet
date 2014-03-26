package org.openhds.mobile.utilities;

import android.content.Context;
import android.widget.Toast;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;

public class MessageUtils {

	public static void showLongToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
	
	public static void showLongToast(Context context, int messageId) {
		String message = getResourceString(context, messageId);
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	public static void showShortToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	
	public static void showShortToast(Context context, int messageId) {
		String message = getResourceString(context, messageId);
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}

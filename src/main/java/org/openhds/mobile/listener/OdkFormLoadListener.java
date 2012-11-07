package org.openhds.mobile.listener;

import android.net.Uri;

public interface OdkFormLoadListener {

	public void onOdkFormLoadSuccess(Uri contentUri);
	
	public void onOdkFormLoadFailure();

}

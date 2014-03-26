package org.openhds.mobile.utilities;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;

import org.openhds.mobile.R;
import org.openhds.mobile.listener.CollectEntitiesListener;
import org.openhds.mobile.task.SyncEntitiesTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask.Status;

public class SyncDatabaseHelper {

	private String username;
	private String password;
	private Context callingContext;
	private ProgressDialog progressDialog;
	private SyncEntitiesTask entitiesTask = null;

	public SyncDatabaseHelper(String username, String password, Context context) {
		this.username = username;
		this.password = password;
		this.callingContext = context;
		initializeProgressDialog();
	}

	private void initializeProgressDialog() {
		progressDialog = new ProgressDialog(callingContext);
		progressDialog.setCancelable(true);
		progressDialog.setOnCancelListener(new SyncingOnCancelListener());
		progressDialog.setTitle("Working...");
		progressDialog.setMessage("Do not interrupt");
	}

	public void startSync() {
		progressDialog.show();

		if (null != entitiesTask && entitiesTask.getStatus() == Status.RUNNING) {
			entitiesTask.cancel(true);
		}

		String openHdsBaseUrl = getPreferenceString(callingContext,
				R.string.openhds_server_url_key, "");
		entitiesTask = new SyncEntitiesTask(openHdsBaseUrl, username, password,
				progressDialog, callingContext, new SyncDatabaseListener());
		entitiesTask.execute();
	}

	private class SyncingOnCancelListener implements OnCancelListener {
		public void onCancel(DialogInterface dialog) {
			ConfirmOnCancelListener listener = new ConfirmOnCancelListener();
			AlertDialog.Builder builder = new AlertDialog.Builder(
					callingContext);
			builder.setMessage("Are you sure you want to stop sync?")
					.setCancelable(false).setPositiveButton("Yes", listener)
					.setNegativeButton("No", listener);
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	private class SyncDatabaseListener implements CollectEntitiesListener {
		@Override
		public void collectionComplete(Boolean result) {
			if (result) {
				showLongToast(callingContext, callingContext.getString(R.string.sync_entities_successful));
			} else {
				showLongToast(callingContext, callingContext.getString(R.string.sync_entities_failure));
			}
			progressDialog.dismiss();
		}
	}

	private class ConfirmOnCancelListener implements
			DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				entitiesTask.cancel(true);
				showLongToast(callingContext, callingContext.getString(R.string.sync_interrupted));
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				if (entitiesTask.getStatus() == Status.RUNNING) {
					progressDialog.show();
				}
				break;
			}

		}
	}
}

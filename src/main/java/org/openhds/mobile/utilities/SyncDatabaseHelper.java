package org.openhds.mobile.utilities;

import static org.openhds.mobile.utilities.MessageUtils.showLongToast;

import org.openhds.mobile.R;
import org.openhds.mobile.listener.SyncDatabaseListener;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;

public class SyncDatabaseHelper implements SyncDatabaseListener {

	private Context callingContext;
	private ProgressDialog progressDialog;
	private AsyncTask<Void, Integer, Boolean> currentTask = null;

	public SyncDatabaseHelper(Context context) {
		this.callingContext = context;
		initializeProgressDialog();
	}

	public AsyncTask<Void, Integer, Boolean> getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(AsyncTask<Void, Integer, Boolean> currentTask) {
		this.currentTask = currentTask;
	}

	public ProgressDialog getProgressDialog() {
		return progressDialog;
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

		if (null != currentTask && currentTask.getStatus() == Status.RUNNING) {
			currentTask.cancel(true);
		}

		currentTask.execute();
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
	
	@Override
	public void collectionComplete(Boolean result) {
		if (result) {
			showLongToast(callingContext, R.string.sync_entities_successful);
		} else {
			showLongToast(callingContext, R.string.sync_entities_failure);
		}
		progressDialog.dismiss();
	}

	private class ConfirmOnCancelListener implements
			DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				currentTask.cancel(true);
				showLongToast(callingContext, R.string.sync_interrupted);
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				if (currentTask.getStatus() == Status.RUNNING) {
					progressDialog.show();
				}
				break;
			}
		}
	}
}

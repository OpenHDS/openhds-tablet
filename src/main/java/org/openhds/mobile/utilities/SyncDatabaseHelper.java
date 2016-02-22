package org.openhds.mobile.utilities;

import static org.openhds.mobile.utilities.MessageUtils.showLongToast;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.SupervisorMainActivity;
import org.openhds.mobile.listener.SyncDatabaseListener;
import org.openhds.mobile.task.HttpTask;
import org.openhds.mobile.task.SyncEntitiesTask;
import org.openhds.mobile.task.SyncFieldworkersTask;
import org.openhds.mobile.task.SyncFormsTask;

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
	private AsyncTask<Void, Integer, HttpTask.EndResult> currentTask = null;

	public SyncDatabaseHelper(Context context) {
		this.callingContext = context;
		initializeProgressDialog();
	}

	public AsyncTask<Void, Integer, HttpTask.EndResult> getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(
			AsyncTask<Void, Integer, HttpTask.EndResult> currentTask) {
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

	public void collectionComplete(HttpTask.EndResult result) {
		if (result.equals(HttpTask.EndResult.SUCCESS)) {
			showLongToast(callingContext, R.string.sync_entities_successful);
			
			//Somewhat hackish way to call the Update-Method that shows the last time the settings were synced
			if((currentTask instanceof SyncEntitiesTask || currentTask instanceof SyncFieldworkersTask || currentTask instanceof SyncFormsTask) 
					&& callingContext instanceof SupervisorMainActivity){				
				((SupervisorMainActivity)callingContext).displayLastSyncDate();
				((SupervisorMainActivity)callingContext).displaySyncStats();
			}
		} 
		else if(result.equals(HttpTask.EndResult.NO_CONTENT)){
			displayEmptyContentFailedDialog();
		}
		else {
			displayEntityFetchFailedDialog();
		}
		progressDialog.dismiss();
		initializeProgressDialog();
	}
	
	private void displayEmptyContentFailedDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(
				callingContext);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle(R.string.sync_entities_failure_no_content_title);
		builder.setMessage(R.string.sync_entities_failure);
		builder.setCancelable(false);
		builder.setPositiveButton("Ok", null);
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void displayEntityFetchFailedDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(
				callingContext);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setMessage(R.string.sync_entities_failure);
		builder.setCancelable(false);
		builder.setPositiveButton("Ok", null);
		AlertDialog alert = builder.create();
		alert.show();
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

	private class ConfirmOnCancelListener implements
			DialogInterface.OnClickListener {

		public void onClick(DialogInterface dialogInterface, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				currentTask.cancel(true);
				initializeProgressDialog();
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

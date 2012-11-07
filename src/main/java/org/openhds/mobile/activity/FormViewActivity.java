package org.openhds.mobile.activity;

import org.openhds.mobile.InstanceProviderAPI;
import org.openhds.mobile.R;
import org.openhds.mobile.database.DatabaseAdapter;
import org.openhds.mobile.model.FormSubmissionRecord;
import org.openhds.mobile.task.OdkFormLoadTask;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FormViewActivity extends AbstractActivity {

	private static final int ODK_FORM_ENTRY_RESULT = 1;

	private Dialog dialog;
	private DatabaseAdapter store;
	private long formId;
	private FormSubmissionRecord record;

	private class LoadRecordTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			record = store.findSubmissionById(formId);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			loadFormSubmission();
			dialog.dismiss();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form_view);
		formId = getFormIdFromIntent();
		dialog = ProgressDialog.show(this, "Loading",
				"Loading form submission...", true);
		store = new DatabaseAdapter(this);
		new LoadRecordTask().execute();
		Button deleteBtn = (Button) findViewById(R.id.delete_form_btn);
		deleteBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				store.deleteSubmission(record.getId());
				finish();
			}
			
		});
	}

	private void loadFormSubmission() {
		TextView tv = (TextView) findViewById(R.id.form_owner_id);
		tv.setText(record.getFormOwnerId());

		tv = (TextView) findViewById(R.id.location_value_txt);
		tv.setText(record.getFormType());
		
		tv = (TextView) findViewById(R.id.form_status_lbl);
		tv.setText(record.isCompleted() ? "Completed" : "Not Completed");

		tv = (TextView) findViewById(R.id.failure_messages);
		StringBuilder builder = new StringBuilder();
		int cnt = 1;
		for (String error : record.getErrors()) {
			builder.append(cnt + ". " + error + "\n\n");
			cnt++;
		}
		tv.setText(builder.toString());

		Button editOdkBtn = (Button) findViewById(R.id.edit_in_odk_btn);
		if (!record.isCompleted()) {
		editOdkBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				dialog = ProgressDialog.show(FormViewActivity.this, "Loading",
						"Loading form into ODK Collect...", true, true);
				new OdkFormLoadTask(record, new OdkFormLoadListener(),
						getContentResolver(), store).execute();
			}
		});
		} else {
			editOdkBtn.setEnabled(false);
		}
	}

	private class OdkFormLoadListener implements OdkFormLoadTask.Listener {

		public void onFailedWritingDirs() {
			dialog.dismiss();
			showToastWithText("There was a problem creating directories");
		}

		public void onFailedWritingXmlFile() {
			dialog.dismiss();
			showToastWithText("There was a problem writing the XML file");
		}

		public void onFailedOdkInsert() {
			dialog.dismiss();
			showToastWithText("There was a problem with ODK Collect");
		}

		public void onSuccess(Uri contentUri) {
			dialog.dismiss();
			Intent intent = new Intent(Intent.ACTION_EDIT, contentUri);
			startActivityForResult(intent, ODK_FORM_ENTRY_RESULT);
		}

		public void onFormAlreadyCompleted() {
			showToastWithText("This form has already been completed");
		}

		public void onOrphanForm() {
			//showToastWithText("")
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ODK_FORM_ENTRY_RESULT:
			handleFormEntry(resultCode);
		}
	}

	private void handleFormEntry(int resultCode) {
		dialog  = ProgressDialog.show(this, "Updating", "Updating form information...", true);
		new UpdateSubmissionTask().execute();
	}
	
	private class UpdateSubmissionTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			Cursor result = getContentResolver().query(Uri.parse(record.getOdkUri()), null,
					null, null, null);
			if (!result.moveToNext()) {
				return false;
			}
			
			String status = result.getString(result.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS));
			result.close();
			if (!InstanceProviderAPI.STATUS_INCOMPLETE.equals(status)) {
				store.updateCompleteStatus(record.getId(), true);
				return true;
			}
			
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (!result) {
				return;
			}
			
			TextView tv = (TextView) findViewById(R.id.form_status_lbl);
			tv.setText("Completed");
			
			Button edit = (Button) findViewById(R.id.edit_in_odk_btn);
			edit.setEnabled(false);
			
			dialog.dismiss();
		}
		
	}

}

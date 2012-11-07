package org.openhds.mobile.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.openhds.mobile.InstanceProviderAPI;
import org.openhds.mobile.database.DatabaseAdapter;
import org.openhds.mobile.model.FormSubmissionRecord;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

/**
 * Async Task that will attempt to write the form instance data to disk, and
 * then use the ODK Content Provider to create a form instance record
 */
public class OdkFormLoadTask extends
		AsyncTask<Void, Void, OdkFormLoadTask.EndResult> {

	enum EndResult {
		FAILED_CREATING_DIRS, FAILED_WRITING_XML_FILE, FAILED_ODK_INSERT, SUCCESS, FORM_ALREADY_COMPLETED, ORPHAN_RECORD
	}

	public interface Listener {
		void onFailedWritingDirs();

		void onFailedWritingXmlFile();

		void onFailedOdkInsert();
		
		void onFormAlreadyCompleted();
		
		void onOrphanForm();

		void onSuccess(Uri contentUri);
	}

	private FormSubmissionRecord record;
	private Listener listener;
	private ContentResolver resolver;
	private DatabaseAdapter store;
	private Uri odkUri;

	public OdkFormLoadTask(FormSubmissionRecord record, Listener listener,
			ContentResolver resolver, DatabaseAdapter store) {
		this.record = record;
		this.listener = listener;
		this.resolver = resolver;
		this.store = store;
	}

	@Override
	protected EndResult doInBackground(Void... arg0) {
		File root = Environment.getExternalStorageDirectory();
		String destinationPath = root.getAbsolutePath() + File.separator
				+ "Android" + File.separator + "data" + File.separator
				+ "org.openhds.mobile" + File.separator + "files";

		File baseDir = new File(destinationPath);
		if (!baseDir.exists()) {
			boolean created = baseDir.mkdirs();
			if (!created) {
				return EndResult.FAILED_CREATING_DIRS;
			}
		}

		destinationPath += File.separator + record.getSaveDate() + ".xml";
		File targetFile = new File(destinationPath);
		if (!targetFile.exists()) {
			try {
				FileWriter writer = new FileWriter(targetFile);
				writer.write(record.getPartialForm());
				writer.close();
			} catch (IOException e) {
				return EndResult.FAILED_WRITING_XML_FILE;
			}
		}

		if (record.getOdkUri() == null) {
			ContentValues values = new ContentValues();
			values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
					targetFile.getAbsolutePath());
			values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
					record.getFormType());
			values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID,
					record.getFormId());
			Uri uri = resolver.insert(
					InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);
			if (uri == null) {
				return EndResult.FAILED_ODK_INSERT;
			}
			record.setOdkUri(uri.toString());
			store.updateOdkUri(record.getId(), uri);
		} else {
			// form already inserted into ODK
			// determine if its still there, and if it is determine
			// if its been completed
			Cursor result = resolver.query(Uri.parse(record.getOdkUri()), null,
					null, null, null);
			if (!result.moveToNext()) {
				return EndResult.ORPHAN_RECORD;
			}
			
			String status = result.getString(result.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS));
			result.close();
			if (!InstanceProviderAPI.STATUS_INCOMPLETE.equals(status)) {
				store.updateCompleteStatus(record.getId(), true);
				return EndResult.FORM_ALREADY_COMPLETED;
			}
		}

		this.odkUri = Uri.parse(record.getOdkUri());

		return EndResult.SUCCESS;
	}

	@Override
	protected void onPostExecute(EndResult result) {
		switch(result) {
		case FAILED_CREATING_DIRS:
			listener.onFailedWritingDirs();
			break;
		case FAILED_ODK_INSERT:
			listener.onFailedOdkInsert();
			break;
		case FAILED_WRITING_XML_FILE:
			listener.onFailedWritingXmlFile();
			break;
		case FORM_ALREADY_COMPLETED:
			listener.onFormAlreadyCompleted();
			break;
		case ORPHAN_RECORD:
			listener.onOrphanForm();
			break;
		case SUCCESS:
			listener.onSuccess(odkUri);
			break;
		}
	}
}

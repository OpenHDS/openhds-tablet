package org.openhds.mobile.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sqlcipher.SQLException;

import org.apache.http.HttpResponse;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.listener.SyncDatabaseListener;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.Settings;
import org.openhds.mobile.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

public class SyncFieldworkersTask extends HttpTask<Void, Integer> {

	private ContentResolver contentResolver;
	private ProgressDialog progressDialog;
	private SyncDatabaseListener syncListener;
	private Context mContext;

	public SyncFieldworkersTask(Context ctx, RequestContext requestContext,
			ContentResolver contentResolver, ProgressDialog progressDialog,
			SyncDatabaseListener syncListener) {
		super(ctx, requestContext);
		this.listener = new SyncFieldWorkerListener();
		this.syncListener = syncListener;
		this.progressDialog = progressDialog;
		this.contentResolver = contentResolver;
		this.mContext = ctx;
	}

	@Override
	protected EndResult handleResponseData(HttpResponse response) {
			try {
				processXMLDocument(response.getEntity().getContent());
			} catch (IllegalStateException e) {
				return EndResult.FAILURE;
			} catch (XmlPullParserException e) {
				return EndResult.FAILURE;
			} catch (IOException e) {
				return EndResult.FAILURE;
			}
	 
		return EndResult.SUCCESS;
	}

	private void processXMLDocument(InputStream content)
			throws XmlPullParserException, IOException {

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);

		XmlPullParser parser = factory.newPullParser();
		parser.setInput(new InputStreamReader(content));

		ArrayList<FieldWorker> list = new ArrayList<FieldWorker>();
		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT && !isCancelled()) {
			String name = null;

			switch (eventType) {
			case XmlPullParser.START_TAG:
				name = parser.getName();

				if (name.equalsIgnoreCase("fieldworker")) {
					list.add(processFieldWorkerParams(parser));
				}
				break;
			}
			eventType = parser.next();
		}
		replaceAllFieldWorkers(list);
		
		processLastSyncDate();
	}

	private FieldWorker processFieldWorkerParams(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		Map<String, String> paramMap = new HashMap<String, String>();
		parser.nextTag();
		paramMap.put("uuid", parser.nextText());

		parser.nextTag();
		paramMap.put("extId", parser.nextText());

		parser.nextTag();
		paramMap.put("firstName", parser.nextText());

		parser.nextTag();
		paramMap.put("lastName", parser.nextText());

		
		parser.nextTag();

		FieldWorker fw = new FieldWorker(paramMap.get("extId"),
				paramMap.get("firstName"), paramMap.get("lastName"));
		
		paramMap.put("passwordHash", parser.nextText());
		fw.setPasswordHash(paramMap.get("passwordHash"));
		
		return fw;
	}

	private void replaceAllFieldWorkers(List<FieldWorker> list) {
		deleteAllFieldWorkers();
		for (FieldWorker fw : list) {
			addFieldWorker(fw);
		}
	}

	public boolean addFieldWorker(FieldWorker fwu) {
		ContentValues cv = new ContentValues();
		cv.put(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_EXTID, fwu.getExtId());
		cv.put(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_FIRSTNAME,
				fwu.getFirstName());
		cv.put(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_LASTNAME,
				fwu.getLastName());
		cv.put(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_PASSWORD,
				fwu.getPasswordHash());

		try {
			contentResolver.insert(OpenHDS.FieldWorkers.CONTENT_URI, cv);
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	public int deleteFieldWorker(FieldWorker fwu) {
		return contentResolver.delete(OpenHDS.FieldWorkers.CONTENT_URI,
				OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_EXTID + " = ?",
				new String[] { fwu.getExtId() });
	}

	public int deleteAllFieldWorkers() {
		return contentResolver.delete(OpenHDS.FieldWorkers.CONTENT_URI, "1",
				null);
	}
	
	/* Insert Last sync date */
	private void processLastSyncDate()
			throws XmlPullParserException, IOException {
		
		final List<ContentValues> values = new ArrayList<ContentValues>();
		final ContentValues[] emptyArray = new ContentValues[] {};
		
		values.clear();
		ContentValues cv;		
		
		//Insert current date
		cv = new ContentValues();
		Calendar rightNow = Calendar.getInstance();
		Date date = rightNow.getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, dateFormat.format(date)); //DATE_OF_LAST_SYNC
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.DATE_OF_LAST_FW_SYNC); //DATE_OF_LAST_SYNC		
		values.add(cv);
			
		if (!values.isEmpty()) {		
			contentResolver.bulkInsert(OpenHDS.Settings.CONTENT_ID_URI_BASE,
			values.toArray(emptyArray));
		}
	}

	private void onSyncSuccess() {
		progressDialog.setTitle(mContext.getString(R.string.synced_fws_lbl));
		syncListener.collectionComplete(HttpTask.EndResult.SUCCESS);
	}

	private void onSyncFailure() {
		progressDialog.setTitle(mContext.getString(R.string.failed_sync_fws_lbl));
		syncListener.collectionComplete(HttpTask.EndResult.FAILURE);
	}

	private class SyncFieldWorkerListener implements TaskListener {
		
		public void onFailedAuthentication() {
			onSyncFailure();
		}

		
		public void onConnectionError() {
			onSyncFailure();
		}

		
		public void onConnectionTimeout() {
			onSyncFailure();
		}

		
		public void onSuccess() {
			onSyncSuccess();
		}

		
		public void onFailure() {
			onSyncFailure();
		}

		
		public void onNoContent() {
			onSyncFailure();
		}
	}
}

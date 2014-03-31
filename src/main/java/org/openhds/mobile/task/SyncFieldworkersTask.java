package org.openhds.mobile.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sqlcipher.SQLException;

import org.apache.http.HttpResponse;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.listener.SyncDatabaseListener;
import org.openhds.mobile.model.FieldWorker;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;

public class SyncFieldworkersTask extends HttpTask<Void, Integer> {

	private ContentResolver contentResolver;
	private ProgressDialog progressDialog;
	private SyncDatabaseListener syncListener;

	public SyncFieldworkersTask(RequestContext requestContext,
			ContentResolver contentResolver, ProgressDialog progressDialog,
			SyncDatabaseListener syncListener) {
		super(requestContext);
		this.listener = new SyncFieldWorkerListener();
		this.syncListener = syncListener;
		this.progressDialog = progressDialog;
		this.contentResolver = contentResolver;
	}

	@Override
	protected EndResult handleResponseData(HttpResponse response) {
		try {
			processXMLDocument(response.getEntity().getContent());
		} catch (IllegalStateException | XmlPullParserException | IOException e) {
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
		fw.setPassword("");
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
				fwu.getPassword());

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

	private void onSyncSuccess() {
		progressDialog.setTitle("Synced field workers.");
		syncListener.collectionComplete(HttpTask.EndResult.SUCCESS);
	}

	private void onSyncFailure() {
		progressDialog.setTitle("Failed to sync field workers.");
		syncListener.collectionComplete(HttpTask.EndResult.FAILURE);
	}

	private class SyncFieldWorkerListener implements TaskListener {
		@Override
		public void onFailedAuthentication() {
			onSyncFailure();
		}

		@Override
		public void onConnectionError() {
			onSyncFailure();
		}

		@Override
		public void onConnectionTimeout() {
			onSyncFailure();
		}

		@Override
		public void onSuccess() {
			onSyncSuccess();
		}

		@Override
		public void onFailure() {
			onSyncFailure();
		}

		@Override
		public void onNoContent() {
			onSyncFailure();
		}
	}
}

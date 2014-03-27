package org.openhds.mobile.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.openhds.mobile.database.DatabaseAdapter;
import org.openhds.mobile.model.FieldWorker;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;

public class SyncFieldworkersTask extends HttpTask<Void, Integer> {

	private DatabaseAdapter databaseAdapter;
	private ProgressDialog progressDialog;

	public SyncFieldworkersTask(RequestContext requestContext,
			DatabaseAdapter databaseAdapter, ProgressDialog progressDialog) {
		super(requestContext);
		this.listener = new SyncFieldWorkerListener();
		this.progressDialog = progressDialog;
		this.databaseAdapter = databaseAdapter;
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

		return new FieldWorker(paramMap.get("extId"),
				paramMap.get("firstName"), paramMap.get("lastName"));
	}

	private void replaceAllFieldWorkers(List<FieldWorker> list) {
		databaseAdapter.deleteAllFieldWorkers();
		for (FieldWorker fw : list) {
			databaseAdapter.addFieldWorker(fw);
		}
	}

	private class SyncFieldWorkerListener implements TaskListener {
		@Override
		public void onFailedAuthentication() {
			// progressDialog.setTitle("Failed to sync field workers.");
		}

		@Override
		public void onConnectionError() {
			// progressDialog.setTitle("Failed to sync field workers.");
		}

		@Override
		public void onConnectionTimeout() {
			// progressDialog.setTitle("Failed to sync field workers.");
		}

		@Override
		public void onSuccess() {
			// progressDialog.setTitle("Synced field workers.");
		}

		@Override
		public void onFailure() {
			// progressDialog.setTitle("Failed to sync field workers.");
		}

		@Override
		public void onNoContent() {
			// progressDialog.setTitle("Failed to sync field workers.");
		}
	}
}

package org.openhds.mobile.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.listener.SyncDatabaseListener;
import org.openhds.mobile.model.Settings;
import org.openhds.mobile.task.HttpTask.EndResult;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

public class SyncSettingsTask extends
	AsyncTask<Void, Integer, HttpTask.EndResult>{

	private static final String API_PATH = "/api/rest";
	
	private String baseurl;
	private String username;
	private String password;
	
	private SyncDatabaseListener listener;
	private ContentResolver resolver;

	private ProgressDialog dialog;
	
	private Context mContext;
	
	private UsernamePasswordCredentials creds;
	private HttpClient client;	
	private HttpGet httpGet;	
	
	private enum State {
		DOWNLOADING, SAVING
	}
	
	private State state;
	
	private final List<ContentValues> values = new ArrayList<ContentValues>();
	private final ContentValues[] emptyArray = new ContentValues[] {};	
	
	public SyncSettingsTask(String url, String username, String password,
			ProgressDialog dialog, Context context,
			SyncDatabaseListener listener) {
		this.baseurl = url;
		this.username = username;
		this.password = password;
		this.dialog = dialog;
		this.listener = listener;
		this.resolver = context.getContentResolver();
		this.mContext = context;
	}
	
	@Override
	protected EndResult doInBackground(Void... params) {		
		creds = new UsernamePasswordCredentials(username, password);

		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
		HttpConnectionParams.setSoTimeout(httpParameters, 90000);
		HttpConnectionParams.setSocketBufferSize(httpParameters, 8192);
		client = new DefaultHttpClient(httpParameters);
		
		deleteAllTables();
		
		try {
			processUrl(baseurl + API_PATH + "/settings");
		} catch (Exception e) {
			return HttpTask.EndResult.FAILURE;
		}
		return HttpTask.EndResult.SUCCESS;
	}
	
	private void deleteAllTables() {
		resolver.delete(OpenHDS.Settings.CONTENT_ID_URI_BASE, null, null);
	}
	
	private void processUrl(String url) throws Exception {
		state = State.DOWNLOADING;
		publishProgress();

		httpGet = new HttpGet(url);
		processResponse();
	}	
	
	private void processResponse() throws Exception {
		InputStream inputStream = getResponse();
		if (inputStream != null)
			processXMLDocument(inputStream);
	}	
	
	private InputStream getResponse() throws AuthenticationException, ClientProtocolException, IOException {
		HttpResponse response = null;
		
		httpGet.addHeader(new BasicScheme().authenticate(creds, httpGet));
		httpGet.addHeader("content-type", "application/xml");
		response = client.execute(httpGet);
		
		//Handle 404
		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND){
			throw new RuntimeException("404 Not found.");
		}
		
		HttpEntity entity = response.getEntity();
		return entity.getContent();
	}	
	
	private void processXMLDocument(InputStream content) throws Exception {
		state = State.SAVING;

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);

		XmlPullParser parser = factory.newPullParser();
		parser.setInput(new InputStreamReader(content));

		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT && !isCancelled()) {
			String name = null;
			switch (eventType) {
			case XmlPullParser.START_TAG:
				name = parser.getName();
				if(name.equalsIgnoreCase("generalSettings")) {
					processSettingsParams(parser);
				}
				break;
			}
			eventType = parser.next();
		}	
	}	
	
	private void processSettingsParams(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		
		values.clear();
		ContentValues cv;
		
		parser.nextTag();
		cv = new ContentValues();
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, parser.nextText()); //minMarriageAge
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.MINIMUM_AGE_OF_MARRIAGE); //minMarriageAge
		values.add(cv);

		parser.nextTag(); //
		cv = new ContentValues();
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, parser.nextText()); //minAgeOfHouseholdHead
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.MINIMUM_AGE_OF_HOUSEHOLDHEAD); //minAgeOfHouseholdHead
		values.add(cv);

		parser.nextTag(); //
		cv = new ContentValues();
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, parser.nextText()); //minAgeOfParents
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.MINIMUM_AGE_OF_PARENTS); //minAgeOfParents
		values.add(cv);

		parser.nextTag(); //
		cv = new ContentValues();
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, parser.nextText()); //minAgeOfPregnancy
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.MINIMUM_AGE_OF_PREGNANCY); //minAgeOfPregnancy
		values.add(cv);

		parser.nextTag(); // </generalSettings>
		
		//Insert current date
		cv = new ContentValues();
		Calendar rightNow = Calendar.getInstance();
		Date date = rightNow.getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, dateFormat.format(date)); //DATE_OF_LAST_SYNC
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.DATE_OF_LAST_SYNC); //DATE_OF_LAST_SYNC		
		values.add(cv);
			
		if (!values.isEmpty()) {			
			resolver.bulkInsert(OpenHDS.Settings.CONTENT_ID_URI_BASE,
			values.toArray(emptyArray));
		}		
	}	
	
	
	protected void onPostExecute(HttpTask.EndResult result) {
		listener.collectionComplete(result);
	}	

}

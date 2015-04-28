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
import org.openhds.mobile.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

/**
 * AsyncTask responsible for downloading the OpenHDS "database", that is a
 * subset of the OpenHDS database records. It does the downloading
 * incrementally, by downloading parts of the data one at a time. For example,
 * it gets all locations and then retrieves all individuals. Ordering is
 * somewhat important here, because the database has a few foreign key
 * references that must be satisfied (e.g. individual references a location
 * location)
 */
public class SyncFormsTask extends AsyncTask<Void, Integer, HttpTask.EndResult> {

    private static final String API_PATH = "/api/rest";

    private SyncDatabaseListener listener;
    private ContentResolver resolver;

    private UsernamePasswordCredentials creds;
    private ProgressDialog dialog;
    private HttpGet httpGet;
    private HttpClient client;

    private String baseurl;
    private String username;
    private String password;

    String lastExtId;

    private final List<ContentValues> values = new ArrayList<ContentValues>();
    private final ContentValues[] emptyArray = new ContentValues[] {};

    private State state;
    private Entity entity;
    private Context mContext;

    private enum State {
        DOWNLOADING, SAVING
    }

    private enum Entity {
        FORMS
    }

    public SyncFormsTask(String url, String username, String password, ProgressDialog dialog, Context context,
    		SyncDatabaseListener listener) {
        this.baseurl = url;
        this.username = username;
        this.password = password;
        this.dialog = dialog;
        this.listener = listener;
        this.mContext = context;
        this.resolver = context.getContentResolver();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        StringBuilder builder = new StringBuilder();
        switch (state) {
        case DOWNLOADING:
            builder.append(mContext.getString(R.string.sync_task_downloading)  + " ");
            break;
        case SAVING:
            builder.append(mContext.getString(R.string.sync_task_saving)  + " ");
            break;
        }

        switch (entity) {
        case FORMS:
            builder.append(mContext.getString(R.string.sync_task_extraforms));
            break;
        }

        if (values.length > 0) {
            builder.append( " " +mContext.getString(R.string.sync_task_saved) + " " + values[0] + " " + mContext.getString(R.string.sync_task_items));
        }

        dialog.setMessage(builder.toString());
    }

    @Override
    protected HttpTask.EndResult doInBackground(Void... params) {
        creds = new UsernamePasswordCredentials(username, password);

        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
        HttpConnectionParams.setSoTimeout(httpParameters, 90000);
        HttpConnectionParams.setSocketBufferSize(httpParameters, 8192);
        client = new DefaultHttpClient(httpParameters);

        // at this point, we don't care to be smart about which data to
        // download, we simply download it all
        deleteAllTables();

        try {
            entity = Entity.FORMS;
            processUrl(baseurl + API_PATH + "/forms/cached");
        } catch (Exception e) {
//            return false;
        	return HttpTask.EndResult.FAILURE;
        }

//        return true;
        return HttpTask.EndResult.SUCCESS;
    }

    private void deleteAllTables() {
        // ordering is somewhat important during delete. a few tables have
        // foreign keys
        resolver.delete(OpenHDS.Forms.CONTENT_ID_URI_BASE, null, null);
     
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
                if (name.equalsIgnoreCase("count")) {
                    parser.next();
                    int cnt = Integer.parseInt(parser.getText());
                    publishProgress(cnt);
                    parser.nextTag();
                } else if (name.equalsIgnoreCase("forms")) {
                    processFormsParams(parser);
                }
                break;
            }
            eventType = parser.next();
        }
        
        processLastSyncDate();
    }

    private void processFormsParams(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.nextTag();

        values.clear();
        while (notEndOfXmlDoc("forms", parser)) {
            ContentValues cv = new ContentValues();

            parser.nextTag();
            cv.put(OpenHDS.Forms.COLUMN_FORM_NAME, parser.nextText());
            
            parser.nextTag();
            cv.put(OpenHDS.Forms.COLUMN_FORM_GENDER, parser.nextText());
            values.add(cv);
            
            parser.nextTag(); // </form>
            parser.nextTag(); // </forms>
        }

        if (!values.isEmpty()) {
            resolver.bulkInsert(OpenHDS.Forms.CONTENT_URI, values.toArray(emptyArray));
        }
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
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.DATE_OF_LAST_FORMS_SYNC); //DATE_OF_LAST_SYNC		
		values.add(cv);
			
		if (!values.isEmpty()) {		
			resolver.bulkInsert(OpenHDS.Settings.CONTENT_ID_URI_BASE,
			values.toArray(emptyArray));
		}
	}    

    private boolean notEndOfXmlDoc(String element, XmlPullParser parser) throws XmlPullParserException {
        return !element.equals(parser.getName()) && parser.getEventType() != XmlPullParser.END_TAG && !isCancelled();
    }

    protected void onPostExecute(final HttpTask.EndResult result) {
        listener.collectionComplete(result);
    }
}

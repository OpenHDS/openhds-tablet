package org.openhds.mobile.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.openhds.mobile.R;
import org.openhds.mobile.activity.FieldWorkerLoginActivity;
import org.openhds.mobile.activity.ServerPreferencesActivity;
import org.openhds.mobile.listener.RetrieveFieldWorkersListener;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.Result;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

public class FieldWorkerLoginTask extends AsyncTask<Boolean, Void, Result> {
	
	private SharedPreferences settings;
	private RetrieveFieldWorkersListener listener ;
	
	private ProgressDialog dialog;
	private UsernamePasswordCredentials creds;
	private HttpClient client;
	
	private String extId;
	private String password;
	private boolean isRegistering;
	
	private List<FieldWorker> list;
    private ContentResolver resolver;
		
	public FieldWorkerLoginTask(Context context, SharedPreferences settings, RetrieveFieldWorkersListener listener, 
			ProgressDialog dialog, String extId, String password, boolean isRegistering) {
		this.resolver = context.getContentResolver();
		this.settings = settings;
		this.listener = listener;
		this.dialog = dialog;
		this.extId = extId;
		this.password = password;
		this.isRegistering = isRegistering;
		
		list = new ArrayList<FieldWorker>();
	}

	@Override
    protected Result doInBackground(Boolean... params) {
		
		if (isRegistering) {
			
			try {
				invokeWebService();
				FieldWorker fieldWorker = validateFieldWorker();
				if (fieldWorker != null) {
					if (createFieldWorker(fieldWorker))
						return Result.CREATED_FIELDWORKER_SUCCESS;
					else
						return Result.FIELDWORKER_ALREADY_EXISTS;
				}
			} catch (AuthenticationException e) {
				return Result.BAD_AUTHENTICATION;
			} catch (ClientProtocolException e) {
				return Result.BAD_AUTHENTICATION;
			}  catch (XmlPullParserException e) {
				return Result.BAD_XML;
			}  catch (IOException e) {
				return Result.UNABLE_TO_CONNECT;
			}
		}
		return Result.BAD_AUTHENTICATION;        
    }
	
	private FieldWorker validateFieldWorker() {
		for (FieldWorker fw : list) {
			if (fw.getExtId().equalsIgnoreCase(extId))
				return fw;
		}
		return null;
	}
	
	private void invokeWebService() throws AuthenticationException, ClientProtocolException, IOException, XmlPullParserException {
		
		String username = settings.getString(ServerPreferencesActivity.OPENHDS_KEY_USERNAME, ((FieldWorkerLoginActivity) listener).getString(R.string.username));
	    String password = settings.getString(ServerPreferencesActivity.OPENHDS_KEY_PASSWORD, ((FieldWorkerLoginActivity) listener).getString(R.string.password));
	    String url = settings.getString(ServerPreferencesActivity.OPENHDS_KEY_SERVER, ((FieldWorkerLoginActivity) listener).getString(R.string.default_openhdsserver));
		
		creds = new UsernamePasswordCredentials(username, password);
		 
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		HttpConnectionParams.setSoTimeout(httpParameters, 10000);
		client = new DefaultHttpClient(httpParameters);
		 
		HttpGet httpGet = new HttpGet(url + "/api/rest/fieldworkers");
		 
		HttpResponse response = null;
		InputStream content = null;

        httpGet.addHeader(new BasicScheme().authenticate(creds, httpGet));
        httpGet.addHeader("content-type", "application/xml");
        response = client.execute(httpGet);
		HttpEntity entity = response.getEntity();
		content = entity.getContent();

		processXMLDocument(content);
	}
	
	private void processXMLDocument(InputStream content) throws XmlPullParserException, IOException {
		
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    factory.setNamespaceAware(true);
	       
	    XmlPullParser parser = factory.newPullParser();
	    parser.setInput(new InputStreamReader(content));
	                      
	    int eventType = parser.getEventType();
	    while (eventType != XmlPullParser.END_DOCUMENT && !isCancelled()) {
	    	String name = null;
	           
	        switch(eventType) {            		
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                        
                    if (name.equalsIgnoreCase("fieldworker")) {
                    	processFieldWorkerParams(parser);
                    	((FieldWorkerLoginActivity) listener).runOnUiThread(changeMessageFieldWorker);
                    }
                    break;
            }
            eventType = parser.next();
        }
	}
	
	private Runnable changeMessageFieldWorker = new Runnable() {
	    public void run() {
	        dialog.setMessage("Retrieving Field Workers");
	    }
	};
	
	private void processFieldWorkerParams(XmlPullParser parser) throws XmlPullParserException, IOException {
		String name = "";
		Map<String, String> paramMap = new HashMap<String, String>();
        parser.nextTag(); // <uuid>
        name = parser.getName();
        paramMap.put("uuid", parser.nextText());
        
        parser.nextTag();
        name = parser.getName();
        paramMap.put("extId", parser.nextText());
        
        parser.nextTag();
        name = parser.getName();
        paramMap.put("firstName", parser.nextText());
        
        parser.nextTag();
        name = parser.getName();
        paramMap.put("lastName", parser.nextText());
        
        parser.nextTag();

        FieldWorker fieldWorker = new FieldWorker(paramMap.get("extId"), paramMap.get("firstName"), paramMap.get("lastName"));
        list.add(fieldWorker);
        
        dialog.incrementProgressBy(1);
	}
	
    protected void onProgressUpdate(Integer... progress) {
    	dialog.incrementProgressBy(progress[0]);
    	if (dialog.getProgress() > dialog.getMax()) {
    		dialog.dismiss();
    		dialog.setProgress(0);
    		dialog.setMax(0);
    	}
    }
    
	protected void onPostExecute(final Result result) {
		dialog.setProgress(0);
		listener.retrieveFieldWorkersComplete(result);
	}
	
	private boolean createFieldWorker(FieldWorker fieldWorker) {
	    Cursor cusor = resolver.query(OpenHDS.FieldWorkers.CONTENT_ID_URI_BASE, new String[]{OpenHDS.FieldWorkers._ID}, OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_EXTID + " = ?", new String[]{fieldWorker.getExtId()}, null);
	    boolean found = cusor != null ? cusor.getCount() > 0 : false;
	    cusor.close();
	    
	    if (found) {
	        return false;
	    }
	    
	    ContentValues cv = new ContentValues();
	    cv.put(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_EXTID, extId);
	    cv.put(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_FIRSTNAME, fieldWorker.getFirstName());
	    cv.put(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_LASTNAME, fieldWorker.getLastName());
	    cv.put(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_PASSWORD, password);
	    
	    Uri uri = resolver.insert(OpenHDS.FieldWorkers.CONTENT_ID_URI_BASE, cv);
	    
		return uri != null;
	}
}

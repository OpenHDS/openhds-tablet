package org.openhds.mobile.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.openhds.mobile.provider.OpenHDSProvider;
import android.os.Environment;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
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
import org.openhds.mobile.R;
import org.openhds.mobile.listener.SyncDatabaseListener;
import org.openhds.mobile.model.Settings;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * AsyncTask responsible for downloading the OpenHDS "database", that is a
 * subset of the OpenHDS database records. It does the downloading
 * incrementally, by downloading parts of the data one at a time. For example,
 * it gets all locations and then retrieves all individuals. Ordering is
 * somewhat important here, because the database has a few foreign key
 * references that must be satisfied (e.g. individual references a location
 * location)
 */
public class SyncEntitiesTask extends
		AsyncTask<Void, Integer, HttpTask.EndResult> {

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
	private boolean isDownloadingZipFile;
	

	private enum State {
		DOWNLOADING, SAVING
	}

	private enum Entity {
		LOCATION_HIERARCHY, LOCATION, ROUND, VISIT, RELATIONSHIP, INDIVIDUAL, SOCIALGROUP, LOCATION_HIERARCHY_LEVELS, SETTINGS
	}

	private Context mContext;
	
	public SyncEntitiesTask(String url, String username, String password,
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
	protected void onProgressUpdate(Integer... values) {
		StringBuilder builder = new StringBuilder();
		switch (state) {
		case DOWNLOADING:
			builder.append(mContext.getString(R.string.sync_task_downloading) + " ");
			break;
		case SAVING:
			builder.append(mContext.getString(R.string.sync_task_saving) + " ");
			break;
		}

		switch (entity) {
		case INDIVIDUAL:
			builder.append(mContext.getString(R.string.sync_task_individuals));
			break;
		case LOCATION:
			builder.append(mContext.getString(R.string.sync_task_locations));
			break;
		case LOCATION_HIERARCHY:
			builder.append(mContext.getString(R.string.sync_task_loc_hierarchy));
			break;
		case LOCATION_HIERARCHY_LEVELS:
			builder.append(mContext.getString(R.string.sync_task_loc_hierarchy_levels));
			break;
		case RELATIONSHIP:
			builder.append(mContext.getString(R.string.sync_task_relationships));
			break;
		case ROUND:
			builder.append(mContext.getString(R.string.sync_task_rounds));
			break;
		case SOCIALGROUP:
			builder.append(mContext.getString(R.string.sync_task_socialgroups));
			break;
		case VISIT:
			builder.append(mContext.getString(R.string.sync_task_visits));
			break;
		case SETTINGS:
			builder.append(mContext.getString(R.string.sync_task_settings));
			break;			
		}

		if (values.length > 0) {
			//builder.append(" " + mContext.getString(R.string.sync_task_saved)  + " " + values[0]  + " " +  mContext.getString(R.string.sync_task_items));
			String msg = " " + mContext.getString(R.string.sync_task_saved)  + " " + values[0]  + " " +  mContext.getString(R.string.sync_task_items);
			
			if (state == State.DOWNLOADING && isDownloadingZipFile){
				msg = " " + mContext.getString(R.string.sync_task_saved)  + " " + values[0]  + "KB";
			}
			
			builder.append(msg);
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
			entity = Entity.SETTINGS;
			processUrl(baseurl + API_PATH + "/settings");
			
			entity = Entity.LOCATION_HIERARCHY;
			processUrl(baseurl + API_PATH + "/locationhierarchies");

			entity = Entity.LOCATION;
			processUrl(baseurl + API_PATH + "/locations/zipped");

			entity = Entity.ROUND;
			processUrl(baseurl + API_PATH + "/rounds");
			
			entity = Entity.LOCATION_HIERARCHY_LEVELS;
			processUrl(baseurl + API_PATH + "/locationhierarchylevels");

			entity = Entity.VISIT;
			processUrl(baseurl + API_PATH + "/visits/zipped");

			entity = Entity.RELATIONSHIP;
			processUrl(baseurl + API_PATH + "/relationships/zipped");

			entity = Entity.INDIVIDUAL;
			processUrl(baseurl + API_PATH + "/individuals/zipped");

			entity = Entity.SOCIALGROUP;
			processUrl(baseurl + API_PATH + "/socialgroups/zipped");	
		} catch (Exception e) {
			if(e instanceof HttpException && e.getMessage() != null){
				if(e.getMessage().equalsIgnoreCase(HttpTask.EndResult.NO_CONTENT.name())){
					return HttpTask.EndResult.NO_CONTENT;
				}
			}
			return HttpTask.EndResult.FAILURE;
		}

		return HttpTask.EndResult.SUCCESS;
	}

	private void deleteAllTables() {
		// ordering is somewhat important during delete. a few tables have
		// foreign keys
		resolver.delete(OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE, null,
				null);
		resolver.delete(OpenHDS.Rounds.CONTENT_ID_URI_BASE, null, null);
		resolver.delete(OpenHDS.Visits.CONTENT_ID_URI_BASE, null, null);
		resolver.delete(OpenHDS.Relationships.CONTENT_ID_URI_BASE, null, null);
		resolver.delete(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, null, null);
		resolver.delete(OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE, null, null);
		resolver.delete(OpenHDS.HierarchyLevels.CONTENT_ID_URI_BASE, null, null);
		resolver.delete(OpenHDS.Individuals.CONTENT_ID_URI_BASE, null, null);
		resolver.delete(OpenHDS.Locations.CONTENT_ID_URI_BASE, null, null);
		resolver.delete(OpenHDS.Settings.CONTENT_ID_URI_BASE, null, null);
	}


	 private String getAppStoragePath(){
	 	File root = Environment.getExternalStorageDirectory();
	 	String destinationPath = root.getAbsolutePath() + File.separator
	 			+ "Android" + File.separator + "data" + File.separator
	 			+ "org.openhds.mobile" + File.separator + "files" + File.separator + "downloads" + File.separator;
	  	File baseDir = new File(destinationPath);
	 	if (!baseDir.exists()) {
	 		boolean created = baseDir.mkdirs();
	 		if (!created) {
	 			return destinationPath;
	 		}
	 	}
	 	
	 	return destinationPath;
	 }
	 
	 private InputStream saveFileToStorage(InputStream inputStream) throws Exception {
	  	String path = getAppStoragePath() + "temp.zip";
	 	FileOutputStream fout = new FileOutputStream(path);
	 	byte[] buffer = new byte[10*1024];
	 	int len = 0;
	 	long total = 0;
	  	publishProgress();
	  	while ((len = inputStream.read(buffer)) != -1){
	 		fout.write(buffer, 0, len);
	 		total += len;
	 		int perc =  (int) ((total/(1024)));
	 		publishProgress(perc);
	 	}
	  	fout.close();
	 	inputStream.close();
	  	FileInputStream fin = new FileInputStream(path);
	  	return fin;
	 }
	 
	 private void processZIPDocument(InputStream inputStream) throws Exception {
	  	Log.d("zip", "processing zip file");
	   	ZipInputStream zin = new ZipInputStream(inputStream);
	 	ZipEntry entry = zin.getNextEntry();
	  	if (entry != null){
	 		processXMLDocument(zin);
	 		zin.closeEntry();
	 	}
	  	zin.close();
	 }
	
	private void processUrl(String url) throws Exception {
		state = State.DOWNLOADING;
		publishProgress();

		this.isDownloadingZipFile = url.endsWith("zipped");
		
		httpGet = new HttpGet(url);
		processResponse();
	}

	private void processResponse() throws Exception {
		InputStream inputStream = getResponse();
				
		if (this.isDownloadingZipFile){
		 	InputStream zipInputStream = saveFileToStorage(inputStream);
		 	if (zipInputStream != null){
		 		Log.d("download", "zip = "+zipInputStream);
		 		processZIPDocument(zipInputStream);
		 		zipInputStream.close();
		 	}
		 		
		}else{
		 	if (inputStream != null)
		 		processXMLDocument(inputStream);
		}
	}

	private InputStream getResponse() throws AuthenticationException,
			ClientProtocolException, IOException, HttpException, Exception {
		HttpResponse response = null;

		httpGet.addHeader(new BasicScheme().authenticate(creds, httpGet));
		httpGet.addHeader("content-type", "application/xml");
		response = client.execute(httpGet);
		
		//Handle 404
		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND){
			throw new RuntimeException("404 Not found.");
		}		

		HttpEntity entity = response.getEntity();
		
		PushbackInputStream in = null;
		boolean empty = false;
		if(entity != null) {
			in = new PushbackInputStream(entity.getContent());
		    try {
		        int firstByte=in.read();
		        if(firstByte != -1) {
		            in.unread(firstByte);
		        }
		        else {
		            // empty
		        	empty = true;
		        	throw new HttpException(HttpTask.EndResult.NO_CONTENT.name());
		        }
		    }
		    catch(Exception e){
		    	throw e;
		    }
		    finally {
		        // Don't close so we can reuse the connection
//		        EntityUtils.consumeQuietly(entity);
		    	
		        // Or, if you're sure you won't re-use the connection
		    	if(empty){
		    		in.close();
		    		in = null;
		    	}
		    }
		}
		
		return in;
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
				} else if (name.equalsIgnoreCase("individuals")) {
					processIndividualParams(parser);
				} else if (name.equalsIgnoreCase("locations")) {
					processLocationParams(parser);
				} else if (name.equalsIgnoreCase("locationhierarchies")) {
					processHierarchyParams(parser);
				} else if (name.equalsIgnoreCase("rounds")) {
					processRoundParams(parser);
				} else if (name.equalsIgnoreCase("locationhierarchylevels")) {
					processHierarchyLevelsParams(parser);
				} else if (name.equalsIgnoreCase("visits")) {
					processVisitParams(parser);
				} else if (name.equalsIgnoreCase("socialgroups")) {
					processSocialGroupParams(parser);
				} else if (name.equalsIgnoreCase("relationships")) {
					processRelationshipParams(parser);
				} else if(name.equalsIgnoreCase("generalSettings")) {
					processSettingsParams(parser);
				}
				break;
			}		
			eventType = parser.next();
		}
	}

	private void processHierarchyParams(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.nextTag();

		values.clear();
		while (notEndOfXmlDoc("locationHierarchies", parser)) {
			ContentValues cv = new ContentValues();

			parser.nextTag();
			cv.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID,
					parser.nextText());

			parser.nextTag(); // <level>
			parser.next(); // <keyIdentifier>
			parser.nextText();
			parser.nextTag(); // <name>
			cv.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL,
					parser.nextText());

			parser.next(); // </level>
			parser.nextTag();
			cv.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME,
					parser.nextText());

			parser.next(); // <parent>
			parser.nextTag(); // <extId>
			cv.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT,
					parser.nextText());

			values.add(cv);

			parser.nextTag(); // </parent>
			parser.nextTag(); // </hierarchy>
			parser.nextTag(); // <hierarchy> or </hiearchys>
		}

		if (!values.isEmpty()) {
			resolver.bulkInsert(OpenHDS.HierarchyItems.CONTENT_URI,
					values.toArray(emptyArray));
		}
	}

	private boolean notEndOfXmlDoc(String element, XmlPullParser parser)
			throws XmlPullParserException {
		return !element.equals(parser.getName())
				&& parser.getEventType() != XmlPullParser.END_TAG
				&& !isCancelled();
	}

	private void processLocationParams(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.nextTag();
		int locationCount = 0;
		OpenHDSProvider provider = OpenHDSProvider.CURRENT_PROVIDER;
		 Log.d("provider-hacking", ""+provider);
		 
		 SQLiteDatabase db = provider.openDatabaseForFastInsert();
		values.clear();
		while (notEndOfXmlDoc("locations", parser)) {
			// skip collected by
			parser.nextTag(); // <collectedBy>
			parser.nextTag(); // <extId>
			parser.nextText();
			parser.nextTag(); // </collectedBy>

			// skip accuracy
			parser.nextTag();
			parser.nextText();

			// skip altitude
			parser.nextTag();
			parser.nextText();

			ContentValues cv = new ContentValues();

			parser.nextTag();
			cv.put(OpenHDS.Locations.COLUMN_LOCATION_EXTID, parser.nextText());

			parser.nextTag();
			cv.put(OpenHDS.Locations.COLUMN_LOCATION_LATITUDE,
					parser.nextText());

			parser.nextTag(); // <locationLevel>
			parser.nextTag();
			cv.put(OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY,
					parser.nextText());
			parser.nextTag(); // </locationLevel>

			parser.nextTag();
			cv.put(OpenHDS.Locations.COLUMN_LOCATION_NAME, parser.nextText());

			// skip location type
			parser.nextTag();
			parser.nextText();

			parser.nextTag();
			cv.put(OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE,
					parser.nextText());

			//values.add(cv);
			//insert one by one
			provider.insert(db, OpenHDS.Locations.CONTENT_ID_URI_BASE, cv);

			locationCount += 1;

			parser.nextTag(); // </location>
			parser.nextTag(); // <location> or </locations>

			if (locationCount % 100 == 0) {
				//persistLocations();
				//values.clear();
				publishProgress(locationCount);
			}
		}

		//persistLocations();
		provider.finishDatabaseFastInsert(db);
	}

	private void persistLocations() {
		if (!values.isEmpty()) {
			resolver.bulkInsert(OpenHDS.Locations.CONTENT_ID_URI_BASE,
					values.toArray(emptyArray));
		}
	}

	private void processIndividualParams(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		
		int individualsParsed = 0;
		parser.nextTag();
		OpenHDSProvider provider = OpenHDSProvider.CURRENT_PROVIDER;
		 Log.d("provider-hacking", ""+provider);
		 		
		 SQLiteDatabase db = provider.openDatabaseForFastInsert();
		values.clear();
		List<ContentValues> individualSocialGroups = new ArrayList<ContentValues>();
		while (notEndOfXmlDoc("individuals", parser)) {
			try {
				ContentValues cv = new ContentValues();
				// memberships
				parser.nextTag();
				parser.nextTag();
				List<String> groups = null;
				if (parser.getEventType() != XmlPullParser.END_TAG) {
					// memberships are present
					groups = parseMembershipExtIds(parser);
				}

				// residencies
				parser.nextTag();
				parser.nextTag();
				if (parser.getEventType() != XmlPullParser.END_TAG) {
					parser.nextTag(); // <endType>
					// parser.nextTag(); // </endType>
					cv.put(OpenHDS.Individuals.COLUMN_RESIDENCE_END_TYPE,
							parser.nextText());
					parser.nextTag(); // <location>
					parser.nextTag(); // <extId>
					cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE,
							parser.nextText());
					parser.nextTag(); // </location>
					parser.nextTag(); // </residency>
					parser.nextTag(); // </residencies>
				}

				parser.nextTag();
				cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB,
						parser.nextText());

				parser.nextTag();
				cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID,
						parser.nextText());
				lastExtId = cv
						.getAsString(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID);

				// father
				parser.nextTag(); // <father>
				parser.nextTag(); // <memberships>
				parser.nextTag(); // </memberships>
				parser.nextTag(); // <residencies>
				parser.nextTag(); // </residencies>
				parser.nextTag(); // <extId>
				cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER,
						parser.nextText());
				parser.nextTag(); // </father>

				parser.nextTag();
				cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRSTNAME,
						parser.nextText());

				parser.nextTag();
				cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER,
						parser.nextText());

				parser.nextTag();
				cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LASTNAME,
						parser.nextText());

				parser.nextTag();
				if ("middlename".equalsIgnoreCase(parser.getName())) {
					parser.nextText();
				}

				// mother
				parser.nextTag(); // <mother>
				parser.nextTag(); // <memberships>
				parser.nextTag(); // </memberships>
				parser.nextTag(); // <residencies>
				parser.nextTag(); // </residencies>
				parser.nextTag(); // <extId>
				cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER,
						parser.nextText());
				parser.nextTag(); // </mother>

				//values.add(cv);

				if (groups != null) {
					for (String item : groups) {
						ContentValues socialGroups = new ContentValues();
						socialGroups.put(OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID, cv.getAsString(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID));
		 				socialGroups.put(OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID, item);
		 				//individualSocialGroups.add(socialGroups);
		 				
		 				//persist individual socialgroup, insert one by one
		 				provider.insert(db, OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE, socialGroups);
					}
				}

				individualsParsed += 1;
				//persist individual, insert one by one
				provider.insert(db, OpenHDS.Individuals.CONTENT_ID_URI_BASE, cv);

				parser.nextTag(); // </individual> / or <religion>
				if(parser.getName().equalsIgnoreCase("religion")){
					//skip again to next element
					parser.nextText();
					parser.nextTag(); // </individual>
				}
				parser.nextTag(); // </individuals> or <individual>

				if (individualsParsed % 100 == 0) {
					//(individualSocialGroups);
					//values.clear();
					//individualSocialGroups.clear();
					publishProgress(individualsParsed);
				}
			} catch (Exception e) {
				Log.e(getClass().getName(), e.getMessage());
			}
		}
		//persistParsedIndividuals(individualSocialGroups);
		provider.finishDatabaseFastInsert(db);
	}

	private void persistParsedIndividuals(
			List<ContentValues> individualSocialGroups) {		
		if (!values.isEmpty()) {
			resolver.bulkInsert(OpenHDS.Individuals.CONTENT_ID_URI_BASE,
					values.toArray(emptyArray));
		}

		if (!individualSocialGroups.isEmpty()) {
			resolver.bulkInsert(OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE,
					individualSocialGroups.toArray(emptyArray));
		}
	}

	private List<String> parseMembershipExtIds(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		List<String> groups = new ArrayList<String>();

		while (!"memberships".equalsIgnoreCase(parser.getName())) {
			parser.nextTag(); // <socialGroup>
			parser.nextTag(); // <extId>
			groups.add(parser.nextText());
			parser.nextTag(); // <socialGroup>
			parser.nextTag(); // <bIsToA>
			parser.nextText();
			parser.nextTag(); // </membership>
			parser.nextTag(); // <membership> or </memberships>
		}

		return groups;
	}

	
	
	private void processHierarchyLevelsParams(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.nextTag();

		values.clear();
		while (notEndOfXmlDoc("locationhierarchylevels", parser)) {
			ContentValues cv = new ContentValues();

			parser.nextTag();
			cv.put(OpenHDS.HierarchyLevels.COLUMN_LEVEL_IDENTIFIER, parser.nextText());

			parser.nextTag();
			cv.put(OpenHDS.HierarchyLevels.COLUMN_LEVEL_NAME, parser.nextText());

			parser.nextTag();
			cv.put(OpenHDS.HierarchyLevels.COLUMN_LEVEL_UUID, parser.nextText());


			values.add(cv);

			parser.nextTag(); // </locationhierarchylevels>
			parser.nextTag(); // </locationhierarchylevels> or <locationhierarchylevel>
		}

		if (!values.isEmpty()) {
			resolver.bulkInsert(OpenHDS.HierarchyLevels.CONTENT_ID_URI_BASE,
					values.toArray(emptyArray));
		}
	}
	private void processRoundParams(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.nextTag();

		values.clear();
		while (notEndOfXmlDoc("rounds", parser)) {
			ContentValues cv = new ContentValues();

			parser.nextTag();
			cv.put(OpenHDS.Rounds.COLUMN_ROUND_ENDDATE, parser.nextText());

			// skip <remarks />
			parser.nextTag();
			parser.nextText();

			parser.nextTag();
			cv.put(OpenHDS.Rounds.COLUMN_ROUND_NUMBER, parser.nextText());

			parser.nextTag();
			cv.put(OpenHDS.Rounds.COLUMN_ROUND_STARTDATE, parser.nextText());

			// skip <uuid />
			parser.nextTag();
			parser.nextText();

			values.add(cv);

			parser.nextTag(); // </round>
			parser.nextTag(); // </rounds> or <round>
		}

		if (!values.isEmpty()) {
			resolver.bulkInsert(OpenHDS.Rounds.CONTENT_ID_URI_BASE,
					values.toArray(emptyArray));
		}
	}

	private void processVisitParams(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.nextTag();
		OpenHDSProvider provider = OpenHDSProvider.CURRENT_PROVIDER;
		Log.d("provider-hacking", ""+provider);
		int visitsCount = 0;
		 
		 SQLiteDatabase db = provider.openDatabaseForFastInsert();
		values.clear();
		while (notEndOfXmlDoc("visits", parser)) {
			// skip collected by
			parser.nextTag(); // <collectedBy>
			parser.nextTag(); // <extId>
			parser.nextText();
			parser.nextTag(); // </collectedBy>

			ContentValues cv = new ContentValues();

			parser.nextTag();
			cv.put(OpenHDS.Visits.COLUMN_VISIT_EXTID, parser.nextText());
			parser.nextTag(); // <realVisit>
			parser.nextText();
			parser.nextTag();
			cv.put(OpenHDS.Visits.COLUMN_VISIT_ROUND, parser.nextText());

			parser.nextTag();
			cv.put(OpenHDS.Visits.COLUMN_VISIT_DATE, parser.nextText());

			parser.nextTag(); // <visitLocation>
			parser.nextTag();
			cv.put(OpenHDS.Visits.COLUMN_VISIT_LOCATION, parser.nextText());
			parser.nextTag(); // </visitLocation>

			//values.add(cv);

			visitsCount++;
		 	
		 	//insert one by one
		 	provider.insert(db, OpenHDS.Visits.CONTENT_ID_URI_BASE, cv);

		 	if (visitsCount % 100 == 0) {
		 		//persistLocations();
		 		//values.clear();
		 		publishProgress(visitsCount);
		 	}
		 
		 			parser.nextTag(); // </visit>
		 			parser.nextTag(); // </visits> or <visit>
		 		}
		 
		 provider.finishDatabaseFastInsert(db);
		 
		 /*
		 		if (!values.isEmpty()) {
		 			resolver.bulkInsert(OpenHDS.Visits.CONTENT_ID_URI_BASE,
		 					values.toArray(emptyArray));
		 		}
		 */
	}

	private void processSocialGroupParams(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.nextTag();
		int sgCounts = 0;
		 
		 OpenHDSProvider provider = OpenHDSProvider.CURRENT_PROVIDER;
		 Log.d("provider-hacking", ""+provider);
		 
		 SQLiteDatabase db = provider.openDatabaseForFastInsert();
		values.clear();
		while (notEndOfXmlDoc("socialGroups", parser)) {
			ContentValues cv = new ContentValues();

			parser.nextTag();
			cv.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID,
					parser.nextText());

			parser.nextTag(); // <groupHead>
			parser.nextTag(); // <memberships>
			parser.nextTag(); // </memberships>
			parser.nextTag(); // <residencies>
			parser.nextTag(); // </residencies>
			parser.nextTag(); // <extId>
			cv.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPHEAD,
					parser.nextText());
			parser.nextTag(); // </groupHead>

			parser.nextTag();
			cv.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME,
					parser.nextText());

			//values.add(cv);
		 	provider.insert(db, OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, cv);

		 	sgCounts++;
		 	
		 	if (sgCounts % 100 == 0) {
		 		//persistLocations();
		 		//values.clear();
		 		publishProgress(sgCounts);
		 	}
		 
		 			parser.nextTag(); // </socialGroup>
		 			parser.nextTag(); // </socialGroups> or <socialGroup>
		 		}
		 
		 provider.finishDatabaseFastInsert(db);
		 
		 /*
		 		if (!values.isEmpty()) {
		 			resolver.bulkInsert(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE,
		 					values.toArray(emptyArray));
		 		}
		 */
	}

	private void processRelationshipParams(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.nextTag();
		int relatCounts = 0;
		 
		 OpenHDSProvider provider = OpenHDSProvider.CURRENT_PROVIDER;
		 Log.d("provider-hacking", ""+provider);
		 
		 SQLiteDatabase db = provider.openDatabaseForFastInsert();
		values.clear();
		while (notEndOfXmlDoc("relationships", parser)) {
			ContentValues cv = new ContentValues();

			parser.nextTag(); // <individualA>
			parser.nextTag(); // <memberships>
			parser.nextTag(); // </memberships>
			parser.nextTag(); // <residencies>
			parser.nextTag(); // <residencies>
			parser.nextTag(); // <extId>
			cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A,
					parser.nextText());
			parser.nextTag(); // </individualA>

			parser.nextTag(); // <individualB>
			parser.nextTag(); // <memberships>
			parser.nextTag(); // </memberships>
			parser.nextTag(); // <residencies>
			parser.nextTag(); // <residencies>
			parser.nextTag();
			cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B,
					parser.nextText());
			parser.nextTag(); // </individualB>

			parser.nextTag();
			cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE,
					parser.nextText());

			parser.nextTag(); // <aIsToB>
			parser.nextText();

			//values.add(cv);
		 	provider.insert(db, OpenHDS.Relationships.CONTENT_ID_URI_BASE, cv);
		 	
		 	relatCounts++;

		 	if (relatCounts % 100 == 0) {
		 		//persistLocations();
		 		//values.clear();
		 		publishProgress(relatCounts);
		 	}
		 
		 			parser.nextTag(); // </relationship>
		 			parser.nextTag(); // </relationships> or <relationship>
		 		}
		 
		 provider.finishDatabaseFastInsert(db);
		 
		 /*
		 		if (!values.isEmpty()) {
		 			resolver.bulkInsert(OpenHDS.Relationships.CONTENT_ID_URI_BASE,
		 					values.toArray(emptyArray));
		 		}
		 */
	}
	
	private void processSettingsParams(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		
		values.clear();
		ContentValues cv;
		
		parser.nextTag(); //
		cv = new ContentValues();
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, parser.nextText()); //earliestEventDate
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.DATE_OF_EARLIEST_EVENT); //earliestEventDate
		values.add(cv);
		
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
				
		parser.nextTag(); // <visitLevel> or <entities>
		
		if(parser.getName().equalsIgnoreCase("entities")){
			parseEntities(parser);
			parser.nextTag();	// <visitLevel>
		}
		
		cv = new ContentValues();
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, parser.nextText()); //visitLevel
		cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.VISIT_LEVEL); //visitLevel
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
	
	private void parseEntities(XmlPullParser parser)
			throws XmlPullParserException, IOException{
		ContentValues cv;
		do{
			parser.nextTag();
			if(parser.getEventType() == XmlPullParser.START_TAG){
				if(parser.getAttributeCount() == 2){
					String attEntityName = parser.getAttributeValue(null, "name");
					String attEntityCount = parser.getAttributeValue(null, "count");
					if(attEntityName != null && attEntityCount != null){
						if(attEntityName.equalsIgnoreCase("visit")){
							cv = new ContentValues();
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, attEntityCount); 
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.NUMBER_OF_VISITS); 
							values.add(cv);
						}
						else if(attEntityName.equalsIgnoreCase("individual")){
							cv = new ContentValues();
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, attEntityCount);
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.NUMBER_OF_INDIVIDUALS);
							values.add(cv);
						}
						else if(attEntityName.equalsIgnoreCase("location")){
							cv = new ContentValues();
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, attEntityCount); 
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.NUMBER_OF_LOCATIONS);
							values.add(cv);
						}	
						else if(attEntityName.equalsIgnoreCase("relationship")){
							cv = new ContentValues();
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, attEntityCount);
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.NUMBER_OF_RELATIONSHIPS);
							values.add(cv);
						}
						else if(attEntityName.equalsIgnoreCase("socialgroup")){
							cv = new ContentValues();
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, attEntityCount);
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.NUMBER_OF_SOCIALGROUPS);
							values.add(cv);
						}
						else if(attEntityName.equalsIgnoreCase("Form")){
							cv = new ContentValues();
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, attEntityCount);
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.NUMBER_OF_EXTRAFORMS);
							values.add(cv);
						}
						else if(attEntityName.equalsIgnoreCase("Fieldworker")){
							cv = new ContentValues();
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_VALUE, attEntityCount);
							cv.put(OpenHDS.Settings.COLUMN_SETTINGS_NAME, Settings.NUMBER_OF_FIELDWORKERS);
							values.add(cv);
						}						
					}
				}
			}
		}
		while(parser.getName().equalsIgnoreCase("entity"));		
	}

	protected void onPostExecute(HttpTask.EndResult result) {
		listener.collectionComplete(result);
	}
}

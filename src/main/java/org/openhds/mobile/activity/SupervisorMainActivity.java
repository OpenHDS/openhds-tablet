package org.openhds.mobile.activity;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LayoutUtils.makeNewGenericButton;
import static org.openhds.mobile.utilities.UrlUtils.buildServerUrl;

import java.util.Map;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.fragment.LoginPreferenceFragment;
import org.openhds.mobile.model.Settings;
import org.openhds.mobile.provider.OpenHDSProvider;
import org.openhds.mobile.task.HttpTask.RequestContext;
import org.openhds.mobile.task.SyncEntitiesTask;
import org.openhds.mobile.task.SyncFieldworkersTask;
import org.openhds.mobile.task.SyncFormsTask;
import org.openhds.mobile.utilities.SyncDatabaseHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProvider;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SupervisorMainActivity extends Activity implements OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

	private FrameLayout prefContainer;
	private LinearLayout supervisorOptionsList;
	private SyncDatabaseHelper syncDatabaseHelper;
	private TextView lastSyncedDatabase;
	private TextView lastSyncedFieldWorkers;
	private TextView lastSyncedExtraForms;
	private static final String completeColor = "#9ACD32";
	private static final String incompleteColor = "#FF4500";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.supervisor_main);

		prefContainer = (FrameLayout) findViewById(R.id.login_pref_container);
		supervisorOptionsList = (LinearLayout) findViewById(R.id.supervisor_activity_options);
		syncDatabaseHelper = new SyncDatabaseHelper(this);
		
		makeNewGenericButton(this,
				getResourceString(this, R.string.sync_database_description),
				getResourceString(this, R.string.sync_database_name),
				getResourceString(this, R.string.sync_database_name), this,
				supervisorOptionsList);
		
        // add text view to display last settings sync time
        lastSyncedDatabase = new TextView(this);
        supervisorOptionsList.addView(lastSyncedDatabase);		

		makeNewGenericButton(
				this,
				getResourceString(this, R.string.sync_field_worker_description),
				getResourceString(this, R.string.sync_field_worker_name),
				getResourceString(this, R.string.sync_field_worker_name), this,
				supervisorOptionsList);	
		
        // add text view to display last settings sync time
		lastSyncedFieldWorkers = new TextView(this);
        supervisorOptionsList.addView(lastSyncedFieldWorkers);			
		
		makeNewGenericButton(
				this,
				getResourceString(this, R.string.download_extraform_button),
				getResourceString(this, R.string.sync_extraforms),
				getResourceString(this, R.string.sync_extraforms), this,
				supervisorOptionsList);	
		
        // add text view to display last settings sync time
		lastSyncedExtraForms = new TextView(this);
        supervisorOptionsList.addView(lastSyncedExtraForms);		
        
        //Button to open statistics view        
        makeNewGenericButton(
				this,
				getResourceString(this, R.string.sync_stats_button),
				getResourceString(this, R.string.sync_stats),
				"btnStats", this,
				supervisorOptionsList);	

		if (null != savedInstanceState) {
			return;
		}

		getFragmentManager().beginTransaction()
				.add(R.id.login_pref_container, new LoginPreferenceFragment())
				.commit();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.login_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Defining what happens when a main menu item is selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean isShowingPreferences = View.VISIBLE == prefContainer
				.getVisibility();
		if (isShowingPreferences) {
			prefContainer.setVisibility(View.GONE);
		} else {
			prefContainer.setVisibility(View.VISIBLE);
		}
		return true;
	}

	public void onClick(View v) {
		String tag = (String) v.getTag();
		if (tag.equals(getResourceString(this, R.string.sync_database_name))) {
			syncDatabase();
		} else if (tag.equals(getResourceString(this,
				R.string.sync_field_worker_name))) {
			syncFieldWorkers();
		} else if (tag.equals(getResourceString(this,
				R.string.sync_extraforms))) {
			syncExtraForms();
		}
		else if (tag.equals("btnStats")) {
			displaySyncStats();
		}
	}
	
	@Override
	protected void onResume() {
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		
		displayLastSyncDate();
		super.onResume();
	}
	
	private void syncExtraForms(){
		String url = getPreferenceString(this, R.string.openhds_server_url_key, "");
		String username = (String) getIntent().getExtras().get( OpeningActivity.USERNAME_KEY);
		String password = (String) getIntent().getExtras().get( OpeningActivity.PASSWORD_KEY);

		SyncFormsTask task = new SyncFormsTask(url, username, password,
				syncDatabaseHelper.getProgressDialog(), this, syncDatabaseHelper);
		
		syncDatabaseHelper.setCurrentTask(task);
		syncDatabaseHelper.startSync();
	}

	private void syncDatabase() {

		String username = (String) getIntent().getExtras().get(
				OpeningActivity.USERNAME_KEY);
		String password = (String) getIntent().getExtras().get(
				OpeningActivity.PASSWORD_KEY);

		String openHdsBaseUrl = getPreferenceString(this,
				R.string.openhds_server_url_key, "");
		SyncEntitiesTask currentTask = new SyncEntitiesTask(openHdsBaseUrl,
				username, password, syncDatabaseHelper.getProgressDialog(),
				this, syncDatabaseHelper);
		syncDatabaseHelper.setCurrentTask(currentTask);
		syncDatabaseHelper.startSync();
	}

	private void syncFieldWorkers() {

		String username = (String) getIntent().getExtras().get(
				OpeningActivity.USERNAME_KEY);
		String password = (String) getIntent().getExtras().get(
				OpeningActivity.PASSWORD_KEY);
		String path = getResourceString(this, R.string.field_workers_sync_url);

		RequestContext requestContext = new RequestContext().user(username)
				.password(password).url(buildServerUrl(this, path));
		SyncFieldworkersTask currentTask = new SyncFieldworkersTask(
				getApplicationContext(), requestContext, getContentResolver(),
				syncDatabaseHelper.getProgressDialog(), syncDatabaseHelper);
		syncDatabaseHelper.setCurrentTask(currentTask);

		syncDatabaseHelper.startSync();
	}
		
	public void displayLastSyncDate(){
		android.database.Cursor c = Queries.getAllSettings(getContentResolver());
		Settings settings = Converter.convertToSettings(c); 
		c.close();
		
		String lastDatabaseSyncDate = settings.getDateOfLastSync();
		String lastSyncDesc = getResourceString(this,R.string.sync_last_sync);
		String lastSyncUnavailable = getResourceString(this,R.string.sync_last_sync_unavailable);		
		lastDatabaseSyncDate = ((lastDatabaseSyncDate==null)||lastDatabaseSyncDate.isEmpty())?lastSyncUnavailable:lastDatabaseSyncDate;
		if(lastSyncedDatabase != null)
			lastSyncedDatabase.setText(lastSyncDesc + " " + lastDatabaseSyncDate);
		
		String lastFieldWorkerSyncDate = settings.getDateOfLastFieldWorkerSync();
		lastFieldWorkerSyncDate = ((lastFieldWorkerSyncDate==null)||lastFieldWorkerSyncDate.isEmpty())?lastSyncUnavailable:lastFieldWorkerSyncDate;
		if(lastSyncedFieldWorkers != null)
			lastSyncedFieldWorkers.setText(lastSyncDesc + " " + lastFieldWorkerSyncDate);
		
		String lastExtraFormsSyncDate = settings.getDateOfLastFormsSync();
		lastExtraFormsSyncDate = ((lastExtraFormsSyncDate==null)||lastExtraFormsSyncDate.isEmpty())?lastSyncUnavailable:lastExtraFormsSyncDate;
		if(lastSyncedExtraForms != null)
			lastSyncedExtraForms.setText(lastSyncDesc + " " + lastExtraFormsSyncDate);
	}
	
	public void displaySyncStats(){
		ContentProvider contentProvider = getContentResolver().acquireContentProviderClient(OpenHDS.AUTHORITY).getLocalContentProvider();
		OpenHDSProvider provider = ((OpenHDSProvider)contentProvider);
		Map<String, Integer> rowCount = provider.getRowCount(Uri.parse(OpenHDS.AUTHORITY));

		android.database.Cursor c = Queries.getAllSettings(getContentResolver());
		Settings settings = Converter.convertToSettings(c); 
		c.close();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton(getResourceString(this,R.string.ok_lbl), null);
		StringBuilder sb = new StringBuilder();
		sb.append("<b>"+ getResourceString(this,R.string.sync_stats_info) +"</b><br/><br/>");
		boolean incompleteEntries = false;
		for(String key: rowCount.keySet()){
			int serverCount = getServerCountForEntity(key, settings);
			int localCount = rowCount.get(key);
			double completeness = calculateEntityCompleteness(key, localCount, serverCount);
			sb.append(getHtmlForEntry(key.trim(), localCount, serverCount, completeness, settings));
			if(completeness != -1 && completeness != 1) incompleteEntries = true;
		}
		sb.append("<br/>Legend: <font color='" + completeColor + "'>--</font>= Complete, " + "<font color='" + incompleteColor + "'>--</font>= Incomplete<br/>");
		builder.setTitle(getResourceString(this,R.string.sync_stats_title));
		builder.setMessage(Html.fromHtml(sb.toString()));
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private String getHtmlForEntry(String key, Integer localCount, int serverCount, double completeness, Settings settings){		
		StringBuffer sb = new StringBuffer();
		if(serverCount != -1){
			String myDouble = String.format("%1$,.2f", completeness*100.0);
			String upperCaseEntity = key.substring(0, 1).toUpperCase() + key.substring(1);
			sb.append("<font color='" + (completeness==1?completeColor:incompleteColor) + "'><b>" + upperCaseEntity + "</b>: " + localCount + "/" + (completeness>=0?serverCount:"?") + " (" + (completeness>=0?myDouble:"?") + "%)</font><br/>");
		}
		return sb.toString();
	}
	
	private int getServerCountForEntity(String entity, Settings settings){
		int serverCount = -1;
		if(OpenHDS.Locations.TABLE_NAME.equalsIgnoreCase(entity)){
			serverCount = settings.getNumberOfLocations();
		}
		else if(OpenHDS.Individuals.TABLE_NAME.equalsIgnoreCase(entity)){
			serverCount = settings.getNumberOfIndividuals();
		}	
		else if(OpenHDS.Relationships.TABLE_NAME.equalsIgnoreCase(entity)){
			serverCount = settings.getNumberOfRelationships();
		}		
		else if(OpenHDS.SocialGroups.TABLE_NAME.equalsIgnoreCase(entity)){
			serverCount = settings.getNumberOfHouseHolds();
		}	
		else if(OpenHDS.Forms.TABLE_NAME.equalsIgnoreCase(entity)){
			serverCount = settings.getNumberOfExtraForms();
		}	
		else if(OpenHDS.FieldWorkers.TABLE_NAME.equalsIgnoreCase(entity)){
			serverCount = settings.getNumberOfFieldWorkers();
		}		
		else if(OpenHDS.Visits.TABLE_NAME.equalsIgnoreCase(entity)){}
		
		return serverCount;
	}
		
	private double calculateEntityCompleteness(String key, int localCount, int serverCount){
		double ratio = -1;
		if(serverCount > 0){
			ratio = (double)localCount/serverCount;
		}
		return ratio;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("displayLanguage")){
			recreate();
		}
	}
}

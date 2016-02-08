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
			showStats();
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
		lastDatabaseSyncDate = ((lastDatabaseSyncDate==null)||lastDatabaseSyncDate.isEmpty())?"n/a":lastDatabaseSyncDate;
		if(lastSyncedDatabase != null)
			lastSyncedDatabase.setText("Last synced on: " + lastDatabaseSyncDate);
		
		String lastFieldWorkerSyncDate = settings.getDateOfLastFieldWorkerSync();
		lastFieldWorkerSyncDate = ((lastFieldWorkerSyncDate==null)||lastFieldWorkerSyncDate.isEmpty())?"n/a":lastFieldWorkerSyncDate;
		if(lastSyncedFieldWorkers != null)
			lastSyncedFieldWorkers.setText("Last synced on: " + lastFieldWorkerSyncDate);
		
		String lastExtraFormsSyncDate = settings.getDateOfLastFormsSync();
		lastExtraFormsSyncDate = ((lastExtraFormsSyncDate==null)||lastExtraFormsSyncDate.isEmpty())?"n/a":lastExtraFormsSyncDate;
		if(lastSyncedExtraForms != null)
			lastSyncedExtraForms.setText("Last synced on: " + lastExtraFormsSyncDate);
	}
	
	public void showStats(){
		ContentProvider contentProvider = getContentResolver().acquireContentProviderClient(OpenHDS.AUTHORITY).getLocalContentProvider();
		OpenHDSProvider provider = ((OpenHDSProvider)contentProvider);
		Map<String, Integer> rowCount = provider.getRowCount(Uri.parse(OpenHDS.AUTHORITY));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton(getString(R.string.ok_lbl), null);
		StringBuilder sb = new StringBuilder();
		sb.append("<b>No. of synced items on this device:</b><br/><br/>");
		for(String key: rowCount.keySet()){
			sb.append(key + ": " + rowCount.get(key) + "<br/>");
		}
		builder.setTitle("Sync statistics");
		builder.setMessage(Html.fromHtml(sb.toString()));
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("displayLanguage")){
			recreate();
		}
	}
}

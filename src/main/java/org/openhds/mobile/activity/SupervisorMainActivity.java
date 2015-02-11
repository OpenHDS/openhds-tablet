package org.openhds.mobile.activity;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LayoutUtils.makeNewGenericButton;
import static org.openhds.mobile.utilities.UrlUtils.buildServerUrl;

import org.openhds.mobile.R;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.fragment.LoginPreferenceFragment;
import org.openhds.mobile.model.Settings;
import org.openhds.mobile.task.HttpTask.RequestContext;
import org.openhds.mobile.task.SyncEntitiesTask;
import org.openhds.mobile.task.SyncFieldworkersTask;
import org.openhds.mobile.task.SyncFormsTask;
import org.openhds.mobile.task.SyncSettingsTask;
import org.openhds.mobile.utilities.SyncDatabaseHelper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SupervisorMainActivity extends Activity implements OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

	private FrameLayout prefContainer;
	private LinearLayout supervisorOptionsList;
	private SyncDatabaseHelper syncDatabaseHelper;
	private TextView lastUpdateText;

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

		makeNewGenericButton(
				this,
				getResourceString(this, R.string.sync_field_worker_description),
				getResourceString(this, R.string.sync_field_worker_name),
				getResourceString(this, R.string.sync_field_worker_name), this,
				supervisorOptionsList);	
		
		makeNewGenericButton(
				this,
				getResourceString(this, R.string.download_extraform_button),
				getResourceString(this, R.string.sync_extraforms),
				getResourceString(this, R.string.sync_extraforms), this,
				supervisorOptionsList);				
		
		makeNewGenericButton(
				this,
				getResourceString(this, R.string.download_settings_button),
				getResourceString(this, R.string.sync_settings),
				getResourceString(this, R.string.sync_settings), this,
				supervisorOptionsList);	
		
        // add text view to display last settings sync time
        lastUpdateText = new TextView(this);
        supervisorOptionsList.addView(lastUpdateText);

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
		} else if (tag.equals(getResourceString(this,
				R.string.sync_settings))) {
			syncSettings();
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
	
	private void syncSettings(){		
		String username = (String) getIntent().getExtras().get(
				OpeningActivity.USERNAME_KEY);
		String password = (String) getIntent().getExtras().get(
				OpeningActivity.PASSWORD_KEY);

		String openHdsBaseUrl = getPreferenceString(this,
				R.string.openhds_server_url_key, "");
		SyncSettingsTask currentTask = new SyncSettingsTask(openHdsBaseUrl,
				username, password, syncDatabaseHelper.getProgressDialog(),
				this, syncDatabaseHelper);
		
		syncDatabaseHelper.setCurrentTask(currentTask);
		syncDatabaseHelper.startSync();
	}
	
	public void displayLastSyncDate(){
		android.database.Cursor c = Queries.getAllSettings(getContentResolver());
		Settings settings = Converter.convertToSettings(c); 
		c.close();
		
		String lastSyncDate = settings.getDateOfLastSync();
		lastSyncDate = ((lastSyncDate==null)||lastSyncDate.isEmpty())?"never":lastSyncDate;
		if(lastUpdateText != null)
			lastUpdateText.setText("Settings last updated on: " + lastSyncDate);
		
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("displayLanguage")){
			recreate();
		}
	}
}

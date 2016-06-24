package org.openhds.mobile.activity;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.LoginPreferenceFragment;
import org.openhds.mobile.fragment.SupervisorLoginFragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

public class OpeningActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	
	public static final String USERNAME_KEY = "usernameKey";
	public static final String PASSWORD_KEY = "passwordKey";

	private FrameLayout loginPrefContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.opening_activity);

		loginPrefContainer = (FrameLayout) findViewById(R.id.login_pref_container);

		if (null != savedInstanceState) {
			return;
		}

		getFragmentManager()
				.beginTransaction()
				.add(R.id.login_pref_container, new LoginPreferenceFragment())
				.add(R.id.field_worker_login_container,
						new FieldWorkerLoginFragment())
				.add(R.id.supervisor_login_container,
						new SupervisorLoginFragment()).commit();
		
		
		try {
			String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			CharSequence title = getTitle();
			setTitle(title.toString() + " (v" + version + ")");
		} catch (NameNotFoundException e) {
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.login_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean isShowingPreferences = View.VISIBLE == loginPrefContainer
				.getVisibility();
		if (isShowingPreferences) {
			loginPrefContainer.setVisibility(View.GONE);
		} else {
			loginPrefContainer.setVisibility(View.VISIBLE);
		}
		
		return true;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		getFragmentManager()
		.beginTransaction()
		.replace(R.id.login_pref_container, new LoginPreferenceFragment()).commit();
		
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		try {
			String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			CharSequence title = getString(R.string.app_name);
			setTitle(title.toString() + " (v" + version + ")");
		} catch (NameNotFoundException e) {
		}
		super.onResume();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("displayLanguage")){
			recreate();
		}
	}	

}

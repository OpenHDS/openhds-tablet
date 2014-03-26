package org.openhds.mobile.activity;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.LoginPreferenceFragment;
import org.openhds.mobile.utilities.SyncDatabaseHelper;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SupervisorMainActivity extends Activity implements OnClickListener {

	private FrameLayout prefContainer;
	private LinearLayout supervisorOptionsList;
	private SyncDatabaseHelper syncDatabaseHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.supervisor_main);

		prefContainer = (FrameLayout) findViewById(R.id.login_pref_container);

		supervisorOptionsList = (LinearLayout) findViewById(R.id.supervisor_activity_options);

		makeNewOptionsButton(
				getResourceString(this, R.string.sync_database_description),
				getResourceString(this, R.string.sync_database_name), this);

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

	private Button makeNewOptionsButton(String description, String buttonName,
			OnClickListener listener) {

		View v = getLayoutInflater().inflate(R.layout.generic_textview_button,
				null);
		supervisorOptionsList.addView(v);
		Button b = (Button) v.findViewById(R.id.generic_button);
		TextView t = (TextView) v.findViewById(R.id.generic_button_description);

		t.setText(description);
		b.setText(buttonName);
		b.setTag(buttonName);
		b.setOnClickListener(listener);

		return b;
	}

	public void onClick(View v) {
		if (v.getTag().equals(
				getResourceString(this, R.string.sync_database_name))) {
			syncDatabase();
		}

	}

	private void syncDatabase() {

		if (null == syncDatabaseHelper) {
			String username = (String) getIntent().getExtras().get(
					OpeningActivity.USERNAME_KEY);
			String password = (String) getIntent().getExtras().get(
					OpeningActivity.PASSWORD_KEY);
			syncDatabaseHelper = new SyncDatabaseHelper(username, password,
					this);
			
		}

		syncDatabaseHelper.startSync();
	}
}

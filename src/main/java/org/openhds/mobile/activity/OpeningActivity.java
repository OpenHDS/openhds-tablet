package org.openhds.mobile.activity;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.LoginPreferenceFragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class OpeningActivity extends Activity {

	LoginPreferenceFragment loginPrefFrag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.opening_activity);

		loginPrefFrag = new LoginPreferenceFragment();
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
		// Handle presses on the action bar items

		if (!loginPrefFrag.isAdded()) {
			addPreferenceFragment();
		} else {
			removePreferenceFragment();
		}

		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		removePreferenceFragment();

		super.onSaveInstanceState(savedInstanceState);
	}

	private void addPreferenceFragment() {

		if (!loginPrefFrag.isAdded()) {
			getFragmentManager().beginTransaction()
					.add(R.id.login_pref_container, loginPrefFrag, "logintag")
					.commit();
		}

	}

	private void removePreferenceFragment() {

		if (loginPrefFrag.isAdded()) {
			getFragmentManager().beginTransaction().remove(loginPrefFrag)
					.commit();
		}

	}

}
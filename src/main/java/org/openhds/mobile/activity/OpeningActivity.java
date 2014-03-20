package org.openhds.mobile.activity;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.LoginPreferenceFragment;
import org.openhds.mobile.fragment.SupervisorLoginFragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;

public class OpeningActivity extends Activity {

	LoginPreferenceFragment loginPrefFrag;
	FieldWorkerLoginFragment fieldWorkerFrag;
	SupervisorLoginFragment supervisorFrag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.opening_activity);

		loginPrefFrag = new LoginPreferenceFragment();

		if (savedInstanceState != null) {
			return;
		}

		fieldWorkerFrag = new FieldWorkerLoginFragment();
		getFragmentManager().beginTransaction()
				.add(R.id.field_worker_login_container, fieldWorkerFrag)
				.commit();

		supervisorFrag = new SupervisorLoginFragment();
		getFragmentManager().beginTransaction()
				.add(R.id.supervisor_login_container, supervisorFrag).commit();

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
			setPreferenceFragmentExpanded(true);
		}

	}

	private void removePreferenceFragment() {

		if (loginPrefFrag.isAdded()) {
			getFragmentManager().beginTransaction().remove(loginPrefFrag)
					.commit();
			setPreferenceFragmentExpanded(false);
		}

	}

	private void setPreferenceFragmentExpanded(boolean isExpanded) {

		int height = 0;
		int width = 0;
		float weight = isExpanded ? 1 : 0;

		// expand or collapse in portrait mode
		if (findViewById(R.id.opening_activity).getTag().equals("portrait")) {

			height = 0;
			width = LayoutParams.MATCH_PARENT;

			// expand or collapse in landscape mode
		} else if (findViewById(R.id.opening_activity).getTag().equals(
				"landscape")) {

			height = LayoutParams.MATCH_PARENT;
			width = 0;

		}

		FrameLayout loginPrefContainer = (FrameLayout) findViewById(R.id.login_pref_container);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width,
				height, weight);
		loginPrefContainer.setLayoutParams(lp);

	}

}

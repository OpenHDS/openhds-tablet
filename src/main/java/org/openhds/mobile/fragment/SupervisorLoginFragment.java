package org.openhds.mobile.fragment;

import java.net.MalformedURLException;
import java.net.URL;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.SupervisorMainActivity;
import org.openhds.mobile.database.DatabaseAdapter;
import org.openhds.mobile.model.Supervisor;
import org.openhds.mobile.task.AbstractHttpTask.RequestContext;
import org.openhds.mobile.task.AuthenticateTask;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SupervisorLoginFragment extends Fragment implements
		OnClickListener {

	EditText usernameEditText;
	EditText passwordEditText;
	Button loginButton;
	DatabaseAdapter databaseAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.generic_login_fragment, container,
				false);
		TextView title = (TextView) v.findViewById(R.id.titleTextView);
		title.setText(R.string.supervisor_login);

		usernameEditText = (EditText) v.findViewById(R.id.usernameEditText);
		passwordEditText = (EditText) v.findViewById(R.id.passwordEditText);
		loginButton = (Button) v.findViewById(R.id.loginButton);
		loginButton.setOnClickListener(this);
		databaseAdapter = new DatabaseAdapter(getActivity());

		return v;
	}

	public void onClick(View view) {
		authenticateSupervisor();
	}

	private String getUsernameFromEditText() {
		String username = usernameEditText.getText().toString();
		return username;
	}

	private String getPasswordFromEditText() {
		String password = passwordEditText.getText().toString();
		return password;
	}

	private URL getUrl() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		String openHdsUrl = preferences.getString(
				getString(R.string.openhds_server_url_key), "");

		// supervisor_login_url needs to be a secured resource on the sever
		// for example openhds/api/rest/socialgroups
		String endpointUrl = openHdsUrl
				+ getString(R.string.supervisor_login_url);

		try {
			return new URL(endpointUrl);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	private void authenticateSupervisor() {
		URL url = getUrl();
		if (null == url) {
			Toast.makeText(getActivity(),
					getString(R.string.openhds_server_url_key) + " is bad.",
					Toast.LENGTH_LONG).show();
			return;
		}

		RequestContext requestCtx = new RequestContext();
		requestCtx.url(url).user(getUsernameFromEditText())
				.password(getPasswordFromEditText());

		AuthenticateTask authenticateTask = new AuthenticateTask(requestCtx,
				new AuthenticateListener(), databaseAdapter);
		authenticateTask.execute();

	}

	private void onConnectedAndAuthenticated() {
		// valid credentials were cached in tablet database by AuthenticateTask
		launchSupervisorMainActivity();
	}

	private void onConnectedButNotAuthenticated() {
		// delete unauthorized user from tablet database
		// to prevent login when not connected to network
		Supervisor user = new Supervisor();
		user.setName(getUsernameFromEditText());
		user.setPassword(getUsernameFromEditText());
		int nDeleted = databaseAdapter.deleteSupervisor(user);
		Toast.makeText(getActivity(), "Deleted " + nDeleted + " supervisors.",
				Toast.LENGTH_LONG).show();
	}

	private void onNotConnected() {
		// attempt to log in using cached credentials in tablet database
	}

	private void launchSupervisorMainActivity() {
		Intent intent = new Intent(getActivity(), SupervisorMainActivity.class);
		String usernameKey = getString(R.string.supervisor_username_key);
		String passwordKey = getString(R.string.supervisor_password_key);
		intent.putExtra(usernameKey, getUsernameFromEditText());
		intent.putExtra(passwordKey, getPasswordFromEditText());
		startActivity(intent);
	}

	private class AuthenticateListener implements AuthenticateTask.TaskListener {
		public void onFailedAuthentication() {
			Toast.makeText(getActivity(),
					"Supervisor credentials not authenticated.",
					Toast.LENGTH_LONG).show();
			onConnectedButNotAuthenticated();
		}

		public void onConnectionError() {
			Toast.makeText(getActivity(),
					"Connection error trying to authenticate supervisor.",
					Toast.LENGTH_LONG).show();
			onNotConnected();
		}

		public void onConnectionTimeout() {
			Toast.makeText(getActivity(),
					"Connection timeout trying to authenticate supervisor.",
					Toast.LENGTH_LONG).show();
			onNotConnected();
		}

		public void onSuccess() {
			Toast.makeText(getActivity(), "Authenticated supervisor.",
					Toast.LENGTH_LONG).show();
			onConnectedAndAuthenticated();
		}

		public void onFailure() {
			Toast.makeText(getActivity(),
					"Unknown failure trying to authenticate supervisor.",
					Toast.LENGTH_LONG).show();
			onNotConnected();
		}

		public void onNoContent() {
			Toast.makeText(getActivity(),
					"No content trying to authenticate supervisor.",
					Toast.LENGTH_LONG).show();
			onNotConnected();
		}
	}
}


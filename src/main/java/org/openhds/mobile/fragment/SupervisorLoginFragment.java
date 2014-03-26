package org.openhds.mobile.fragment;

import java.net.MalformedURLException;
import java.net.URL;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.SupervisorMainActivity;
import org.openhds.mobile.database.DatabaseAdapter;
import org.openhds.mobile.model.Supervisor;
import org.openhds.mobile.task.HttpTask;
import org.openhds.mobile.task.SupervisorLoginTask;
import org.openhds.mobile.task.HttpTask.RequestContext;

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

		HttpTask<Void, Void> httpTask = new HttpTask<Void, Void>(requestCtx,
				new AuthenticateListener());
		httpTask.execute();

	}

	private void onConnectedAndAuthenticated() {
		// valid credentials were cached in tablet database by AuthenticateTask
		// delete any stale credentials from local then add authenticated
		// credentials to match server
		deleteSupervisor();
		addSupervisor();
		launchSupervisorMainActivity();
	}

	private void onConnectedButNotAuthenticated() {
		// delete unauthorized user from tablet database
		// to prevent login when not connected to network
		Toast.makeText(getActivity(),
				getString(R.string.supervisor_bad_credentials),
				Toast.LENGTH_LONG).show();
		deleteSupervisor();
	}

	private void deleteSupervisor() {
		Supervisor user = new Supervisor();
		user.setName(getUsernameFromEditText());
		databaseAdapter.deleteSupervisor(user);
	}

	private void addSupervisor() {
		Supervisor user = new Supervisor();
		user.setName(getUsernameFromEditText());
		user.setPassword(getPasswordFromEditText());
		databaseAdapter.addSupervisor(user);
	}

	private void onNotConnected() {
		// attempt to log in using cached credentials in tablet database
		SupervisorLoginTask loginTask = new SupervisorLoginTask(
				databaseAdapter, getUsernameFromEditText(),
				getPasswordFromEditText(), new LoginListener());
		loginTask.execute();
	}

	private void launchSupervisorMainActivity() {
		Intent intent = new Intent(getActivity(), SupervisorMainActivity.class);
		String usernameKey = getString(R.string.supervisor_username_key);
		String passwordKey = getString(R.string.supervisor_password_key);
		intent.putExtra(usernameKey, getUsernameFromEditText());
		intent.putExtra(passwordKey, getPasswordFromEditText());
		startActivity(intent);
	}

	private class AuthenticateListener implements HttpTask.TaskListener {
		public void onFailedAuthentication() {
			onConnectedButNotAuthenticated();
		}

		public void onConnectionError() {
			onNotConnected();
		}

		public void onConnectionTimeout() {
			onNotConnected();
		}

		public void onSuccess() {
			onConnectedAndAuthenticated();
		}

		public void onFailure() {
			onNotConnected();
		}

		public void onNoContent() {
			onNotConnected();
		}
	}

	private class LoginListener implements SupervisorLoginTask.Listener {
		public void onAuthenticated() {
			launchSupervisorMainActivity();
		}

		public void onBadAuthentication() {
			Toast.makeText(getActivity(),
					getString(R.string.supervisor_bad_credentials),
					Toast.LENGTH_LONG).show();
		}
	}
}


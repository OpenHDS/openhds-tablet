package org.openhds.mobile.fragment;

import java.net.MalformedURLException;
import java.net.URL;

import org.openhds.mobile.R;
import org.openhds.mobile.database.DatabaseAdapter;
import org.openhds.mobile.task.AbstractHttpTask.RequestContext;
import org.openhds.mobile.task.AuthenticateTask;

import android.app.Fragment;
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

	private class AuthenticateListener implements AuthenticateTask.TaskListener {
		public void onFailedAuthentication() {
			Toast.makeText(getActivity(),
					"Supervisor credentials not authenticated.",
					Toast.LENGTH_LONG).show();
		}

		public void onConnectionError() {
			Toast.makeText(getActivity(),
					"Connection error authenticating supervisor.",
					Toast.LENGTH_LONG).show();
		}

		public void onConnectionTimeout() {
			Toast.makeText(getActivity(),
					"Connection timeout authenticating supervisor.",
					Toast.LENGTH_LONG).show();
		}

		public void onSuccess() {
			Toast.makeText(getActivity(), "Authenticated supervisor.",
					Toast.LENGTH_LONG).show();
		}

		public void onFailure() {
			Toast.makeText(getActivity(),
					"Unknown failure authenticating supervisor.",
					Toast.LENGTH_LONG).show();
		}

		public void onNoContent() {
			Toast.makeText(getActivity(),
					"No content authenticating supervisor.", Toast.LENGTH_LONG)
					.show();
		}
	}

	public void onClick(View view) {

		Toast.makeText(getActivity(), "WORKS.", Toast.LENGTH_LONG).show();
		authenticateSupervisor();
	}
}
package org.openhds.mobile.fragment;

import static org.openhds.mobile.utilities.MessageUtils.showLongToast;
import static org.openhds.mobile.utilities.UrlUtils.buildServerUrl;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;

import java.net.URL;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.OpeningActivity;
import org.openhds.mobile.activity.SupervisorMainActivity;
import org.openhds.mobile.database.DatabaseAdapter;
import org.openhds.mobile.model.Supervisor;
import org.openhds.mobile.task.HttpTask;
import org.openhds.mobile.task.HttpTask.RequestContext;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SupervisorLoginFragment extends Fragment implements
		OnClickListener {

	private EditText usernameEditText;
	private EditText passwordEditText;
	private Button loginButton;
	private DatabaseAdapter databaseAdapter;

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
		loginButton.setEnabled(false);
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
		// supervisor_login_url needs to be a secured resource on the sever
		// for example openhds/api/rest/socialgroups
		String path = ""; //getResourceString(getActivity(), R.string.supervisor_login_url);
		return buildServerUrl(getActivity(), path);
	}

	private void authenticateSupervisor() {
		URL url = getUrl();
		if (null == url) {
			String urlName = getResourceString(getActivity(),
					R.string.openhds_server_url_key);
			showLongToast(getActivity(), urlName + " is bad.");
			return;
		}

		RequestContext requestCtx = new RequestContext();
		requestCtx.url(url).user(getUsernameFromEditText())
				.password(getPasswordFromEditText());

		HttpTask<Void, Void> httpTask = new HttpTask<Void, Void>(getActivity().getApplicationContext(), requestCtx,
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
		showLongToast(getActivity(), R.string.supervisor_bad_credentials);
		deleteSupervisor();
		loginButton.setEnabled(true);	
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
//		SupervisorLoginTask loginTask = new SupervisorLoginTask(
//				databaseAdapter, getUsernameFromEditText(),
//				getPasswordFromEditText(), new LoginListener());
//		loginTask.execute();
		
		loginButton.setEnabled(true);	
		showLongToast(getActivity(), R.string.supervisor_bad_credentials);
	}
	
	private void onNetworkConnection(){
		loginButton.setEnabled(true);	
		showLongToast(getActivity(), R.string.supervisor_connection_error);
	}

	private void launchSupervisorMainActivity() {
		Intent intent = new Intent(getActivity(), SupervisorMainActivity.class);
		intent.putExtra(OpeningActivity.USERNAME_KEY, getUsernameFromEditText());
		intent.putExtra(OpeningActivity.PASSWORD_KEY, getPasswordFromEditText());
//		usernameEditText.setText("");
		passwordEditText.setText("");
		loginButton.setEnabled(true);		
		startActivity(intent);
	}

	private class AuthenticateListener implements HttpTask.TaskListener {
		public void onFailedAuthentication() {
			onConnectedButNotAuthenticated();
		}

		public void onConnectionError() {
			//onNotConnected();
			onNetworkConnection();
		}

		public void onConnectionTimeout() {
			//onNotConnected();
			onNetworkConnection();
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
}


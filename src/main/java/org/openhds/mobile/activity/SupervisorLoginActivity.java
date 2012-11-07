package org.openhds.mobile.activity;

import org.openhds.mobile.R;
import org.openhds.mobile.database.DatabaseAdapter;
import org.openhds.mobile.task.AbstractHttpTask.RequestContext;
import org.openhds.mobile.task.AuthenticateTask;
import org.openhds.mobile.task.SupervisorLoginTask;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SupervisorLoginActivity extends AbstractActivity {

	private TextView userTxt;
	private TextView passTxt;
	private DatabaseAdapter store;

	private static final int PROGRESS_DIALOG = 0;
	private static final int DIALOG_BAD_AUTH = 1;
	private static final int DIALOG_NEW_USER = 2;
	private static final int DIALOG_CREATED_USER = 4;
	private static final int DIALOG_CONNECTION_ERROR = 8;
	private static final int DIALOG_ERROR_USER = 16;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.supervisor_login);

		//getApplicationContext().deleteDatabase("form_submission");

		Button loginBtn = (Button) findViewById(R.id.login_btn);
		userTxt = (TextView) findViewById(R.id.user_txt);
		passTxt = (TextView) findViewById(R.id.password_txt);
		loginBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (!isValidated()) {
					return;
				}
				showProgressDialog();
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_BAD_AUTH:
			dialog = buildFailedDialog("Failed to authenticate username and password");
			break;
		case PROGRESS_DIALOG:
			dialog = buildProgressDialog();
			break;
		case DIALOG_CREATED_USER:
			dialog = buildSuccessDialog("Successfully created new user");
			break;
		case DIALOG_NEW_USER:
			dialog = buildNewUserDialog();
			break;
		case DIALOG_CONNECTION_ERROR:
			dialog = buildFailedDialog("There was a problem communicating with the server");
			break;
		case DIALOG_ERROR_USER:
			dialog = buildFailedDialog("There was an error creating the user");
			break;
		default:
			dialog = null;
		}

		return dialog;
	}

	private Dialog buildNewUserDialog() {
		return buildGenericOkDialog("User not recognized",
				"Do you want to authenticate with the server?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								showDialog(PROGRESS_DIALOG);
								executeAuthenticateTask();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
	}

	private void executeAuthenticateTask() {
		RequestContext requestCtx = new RequestContext();
		requestCtx.url(getServerUrl("/api/user/authenticate"))
				.user(getUsernameValue())
				.password(getPasswordValue());
		AuthenticateTask authenticateTask = new AuthenticateTask(requestCtx,
				new AuthenticateListener(), getPersistentStore());
		authenticateTask.execute();
	}

	private DatabaseAdapter getPersistentStore() {
		if (store == null) {
			store = new DatabaseAdapter(this);
		}

		return store;
	}

	private Dialog buildFailedDialog(String message) {
		return buildGenericOkDialog("Error", message).setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
	}

	private AlertDialog.Builder buildGenericOkDialog(String title,
			String message) {
		return new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert).setTitle(title)
				.setMessage(message);
	}

	private Dialog buildSuccessDialog(String message) {
		return buildGenericOkDialog("Success", message).setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						startMainFormActivity();
					}
				}).create();
	}

	private Dialog buildProgressDialog() {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setCancelable(true);
		dialog.setTitle("Please Wait");
		dialog.setMessage("Trying to authenticate...");
		dialog.setIndeterminate(true);

		return dialog;
	}

	protected void showProgressDialog() {
		showDialog(PROGRESS_DIALOG);
		DatabaseAdapter ps = new DatabaseAdapter(this);
		SupervisorLoginTask loginTask = new SupervisorLoginTask(ps, getUsernameValue(),
				getPasswordValue(), new LoginListener());
		loginTask.execute(false);
	}

	private void startMainFormActivity() {
		Intent intent = new Intent(getApplicationContext(), SupervisorMainActivity.class);
		intent.putExtra(USERNAME_PARAM, getUsernameValue());
		intent.putExtra(PASSWORD_PARAM, getPasswordValue());
		passTxt.setText("");
		startActivity(intent);
	}

	private String getUsernameValue() {
		return userTxt.getText().toString();
	}

	private String getPasswordValue() {
		return passTxt.getText().toString();
	}

	private boolean isValidated() {
		String user = getUsernameValue();
		if ("".equals(user.trim())) {
			Toast.makeText(getBaseContext(), "Please enter in a user",
					Toast.LENGTH_LONG).show();
			return false;
		}

		String password = getPasswordValue();
		if ("".equals(password.trim())) {
			Toast.makeText(getBaseContext(), "Please enter in a password",
					Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}

	private class LoginListener implements SupervisorLoginTask.Listener {
		public void onNewUser() {
			removeAndShow(DIALOG_NEW_USER);
		}

		public void onAuthenticated() {
			removeDialog(PROGRESS_DIALOG);
			startMainFormActivity();
		}

		public void onBadAuthentication() {
			removeAndShow(DIALOG_BAD_AUTH);
		}

		public void onCreatedUser() {
			removeAndShow(DIALOG_CREATED_USER);
		}
	}

	private class AuthenticateListener implements AuthenticateTask.TaskListener {

		public void onFailedAuthentication() {
			removeAndShow(DIALOG_BAD_AUTH);
		}

		public void onConnectionError() {
			removeAndShow(DIALOG_CONNECTION_ERROR);
		}

		public void onConnectionTimeout() {
			removeAndShow(DIALOG_CONNECTION_ERROR);
		}

		public void onSuccess() {
			removeAndShow(DIALOG_CREATED_USER);
		}

		public void onFailure() {
			removeAndShow(DIALOG_ERROR_USER);
		}

		public void onNoContent() {
			
		}
	}

	private void removeAndShow(int toShow) {
		removeDialog(PROGRESS_DIALOG);
		showDialog(toShow);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Server Preferences");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		startActivity(new Intent(getApplicationContext(),
				ServerPreferencesActivity.class));
		return true;
	}
}

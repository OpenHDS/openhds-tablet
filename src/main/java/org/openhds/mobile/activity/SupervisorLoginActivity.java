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
			dialog = buildFailedDialog(getString(R.string.sprvlogin_bad_auth));
			break;
		case PROGRESS_DIALOG:
			dialog = buildProgressDialog();
			break;
		case DIALOG_CREATED_USER:
			dialog = buildSuccessDialog(getString(R.string.sprvlogin_created_user));
			break;
		case DIALOG_NEW_USER:
			dialog = buildNewUserDialog();
			break;
		case DIALOG_CONNECTION_ERROR:
			dialog = buildFailedDialog(getString(R.string.sprvlogin_connection_error));
			break;
		case DIALOG_ERROR_USER:
			dialog = buildFailedDialog(getString(R.string.sprvlogin_error_user));
			break;
		default:
			dialog = null;
		}

		return dialog;
	}

	private Dialog buildNewUserDialog() {
		return buildGenericOkDialog(getString(R.string.sprvlogin_user_notrecon),
				getString(R.string.sprvlogin_authenticate_with_server))
				.setPositiveButton(getString(R.string.yes_lbl),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								showDialog(PROGRESS_DIALOG);
								executeAuthenticateTask();
							}
						})
				.setNegativeButton(getString(R.string.no_lbl), new DialogInterface.OnClickListener() {
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
		return buildGenericOkDialog(getString(R.string.error_lbl), message).setPositiveButton("Ok",
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
		return buildGenericOkDialog(getString(R.string.success_lbl), message).setPositiveButton("Ok",
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
		dialog.setTitle(getString(R.string.please_wait_lbl));
		dialog.setMessage(getString(R.string.trying_to_authenticate_lbl));
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
			Toast.makeText(getBaseContext(), getString(R.string.sprvlogin_enter_in_user),
					Toast.LENGTH_LONG).show();
			return false;
		}

		String password = getPasswordValue();
		if ("".equals(password.trim())) {
			Toast.makeText(getBaseContext(), getString(R.string.sprvlogin_enter_in_pass),
					Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}

	private class LoginListener implements SupervisorLoginTask.Listener {
		public void onNewUser() {
			removeAndShow(DIALOG_NEW_USER);
		}

		@SuppressWarnings("deprecation")
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
		menu.add(getString(R.string.server_preferences_lbl));
		return true;
	}

}

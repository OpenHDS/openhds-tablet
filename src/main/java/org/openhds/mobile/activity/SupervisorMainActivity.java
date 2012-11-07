package org.openhds.mobile.activity;

import java.net.URL;

import org.openhds.mobile.R;
import org.openhds.mobile.task.AbstractHttpTask.RequestContext;
import org.openhds.mobile.task.DownloadFormsTask;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SupervisorMainActivity extends AbstractActivity {
	
	private Dialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.supervisor_main);

		Button downloadBtn = (Button) findViewById(R.id.download_btn);
		downloadBtn.setOnClickListener(new DownloadButtonListener());

		Button viewFormBtn = (Button) findViewById(R.id.view_form_btn);
		viewFormBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(),
						FormListActivity.class);
				setUsernameOnIntent(intent);
				startActivity(intent);
			}
		});

		Button logoutBtn = (Button) findViewById(R.id.logout_btn);
		logoutBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	private class DownloadButtonListener implements OnClickListener {
		public void onClick(View arg0) {
			dialog = ProgressDialog.show(SupervisorMainActivity.this, "Working", "Downloading all forms...");
			URL parsedUrl = getServerUrl("/api/form/download");
			if (parsedUrl == null) {
				return;
			}

			RequestContext requestCtx = new RequestContext();
			requestCtx.url(parsedUrl).user(getUsernameFromIntent())
					.password(getPasswordFromIntent());

			DownloadFormsTask task = new DownloadFormsTask(requestCtx,
					new DownloadFormsTask.TaskListener() {
						public void onFailedAuthentication() {
							dialog.dismiss();
							showToastWithText("Bad username and/or password");
						}

						public void onFailure() {
							dialog.dismiss();
							showToastWithText("There was a problem reading response from server");
						}

						public void onConnectionError() {
							dialog.dismiss();
							showToastWithText("There was a error with the network connection");
						}

						public void onConnectionTimeout() {
							dialog.dismiss();
							showToastWithText("Connection to the server timed out");
						}

						public void onSuccess() {
							dialog.dismiss();
							showToastWithText("Download all forms successfully");
						}

						public void onNoContent() {
							dialog.dismiss();
							showToastWithText("No forms to download");							
						}
					}, getBaseContext());
			task.execute();
		}
	}

}

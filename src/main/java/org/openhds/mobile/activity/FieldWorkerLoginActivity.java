package org.openhds.mobile.activity;

import org.openhds.mobile.Converter;
import org.openhds.mobile.Queries;
import org.openhds.mobile.R;
import org.openhds.mobile.activity.FieldWorkerLoginActivity.AuthenticateFieldWorker.Listener;
import org.openhds.mobile.listener.RetrieveFieldWorkersListener;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.Result;
import org.openhds.mobile.task.FieldWorkerLoginTask;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class FieldWorkerLoginActivity extends Activity implements OnClickListener, RetrieveFieldWorkersListener {
	
    private static final int LOGIN_ACTIVITY = 1;

	private TextView extIdText;
	private TextView passwordText;
	private Button loginButton;
	private CheckBox registerChkBox;
	
	private ProgressDialog dialog;
	
    private FieldWorkerLoginTask loginTask = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.fieldworker_login);
	    
	    initializeProgressDialog();
	    
        extIdText = (TextView) findViewById(R.id.extIdText);
        passwordText = (TextView) findViewById(R.id.passwordText);
        
	    loginButton = (Button) findViewById(R.id.loginBtn);
	    loginButton.setOnClickListener(this);
	    
	    registerChkBox = (CheckBox) findViewById(R.id.registerChkBox);
	    registerChkBox.setOnClickListener(this);
	    
	    ActionBar actionBar = getActionBar();
	    actionBar.show();
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {	
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.loginmenu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.configure_server:
                Intent i = new Intent(this, ServerPreferencesActivity.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
		
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.registerChkBox: 
			if (registerChkBox.isChecked()) 
				loginButton.setText("Register");
			else 
				loginButton.setText("Login");
			break;
		case R.id.loginBtn: 
			
			String extId = extIdText.getText().toString();
			String password = passwordText.getText().toString();
			
			if (registerChkBox.isChecked()) {
				dialog.show();
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				if (loginTask == null)
					loginTask = new FieldWorkerLoginTask(this, settings, this, dialog, extId, password, true);
				
	    		if (loginTask.getStatus() == Status.PENDING) {
                    loginTask.execute();
                }
			}
			else {
                dialog = ProgressDialog.show(this, "Authenticating...", "Please Wait");
                new AuthenticateFieldWorker(getContentResolver(), extId, password, new Listener() {

                    public void onAuthenticated(FieldWorker fw) {
                        dialog.dismiss();
                        if (fw == null) {
                            Toast.makeText(getApplicationContext(), getString(R.string.bad_authentication),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            startUpdateActivity(fw);
                        }
                    }

                }).execute();
			}
			break;
		}
	}
	
	public static class AuthenticateFieldWorker extends android.os.AsyncTask<Void, Void, org.openhds.mobile.model.FieldWorker> {

	    private ContentResolver resolver;
        private String extId;
        private String password;
        private Listener listener;
	    
	    interface Listener {
	        void onAuthenticated(FieldWorker fw);
	    }

        public AuthenticateFieldWorker(ContentResolver resolver, String extId, String password, Listener listener) {
	        this.resolver = resolver;
	        this.extId = extId;
	        this.password = password;
	        this.listener = listener;
	    }
	    
        @Override
        protected FieldWorker doInBackground(Void... arg0) {
            if (Queries.hasFieldWorker(resolver, extId, password)) {
                Cursor cursor = Queries.getFieldWorkByExtId(resolver, extId);
                return Converter.toFieldWorker(cursor);
            } else {
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(FieldWorker result) {
            listener.onAuthenticated(result);
        }
	    
	}

    private void startUpdateActivity(FieldWorker fieldWorker) {
		Intent intent = new Intent(getApplicationContext(), UpdateActivity.class);
        intent.putExtra("fieldWorker", fieldWorker);
        passwordText.setText("");
        startActivity(intent);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (loginTask != null)
			loginTask.cancel(true);
	}
		
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    	initializeProgressDialog();

        if (resultCode == RESULT_CANCELED) {
            // request was canceled, so do nothing
            return;
        }

        switch (requestCode) {
            case LOGIN_ACTIVITY:
            	break;
        }
    }
	
	private void initializeProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("Working...");
        dialog.setMessage("Do not interrupt");
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new MyOnCancelListener());
	}

	public void retrieveFieldWorkersComplete(Result result) {
		switch (result) {
			case CREATED_FIELDWORKER_SUCCESS:
				Toast.makeText(getApplicationContext(),	getString(R.string.field_worker_created), Toast.LENGTH_SHORT).show();
				break;
			case BAD_AUTHENTICATION:
				Toast.makeText(getApplicationContext(),	getString(R.string.bad_authentication), Toast.LENGTH_SHORT).show();
				break;
			case BAD_XML:
				Toast.makeText(getApplicationContext(),	getString(R.string.bad_xml), Toast.LENGTH_SHORT).show();
				break;
			case FIELDWORKER_ALREADY_EXISTS:
				Toast.makeText(getApplicationContext(),	getString(R.string.field_worker_already_exists), Toast.LENGTH_SHORT).show();
				break;
			case UNABLE_TO_CONNECT:
				Toast.makeText(getApplicationContext(),	getString(R.string.unable_to_connect), Toast.LENGTH_SHORT).show();
		}
		dialog.dismiss();
		loginTask = null;
	}
	
	private class MyOnCancelListener implements OnCancelListener {
		public void onCancel(DialogInterface dialog) {
			if (loginTask != null)
				loginTask.cancel(true);
			finish();
			Toast.makeText(getApplicationContext(),	getString(R.string.retrieving_fieldworkers_interrupted), Toast.LENGTH_SHORT).show();
		}	
	}
}

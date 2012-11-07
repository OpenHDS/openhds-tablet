package org.openhds.mobile.activity;

import org.openhds.mobile.R;
import org.openhds.mobile.listener.CollectEntitiesListener;
import org.openhds.mobile.task.SyncEntitiesTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SyncDatabaseActivity extends Activity implements CollectEntitiesListener {
	
	final Context context = this;
    public static final int ENTITY_ACTIVITY = 1;
	
	private ProgressDialog dialog;
	private SharedPreferences settings;
    private SyncEntitiesTask entitiesTask = null;
    private PowerManager.WakeLock wl;
    
    private String url;
    private String username;
    private String password;
		
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setTitle(getString(R.string.app_name) + " > " + getString(R.string.syncDatabase));
		 //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		 setContentView(R.layout.sync_database);
		 
		 PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		 wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
	   	 
	     initializeProgressDialog();
	     
		 settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	     url = settings.getString(ServerPreferencesActivity.OPENHDS_KEY_SERVER, getString(R.string.default_openhdsserver));
	     username = settings.getString(ServerPreferencesActivity.OPENHDS_KEY_USERNAME, getString(R.string.username));
	     password = settings.getString(ServerPreferencesActivity.OPENHDS_KEY_PASSWORD, getString(R.string.password));
	     
	     Button syncButton = (Button) findViewById(R.id.syncButton);
	     syncButton.setOnClickListener(new OnClickListener() {
	    	 public void onClick(View v) {  
	    		 startSync();
	    	 }
	     });

	}
	
	private void initializeProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new MyOnCancelListener());
	}
	
	private void initializeAlertDialog(){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to stop sync?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface d, int id) {
		        	   entitiesTask.cancel(true); 
		        	   finish();
		   			Toast.makeText(getApplicationContext(),	getString(R.string.sync_interrupted), Toast.LENGTH_SHORT).show();
		   			
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface d, int id) {
		                //dialog.cancel();
		        	Toast.makeText(getApplicationContext(),	getString(R.string.sync_restarting), Toast.LENGTH_SHORT).show();
			   		startSync();
		        	dialog.show();
		        	//entitiesTask.execute();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void startSync() {

		if (entitiesTask == null) {
			dialog.setTitle("Working...");
			dialog.setMessage("Do not interrupt");
			dialog.show();
			
			entitiesTask = new SyncEntitiesTask(url, username, password,
					dialog, getBaseContext(), SyncDatabaseActivity.this);
		}
		if (entitiesTask.getStatus() == Status.PENDING)
			entitiesTask.execute();

	}
	
	@Override
	protected void onPause() {
		super.onPause();
		wl.release();
		if (entitiesTask != null)
			entitiesTask.cancel(true);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		wl.acquire();
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
            case ENTITY_ACTIVITY:
            	break;
        }
    }

	public void collectionComplete(Boolean result) {
		if (result) 
			Toast.makeText(getApplicationContext(),	getString(R.string.sync_entities_successful), Toast.LENGTH_SHORT).show();
		else 
			Toast.makeText(getApplicationContext(), getString(R.string.sync_entities_failure), Toast.LENGTH_SHORT).show();
		entitiesTask = null;
		dialog.dismiss();
	}
		
	private class MyOnCancelListener implements OnCancelListener {
		public void onCancel(DialogInterface dialog) {
			if (entitiesTask != null){
				dialog.dismiss();
				initializeAlertDialog();
			}
		}	
	}
}

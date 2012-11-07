package org.openhds.mobile.activity;

import org.openhds.mobile.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class OpeningActivity extends Activity implements OnClickListener {
	
	private Button fieldWorkerLoginBtn;
	private Button supervisorLoginBtn;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.opening_screen);
		    
		fieldWorkerLoginBtn = (Button) findViewById(R.id.fieldWorkerLoginBtn);
		fieldWorkerLoginBtn.setOnClickListener(this);
		    
		supervisorLoginBtn = (Button) findViewById(R.id.supervisorLoginBtn);
		supervisorLoginBtn.setOnClickListener(this);   
	}

	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.fieldWorkerLoginBtn: 
				Intent fwIntent = new Intent(getApplicationContext(), FieldWorkerLoginActivity.class);
				startActivity(fwIntent);
				break;
			case R.id.supervisorLoginBtn:
				Intent supervisorIntent = new Intent(getApplicationContext(), SupervisorLoginActivity.class);
				startActivity(supervisorIntent);
				break;
		}
	}
}

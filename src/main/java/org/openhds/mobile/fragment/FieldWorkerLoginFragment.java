package org.openhds.mobile.fragment;

import static org.openhds.mobile.utilities.MessageUtils.showLongToast;

import org.mindrot.jbcrypt.BCrypt;
import org.openhds.mobile.R;
import org.openhds.mobile.activity.BaselineActivity;
import org.openhds.mobile.activity.UpdateActivity;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment.AuthenticateFieldWorker.AuthenticateFieldWorkerListener;
import org.openhds.mobile.model.FieldWorker;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class FieldWorkerLoginFragment extends Fragment implements
		OnClickListener {

	private EditText usernameEditText;
	private EditText passwordEditText;
	private CheckBox baseLineCheckbox;
	private Button loginButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.generic_login_fragment, container,
				false);
		TextView title = (TextView) v.findViewById(R.id.titleTextView);
		title.setText(R.string.fieldworker_login);

		usernameEditText = (EditText) v.findViewById(R.id.usernameEditText);
		
		//DEBUG
//		usernameEditText.setText("FWEB1");
		passwordEditText = (EditText) v.findViewById(R.id.passwordEditText);
		loginButton = (Button) v.findViewById(R.id.loginButton);
		loginButton.setOnClickListener(this);
		
		baseLineCheckbox = new CheckBox(v.getContext());
		baseLineCheckbox.setText("Baseline");
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		//params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.BELOW, loginButton.getId());
		//params.leftMargin = 107;
		RelativeLayout child = (RelativeLayout)v.findViewById(R.id.generic_login_fragment); 
		child.addView(baseLineCheckbox, params);
		
		return v;
	}

	public void onClick(View view) {
		loginButton.setEnabled(false);
		authenticateFieldWorker();
	}

	private String getUsernameFromEditText() {
		String username = usernameEditText.getText().toString();
		return username;
	}

	private String getPasswordFromEditText() {
		String password = passwordEditText.getText().toString();
		return password;
	}
	
	private boolean getBaselineEnabled() {
		boolean isChecked = baseLineCheckbox.isChecked();
		return isChecked;
	}	

	private void authenticateFieldWorker() {
		
		// current implementation does not require password
		String password = getPasswordFromEditText();
		String extId = getUsernameFromEditText();
		
		ContentResolver cr = this.getActivity().getContentResolver();
		final Context ctx = this.getActivity().getApplicationContext();
		new AuthenticateFieldWorker(cr, extId, password, new AuthenticateFieldWorkerListener() {

            public void onAuthenticated(FieldWorker fw) {
            	loginButton.setEnabled(true);
                if (fw == null) {
                	showLongToast(ctx, getString(R.string.field_worker_bad_credentials));
                } else {
                	if(getBaselineEnabled())
                		launchCensusActivity(fw);
                	else
                		launchUpdateActivity(fw);
                }
            }

        }).execute();

//		boolean fieldWorkerFound = Queries.hasFieldWorker(getActivity().getContentResolver(),
//				extId, password);
//		if (fieldWorkerFound && getBaselineEnabled()) {
//			launchCensusActivity();
//		}
//		else if(fieldWorkerFound){
//			FieldWorker fw = new FieldWorker();
//			fw.setExtId(extId);
//			fw.setFirstName("TEST");
//			fw.setLastName("USER");
//			fw.setPassword("");
//			launchUpdateActivity(fw);			
//		}
//		else{
//			showLongToast(getActivity(), R.string.field_worker_bad_credentials);
//		}
	}

	private void launchCensusActivity(FieldWorker fieldWorker) {
		//TODO pass in a fieldworker object
//		Intent intent = new Intent(getActivity(), CensusActivity.class);
//		startActivity(intent);
		Intent intent = new Intent(getActivity(), BaselineActivity.class);
		intent.putExtra("fieldWorker", fieldWorker);
//		usernameEditText.setText("");
		passwordEditText.setText("");
		startActivity(intent);		
	}
	
	private void launchUpdateActivity(FieldWorker fieldWorker) {
		//TODO pass in a fieldworker object
		Intent intent = new Intent(getActivity(), UpdateActivity.class);
		intent.putExtra("fieldWorker", fieldWorker);
//		usernameEditText.setText("");
		passwordEditText.setText("");		
		startActivity(intent);
	}	
	
	public static class AuthenticateFieldWorker
			extends
			android.os.AsyncTask<Void, Void, org.openhds.mobile.model.FieldWorker> {

		private ContentResolver resolver;
		private String extId;
		private String password;
		private AuthenticateFieldWorkerListener listener;

		interface AuthenticateFieldWorkerListener {
			void onAuthenticated(FieldWorker fw);
		}

		public AuthenticateFieldWorker(ContentResolver resolver, String extId,
				String password, AuthenticateFieldWorkerListener listener) {
			this.resolver = resolver;
			this.extId = extId.trim();
			this.password = password.trim();
			this.listener = listener;
		}

		@Override
		protected FieldWorker doInBackground(Void... arg0) {
			if (Queries.hasFieldWorker(resolver, extId)) {
				Cursor cursor = Queries.getFieldWorkByExtId(resolver, extId);
				return Converter.toFieldWorker(cursor);
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(FieldWorker result) {
			// BCrypt hash authentication
			if(result != null && validHash(result.getPasswordHash())){
				if(BCrypt.checkpw(password,result.getPasswordHash())){
					listener.onAuthenticated(result);
				}
				else{
					listener.onAuthenticated(null);
				}
			}
			else{
				listener.onAuthenticated(null);
			}
		}
		
		//*Check if pw-hash is valid bcrypt */
		private boolean validHash(String hash){
			return hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2x$") || hash.startsWith("$2y$"));
		}
	}

}

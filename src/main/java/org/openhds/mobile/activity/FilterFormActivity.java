package org.openhds.mobile.activity;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.SelectionFormFragment;
import org.openhds.mobile.fragment.ValueFormFragment;
import org.openhds.mobile.fragment.ValueFormFragment.ValueListener;
import org.openhds.mobile.model.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * This activity is only used in searching for a Form. 
 */
public class FilterFormActivity extends Activity implements ValueListener, SelectionFormFragment.Listener {

    private SelectionFormFragment selectionFormFragment;
    private ValueFormFragment valueFragment;
    private LocationVisit locationVisit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filterforms);

        selectionFormFragment = (SelectionFormFragment) getFragmentManager().findFragmentById(
                R.id.selectionFormFragment);
        valueFragment = (ValueFormFragment) getFragmentManager().findFragmentById(R.id.valueFormFragment);

        processExtras();
    }

    private void processExtras() {
       locationVisit = (LocationVisit) getIntent().getExtras().getSerializable("location");
        
       // selectionFormFragment.setForm(form.getName());
    }



    

    public void onFormSelected(Form form) {
    	selectionFormFragment.updateFormText(form.getName());
        Intent i = new Intent();
        i.putExtra("form", form);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    
   

    public void onSearch(String form) {
        valueFragment.loadFilteredForms(form, locationVisit);
    }





	
}

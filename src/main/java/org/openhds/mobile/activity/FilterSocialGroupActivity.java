package org.openhds.mobile.activity;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.SelectionFilterSocialgroupFragment;
import org.openhds.mobile.fragment.ValueFragment;
import org.openhds.mobile.fragment.ValueFragment.ValueListener;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.Round;
import org.openhds.mobile.model.SocialGroup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class FilterSocialGroupActivity extends Activity implements ValueListener, SelectionFilterSocialgroupFragment.Listener {

    private SelectionFilterSocialgroupFragment selectionFilterSocialgroupFragment;
    private ValueFragment valueFragment;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_socialgroups);

        setSelectionFilterSocialgroupFragment((SelectionFilterSocialgroupFragment) getFragmentManager().findFragmentById(
                R.id.selectionFilterSocialgroupFragment));
        valueFragment = (ValueFragment) getFragmentManager().findFragmentById(R.id.valueFragment);

        processExtras();
    }
    
    private void processExtras() {       
//        Location location = (Location) getIntent().getExtras().getSerializable("location");
    }    
    
    public void onIndividualSelected(Individual individual) {
    	// not implemented
    }
    
    public void onSocialGroupSelected(SocialGroup socialGroup) {
        Intent i = new Intent();
        i.putExtra("socialGroup", socialGroup);
        setResult(Activity.RESULT_OK, i);
        finish();
    }    

    public void onHierarchy1Selected(LocationHierarchy hierarchy) {
    	// not implemented
    }

    public void onHierarchy2Selected(LocationHierarchy hierarchy) {
    	// not implemented
    }
    
    public void onHierarchy3Selected(LocationHierarchy hierarchy) {
    	// not implemented
    }

    public void onHierarchy4Selected(LocationHierarchy hierarchy) {
    	// not implemented
    }

    public void onRoundSelected(Round round) {
        // not implemented
    }

    public void onLocationSelected(Location location) {
    	// not implemented
    }    
    
    public void onSearch(String extId, String groupName) {
    	valueFragment.loadFilteredSocialGroups(extId, groupName);
    }

	
	public void onHierarchy5Selected(LocationHierarchy hierarchy5) {
		// TODO Auto-generated method stub
		
	}

	
	public void onHierarchy6Selected(LocationHierarchy hierarchy6) {
		// TODO Auto-generated method stub
		
	}

	
	public void onHierarchy7Selected(LocationHierarchy hierarchy7) {
		// TODO Auto-generated method stub
		
	}

	
	public void onHierarchy8Selected(LocationHierarchy hierarchy8) {
		// TODO Auto-generated method stub
		
	}

	public SelectionFilterSocialgroupFragment getSelectionFilterSocialgroupFragment() {
		return selectionFilterSocialgroupFragment;
	}

	public void setSelectionFilterSocialgroupFragment(
			SelectionFilterSocialgroupFragment selectionFilterSocialgroupFragment) {
		this.selectionFilterSocialgroupFragment = selectionFilterSocialgroupFragment;
	}   
}

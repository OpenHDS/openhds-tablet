package org.openhds.mobile.activity;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.SelectionFilterLocFragment;
import org.openhds.mobile.fragment.ValueLocFragment.ValueListener;
import org.openhds.mobile.fragment.ValueLocFragment;
import org.openhds.mobile.model.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * This activity is only used in searching for an Individual. This activity is
 * launched before creating Relationship and Internal In Migration events. It's
 * also launched before creating a new Location.
 */
public class FilterLocationActivity extends Activity implements ValueListener, SelectionFilterLocFragment.Listener {

    private SelectionFilterLocFragment selectionFilterLocFragment;
    private ValueLocFragment valueFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filterloc);

        selectionFilterLocFragment = (SelectionFilterLocFragment) getFragmentManager().findFragmentById(
                R.id.selectionLocFilterFragment);
        valueFragment = (ValueLocFragment) getFragmentManager().findFragmentById(R.id.valueLocFragment);

        processExtras();
    }

    private void processExtras() {

        Location location = (Location) getIntent().getExtras().getSerializable("location");

        selectionFilterLocFragment.setLocation(location.getExtId());
    }



    public void onHierarchy1Selected(LocationHierarchy hierarchy) {
    }

    public void onHierarchy2Selected(LocationHierarchy hierarchy) {
    }
    
    public void onHierarchy3Selected(LocationHierarchy hierarchy) {
    }

    public void onHierarchy4Selected(LocationHierarchy hierarchy) {
    }

    public void onRoundSelected(Round round) {
        // not implemented
    }

    public void onLocationSelected(Location location) {
    	selectionFilterLocFragment.updateLocationText(location.getExtId());
    	
        Intent i = new Intent();
        i.putExtra("location", location);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    public void onSeeListHierarchy1() {
     //   valueFragment.loadLocationHierarchy();
    }

    public void onSeeListHierarchy2(String region) {
     //   valueFragment.loadHierarchy2(region);
    }
    
    public void onSeeListHierarchy3(String hierarchyExtId) {
  //      valueFragment.loadHierarchy3(hierarchyExtId);
    }

    public void onSeeListHierarchy4(String subregion) {
   //     valueFragment.loadHierarchy4(subregion);
    }

    public void onSeeListLocation(String village) {
        valueFragment.loadLocations(village);
    }

    public void onSearch(String location) {
        valueFragment.loadFilteredLocations(location);
    }

	public void onIndividualSelected(Individual individual) {
		// TODO Auto-generated method stub
		
	}



}

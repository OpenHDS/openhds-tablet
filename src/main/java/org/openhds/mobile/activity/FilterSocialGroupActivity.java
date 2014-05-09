package org.openhds.mobile.activity;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.SelectionFilterFragment;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FilterSocialGroupActivity extends Activity implements ValueListener, SelectionFilterSocialgroupFragment.Listener {

    private SelectionFilterSocialgroupFragment selectionFilterSocialgroupFragment;
    private ValueFragment valueFragment;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_socialgroups);

        selectionFilterSocialgroupFragment = (SelectionFilterSocialgroupFragment) getFragmentManager().findFragmentById(
                R.id.selectionFilterSocialgroupFragment);
        valueFragment = (ValueFragment) getFragmentManager().findFragmentById(R.id.valueFragment);

        processExtras();
    }
    
    private void processExtras() {
        LocationHierarchy hierarchy1 = (LocationHierarchy) getIntent().getExtras().getSerializable("hierarchy1");
        LocationHierarchy hierarchy2 = (LocationHierarchy) getIntent().getExtras().getSerializable("hierarchy2");
        LocationHierarchy hierarchy3 = (LocationHierarchy) getIntent().getExtras().getSerializable("hierarchy3");
        LocationHierarchy hierarchy4 = (LocationHierarchy) getIntent().getExtras().getSerializable("hierarchy4");
        
        Location location = (Location) getIntent().getExtras().getSerializable("location");

        selectionFilterSocialgroupFragment.setHierarchy1(hierarchy1.getExtId());
        selectionFilterSocialgroupFragment.setHierarchy2(hierarchy2.getExtId());
        selectionFilterSocialgroupFragment.setHierarchy3(hierarchy3.getExtId());
        selectionFilterSocialgroupFragment.setHierarchy4(hierarchy4.getExtId());
        selectionFilterSocialgroupFragment.setLocation(location.getExtId());
    }    
    
    public void onIndividualSelected(Individual individual) {
        Intent i = new Intent();
        //i.putExtra("individual", individual);
        setResult(Activity.RESULT_OK, i);
        finish();
    }
    
    public void onSocialGroupSelected(SocialGroup socialGroup) {
        Intent i = new Intent();
        i.putExtra("socialGroup", socialGroup);
        setResult(Activity.RESULT_OK, i);
        finish();
    }    

    public void onHierarchy1Selected(LocationHierarchy hierarchy) {
        selectionFilterSocialgroupFragment.updateHierarchy1Text(hierarchy.getExtId());
    }

    public void onHierarchy2Selected(LocationHierarchy hierarchy) {
        selectionFilterSocialgroupFragment.updateHierarchy2Text(hierarchy.getExtId());
    }
    
    public void onHierarchy3Selected(LocationHierarchy hierarchy) {
        selectionFilterSocialgroupFragment.updateHierarchy3Text(hierarchy.getExtId());
    }

    public void onHierarchy4Selected(LocationHierarchy hierarchy) {
        selectionFilterSocialgroupFragment.updateHierarchy4Text(hierarchy.getExtId());
    }

    public void onRoundSelected(Round round) {
        // not implemented
    }

    public void onLocationSelected(Location location) {
        selectionFilterSocialgroupFragment.updateLocationText(location.getExtId());
    }    
    
    public void onSearch(String location, String firstName, String lastName, String gender) {
        //valueFragment.loadFilteredIndividuals2(location, firstName, lastName, gender);
    	valueFragment.loadFilteredSocialGroups(location);
    }

    public void onSeeListHierarchy1() {
        valueFragment.loadLocationHierarchy();
    }

    public void onSeeListHierarchy2(String region) {
        valueFragment.loadHierarchy2(region);
    }
    
    public void onSeeListHierarchy3(String hierarchyExtId) {
        valueFragment.loadHierarchy3(hierarchyExtId);
    }

    public void onSeeListHierarchy4(String subregion) {
        valueFragment.loadHierarchy4(subregion);
    }

    public void onSeeListLocation(String village) {
        valueFragment.loadLocations(village);
    }    
}

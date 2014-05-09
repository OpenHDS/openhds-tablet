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
        //valueFragment.loadFilteredIndividuals2(location, firstName, lastName, gender);
    	valueFragment.loadFilteredSocialGroups(extId, groupName);
    }   
}

package org.openhds.mobile.activity;

import android.widget.Toast;
import org.openhds.mobile.R;
import org.openhds.mobile.fragment.SelectionFilterFragment;
import org.openhds.mobile.fragment.ValueFragment;
import org.openhds.mobile.fragment.ValueFragment.ValueListener;
import org.openhds.mobile.model.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * This activity is only used in searching for an Individual. This activity is
 * launched before creating Relationship and Internal In Migration events. It's
 * also launched before creating a new Location.
 */
public class FilterActivity extends Activity implements ValueListener, SelectionFilterFragment.Listener {

    private SelectionFilterFragment selectionFilterFragment;
    private ValueFragment valueFragment;
    private String requireGender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter);

        selectionFilterFragment = (SelectionFilterFragment) getFragmentManager().findFragmentById(
                R.id.selectionFilterFragment);
        valueFragment = (ValueFragment) getFragmentManager().findFragmentById(R.id.valueFragment);

        processExtras();
    }

    private void processExtras() {
        LocationHierarchy hierarchy1 = (LocationHierarchy) getIntent().getExtras().getSerializable("hierarchy1");
        LocationHierarchy hierarchy2 = (LocationHierarchy) getIntent().getExtras().getSerializable("hierarchy2");
        LocationHierarchy hierarchy3 = (LocationHierarchy) getIntent().getExtras().getSerializable("hierarchy3");
        LocationHierarchy hierarchy4 = (LocationHierarchy) getIntent().getExtras().getSerializable("hierarchy4");
        
        Location location = (Location) getIntent().getExtras().getSerializable("location");
        requireGender = getIntent().getExtras().getString("requireGender");

        selectionFilterFragment.setHierarchy1(hierarchy1.getExtId());
        selectionFilterFragment.setHierarchy2(hierarchy2.getExtId());
        selectionFilterFragment.setHierarchy3(hierarchy3.getExtId());
        selectionFilterFragment.setHierarchy4(hierarchy4.getExtId());
        selectionFilterFragment.setLocation(location.getExtId());
    }

    public void onIndividualSelected(Individual individual) {
        if (requireGender != null && !requireGender.equals(individual.getGender())) {
            Toast.makeText(getApplicationContext(), "Please choose " + requireGender, Toast.LENGTH_LONG).show();
            return;
        }

        Intent i = new Intent();
        i.putExtra("individual", individual);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    public void onHierarchy1Selected(LocationHierarchy hierarchy) {
        selectionFilterFragment.updateHierarchy1Text(hierarchy.getExtId());
    }

    public void onHierarchy2Selected(LocationHierarchy hierarchy) {
        selectionFilterFragment.updateHierarchy2Text(hierarchy.getExtId());
    }
    
    public void onHierarchy3Selected(LocationHierarchy hierarchy) {
        selectionFilterFragment.updateHierarchy3Text(hierarchy.getExtId());
    }

    public void onHierarchy4Selected(LocationHierarchy hierarchy) {
        selectionFilterFragment.updateHierarchy4Text(hierarchy.getExtId());
    }

    public void onRoundSelected(Round round) {
        // not implemented
    }

    public void onLocationSelected(Location location) {
        selectionFilterFragment.updateLocationText(location.getExtId());
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

    public void onSearch(String location, String firstName, String lastName, String gender) {
        valueFragment.loadFilteredIndividuals(location, firstName, lastName, gender);
    }

}

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
    private boolean isBirth = false;

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
        LocationHierarchy region = (LocationHierarchy) getIntent().getExtras().getSerializable("region");
        LocationHierarchy subRegion = (LocationHierarchy) getIntent().getExtras().getSerializable("subRegion");
        LocationHierarchy village = (LocationHierarchy) getIntent().getExtras().getSerializable("village");
        Location location = (Location) getIntent().getExtras().getSerializable("location");
        isBirth = getIntent().getExtras().getBoolean("isBirth");

        selectionFilterFragment.setRegion(region.getExtId());
        selectionFilterFragment.setSubregion(subRegion.getExtId());
        selectionFilterFragment.setVillage(village.getExtId());
        selectionFilterFragment.setLocation(location.getExtId());
    }

    public void onIndividualSelected(Individual individual) {
        if (isBirth && individual.getGender().equals("Female")) {
            Toast.makeText(getApplicationContext(), "Please choose Male", Toast.LENGTH_LONG).show();
            return;
        }

        Intent i = new Intent();
        i.putExtra("individual", individual);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    public void onHierarchySelected(LocationHierarchy hierarchy) {
        selectionFilterFragment.updateRegionText(hierarchy.getExtId());
    }

    public void onSubRegionSelected(LocationHierarchy subregion) {
        selectionFilterFragment.updateSubregionText(subregion.getExtId());
    }

    public void onVillageSelected(LocationHierarchy village) {
        selectionFilterFragment.updateVillageText(village.getExtId());
    }

    public void onRoundSelected(Round round) {
        // not implemented
    }

    public void onLocationSelected(Location location) {
        selectionFilterFragment.updateLocationText(location.getExtId());
    }

    public void onSeeListRegion() {
        valueFragment.loadLocationHierarchy();
    }

    public void onSeeListSubRegion(String region) {
        valueFragment.loadSubRegion(region);
    }

    public void onSeeListVillage(String subregion) {
        valueFragment.loadVillage(subregion);
    }

    public void onSeeListLocation(String village) {
        valueFragment.loadLocations(village);
    }

    public void onSearch(String location, String firstName, String lastName, String gender) {
        valueFragment.loadFilteredIndividuals(location, firstName, lastName, gender);
    }
}

package org.openhds.mobile.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
    private String img;
    private int minimumAge;


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
        minimumAge = getIntent().getExtras().getInt("minimumAge");
        img = getIntent().getExtras().getString("img");

        selectionFilterFragment.setHierarchy1(hierarchy1.getExtId());
        selectionFilterFragment.setHierarchy2(hierarchy2.getExtId());
        selectionFilterFragment.setHierarchy3(hierarchy3.getExtId());
        selectionFilterFragment.setHierarchy4(hierarchy4.getExtId());
        selectionFilterFragment.setLocation(location.getExtId());
    }

    public void onIndividualSelected(Individual individual) {
        if (requireGender != null && !requireGender.equals(individual.getGender())) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_choose_lbl) + " " + requireGender, Toast.LENGTH_LONG).show();
            return;
        }
        
        if( minimumAge != 0){
        	int ageInYears = calculateAgeInYears(individual.getDob());
        	if(ageInYears < minimumAge){
                Toast.makeText(getApplicationContext(), getString(R.string.parenthood_minimium_age) + minimumAge, Toast.LENGTH_LONG).show();
                return;
        	}
        }

        Intent i = new Intent();
        i.putExtra("individual", individual);
        i.putExtra("origin", individual.getCurrentResidence());
        setResult(Activity.RESULT_OK, i);
        finish();
    }
    
    private int calculateAgeInYears(String dateString){
    	int ageInYears = 0;
    	Date dateOfBirth;
		try {
			dateOfBirth = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
	    	Calendar dob = Calendar.getInstance();  
	    	dob.setTime(dateOfBirth);  
	    	Calendar today = Calendar.getInstance();  
	    	int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);  
	    	if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
	    	  age--;  
	    	} else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
	    	    && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
	    	  age--;  
	    	}
	    	ageInYears = age;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return ageInYears;
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
    	if (img !=null) {
    		valueFragment.loadAllFilteredIndividuals(location, firstName, lastName, gender, img);
    		} else {
            valueFragment.loadFilteredIndividuals(location, firstName, lastName, gender);
    	}
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

}

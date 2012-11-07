package org.openhds.mobile.fragment;

import org.openhds.mobile.R;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class SelectionFilterFragment extends Fragment implements OnClickListener {
	
	public interface Listener {
		void onSeeListRegion();
		
		void onSeeListSubRegion(String region);
		
		void onSeeListVillage(String subregion);
		
		void onSeeListLocation(String village);
	
		void onSearch(String location, String firstName, String lastName, String gender);
	}
	
	// keep track of the original values - these may be set by an activity
	// these will be used if user presses the 'clear' button
	private String region = "";
	private String subregion = "";
	private String village = "";
	private String location = "";
	
	private Button regionBtn, subRegionBtn, villageBtn, locationBtn;
	private TextView regionTxt, subregionTxt, villageTxt, locationTxt, firstNameTxt, lastNameTxt;
	private RadioButton maleBtn, femaleBtn;
	private Button clearBtn, searchBtn;
	private Listener listener;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.selection_filter, container, false);
		
		regionBtn = (Button) view.findViewById(R.id.region_see_list);
	    regionBtn.setOnClickListener(this);
	    
	    subRegionBtn = (Button) view.findViewById(R.id.subregion_see_list);
	    subRegionBtn.setOnClickListener(this);
	    
	    villageBtn = (Button) view.findViewById(R.id.village_see_list);
	    villageBtn.setOnClickListener(this);
	    
	    locationBtn = (Button) view.findViewById(R.id.location_see_list);
	    locationBtn.setOnClickListener(this);
	    
	    clearBtn = (Button) view.findViewById(R.id.clearFilterBtn);
	    clearBtn.setOnClickListener(this);
	    
	    searchBtn = (Button) view.findViewById(R.id.searchFilterBtn);
	    searchBtn.setOnClickListener(this);   
	    
	    regionTxt = (TextView) view.findViewById(R.id.regionTxt); 
	    subregionTxt = (TextView) view.findViewById(R.id.subRegionTxt); 
	    villageTxt = (TextView) view.findViewById(R.id.villageTxt);
	    locationTxt = (TextView) view.findViewById(R.id.locationTxt);
	    firstNameTxt = (TextView) view.findViewById(R.id.firstNameTxt);
	    lastNameTxt = (TextView) view.findViewById(R.id.lastNameTxt);
	    
	    maleBtn = (RadioButton) view.findViewById(R.id.maleBtn);
	    femaleBtn = (RadioButton) view.findViewById(R.id.femaleBtn);
       
        return view;
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			listener = (Listener)activity;
		} catch(ClassCastException e) {
			throw new ClassCastException("The activity must implement the " + Listener.class.getName() + " interface");
		}
	}
	
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.region_see_list:
				listener.onSeeListRegion();
				break;
			case R.id.subregion_see_list:
				listener.onSeeListSubRegion(regionTxt.getText().toString());
				break;
			case R.id.village_see_list:
				listener.onSeeListVillage(subregionTxt.getText().toString());
				break;
			case R.id.location_see_list:
				listener.onSeeListLocation(villageTxt.getText().toString());
				break;
			case R.id.searchFilterBtn:
				String gender = "";
				if (maleBtn.isChecked() || femaleBtn.isChecked()) {
					gender = maleBtn.isChecked() ? "Male" : "Female";
				}
				listener.onSearch(locationTxt.getText().toString(), firstNameTxt.getText().toString(), lastNameTxt.getText().toString(), gender);
				break;
			case R.id.clearFilterBtn:
				clear();
				break;
		}
	}
	
	private void clear() {
		regionTxt.setText(region);
		subregionTxt.setText(subregion);
		villageTxt.setText(village);
		locationTxt.setText(location);
		
		firstNameTxt.setText("");
		lastNameTxt.setText("");
		maleBtn.setChecked(false);
		femaleBtn.setChecked(false);
	}

	public void setRegion(String region) {
		this.region = region;
		updateRegionText(region);
	}

	public void setSubregion(String subregion) {
		this.subregion = subregion;
		updateSubregionText(subregion);
	}

	public void setVillage(String village) {
		this.village = village;
		updateVillageText(village);
	}

	public void setLocation(String location) {
		this.location = location;
		updateLocationText(location);
	}
	
	public void updateRegionText(String text) {
		regionTxt.setText(text);
	}
	
	public void updateSubregionText(String text) {
		subregionTxt.setText(text);
	}
	
	public void updateVillageText(String text) {
		villageTxt.setText(text);
	}
	
	public void updateLocationText(String text) {
		locationTxt.setText(text);
	}
}

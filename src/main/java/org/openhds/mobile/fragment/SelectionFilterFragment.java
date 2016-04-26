package org.openhds.mobile.fragment;

import java.util.ArrayList;
import java.util.List;

import org.openhds.mobile.R;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.model.LocationHierarchyLevel;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
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
        void onSeeListHierarchy1();

        void onSeeListHierarchy2(String region);

        void onSeeListHierarchy3(String hierarchyExtId);

        void onSeeListHierarchy4(String subregion);

        void onSeeListLocation(String village);

        void onSearch(String location, String firstName, String lastName, String gender);
    }

    // keep track of the original values - these may be set by an activity
    // these will be used if user presses the 'clear' button
    private String hierarchy1 = "";
    private String hierarchy2 = "";
    private String hierarchy3 = "";
    private String hierarchy4 = "";
    private String location = "";

    private Button hierarchy1Btn, hierarchy2Btn, hierarchy3Btn, hierarchy4Btn,hierarchy5Btn, hierarchy6Btn, hierarchy7Btn, hierarchy8Btn, locationBtn;
    private TextView hierarchy1Txt, hierarchy2Txt, hierarchy3Txt, hierarchy4Txt, locationTxt, firstNameTxt,
            lastNameTxt,hierarchy1Lbl,hierarchy2Lbl,hierarchy3Lbl,hierarchy4Lbl,hierarchy5Txt,  hierarchy6Txt,
            hierarchy7Txt, hierarchy8Txt,hierarchy5Lbl, hierarchy6Lbl, hierarchy7Lbl, hierarchy8Lbl;
    private RadioButton maleBtn, femaleBtn;
    private Button clearBtn, searchBtn;
    private Listener listener;
    private List<Button> hierarchyButtons;
    private List<TextView> hierarchyLabelViews;
    private List<TextView> hierarchyHintViews;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.selection_filter, container, false);

        hierarchy1Btn = (Button) view.findViewById(R.id.hierarchy1_see_list);
        hierarchy1Btn.setOnClickListener(this);



        hierarchy2Btn = (Button) view.findViewById(R.id.hierarchy2_see_list);
        hierarchy2Btn.setOnClickListener(this);

        hierarchy3Btn = (Button) view.findViewById(R.id.hierarchy3_see_list);
        hierarchy3Btn.setOnClickListener(this);

        hierarchy4Btn = (Button) view.findViewById(R.id.hierarchy4_see_list);
        hierarchy4Btn.setOnClickListener(this);

        locationBtn = (Button) view.findViewById(R.id.location_see_list);
        locationBtn.setOnClickListener(this);
        

        clearBtn = (Button) view.findViewById(R.id.clearFilterBtn);
        clearBtn.setOnClickListener(this);

        searchBtn = (Button) view.findViewById(R.id.searchFilterBtn);
        searchBtn.setOnClickListener(this);

        hierarchy1Txt = (TextView) view.findViewById(R.id.hierarchy1Txt);
        hierarchy2Txt = (TextView) view.findViewById(R.id.hierarchy2Txt);
        hierarchy3Txt = (TextView) view.findViewById(R.id.hierarchy3Txt);
        hierarchy4Txt = (TextView) view.findViewById(R.id.hierarchy4Txt);
        hierarchy1Lbl = (TextView) view.findViewById(R.id.hierarchy1Lbl);
        hierarchy2Lbl = (TextView) view.findViewById(R.id.hierarchy2Lbl);
        hierarchy3Lbl = (TextView) view.findViewById(R.id.hierarchy3Lbl);
        hierarchy4Lbl = (TextView) view.findViewById(R.id.hierarchy4Lbl);
        
        hierarchy1Txt.setVisibility(View.GONE);
        hierarchy1Btn.setVisibility(View.GONE);
        hierarchy1Lbl.setVisibility(View.GONE);
        
        hierarchy2Txt.setVisibility(View.GONE);
        hierarchy2Btn.setVisibility(View.GONE);
        hierarchy2Lbl.setVisibility(View.GONE);
        
        hierarchy3Txt.setVisibility(View.GONE);
        hierarchy3Btn.setVisibility(View.GONE);
        hierarchy3Lbl.setVisibility(View.GONE);
        
        hierarchy4Txt.setVisibility(View.GONE);
        hierarchy4Btn.setVisibility(View.GONE);
        hierarchy4Lbl.setVisibility(View.GONE);
        
        hierarchy5Btn = (Button) view.findViewById(R.id.hierarchy5_see_list);
        hierarchy5Btn.setOnClickListener(this);
        hierarchy5Txt = (TextView) view.findViewById(R.id.hierarchy5Txt);
        hierarchy5Lbl = (TextView) view.findViewById(R.id.hierarchy5Lbl);
        hierarchy5Btn.setVisibility(View.GONE);
        hierarchy5Txt.setVisibility(View.GONE);
        hierarchy5Lbl.setVisibility(View.GONE);
        
/*        hierarchy6Btn = (Button) view.findViewById(R.id.hierarchy6_see_list);
        hierarchy6Btn.setOnClickListener(this);
        hierarchy6Text = (TextView) view.findViewById(R.id.hierarchy6Txt);
        hierarchy6Lbl = (TextView) view.findViewById(R.id.hierarchy6Lbl);
        hierarchy6Btn.setVisibility(View.GONE);
        hierarchy6Text.setVisibility(View.GONE);
        hierarchy6Lbl.setVisibility(View.GONE);
        
        hierarchy7Btn = (Button) view.findViewById(R.id.hierarchy7_see_list);
        hierarchy7Btn.setOnClickListener(this);
        hierarchy7Text = (TextView) view.findViewById(R.id.hierarchy7Txt);
        hierarchy7Lbl = (TextView) view.findViewById(R.id.hierarchy7Lbl);
        hierarchy7Btn.setVisibility(View.GONE);
        hierarchy7Text.setVisibility(View.GONE);
        hierarchy7Lbl.setVisibility(View.GONE);

        hierarchy8Btn = (Button) view.findViewById(R.id.hierarchy8_see_list);
        hierarchy8Btn.setOnClickListener(this);
        hierarchy8Text = (TextView) view.findViewById(R.id.hierarchy7Txt);
        hierarchy8Lbl = (TextView) view.findViewById(R.id.hierarchy8Lbl);
        hierarchy8Btn.setVisibility(View.GONE);
        hierarchy8Text.setVisibility(View.GONE);
        hierarchy8Lbl.setVisibility(View.GONE);*/


        
        
        
        
        locationTxt = (TextView) view.findViewById(R.id.locationTxt);
        firstNameTxt = (TextView) view.findViewById(R.id.firstNameTxt);
        lastNameTxt = (TextView) view.findViewById(R.id.lastNameTxt);

        maleBtn = (RadioButton) view.findViewById(R.id.maleBtn);
        femaleBtn = (RadioButton) view.findViewById(R.id.femaleBtn);

        
        hierarchyLabelViews= new ArrayList<TextView>();
        hierarchyLabelViews.add(hierarchy1Lbl);
        hierarchyLabelViews.add(hierarchy2Lbl);
        hierarchyLabelViews.add(hierarchy3Lbl);
        hierarchyLabelViews.add(hierarchy4Lbl);
        hierarchyLabelViews.add(hierarchy5Lbl);
       /* hierarchyLabelViews.add(hierarchy6Lbl);
        hierarchyLabelViews.add(hierarchy7Lbl);
        hierarchyLabelViews.add(hierarchy8Lbl);*/
        hierarchyHintViews = new ArrayList<TextView>();
        hierarchyHintViews.add(hierarchy1Txt);
        hierarchyHintViews.add(hierarchy2Txt);
        hierarchyHintViews.add(hierarchy3Txt);
        hierarchyHintViews.add(hierarchy4Txt);
        hierarchyHintViews.add(hierarchy5Txt);
       /* hierarchyHintViews.add(hierarchy6Txt);
        hierarchyHintViews.add(hierarchy7Txt);
        hierarchyHintViews.add(hierarchy8Txt);*/
        
        
        hierarchyButtons = new ArrayList<Button>();
        hierarchyButtons.add(hierarchy1Btn);
        hierarchyButtons.add(hierarchy2Btn);
        hierarchyButtons.add(hierarchy3Btn);
        hierarchyButtons.add(hierarchy4Btn);
        hierarchyButtons.add(hierarchy5Btn);
       /* hierarchyButtons.add(hierarchy6Btn);
        hierarchyButtons.add(hierarchy7Btn);
        hierarchyButtons.add(hierarchy8Btn);*/
        
      
        
        setHierarchyLabels();
        
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("The activity must implement the " + Listener.class.getName() + " interface");
        }
    }

    public void onClick(View view) {
        int id = view.getId();
		if (id == R.id.hierarchy1_see_list) {
			listener.onSeeListHierarchy1();
		} else if (id == R.id.hierarchy2_see_list) {
			listener.onSeeListHierarchy2(hierarchy1Txt.getText().toString());
		} else if (id == R.id.hierarchy3_see_list) {
			listener.onSeeListHierarchy3(hierarchy2Txt.getText().toString());
		} else if (id == R.id.hierarchy4_see_list) {
			listener.onSeeListHierarchy4(hierarchy3Txt.getText().toString());
		} else if (id == R.id.location_see_list) {
			listener.onSeeListLocation(hierarchy4Txt.getText().toString());
		} else if (id == R.id.searchFilterBtn) {
			String gender = "";
			String loc = "";
			loc = locationTxt.getText().toString();
			if (maleBtn.isChecked() || femaleBtn.isChecked()) {
                gender = maleBtn.isChecked() ? "M" : "F";
            }
			listener.onSearch(loc, firstNameTxt.getText().toString(), lastNameTxt
                    .getText().toString(), gender);
		} else if (id == R.id.clearFilterBtn) {
			clear();
		}
    }

    private void clear() {
        hierarchy1Txt.setText(hierarchy1);
        hierarchy2Txt.setText(hierarchy2);
        hierarchy3Txt.setText(hierarchy3);
        hierarchy4Txt.setText(hierarchy4);
        locationTxt.setText(location);

        firstNameTxt.setText("");
        lastNameTxt.setText("");
        maleBtn.setChecked(false);
        femaleBtn.setChecked(false);
    }

    public void setHierarchy1(String region) {
        this.hierarchy1 = region;
        updateHierarchy1Text(region);
    }

    public void setHierarchy2(String subregion) {
        this.hierarchy2 = subregion;
        updateHierarchy2Text(subregion);
    }
    
    public void setHierarchy3(String hierarchy) {
        this.hierarchy3 = hierarchy;
        hierarchy3Txt.setText(hierarchy);
    }

    public void setHierarchy4(String village) {
        this.hierarchy4 = village;
        updateHierarchy4Text(village);
    }

    public void setLocation(String location) {
        this.location = location;
        updateLocationText(location);
    }

    public void updateHierarchy1Text(String text) {
        hierarchy1Txt.setText(text);
    }

    public void updateHierarchy2Text(String text) {
        hierarchy2Txt.setText(text);
    }

    public void updateHierarchy3Text(String text) {
        hierarchy3Txt.setText(text);
    }    
    
    public void updateHierarchy4Text(String text) {
        hierarchy4Txt.setText(text);
    }

    public void updateLocationText(String text) {
        locationTxt.setText(text);
    }
    
    /*private void setHierarchyLabels(){
        Cursor c = Queries.getAllHierarchyLevels(getActivity().getContentResolver());
        List<LocationHierarchyLevel> lhll = Converter.toLocationHierarchyLevelList(c); 
        c.close();
        
        int levelNumbers = lhll.size()-1;
        int startLevel = 1;
        for(int i = 0; i < levelNumbers; i++){
        	hierarchyLabelViews.get(i).setText(lhll.get(startLevel).getName());
        	hierarchyHintViews.get(i).setHint(lhll.get(startLevel).getName());
        	startLevel++;
        }     	
    }*/
    
    
    private void setHierarchyLabels(){
        Cursor c = Queries.getAllHierarchyLevels(getActivity().getContentResolver());
        List<LocationHierarchyLevel> lhll = Converter.toLocationHierarchyLevelList(c); 
        c.close();
        
        int levelNumbers = lhll.size()-1;
        int startLevel = 1;
        for(int i = 0; i < levelNumbers; i++){
        	hierarchyButtons.get(i).setText(lhll.get(startLevel).getName());
        	hierarchyLabelViews.get(i).setText(lhll.get(startLevel).getName());
        	hierarchyHintViews.get(i).setHint(lhll.get(startLevel).getName());
        	hierarchyButtons.get(i).setVisibility(View.VISIBLE);
        	hierarchyHintViews.get(i).setVisibility(View.VISIBLE);
        	hierarchyLabelViews.get(i).setVisibility(View.VISIBLE);
        	startLevel++;
        }     	
    }
    
    
}

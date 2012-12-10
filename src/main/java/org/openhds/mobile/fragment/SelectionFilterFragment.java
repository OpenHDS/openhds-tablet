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

    private Button hierarchy1Btn, hierarchy2Btn, hierarchy3Btn, hierarchy4Btn, locationBtn;
    private TextView hierarchy1Txt, hierarchy2Txt, hierarchy3Txt, hierarchy4Txt, locationTxt, firstNameTxt,
            lastNameTxt;
    private RadioButton maleBtn, femaleBtn;
    private Button clearBtn, searchBtn;
    private Listener listener;

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
            listener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("The activity must implement the " + Listener.class.getName() + " interface");
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.hierarchy1_see_list:
            listener.onSeeListHierarchy1();
            break;
        case R.id.hierarchy2_see_list:
            listener.onSeeListHierarchy2(hierarchy1Txt.getText().toString());
            break;
        case R.id.hierarchy3_see_list:
            listener.onSeeListHierarchy3(hierarchy2Txt.getText().toString());
            break;
        case R.id.hierarchy4_see_list:
            listener.onSeeListHierarchy4(hierarchy3Txt.getText().toString());
            break;
        case R.id.location_see_list:
            listener.onSeeListLocation(hierarchy4Txt.getText().toString());
            break;
        case R.id.searchFilterBtn:
            String gender = "";
            if (maleBtn.isChecked() || femaleBtn.isChecked()) {
                gender = maleBtn.isChecked() ? "Male" : "Female";
            }
            listener.onSearch(locationTxt.getText().toString(), firstNameTxt.getText().toString(), lastNameTxt
                    .getText().toString(), gender);
            break;
        case R.id.clearFilterBtn:
            clear();
            break;
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
}

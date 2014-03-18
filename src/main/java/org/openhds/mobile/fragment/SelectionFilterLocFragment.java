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
import android.widget.TextView;

public class SelectionFilterLocFragment extends Fragment implements OnClickListener {

    public interface Listener {
        void onSeeListHierarchy1();

        void onSeeListHierarchy2(String region);

        void onSeeListHierarchy3(String hierarchyExtId);

        void onSeeListHierarchy4(String subregion);

        void onSeeListLocation(String village);

        void onSearch(String location);
    }

    // keep track of the original values - these may be set by an activity
    // these will be used if user presses the 'clear' button
    private String location = "";

    private TextView locationTxt;
    private Button clearBtn, searchBtn;
    private Listener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.selection_filter_loc, container, false);

        clearBtn = (Button) view.findViewById(R.id.clearFilterBtn);
        clearBtn.setOnClickListener(this);

        searchBtn = (Button) view.findViewById(R.id.searchFilterBtn);
        searchBtn.setOnClickListener(this);

        locationTxt = (TextView) view.findViewById(R.id.locationTxt);
     

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
		if (id == R.id.searchFilterBtn) {
			listener.onSearch(locationTxt.getText().toString());
		} else if (id == R.id.clearFilterBtn) {
			clear();
		}
    }

    private void clear() {
        locationTxt.setText(location);
    }


    public void setLocation(String location) {
        this.location = location;
        if (location.length()>0) {
        updateLocationText(location);
        }
    }


    public void updateLocationText(String text) {
        locationTxt.setText(text);
    }
}

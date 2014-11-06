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

public class SelectionFilterSocialgroupFragment extends Fragment implements OnClickListener {

    public interface Listener {
        void onSearch(String extId, String groupName);
    }

    private TextView groupNameTxt, groupExtIdTxt;
    private Button clearBtn, searchBtn;
    private Listener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.selection_filter_socialgroup, container, false);

        clearBtn = (Button) view.findViewById(R.id.clearFilterBtn);
        clearBtn.setOnClickListener(this);

        searchBtn = (Button) view.findViewById(R.id.searchFilterBtn);
        searchBtn.setOnClickListener(this);

        groupNameTxt = (TextView) view.findViewById(R.id.groupNameTxt);
        groupExtIdTxt = (TextView) view.findViewById(R.id.groupExtIdTxt);

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
			listener.onSearch(groupExtIdTxt.getText().toString().trim(), groupNameTxt.getText().toString().trim());
		} else if (id == R.id.clearFilterBtn) {
			clear();
		}
    }

    private void clear() {
    	groupNameTxt.setText("");
    	groupExtIdTxt.setText("");
    }
}

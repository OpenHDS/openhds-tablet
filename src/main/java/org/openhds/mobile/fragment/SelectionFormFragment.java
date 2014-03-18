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

public class SelectionFormFragment extends Fragment implements OnClickListener {

    public interface Listener {
          void onSearch(String form);
    }

    // keep track of the original values - these may be set by an activity
    // these will be used if user presses the 'clear' button
    private String form = "";

    private TextView formTxt;
    private Button clearBtn, searchBtn;
    private Listener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.selection_filter_form, container, false);

        clearBtn = (Button) view.findViewById(R.id.clearFilterBtn);
        clearBtn.setOnClickListener(this);

        searchBtn = (Button) view.findViewById(R.id.searchFilterBtn);
        searchBtn.setOnClickListener(this);

        formTxt = (TextView) view.findViewById(R.id.formTxt);
     

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
			listener.onSearch(formTxt.getText().toString());
		} else if (id == R.id.clearFilterBtn) {
			clear();
		}
    }

    private void clear() {
        formTxt.setText(form);
    }


    public void setForm(String form) {
        this.form = form;
        if (form.length()>0) {
        updateFormText(form);
        }
    }


    public void updateFormText(String text) {
        formTxt.setText(text);
    }
}

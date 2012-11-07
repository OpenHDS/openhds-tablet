package org.openhds.mobile.fragment;

import org.openhds.mobile.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple fragment that displays a progress bar
 */
public class ProgressFragment extends Fragment {

    public static ProgressFragment newInstance() {
        ProgressFragment f = new ProgressFragment();

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.value, container, false);
    }
}

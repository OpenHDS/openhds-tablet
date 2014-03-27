package org.openhds.mobile.fragment;

import org.openhds.mobile.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FieldWorkerLoginFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.generic_login_fragment, container, false);
        TextView title = (TextView) v.findViewById(R.id.titleTextView);
        title.setText("Field Worker Login");
        return v;
    }
    
//    Use this to query for field workers:
//        if (Queries.hasFieldWorker(resolver, extId, password)) {
//            Cursor cursor = Queries.getFieldWorkByExtId(resolver, extId);
//            return Converter.toFieldWorker(cursor);

}

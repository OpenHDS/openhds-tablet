package org.openhds.mobile.fragment;

import java.util.List;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.HierarchyNavigator;
import org.openhds.mobile.database.queries.QueryResult;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class HierarchyValueFragment extends Fragment {

	private HierarchyNavigator navigator;
	ArrayAdapter<QueryResult> queryResultAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout valueContainer = (LinearLayout) inflater.inflate(R.layout.hierarchy_value_fragment,
				container, false);

		return valueContainer;
	}

	public void setNavigator(HierarchyNavigator navigator) {
		this.navigator = navigator;
	}

	public void populateValues(List<QueryResult> values) {
		queryResultAdapter = new ArrayAdapter<QueryResult>(getActivity(), R.layout.listview_value_container,
				R.id.listview_text_container, values);
		ListView listView = (ListView) getActivity().findViewById(R.id.value_fragment_listview);
		listView.setAdapter(queryResultAdapter);
		listView.setOnItemClickListener(new HierarchyValueListener());
	}

	private class HierarchyValueListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			QueryResult selected = queryResultAdapter.getItem(position);
			navigator.stepDown(selected);
		}
	}
}

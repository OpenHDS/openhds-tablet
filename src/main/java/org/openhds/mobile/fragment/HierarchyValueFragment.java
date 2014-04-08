package org.openhds.mobile.fragment;

import java.util.List;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.HierarchyNavigator;
import org.openhds.mobile.database.queries.QueryResult;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class HierarchyValueFragment extends Fragment {

	private HierarchyNavigator navigator;
	HierarchyValueAdapter queryResultAdapter;

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
		queryResultAdapter = new HierarchyValueAdapter(getActivity(), R.layout.listview_value_container,
				values);

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

	private class HierarchyValueAdapter extends ArrayAdapter<QueryResult> {

		public HierarchyValueAdapter(Context context, int resource, List<QueryResult> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.listview_value_container,
						null);
			}

			QueryResult qr = queryResultAdapter.getItem(position);

			TextView queryResultName = (TextView) convertView.findViewById(R.id.name_text);
			queryResultName.setText(qr.getName());

			TextView queryResultExtId = (TextView) convertView.findViewById(R.id.ext_id_text);
			queryResultExtId.setText(qr.getExtId());

			return convertView;
		}
	}
}

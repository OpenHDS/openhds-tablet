package org.openhds.mobile.fragment;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LayoutUtils.makeNewGenericButton;

import java.util.Map;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.HierarchyNavigator;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class HierarchySelectionFragment extends Fragment {

	private HierarchyNavigator navigator;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout buttonContainer = (LinearLayout) inflater.inflate(R.layout.hierarchy_selection_fragment,
				container, false);

		HierarchyButtonListener listener = new HierarchyButtonListener();
		Map<String, Integer> labels = navigator.getStateLabels();
		for (String state : navigator.getStateSequence()) {
			makeNewGenericButton(getActivity(), "", getResourceString(getActivity(), labels.get(state)),
					state, listener, buttonContainer);
		}

		return buttonContainer;
	}

	public void setNavigator(HierarchyNavigator navigator) {
		this.navigator = navigator;
	}

	private class HierarchyButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			navigator.jumpUp((String) v.getTag());
		}
	}
}

package org.openhds.mobile.fragment;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LayoutUtils.makeNewGenericButton;

import java.util.HashMap;
import java.util.Map;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.HierarchyNavigator;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;

public class HierarchySelectionFragment extends Fragment {

	private HierarchyNavigator navigator;
	private Map<String, Button> buttonsForStates;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout buttonContainer = (LinearLayout) inflater.inflate(R.layout.hierarchy_selection_fragment,
				container, false);

		HierarchyButtonListener listener = new HierarchyButtonListener();
		Map<String, Integer> labels = navigator.getStateLabels();
		buttonsForStates = new HashMap<String, Button>();
		for (String state : navigator.getStateSequence()) {
			final String description = null;
			// MAKE BUTTON
			Button b = makeNewGenericButton(getActivity(), description,
					getResourceString(getActivity(), labels.get(state)), state, listener, buttonContainer);
			// CUSTOMIZE BUTTON WITH A LAYOUTPARAMS
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 20, 0, 0);
			b.setLayoutParams(params);
			buttonsForStates.put(state, b);
			setButtonAllowed(state, false);
			setButtonHighlighted(state, false);
		}

		return buttonContainer;
	}

	public void setNavigator(HierarchyNavigator navigator) {
		this.navigator = navigator;
	}

	public void setButtonAllowed(String state, boolean isShown) {
		Button b = buttonsForStates.get(state);

		if (null == b) {
			return;
		}
		b.setVisibility(isShown ? View.VISIBLE : View.GONE);

	}

	public void setButtonLabel(String state, String label) {
		Button b = buttonsForStates.get(state);
		if (null == b) {
			return;
		}
		b.setText(label);
	}

	private class HierarchyButtonListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			navigator.jumpUp((String) v.getTag());
		}
	}

	public void setButtonHighlighted(String state, boolean isHighlighted) {
		Button b = buttonsForStates.get(state);

		if (null == b) {
			return;
		}
		b.setBackgroundColor(isHighlighted ? getResources().getColor(R.color.SommerBlue) : 
			getResources().getColor(R.color.WolfGray));
	}
}

package org.openhds.mobile.activity;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openhds.mobile.R;
import org.openhds.mobile.database.queries.QueryHelper;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.fragment.HierarchySelectionFragment;
import org.openhds.mobile.fragment.HierarchyValueFragment;
import org.openhds.mobile.model.StateMachine;
import org.openhds.mobile.model.StateMachine.StateListener;

import android.app.Activity;
import android.os.Bundle;

public class CensusActivity extends Activity implements HierarchyNavigator {

	public static final String REGION_STATE = "region";
	public static final String MAP_AREA_STATE = "mapArea";
	public static final String SECTOR_STATE = "sector";
	public static final String HOUSEHOLD_STATE = "household";
	public static final String INDIVIDUAL_STATE = "individual";
	public static final String BOTTOM_STATE = "bottom";

	private static final List<String> stateSequence = new ArrayList<String>();
	private static final Map<String, Integer> stateLabels = new HashMap<String, Integer>();
	static {
		stateSequence.add(REGION_STATE);
		stateSequence.add(MAP_AREA_STATE);
		stateSequence.add(SECTOR_STATE);
		stateSequence.add(HOUSEHOLD_STATE);
		stateSequence.add(INDIVIDUAL_STATE);
		stateSequence.add(BOTTOM_STATE);

		stateLabels.put(REGION_STATE, R.string.region_label);
		stateLabels.put(MAP_AREA_STATE, R.string.map_area_label);
		stateLabels.put(SECTOR_STATE, R.string.sector_label);
		stateLabels.put(HOUSEHOLD_STATE, R.string.household_label);
		stateLabels.put(INDIVIDUAL_STATE, R.string.individual_label);
		stateLabels.put(BOTTOM_STATE, R.string.bottom_label);
	}

	private static final String SELECTION_FRAGMENT_TAG = "hierarchySelectionFragment";
	private static final String VALUE_FRAGMENT_TAG = "hierarchyValueFragment";

	private StateMachine stateMachine;
	private Map<String, QueryResult> hierarchyPath;
	private List<QueryResult> currentResults;
	private HierarchySelectionFragment selectionFragment;
	private HierarchyValueFragment valueFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.census_activity);
		hierarchyPath = new HashMap<String, QueryResult>();
		stateMachine = new StateMachine(new HashSet<String>(stateSequence), stateSequence.get(0));
		for (String state : stateSequence) {
			stateMachine.registerListener(state, new HierarchyStateListener());
		}

		if (null == savedInstanceState) {
			// create fresh activity
			selectionFragment = new HierarchySelectionFragment();
			selectionFragment.setNavigator(this);
			valueFragment = new HierarchyValueFragment();
			valueFragment.setNavigator(this);
			getFragmentManager().beginTransaction()
					.add(R.id.left_column, selectionFragment, SELECTION_FRAGMENT_TAG)
					.add(R.id.middle_column, valueFragment, VALUE_FRAGMENT_TAG).commit();

		} else {
			// restore saved activity state
			selectionFragment = (HierarchySelectionFragment) getFragmentManager().findFragmentByTag(
					SELECTION_FRAGMENT_TAG);
			selectionFragment.setNavigator(this);
			valueFragment = (HierarchyValueFragment) getFragmentManager().findFragmentByTag(
					VALUE_FRAGMENT_TAG);
			valueFragment.setNavigator(this);

			// try to re-fetch selected data all the way down the hierarchy path
			for (String state : stateSequence) {
				if (savedInstanceState.containsKey(state)) {
					String extId = savedInstanceState.getString(state);
					if (!restoreHierarchyPath(state, extId)) {
						break;
					}
				} else {
					break;
				}
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// save extIds all down the hierarchy path
		for (String state : stateSequence) {
			if (hierarchyPath.containsKey(state)) {
				QueryResult selected = hierarchyPath.get(state);
				savedInstanceState.putString(state, selected.getExtId());
			}
		}
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		hierarchySetup();
	}

	// attempt to re-fetch the selected data at the given state
	private boolean restoreHierarchyPath(String state, String extId) {
		currentResults = QueryHelper.getAll(getContentResolver(), state);
		for (QueryResult qr : currentResults) {
			if (extId.equals(qr.getExtId())) {
				hierarchyPath.put(state, qr);
				return true;
			}
		}
		return false;
	}

	private void hierarchySetup() {
		int stateIndex = 0;
		for (String state : stateSequence) {
			if (hierarchyPath.containsKey(state)) {
				updateButtonLabel(state);
				selectionFragment.setButtonAllowed(state, true);
				stateIndex++;
			} else {
				break;
			}
		}

		String state = stateSequence.get(stateIndex);
		if (0 == stateIndex) {
			selectionFragment.setButtonAllowed(state, true);
			currentResults = QueryHelper.getAll(getContentResolver(), stateSequence.get(0));
		} else {
			String previousState = stateSequence.get(stateIndex - 1);
			QueryResult previousSelection = hierarchyPath.get(previousState);
			currentResults = QueryHelper.getChildren(getContentResolver(), previousSelection, state);
		}
		stateMachine.transitionTo(state);

		if (1 == currentResults.size()) {
			stepDown(currentResults.get(0));
		} else {
			valueFragment.populateValues(currentResults);
		}
	}

	private void updateButtonLabel(String state) {
		
		String buttonLabel = getResourceString(CensusActivity.this, stateLabels.get(state));
		QueryResult selected = hierarchyPath.get(state);
		boolean buttonIsHighlighted = true;
		if (null != selected) {
			buttonIsHighlighted = false;
			buttonLabel = selected.getName() + "\n" + selected.getExtId();
		}
		selectionFragment.setButtonLabel(state, buttonLabel);
		selectionFragment.setButtonHighlighted(state, buttonIsHighlighted);
	}

	@Override
	public Map<String, Integer> getStateLabels() {
		return stateLabels;
	}

	@Override
	public List<String> getStateSequence() {
		return stateSequence;
	}

	// Required to update currentResults for targetState and transition to the
	// target state.
	@Override
	public void jumpUp(String targetState) {
		int targetIndex = stateSequence.indexOf(targetState);
		if (targetIndex < 0) {
			throw new IllegalStateException("Target state <" + targetState + "> is not a valid state");
		}

		String currentState = stateMachine.getState();
		int currentIndex = stateSequence.indexOf(currentState);
		if (targetIndex >= currentIndex) {
			// use stepDown() to go down the hierarchy
			return;
		}

		// un-traverse the hierarchy up to the target state
		for (int i = currentIndex; i >= targetIndex; i--) {
			String state = stateSequence.get(i);
			selectionFragment.setButtonAllowed(state, false);
			hierarchyPath.remove(state);
		}

		// prepare to stepDown() from this target state
		if (0 == targetIndex) {
			// root of the hierarchy
			currentResults = QueryHelper.getAll(getContentResolver(), stateSequence.get(0));
		} else {
			// middle of the hierarchy
			String previousState = stateSequence.get(targetIndex - 1);
			QueryResult previousSelection = hierarchyPath.get(previousState);
			currentResults = QueryHelper.getChildren(getContentResolver(), previousSelection, targetState);
		}
		stateMachine.transitionTo(targetState);
	}

	// Required to update currentResults with children of selected, and
	// transition to the next state down.
	@Override
	public void stepDown(QueryResult selected) {

		String currentState = stateMachine.getState();
		if (!currentState.equals(selected.getState())) {
			throw new IllegalStateException("Selected state <" + selected.getState()
					+ "> mismatch with current state <" + currentState + ">");
		}

		int currentIndex = stateSequence.indexOf(currentState);
		if (currentIndex >= 0 && currentIndex < stateSequence.size() - 1) {
			String nextState = stateSequence.get(currentIndex + 1);
			currentResults = QueryHelper.getChildren(getContentResolver(), selected, nextState);
			hierarchyPath.put(currentState, selected);
			stateMachine.transitionTo(nextState);
		}
	}

	private class HierarchyStateListener implements StateListener {

		@Override
		public void onEnterState() {
			String state = stateMachine.getState();
			updateButtonLabel(state);
			valueFragment.populateValues(currentResults);
			if (!state.equals(stateSequence.get(stateSequence.size() - 1))) {
				selectionFragment.setButtonAllowed(state, true);
				// show forms
			}
		}

		@Override
		public void onExitState() {
			String state = stateMachine.getState();
			updateButtonLabel(state);
		}
	}
}

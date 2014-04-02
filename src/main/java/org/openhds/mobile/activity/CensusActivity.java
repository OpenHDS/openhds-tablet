package org.openhds.mobile.activity;

import static org.openhds.mobile.utilities.MessageUtils.showShortToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openhds.mobile.R;
import org.openhds.mobile.database.queries.QueryHelper;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.fragment.HierarchySelectionFragment;
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

	private static final List<String> stateSequence = new ArrayList<String>();
	private static final Map<String, Integer> stateLabels = new HashMap<String, Integer>();
	static {
		stateSequence.add(REGION_STATE);
		stateSequence.add(MAP_AREA_STATE);
		stateSequence.add(SECTOR_STATE);
		stateSequence.add(HOUSEHOLD_STATE);
		stateSequence.add(INDIVIDUAL_STATE);

		stateLabels.put(REGION_STATE, R.string.region_label);
		stateLabels.put(MAP_AREA_STATE, R.string.map_area_label);
		stateLabels.put(SECTOR_STATE, R.string.sector_label);
		stateLabels.put(HOUSEHOLD_STATE, R.string.household_label);
		stateLabels.put(INDIVIDUAL_STATE, R.string.individual_label);
	}

	private static final String CURRENT_STATE_KEY = "currentState";
	private static final String HIERARCHY_SELECTION_FRAGMENT_TAG = "hierarchySelectionFragment";

	private StateMachine stateMachine;
	private Map<String, QueryResult> hierarchyPath;
	private List<QueryResult> currentResults;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.census_activity);
		hierarchyPath = new HashMap<String, QueryResult>();

		if (null == savedInstanceState) {
			// create fresh activity
			stateMachine = new StateMachine(new HashSet<String>(stateSequence), stateSequence.get(0));
			for (String state : stateSequence) {
				stateMachine.registerListener(state, new HierarchyStateListener());
			}
			topLevelHierarchySetup();
			HierarchySelectionFragment selectionFragment = new HierarchySelectionFragment();
			selectionFragment.setNavigator(this);
			getFragmentManager().beginTransaction()
					.add(R.id.left_column, selectionFragment, HIERARCHY_SELECTION_FRAGMENT_TAG).commit();
		} else {
			// restore saved activity state
			// TODO restore hierarchyPath from savedInstanceState
			stateMachine = new StateMachine(new HashSet<String>(stateSequence),
					savedInstanceState.getString(CURRENT_STATE_KEY));
			HierarchySelectionFragment selectionFragment = (HierarchySelectionFragment) getFragmentManager()
					.findFragmentByTag(HIERARCHY_SELECTION_FRAGMENT_TAG);
			selectionFragment.setNavigator(this);
		}

	}

	private void topLevelHierarchySetup() {
		String topLevelState = stateSequence.get(0);
		List<QueryResult> topLevelResults = QueryHelper.getAll(getContentResolver(), topLevelState);
		if (null == topLevelResults) {
			showShortToast(this, "No results for top level state: " + topLevelState);
			return;
		}
		descend(topLevelResults.get(0));
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// TODO save hierarchyPath for savedInstanceState
		savedInstanceState.putString(CURRENT_STATE_KEY, stateMachine.getState());
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public Map<String, Integer> getStateLabels() {
		return stateLabels;
	}

	@Override
	public List<String> getStateSequence() {
		return stateSequence;
	}

	@Override
	public void jump(String state) {
		stateMachine.transitionTo(state);
	}

	@Override
	public void descend(QueryResult selectedResult) {

		String currentState = stateMachine.getState();

		if (!currentState.equals(selectedResult.getState())) {
			throw new IllegalStateException("Selected state <" + selectedResult.getState()
					+ "> mismatch with current state <" + currentState + ">");
		}

		int stateIndex = stateSequence.indexOf(currentState);
		if (stateIndex >= 0 && stateIndex < stateSequence.size() - 1) {
			String nextState = stateSequence.get(stateIndex + 1);
			currentResults = QueryHelper.getChildren(getContentResolver(), selectedResult, nextState);
			hierarchyPath.put(currentState, selectedResult);
			stateMachine.transitionTo(nextState);
		}
	}

	private class HierarchyStateListener implements StateListener {

		@Override
		public void onEnterState() {
			showShortToast(CensusActivity.this, "Entered state: " + stateMachine.getState());
			// TODO: update value fragment with new list of results.
		}

		@Override
		public void onExitState() {
			showShortToast(CensusActivity.this, "Exited state: " + stateMachine.getState());
		}
	}
}

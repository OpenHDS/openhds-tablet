package org.openhds.mobile.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 
 * 
 */
public class StateMachine {

	private Set<String> stateSet;
	private String currentState;
	private Map<String, Set<StateListener>> stateListeners;

	public interface StateListener {
		void onEnterState();

		void onExitState();
	}

	public StateMachine(Set<String> stateSet, String defaultState) {
		this.stateSet = stateSet;
		stateListeners = new HashMap<String, Set<StateListener>>();
		transitionTo(defaultState);
	}

	public String getState() {
		return currentState;
	}

	public Set<String> getStateSet() {
		return stateSet;
	}

	public void transitionTo(String state) {
		if (!stateSet.contains(state)) {
			throw new IllegalStateException("State machine has no such state: "
					+ state);
		}

		fireOnExitListeners();
		currentState = state;
		fireOnEnterListeners();
	}

	public void registerListener(String state, StateListener stateListener) {

		if (!stateSet.contains(state)) {
			throw new IllegalStateException("State machine has no such state: "
					+ state);
		}

		if (stateListeners.get(state) == null) {
			stateListeners.put(state, new HashSet<StateListener>());
		}
		stateListeners.get(state).add(stateListener);
	}

	private void fireOnExitListeners() {
		Set<StateListener> listenersToFire = stateListeners.get(currentState);
		if (listenersToFire == null) {
			return;
		}

		for (StateListener listener : listenersToFire) {
			listener.onExitState();
		}
	}

	private void fireOnEnterListeners() {
		Set<StateListener> listenersToFire = stateListeners.get(currentState);
		if (listenersToFire == null) {
			return;
		}

		for (StateListener listener : listenersToFire) {
			listener.onEnterState();
		}
	}
	
	public void transitionInSequence(String toState) {
		Object stateArray[] = stateSet.toArray();
        for (int i = 0; i < stateSet.size(); i++) {
            String state = (String)stateArray[i];
            
            if (state.equals(toState)) {
                break;
            }

            transitionTo(state);
        }

        transitionTo(toState);
    }	
}

package org.openhds.mobile.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The state machine represents the user interactions using the update activity.
 * The tasks the user completes can be thought of as a state machine. For
 * example, selecting the initial location hierarchy:<br />
 * State 1: Select Region <br />
 * State 2: Select Subregion <br />
 * State 3: Select village <br />
 * As the user transitions through the states, certain actions need to happen.
 * For example, when the user selects a round, then the select location and
 * create location buttons need to become enabled. Interested clients can
 * register to listen on transitions from one state to the next. A transition
 * out of a state signifies the user has made a selection.
 */
public class StateMachine {

    public enum State {
        SELECT_HIERARCHY_1, SELECT_HIERARCHY_2, SELECT_HIERARCHY_3, SELECT_HIERARCHY_4, SELECT_ROUND, SELECT_LOCATION, CREATE_VISIT, SELECT_INDIVIDUAL, SELECT_EVENT, FINISH_VISIT
    }

    private static final List<State> STATE_SEQUENCE = new ArrayList<State>();

    static {
        STATE_SEQUENCE.add(State.SELECT_HIERARCHY_1);
        STATE_SEQUENCE.add(State.SELECT_HIERARCHY_2);
        STATE_SEQUENCE.add(State.SELECT_HIERARCHY_3);
        STATE_SEQUENCE.add(State.SELECT_HIERARCHY_4);
        STATE_SEQUENCE.add(State.SELECT_ROUND);
        STATE_SEQUENCE.add(State.SELECT_LOCATION);
        STATE_SEQUENCE.add(State.CREATE_VISIT);
        STATE_SEQUENCE.add(State.SELECT_INDIVIDUAL);
        STATE_SEQUENCE.add(State.SELECT_EVENT);
        STATE_SEQUENCE.add(State.FINISH_VISIT);
    }

    public interface StateListener {
        void onEnterState();

        void onLeaveState();
    }

    private State currentState;
    private Map<State, List<StateListener>> listeners = new HashMap<State, List<StateListener>>();

    public StateMachine() {
        currentState = State.SELECT_HIERARCHY_1;
    }

    public void transitionTo(State state) {
        fireOnExitListeners();
        currentState = state;
        fireOnEnterListeners();
    }

    private void fireOnExitListeners() {
        List<StateListener> listenersToFire = listeners.get(currentState);
        if (listenersToFire == null) {
            return;
        }

        for (StateListener listener : listenersToFire) {
            listener.onLeaveState();
        }
    }

    private void fireOnEnterListeners() {
        List<StateListener> listenersToFire = listeners.get(currentState);
        if (listenersToFire == null) {
            return;
        }

        for (StateListener listener : listenersToFire) {
            listener.onEnterState();
        }
    }

    public State getState() {
        return currentState;
    }

    public void registerListener(State state, StateListener stateListener) {
        if (listeners.get(state) == null) {
            listeners.put(state, new ArrayList<StateListener>());
        }

        listeners.get(state).add(stateListener);
    }

    public void transitionInSequence(State toState) {
        for (int i = 0; i < STATE_SEQUENCE.size(); i++) {
            State state = STATE_SEQUENCE.get(i);
            if (state.equals(toState)) {
                break;
            }

            transitionTo(state);
        }

        transitionTo(toState);
    }

}

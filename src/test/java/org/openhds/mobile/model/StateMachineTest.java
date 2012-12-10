package org.openhds.mobile.model;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.openhds.mobile.model.StateMachine.State;
import org.openhds.mobile.model.StateMachine.StateListener;

public class StateMachineTest extends TestCase {

    public void testShouldMoveToNextState() {
        StateMachine machine = new StateMachine();
        machine.transitionTo(State.SELECT_HIERARCHY_2);

        assertEquals(State.SELECT_HIERARCHY_2, machine.getState());
    }

    public void testShouldFireListenerOnEnterState() {
        StateMachine sm = new StateMachine();
        EnterStateListener listener = new EnterStateListener();
        sm.registerListener(State.SELECT_HIERARCHY_2, listener);

        sm.transitionTo(State.SELECT_HIERARCHY_2);

        assertTrue(listener.fired);
    }

    static class EnterStateListener implements StateListener {
        boolean fired = false;

        public void onEnterState() {
            fired = true;
        }

        public void onLeaveState() {
        }

    }

    public void testShouldFireListenerOnExitState() {
        StateMachine sm = new StateMachine();
        ExitStateListener listener = new ExitStateListener();
        sm.registerListener(State.SELECT_HIERARCHY_1, listener);

        sm.transitionTo(State.SELECT_HIERARCHY_2);

        assertTrue(listener.fired);
    }

    static class ExitStateListener implements StateListener {
        boolean fired = false;

        public void onEnterState() {
        }

        public void onLeaveState() {
            fired = true;
        }

    }

    public void testShouldFireTransitionsInSequence() {
        // TODO: do not depend on static field
        OrderedStateListener.callCount = 0;
        StateMachine sm = new StateMachine();
        ArrayList<OrderedStateListener> listeners = new ArrayList<OrderedStateListener>();
        for (int i = 0; i < 7; i++)
            listeners.add(new OrderedStateListener());

        sm.registerListener(State.SELECT_HIERARCHY_1, listeners.get(0));
        sm.registerListener(State.SELECT_HIERARCHY_2, listeners.get(1));
        sm.registerListener(State.SELECT_HIERARCHY_4, listeners.get(2));
        sm.registerListener(State.SELECT_ROUND, listeners.get(3));
        sm.registerListener(State.SELECT_LOCATION, listeners.get(4));
        sm.registerListener(State.CREATE_VISIT, listeners.get(5));
        sm.registerListener(State.SELECT_INDIVIDUAL, listeners.get(6));

        sm.transitionInSequence(State.SELECT_EVENT);

        for (int i = 0; i < 7; i++)
            assertEquals(i, listeners.get(i).calledOrder);
    }

    static class OrderedStateListener implements StateListener {
        static int callCount = 0;

        private int calledOrder = -1;

        public void onEnterState() {
            calledOrder = callCount;
            callCount += 1;
        }

        public void onLeaveState() {

        }
    }

    public void testShouldNotTransitionOnFirstState() {
        // TODO: do not depend on static field
        OrderedStateListener.callCount = 0;
        StateMachine sm = new StateMachine();
        ArrayList<OrderedStateListener> listeners = new ArrayList<OrderedStateListener>();
        for (int i = 0; i < 7; i++)
            listeners.add(new OrderedStateListener());

        sm.registerListener(State.SELECT_HIERARCHY_1, listeners.get(0));
        sm.registerListener(State.SELECT_HIERARCHY_2, listeners.get(1));
        sm.registerListener(State.SELECT_HIERARCHY_4, listeners.get(2));
        sm.registerListener(State.SELECT_ROUND, listeners.get(3));
        sm.registerListener(State.SELECT_LOCATION, listeners.get(4));
        sm.registerListener(State.CREATE_VISIT, listeners.get(5));
        sm.registerListener(State.SELECT_INDIVIDUAL, listeners.get(6));

        sm.transitionInSequence(State.SELECT_HIERARCHY_1);

        assertEquals(0, listeners.get(0).calledOrder);

        for (int i = 1; i < 7; i++)
            assertEquals(-1, listeners.get(i).calledOrder);
    }

}

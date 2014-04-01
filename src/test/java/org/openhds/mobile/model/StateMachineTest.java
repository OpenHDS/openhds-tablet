package org.openhds.mobile.model;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.openhds.mobile.model.StateMachine.StateListener;

public class StateMachineTest extends TestCase {

	private static final Set<String> testStateSet = new HashSet<String>();

	static {
		testStateSet.add("Star System");
		testStateSet.add("Solar System");
		testStateSet.add("Planet");
		testStateSet.add("Continent");
		testStateSet.add("Country");
		testStateSet.add("Province");
		testStateSet.add("City");
		testStateSet.add("House");
	}

	public void testShouldMoveToNextState() {
		StateMachine machine = new StateMachine(testStateSet, "Star System");
		assertEquals("Star System", machine.getState());

		machine.transitionTo("Planet");
		assertEquals("Planet", machine.getState());
	}

	public void testShouldFireListener() {
		StateMachine sm = new StateMachine(testStateSet, "Star System");
		TestStateListener listener = new TestStateListener();
		sm.registerListener("Solar System", listener);

		sm.transitionTo("Solar System");
		assertTrue(listener.firedOnEnter);
		sm.transitionTo("Planet");
		assertTrue(listener.firedOnExit);
	}

	static class TestStateListener implements StateListener {
		boolean firedOnEnter = false;
		boolean firedOnExit = false;

		public void onEnterState() {
			firedOnEnter = true;
		}

		public void onExitState() {
			firedOnExit = true;
		}
	}
}

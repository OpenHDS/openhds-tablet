package org.openhds.mobile.fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.openhds.mobile.R;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.LocationVisit;
import org.openhds.mobile.model.StateMachine;
import org.openhds.mobile.model.StateMachine.State;
import org.openhds.mobile.model.StateMachine.StateListener;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * EventFragment is the right most column in the update activity and displays a
 * column of buttons for letting the user perform actions during the update. The
 * events it publishes can be handled by implementing the listener interface.
 */
public class EventFragment extends Fragment implements OnClickListener {
    private static final int FEMALE_MINIMUM_PREGNANCY_AGE = 12;

    private Button findLocationGeoPointBtn, createLocationBtn, createVisitBtn, householdBtn, membershipBtn,
            relationshipBtn, inMigrationBtn, outMigrationBtn, pregRegBtn, birthRegBtn, deathBtn, finishVisitBtn,
            clearIndividualBtn;

    private Listener listener;
    private LocationVisit locationVisit;

    /**
     * Listener interface for activities using this fragment
     */
    public interface Listener {

        void onLocationGeoPoint();

        void onCreateLocation();

        void onCreateVisit();

        void onFinishVisit();

        void onHousehold();

        void onMembership();

        void onRelationship();

        void onInMigration();

        void onOutMigration();

        void onPregnancyRegistration();

        void onPregnancyOutcome();

        void onDeath();

        void onClearIndividual();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement: " + Listener.class.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event, container, false);

        bindViews(view);

        return view;
    }

    private void bindViews(View view) {
        finishVisitBtn = (Button) view.findViewById(R.id.finishVisitBtn);
        finishVisitBtn.setOnClickListener(this);

        findLocationGeoPointBtn = (Button) view.findViewById(R.id.findLocationGeoPointBtn);
        findLocationGeoPointBtn.setOnClickListener(this);

        createLocationBtn = (Button) view.findViewById(R.id.createLocationBtn);
        createLocationBtn.setOnClickListener(this);

        createVisitBtn = (Button) view.findViewById(R.id.createVisitBtn);
        createVisitBtn.setOnClickListener(this);

        householdBtn = (Button) view.findViewById(R.id.householdBtn);
        householdBtn.setOnClickListener(this);

        membershipBtn = (Button) view.findViewById(R.id.membershipBtn);
        membershipBtn.setOnClickListener(this);

        relationshipBtn = (Button) view.findViewById(R.id.relationshipBtn);
        relationshipBtn.setOnClickListener(this);

        inMigrationBtn = (Button) view.findViewById(R.id.inMigrationBtn);
        inMigrationBtn.setOnClickListener(this);

        outMigrationBtn = (Button) view.findViewById(R.id.outMigrationBtn);
        outMigrationBtn.setOnClickListener(this);

        pregRegBtn = (Button) view.findViewById(R.id.pregRegBtn);
        pregRegBtn.setOnClickListener(this);

        birthRegBtn = (Button) view.findViewById(R.id.birthRegBtn);
        birthRegBtn.setOnClickListener(this);

        deathBtn = (Button) view.findViewById(R.id.deathBtn);
        deathBtn.setOnClickListener(this);

        clearIndividualBtn = (Button) view.findViewById(R.id.clearIndividualBtn);
        clearIndividualBtn.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.findLocationGeoPointBtn:
            listener.onLocationGeoPoint();
            break;
        case R.id.createLocationBtn:
            listener.onCreateLocation();
            break;
        case R.id.createVisitBtn:
            listener.onCreateVisit();
            break;
        case R.id.finishVisitBtn:
            listener.onFinishVisit();
            break;
        case R.id.householdBtn:
            listener.onHousehold();
            break;
        case R.id.membershipBtn:
            listener.onMembership();
            break;
        case R.id.relationshipBtn:
            listener.onRelationship();
            break;
        case R.id.inMigrationBtn:
            listener.onInMigration();
            break;
        case R.id.outMigrationBtn:
            listener.onOutMigration();
            break;
        case R.id.pregRegBtn:
            listener.onPregnancyRegistration();
            break;
        case R.id.birthRegBtn:
            listener.onPregnancyOutcome();
            break;
        case R.id.deathBtn:
            listener.onDeath();
            break;
        case R.id.clearIndividualBtn:
            listener.onClearIndividual();
            break;
        }
    }

    public void setLocationVisit(LocationVisit locationVisit) {
        this.locationVisit = locationVisit;
    }

    public void registerTransitions(StateMachine machine) {
        registerRegionListener(machine);
        registerLocationListener(machine);
        registerVisitListener(machine);
        registerIndividualListeners(machine);
        registerEventListeners(machine);
    }

    private void registerEventListeners(StateMachine machine) {
        machine.registerListener(State.SELECT_EVENT, new StateListener() {
            public void onEnterState() {
                householdBtn.setEnabled(true);
                finishVisitBtn.setEnabled(true);
                membershipBtn.setEnabled(true);
                relationshipBtn.setEnabled(true);
                outMigrationBtn.setEnabled(true);
                deathBtn.setEnabled(true);
                clearIndividualBtn.setEnabled(true);

                Individual indiv = locationVisit.getSelectedIndividual();
                if ("f".equalsIgnoreCase(indiv.getGender()) && individualMeetsMinimumAge(indiv)) {
                    pregRegBtn.setEnabled(true);
                    birthRegBtn.setEnabled(true);
                }
            }

            public void onLeaveState() {
                membershipBtn.setEnabled(false);
                relationshipBtn.setEnabled(false);
                outMigrationBtn.setEnabled(false);
                deathBtn.setEnabled(false);
                clearIndividualBtn.setEnabled(false);
                pregRegBtn.setEnabled(false);
                birthRegBtn.setEnabled(false);
                finishVisitBtn.setEnabled(false);
                householdBtn.setEnabled(false);
            }
        });
    }

    private boolean individualMeetsMinimumAge(Individual indiv) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date dob = formatter.parse(indiv.getDob());
            Calendar cal = Calendar.getInstance();
            cal.setTime(dob);
            if ((new GregorianCalendar().get(Calendar.YEAR) - cal.get(Calendar.YEAR)) > FEMALE_MINIMUM_PREGNANCY_AGE) {
                return true;
            }
        } catch (Exception e) {
            // no dob or malformed
            return true;
        }

        return false;
    }

    private void registerIndividualListeners(StateMachine machine) {
        machine.registerListener(State.SELECT_INDIVIDUAL, new StateListener() {
            public void onEnterState() {
                finishVisitBtn.setEnabled(true);
                inMigrationBtn.setEnabled(true);
            }

            public void onLeaveState() {
                finishVisitBtn.setEnabled(false);
                inMigrationBtn.setEnabled(false);
            }
        });
    }

    private void registerVisitListener(StateMachine machine) {
        machine.registerListener(State.CREATE_VISIT, new StateListener() {
            public void onEnterState() {
                createVisitBtn.setEnabled(true);
            }

            public void onLeaveState() {
                createVisitBtn.setEnabled(false);
            }
        });
    }

    private void registerLocationListener(StateMachine machine) {
        machine.registerListener(State.SELECT_LOCATION, new StateListener() {
            public void onEnterState() {
                createLocationBtn.setEnabled(true);
            }

            public void onLeaveState() {
                createLocationBtn.setEnabled(false);
            }
        });
    }

    private void registerRegionListener(StateMachine machine) {
        machine.registerListener(State.SELECT_HIERARCHY_1, new StateListener() {
            public void onEnterState() {
                findLocationGeoPointBtn.setEnabled(true);
            }

            public void onLeaveState() {
                findLocationGeoPointBtn.setEnabled(false);
            }
        });
    }
}

package org.openhds.mobile.fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.openhds.mobile.R;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.LocationVisit;
import org.openhds.mobile.model.Settings;
import org.openhds.mobile.model.StateMachine;
import org.openhds.mobile.model.StateMachine.StateListener;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * EventFragment is the right most column in the update activity and displays a
 * column of buttons for letting the user perform actions during the update. The
 * events it publishes can be handled by implementing the listener interface.
 */
public class EventFragment extends Fragment implements OnClickListener {
    private static int FEMALE_MINIMUM_PREGNANCY_AGE;
    private static int DEFAULT_FEMALE_MINIMUM_PREGNANCY_AGE = 12;

    private Button findLocationGeoPointBtn, createLocationBtn, createVisitBtn, householdBtn, membershipBtn,
            relationshipBtn, inMigrationBtn, outMigrationBtn, pregRegBtn, birthRegBtn, deathBtn, finishVisitBtn,
            clearIndividualBtn, changeHouseholdHeadBtn;

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

        void onBaseline();

        void onOutMigration();

        void onPregnancyRegistration();

        void onPregnancyOutcome();

        void onDeath();

        void onClearIndividual();
        
        void onInMigration();
        
        void onChangeHouseholdHead();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
		android.database.Cursor c = Queries.getAllSettings(activity.getContentResolver());
		Settings settings = Converter.convertToSettings(c); 
		c.close();
		FEMALE_MINIMUM_PREGNANCY_AGE = settings.getMinimumAgeOfPregnancy() == 0 ? DEFAULT_FEMALE_MINIMUM_PREGNANCY_AGE : settings.getMinimumAgeOfPregnancy();		

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
        
        changeHouseholdHeadBtn = (Button) view.findViewById(R.id.changeHouseholdHeadBtn);
        changeHouseholdHeadBtn.setOnClickListener(this);
    }
    
    public void setBaseLine(){
    	if(inMigrationBtn != null){
    		inMigrationBtn.setText("Baseline");
    	}
    	if(householdBtn != null){
    		householdBtn.setVisibility(LinearLayout.GONE);
    	}
    	if(outMigrationBtn != null){
    		outMigrationBtn.setVisibility(LinearLayout.GONE);
    	}
    	if(deathBtn != null){
    		deathBtn.setVisibility(LinearLayout.GONE);
    	}
    	if(birthRegBtn != null){
    		birthRegBtn.setVisibility(LinearLayout.GONE);
    	}
    	if(findLocationGeoPointBtn != null){
    		LayoutParams lp = findLocationGeoPointBtn.getLayoutParams();
    		findLocationGeoPointBtn.setVisibility(LinearLayout.GONE);
    		createLocationBtn.setLayoutParams(lp);
    	}    
    	if(changeHouseholdHeadBtn != null){
    		changeHouseholdHeadBtn.setVisibility(LinearLayout.GONE);
    	}
    }

    public void onClick(View view) {
        int id = view.getId();
        view.setEnabled(false);
		if (id == R.id.findLocationGeoPointBtn) {
			listener.onLocationGeoPoint();
		} else if (id == R.id.createLocationBtn) {
			listener.onCreateLocation();
		} else if (id == R.id.createVisitBtn) {
			listener.onCreateVisit();
		} else if (id == R.id.finishVisitBtn) {
			listener.onFinishVisit();
		} else if (id == R.id.householdBtn) {
			listener.onHousehold();
		} else if (id == R.id.membershipBtn) {
			listener.onMembership();
		} else if (id == R.id.relationshipBtn) {
			listener.onRelationship();
		} else if (id == R.id.inMigrationBtn) {
			listener.onBaseline();
		} else if (id == R.id.outMigrationBtn) {
			listener.onOutMigration();
		} else if (id == R.id.pregRegBtn) {
			listener.onPregnancyRegistration();
		} else if (id == R.id.birthRegBtn) {
			listener.onPregnancyOutcome();
		} else if (id == R.id.deathBtn) {
			listener.onDeath();
		} else if (id == R.id.clearIndividualBtn) {
			listener.onClearIndividual();
		} else if (id == R.id.changeHouseholdHeadBtn){
			listener.onChangeHouseholdHead();
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
        machine.registerListener("Select Event", new StateListener() {
            public void onEnterState() {
            	enableButtons();
            }

            public void onExitState() {
            	disableButtons();
            }
        });
    }
    
    public void disableButtons(){
        membershipBtn.setEnabled(false);
        relationshipBtn.setEnabled(false);
        outMigrationBtn.setEnabled(false);
        deathBtn.setEnabled(false);
        clearIndividualBtn.setEnabled(false);
        pregRegBtn.setEnabled(false);
        birthRegBtn.setEnabled(false);
        finishVisitBtn.setEnabled(false);
        householdBtn.setEnabled(false);    	
        changeHouseholdHeadBtn.setEnabled(false);
    }
    
    public void enableButtons(){
        householdBtn.setEnabled(true);
        finishVisitBtn.setEnabled(true);
        //Check no of households and disable button if necessary
        
        membershipBtn.setEnabled(true);
       // relationshipBtn.setEnabled(true);
        outMigrationBtn.setEnabled(true);
        deathBtn.setEnabled(true);
        clearIndividualBtn.setEnabled(true);
        changeHouseholdHeadBtn.setEnabled(false);

        Individual indiv = locationVisit.getSelectedIndividual();
        if (indiv != null && "f".equalsIgnoreCase(indiv.getGender()) && individualMeetsMinimumAge(indiv)) {
            pregRegBtn.setEnabled(true);
            birthRegBtn.setEnabled(true);
            relationshipBtn.setEnabled(true);
        }    	
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
        machine.registerListener("Select Individual", new StateListener() {
            public void onEnterState() {
                finishVisitBtn.setEnabled(true);
                inMigrationBtn.setEnabled(true);
                changeHouseholdHeadBtn.setEnabled(true);
            }

            public void onExitState() {
                finishVisitBtn.setEnabled(false);
                inMigrationBtn.setEnabled(false);
                changeHouseholdHeadBtn.setEnabled(false);
            }
        });
    }

    private void registerVisitListener(StateMachine machine) {
        machine.registerListener("Create Visit", new StateListener() {
            public void onEnterState() {
                createVisitBtn.setEnabled(true);
            }

            public void onExitState() {
                createVisitBtn.setEnabled(false);
            }
        });
    }

    private void registerLocationListener(StateMachine machine) {
        machine.registerListener("Select Location", new StateListener() {
            public void onEnterState() {
                createLocationBtn.setEnabled(true);
            }

            public void onExitState() {
                createLocationBtn.setEnabled(false);
            }
        });
    }

    private void registerRegionListener(StateMachine machine) {
        machine.registerListener("Select Hierarchy 1", new StateListener() {
            public void onEnterState() {
                findLocationGeoPointBtn.setEnabled(true);
            }

            public void onExitState() {
                findLocationGeoPointBtn.setEnabled(false);
            }
        });
    }
    
}

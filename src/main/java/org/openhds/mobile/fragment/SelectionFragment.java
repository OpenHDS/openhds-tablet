package org.openhds.mobile.fragment;

import java.util.ArrayList;
import java.util.List;

import org.openhds.mobile.R;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.LocationVisit;
import org.openhds.mobile.model.Round;
import org.openhds.mobile.model.StateMachine;
import org.openhds.mobile.model.StateMachine.StateListener;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.PorterDuff;

public class SelectionFragment extends Fragment implements OnClickListener {

    public static interface Listener {
        void onHierarchy1();

        void onHierarchy2();

        void onHierarchy3();

        void onHierarchy4();

        void onLocation();

        void onRound();

        void onIndividual();
        
        void onFilterLocation();
    }

    private Listener listener;
    private LocationVisit locationVisit;

    private Button hierarchy1Btn, hierarchy2Btn, hierarchy3Btn, hierarchy4Btn, locationBtn, roundBtn, individualBtn, searchlBtn;
    private List<Button> hierarchyButtons;
    private Drawable defaultDrawable;

    private TextView loginGreetingText, hierarchy1NameText, hierarchy1ExtIdText, hierarchy2NameText,
            hierarchy2ExtIdText, hierarchy3NameText, hierarchy3ExtIdText, hierarchy4NameText, hierarchy4ExtIdText,
            roundNumberText, roundStartDateText, roundEndDateText, locationNameText, locationExtIdText,
            locationLatitudeText, locationLongitudeText, individualFirstNameText, individualLastNameText,
            individualExtIdText, individualDobText;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (Listener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.selection, container, false);
        bindViews(view);
        
        return view;
    }

    private void bindViews(View view) {
        loginGreetingText = (TextView) view.findViewById(R.id.loginGreetingText);

        hierarchy1Btn = (Button) view.findViewById(R.id.hierarchy1Btn);
        hierarchy1Btn.setOnClickListener(this);
        hierarchy1NameText = (TextView) view.findViewById(R.id.hierarchy1Name);
        hierarchy1ExtIdText = (TextView) view.findViewById(R.id.hierarchy1ExtId);

        hierarchy2Btn = (Button) view.findViewById(R.id.hierarchy2Btn);
        hierarchy2Btn.setOnClickListener(this);
        hierarchy2NameText = (TextView) view.findViewById(R.id.hierarchy2Name);
        hierarchy2ExtIdText = (TextView) view.findViewById(R.id.hierarchy2ExtId);

        hierarchy3Btn = (Button) view.findViewById(R.id.hierarchy3Btn);
        hierarchy3Btn.setOnClickListener(this);
        hierarchy3NameText = (TextView) view.findViewById(R.id.hierarchy3Name);
        hierarchy3ExtIdText = (TextView) view.findViewById(R.id.hierarchy3ExtId);

        hierarchy4Btn = (Button) view.findViewById(R.id.hierarchy4Btn);
        hierarchy4Btn.setOnClickListener(this);
        hierarchy4NameText = (TextView) view.findViewById(R.id.hierarchy4Name);
        hierarchy4ExtIdText = (TextView) view.findViewById(R.id.hierarchy4ExtId);

        locationBtn = (Button) view.findViewById(R.id.locationBtn);
        locationBtn.setOnClickListener(this);
        
        searchlBtn = (Button) view.findViewById(R.id.searchlBtn);
        searchlBtn.setOnClickListener(this);
        
        locationNameText = (TextView) view.findViewById(R.id.locationNameText);
        locationExtIdText = (TextView) view.findViewById(R.id.locationExtIdText);
        locationLatitudeText = (TextView) view.findViewById(R.id.locationLatitudeText);
        locationLongitudeText = (TextView) view.findViewById(R.id.locationLongitudeText);

        roundBtn = (Button) view.findViewById(R.id.roundBtn);
        roundBtn.setOnClickListener(this);
        roundNumberText = (TextView) view.findViewById(R.id.roundNumberText);
        roundStartDateText = (TextView) view.findViewById(R.id.roundStartDateText);
        roundEndDateText = (TextView) view.findViewById(R.id.roundEndDateText);

        individualBtn = (Button) view.findViewById(R.id.individualBtn);
        individualBtn.setOnClickListener(this);
        individualExtIdText = (TextView) view.findViewById(R.id.individualExtIdText);
        individualFirstNameText = (TextView) view.findViewById(R.id.individualFirstNameText);
        individualLastNameText = (TextView) view.findViewById(R.id.individualLastNameText);
        individualDobText = (TextView) view.findViewById(R.id.individualDobText);
        
        hierarchyButtons = new ArrayList<Button>();
        hierarchyButtons.add(hierarchy1Btn);
        hierarchyButtons.add(hierarchy2Btn);
        hierarchyButtons.add(hierarchy3Btn);
        hierarchyButtons.add(hierarchy4Btn);
        hierarchyButtons.add(roundBtn);
        
        defaultDrawable = hierarchy1Btn.getBackground();
        roundBtn.setVisibility(View.GONE);
    }
    
    private Button getButtonForState(String state){
    	for(Button button : hierarchyButtons){
    		if(button.getText().toString().equals(state)){
    			return button;
    		}
    	}
    	return null;
    }

    private void setHierarchy1() {
        LocationHierarchy region = locationVisit.getHierarchy1();
        if (region == null) {
            region = LocationHierarchy.emptyHierarchy();
        }

        hierarchy1NameText.setText(region.getName());
        hierarchy1ExtIdText.setText(region.getExtId());
    }

    private void setHierarchy2() {
        LocationHierarchy subRegion = locationVisit.getHierarchy2();
        if (subRegion == null) {
            subRegion = LocationHierarchy.emptyHierarchy();
        }

        hierarchy2NameText.setText(subRegion.getName());
        hierarchy2ExtIdText.setText(subRegion.getExtId());
    }

    private void setHierarchy3() {
        LocationHierarchy hierarchy3 = locationVisit.getHierarchy3();
        if (hierarchy3 == null) {
            hierarchy3 = LocationHierarchy.emptyHierarchy();
        }

        hierarchy3NameText.setText(hierarchy3.getName());
        hierarchy3ExtIdText.setText(hierarchy3.getExtId());
    }

    private void setHierarchy4() {
        LocationHierarchy village = locationVisit.getHierarchy4();
        if (village == null) {
            village = LocationHierarchy.emptyHierarchy();
        }

        hierarchy4NameText.setText(village.getName());
        hierarchy4ExtIdText.setText(village.getExtId());
    }

    private void setRound() {
        Round round = locationVisit.getRound();
        if (round == null) {
            round = Round.getEmptyRound();
        }

        roundNumberText.setText(round.getRoundNumber());
        roundStartDateText.setText(round.getStartDate());
        roundEndDateText.setText(round.getEndDate());
    }

    public void onClick(View view) {
        int id = view.getId();
		if (id == R.id.hierarchy1Btn) {
			listener.onHierarchy1();
		} else if (id == R.id.hierarchy2Btn) {
			listener.onHierarchy2();
		} else if (id == R.id.hierarchy3Btn) {
			listener.onHierarchy3();
		} else if (id == R.id.hierarchy4Btn) {
			listener.onHierarchy4();
		} else if (id == R.id.locationBtn) {
			listener.onLocation();
		} else if (id == R.id.roundBtn) {
			listener.onRound();
		} else if (id == R.id.individualBtn) {
			listener.onIndividual();
		} else if (id == R.id.searchlBtn) {
			listener.onFilterLocation();
		}
    }
    
    public void updateButtons(int level){
//    	int selectedLevel = level;
//    	if(selectedLevel >= 0 && selectedLevel < hierarchyButtons.size()){
//    		for(int i = 0 ; i < hierarchyButtons.size(); i++){
//    			
//    			Button btn = hierarchyButtons.get(i);
//    			
//    			if(btn.isEnabled()){
////	    			if(i < selectedLevel){
////	    				btn.setBackgroundColor(Color.GREEN);
////	    			}
////	    			else{
////	    				btn.setBackgroundColor(Color.RED);
////	    			}
//    				btn.setBackgroundColor(Color.LTGRAY);
//    			}
//    			else
//    			{
//    				btn.setBackgroundColor(Color.DKGRAY);
//    			}
//    		}
//    	}
//    	if(selectedLevel+1 < hierarchyButtons.size()){
//    		Button btn = hierarchyButtons.get(selectedLevel+1);
//    		btn.setBackgroundColor(Color.GREEN);
//    	}
    }

    public void setLocationVisit(LocationVisit locationVisit) {
        this.locationVisit = locationVisit;
        loginGreetingText.setText(getString(R.string.hello_lbl)+", " + locationVisit.getFieldWorker().getFirstName() + " "
                + locationVisit.getFieldWorker().getLastName());
        
        //Restore previous selections
        setAll();
    }

    public void registerTransitions(StateMachine stateMachine) {
        registerHierarchy1Listener(stateMachine);
        registerHierarchy2Listener(stateMachine);
        registerHierarchy3Listener(stateMachine);
        registerHierarchy4Listener(stateMachine);
        registerRoundListener(stateMachine);
        registerLocationListener(stateMachine);
        registerVisitListener(stateMachine);
        registerIndividualListener(stateMachine);
        registerEventListener(stateMachine);
        registerFinishVisitListener(stateMachine);
    }

    private void registerFinishVisitListener(StateMachine stateMachine) {
        stateMachine.registerListener("Finish Visit", new StateListener() {
            public void onEnterState() {
            }

            public void onExitState() {
                hierarchy1Btn.setEnabled(true);
                hierarchy2Btn.setEnabled(true);
                hierarchy3Btn.setEnabled(true);
                hierarchy4Btn.setEnabled(true);
                roundBtn.setEnabled(true);
                searchlBtn.setVisibility(8);
            }
        });
    }

    private void registerEventListener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Event", new StateListener() {
            public void onEnterState() {
                setIndividual();
            }

            public void onExitState() {
            }
        });
    }

    private void registerVisitListener(StateMachine stateMachine) {
        stateMachine.registerListener("Create Visit", new StateListener() {
            public void onEnterState() {
            }

            public void onExitState() {
                hierarchy1Btn.setEnabled(false);
                hierarchy2Btn.setEnabled(false);
                hierarchy3Btn.setEnabled(false);
                hierarchy4Btn.setEnabled(false);
                roundBtn.setEnabled(false);
//                locationBtn.setEnabled(false);
                locationBtn.setEnabled(true);
                searchlBtn.setVisibility(8);
            }
        });
    }

    private void registerIndividualListener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Individual", new StateListener() {
            public void onEnterState() {
                setIndividual();
                individualBtn.setEnabled(true);
                individualBtn.setBackgroundColor(Color.LTGRAY);
            }

            public void onExitState() {
                individualBtn.setEnabled(false);
                individualBtn.setBackgroundColor(Color.DKGRAY);
            }
        });
    }

    private void registerLocationListener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Location", new StateListener() {
            public void onEnterState() {
                resetToDefaultState(5, false);
                locationBtn.setEnabled(true);
                searchlBtn.setVisibility(1);
                locationBtn.setBackgroundColor(Color.LTGRAY);
            }

            public void onExitState() {
                setLocation();
                searchlBtn.setVisibility(8);
                locationBtn.setEnabled(false);
                locationBtn.setBackgroundColor(Color.DKGRAY);
            }
        });
    }

    private void registerRoundListener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Round", new StateListener() {
            public void onEnterState() {
                resetToDefaultState(4, false);
                roundBtn.setEnabled(true);
                roundBtn.setBackgroundColor(Color.LTGRAY);
            }

            public void onExitState() {
                setRound();
                roundBtn.setBackgroundColor(Color.DKGRAY);
            }
        });
    }

    private void registerHierarchy3Listener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Hierarchy 3", new StateListener() {
            public void onEnterState() {
                resetToDefaultState(2, false);
                hierarchy3Btn.setEnabled(true);
                hierarchy3Btn.setBackgroundColor(Color.LTGRAY);
            }

            public void onExitState() {
                setHierarchy3();
                hierarchy3Btn.setBackgroundColor(Color.DKGRAY);
            }
        });
    }

    private void registerHierarchy4Listener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Hierarchy 4", new StateListener() {
            public void onEnterState() {
                resetToDefaultState(3, false);
                hierarchy4Btn.setEnabled(true);
                hierarchy4Btn.setBackgroundColor(Color.LTGRAY);
            }

            public void onExitState() {
                setHierarchy4();
                hierarchy4Btn.setBackgroundColor(Color.DKGRAY);
            }
        });
    }

    private void registerHierarchy2Listener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Hierarchy 2", new StateListener() {
            public void onEnterState() {
                resetToDefaultState(1, false);
                hierarchy2Btn.setEnabled(true);
                hierarchy2Btn.setBackgroundColor(Color.LTGRAY);
            }

            public void onExitState() {
                setHierarchy2();
                hierarchy2Btn.setBackgroundColor(Color.DKGRAY);
            }
        });
    }

    private void registerHierarchy1Listener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Hierarchy 1", new StateListener() {
            public void onEnterState() {
                resetToDefaultState(0, false);
                hierarchy1Btn.setEnabled(true);
                hierarchy1Btn.setBackgroundColor(Color.LTGRAY);
            }

            public void onExitState() {
                setHierarchy1();
                hierarchy1Btn.setBackgroundColor(Color.DKGRAY);
            }
        });
    }

    private void resetToDefaultState(int level, boolean enabled) {
        switch (level) {
        case 0:
            hierarchy1Btn.setEnabled(enabled);
            searchlBtn.setVisibility(8);
            setHierarchy1();
        case 1:
            hierarchy2Btn.setEnabled(enabled);
            setHierarchy2();
        case 2:
            hierarchy3Btn.setEnabled(enabled);
            setHierarchy3();
        case 3:
            hierarchy4Btn.setEnabled(enabled);
            setHierarchy4();
        case 4:
            roundBtn.setEnabled(enabled);
            setRound();
        case 5:
            locationBtn.setEnabled(enabled);
            searchlBtn.setVisibility(1);
            setLocation();
        case 6:
            individualBtn.setEnabled(enabled);
            searchlBtn.setVisibility(8);
            setIndividual();
        }
        
        updateButtons(level);
    }

    private void setIndividual() {
        Individual selectedIndividual = locationVisit.getSelectedIndividual();
        if (selectedIndividual == null) {
            selectedIndividual = Individual.emptyIndividual();
        }

        individualFirstNameText.setText(selectedIndividual.getFirstName());
        individualLastNameText.setText(selectedIndividual.getLastName());
        individualExtIdText.setText(selectedIndividual.getExtId());
        individualDobText.setText(selectedIndividual.getDob());
    }

    private void setLocation() {
        Location location = locationVisit.getLocation();
        if (location == null) {
            location = Location.emptyLocation();
        }

        locationNameText.setText(location.getName());
        locationExtIdText.setText(location.getExtId());
        locationLatitudeText.setText(location.getLatitude());
        locationLongitudeText.setText(location.getLongitude());
    }

	public void setAll() {
		setLocation();
		setHierarchy1();
		setHierarchy2();
		setHierarchy3();
		setHierarchy4();
		setRound();
		setLocation();
		setIndividual();
	}
}

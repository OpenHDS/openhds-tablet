package org.openhds.mobile.fragment;

import java.util.ArrayList;
import java.util.List;

import org.openhds.mobile.R;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.LocationHierarchyLevel;
import org.openhds.mobile.model.LocationVisit;
import org.openhds.mobile.model.Round;
import org.openhds.mobile.model.StateMachine;
import org.openhds.mobile.model.StateMachine.StateListener;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class SelectionFragment extends Fragment implements OnClickListener {

    public static interface Listener {
        void onHierarchy1();

        void onHierarchy2();

        void onHierarchy3();

        void onHierarchy4();
        
        void onHierarchy5();
        
        void onHierarchy6();

        void onHierarchy7();
        
        void onHierarchy8();

        void onLocation();

        void onRound();

        void onIndividual();
        
        void onFilterLocation();
    }

    private Listener listener;
    private LocationVisit locationVisit;

    private Button hierarchy1Btn, hierarchy2Btn, hierarchy3Btn, hierarchy4Btn,hierarchy5Btn, hierarchy6Btn, hierarchy7Btn, hierarchy8Btn, locationBtn, roundBtn, individualBtn, searchlBtn;
    private List<Button> hierarchyButtons;
    private List<TextView> hierarchyTextViews;
    private List<TextView> hierarchyLabelViews;
    private List<TextView> hierarchyLabelExtIdViews;
    private List<TextView> hierarchyExtIdViews;


    private TextView loginGreetingText, hierarchy1NameText, hierarchy1ExtIdText, hierarchy2NameText,
            hierarchy2ExtIdText, hierarchy3NameText, hierarchy3ExtIdText, hierarchy4NameText, hierarchy4ExtIdText,
            hierarchy5NameText, hierarchy5ExtIdText, hierarchy6NameText,
            hierarchy6ExtIdText, hierarchy7NameText, hierarchy7ExtIdText, hierarchy8NameText, hierarchy8ExtIdText,
            hierarchy1LabelText, hierarchy2LabelText, hierarchy3LabelText, hierarchy4LabelText,
            hierarchy5LabelText, hierarchy6LabelText, hierarchy7LabelText, hierarchy8LabelText,
            hierarchy1LabelExtIdText, hierarchy2LabelExtIdText, hierarchy3LabelExtIdText, hierarchy4LabelExtIdText,
            hierarchy5LabelExtIdText, hierarchy6LabelExtIdText, hierarchy7LabelExtIdText, hierarchy8LabelExtIdText,
            roundNumberText, roundStartDateText, roundEndDateText, locationNameText, locationExtIdText,
            locationLatitudeText, locationLongitudeText, individualFirstNameText, individualLastNameText,
            individualExtIdText, individualDobText,
            individualExtId, individualFirstName, individualLastName, individualDob;

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
        hierarchy1LabelText = (TextView) view.findViewById(R.id.hierarchy1Label);
        hierarchy1LabelExtIdText = (TextView) view.findViewById(R.id.hierarchy1LabelExtId);
        hierarchy1ExtIdText = (TextView) view.findViewById(R.id.hierarchy1ExtId);
        hierarchy1Btn.setVisibility(View.GONE);
        hierarchy1NameText.setVisibility(View.GONE);
        hierarchy1LabelText.setVisibility(View.GONE);
        hierarchy1LabelExtIdText.setVisibility(View.GONE);
        hierarchy1ExtIdText.setVisibility(View.GONE);
        
        hierarchy2Btn = (Button) view.findViewById(R.id.hierarchy2Btn);
        hierarchy2Btn.setOnClickListener(this);
        hierarchy2NameText = (TextView) view.findViewById(R.id.hierarchy2Name);
        hierarchy2LabelText = (TextView) view.findViewById(R.id.hierarchy2Label);
        hierarchy2LabelExtIdText = (TextView) view.findViewById(R.id.hierarchy2LabelExtId);
        hierarchy2ExtIdText = (TextView) view.findViewById(R.id.hierarchy2ExtId);
        hierarchy2Btn.setVisibility(View.GONE);
        hierarchy2NameText.setVisibility(View.GONE);
        hierarchy2LabelText.setVisibility(View.GONE);
        hierarchy2LabelExtIdText.setVisibility(View.GONE);
        hierarchy2ExtIdText.setVisibility(View.GONE);

        hierarchy3Btn = (Button) view.findViewById(R.id.hierarchy3Btn);
        hierarchy3Btn.setOnClickListener(this);
        hierarchy3NameText = (TextView) view.findViewById(R.id.hierarchy3Name);
        hierarchy3LabelText = (TextView) view.findViewById(R.id.hierarchy3Label);
        hierarchy3LabelExtIdText = (TextView) view.findViewById(R.id.hierarchy3LabelExtId);
        hierarchy3ExtIdText = (TextView) view.findViewById(R.id.hierarchy3ExtId);
        hierarchy3Btn.setVisibility(View.GONE);
        hierarchy3NameText.setVisibility(View.GONE);
        hierarchy3LabelText.setVisibility(View.GONE);
        hierarchy3LabelExtIdText.setVisibility(View.GONE);
        hierarchy3ExtIdText.setVisibility(View.GONE);
        
        hierarchy4Btn = (Button) view.findViewById(R.id.hierarchy4Btn);
        hierarchy4Btn.setOnClickListener(this);
        hierarchy4NameText = (TextView) view.findViewById(R.id.hierarchy4Name);
        hierarchy4LabelText = (TextView) view.findViewById(R.id.hierarchy4Label);
        hierarchy4ExtIdText = (TextView) view.findViewById(R.id.hierarchy4ExtId);
        hierarchy4LabelExtIdText = (TextView) view.findViewById(R.id.hierarchy4LabelExtId);
        hierarchy4Btn.setVisibility(View.GONE);
        hierarchy4NameText.setVisibility(View.GONE);
        hierarchy4LabelText.setVisibility(View.GONE);
        hierarchy4LabelExtIdText.setVisibility(View.GONE);
        hierarchy4ExtIdText.setVisibility(View.GONE);
        
        hierarchy5Btn = (Button) view.findViewById(R.id.hierarchy5Btn);
        hierarchy5Btn.setOnClickListener(this);
        hierarchy5NameText = (TextView) view.findViewById(R.id.hierarchy5Name);
        hierarchy5LabelText = (TextView) view.findViewById(R.id.hierarchy5Label);
        hierarchy5LabelExtIdText = (TextView) view.findViewById(R.id.hierarchy5LabelExtId);
        hierarchy5ExtIdText = (TextView) view.findViewById(R.id.hierarchy5ExtId);
        hierarchy5Btn.setVisibility(View.GONE);
        hierarchy5NameText.setVisibility(View.GONE);
        hierarchy5ExtIdText.setVisibility(View.GONE);
        hierarchy5LabelText.setVisibility(View.GONE);
        hierarchy5LabelExtIdText.setVisibility(View.GONE);
        hierarchy5ExtIdText.setVisibility(View.GONE);
        
        hierarchy6Btn = (Button) view.findViewById(R.id.hierarchy6Btn);
        hierarchy6Btn.setOnClickListener(this);
        hierarchy6NameText = (TextView) view.findViewById(R.id.hierarchy6Name);
        hierarchy6LabelText = (TextView) view.findViewById(R.id.hierarchy6Label);
        hierarchy6ExtIdText = (TextView) view.findViewById(R.id.hierarchy6ExtId);
        hierarchy6LabelExtIdText = (TextView) view.findViewById(R.id.hierarchy6LabelExtId);
        hierarchy6Btn.setVisibility(View.GONE);
        hierarchy6NameText.setVisibility(View.GONE);
        hierarchy6ExtIdText.setVisibility(View.GONE);
        hierarchy6LabelText.setVisibility(View.GONE);
        hierarchy6LabelExtIdText.setVisibility(View.GONE);
        hierarchy6ExtIdText.setVisibility(View.GONE);

        hierarchy7Btn = (Button) view.findViewById(R.id.hierarchy7Btn);
        hierarchy7Btn.setOnClickListener(this);
        hierarchy7NameText = (TextView) view.findViewById(R.id.hierarchy7Name);
        hierarchy7LabelText = (TextView) view.findViewById(R.id.hierarchy7Label);
        hierarchy7LabelExtIdText = (TextView) view.findViewById(R.id.hierarchy7LabelExtId);
        hierarchy7ExtIdText = (TextView) view.findViewById(R.id.hierarchy7ExtId);
        hierarchy7Btn.setVisibility(View.GONE);
        hierarchy7NameText.setVisibility(View.GONE);
        hierarchy7ExtIdText.setVisibility(View.GONE);
        hierarchy7LabelText.setVisibility(View.GONE);
        hierarchy7LabelExtIdText.setVisibility(View.GONE);
        hierarchy7ExtIdText.setVisibility(View.GONE);

        hierarchy8Btn = (Button) view.findViewById(R.id.hierarchy8Btn);
        hierarchy8Btn.setOnClickListener(this);
        hierarchy8NameText = (TextView) view.findViewById(R.id.hierarchy8Name);
        hierarchy8LabelText = (TextView) view.findViewById(R.id.hierarchy8Label);
        hierarchy8LabelExtIdText = (TextView) view.findViewById(R.id.hierarchy8LabelExtId);
        hierarchy8ExtIdText = (TextView) view.findViewById(R.id.hierarchy8ExtId);
        hierarchy8NameText.setVisibility(View.GONE);
        hierarchy8Btn.setVisibility(View.GONE);
        hierarchy8ExtIdText.setVisibility(View.GONE);
        hierarchy8LabelText.setVisibility(View.GONE);
        hierarchy8LabelExtIdText.setVisibility(View.GONE);
        hierarchy8ExtIdText.setVisibility(View.GONE);

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
        
        individualExtId = (TextView) view.findViewById(R.id.individualExtId);
        individualFirstName = (TextView) view.findViewById(R.id.individualFirstName);
        individualLastName = (TextView) view.findViewById(R.id.individualLastName);
        individualDob = (TextView) view.findViewById(R.id.individualDob);
        
        hierarchyButtons = new ArrayList<Button>();
        hierarchyButtons.add(hierarchy1Btn);
        hierarchyButtons.add(hierarchy2Btn);
        hierarchyButtons.add(hierarchy3Btn);
        hierarchyButtons.add(hierarchy4Btn);
        hierarchyButtons.add(hierarchy5Btn);
        hierarchyButtons.add(hierarchy6Btn);
        hierarchyButtons.add(hierarchy7Btn);
        hierarchyButtons.add(hierarchy8Btn);
        //hierarchyButtons.add(roundBtn);
        
        hierarchyTextViews = new ArrayList<TextView>();
        hierarchyTextViews.add(hierarchy1NameText);
        hierarchyTextViews.add(hierarchy2NameText);
        hierarchyTextViews.add(hierarchy3NameText);
        hierarchyTextViews.add(hierarchy4NameText);
        hierarchyTextViews.add(hierarchy5NameText);
        hierarchyTextViews.add(hierarchy6NameText);
        hierarchyTextViews.add(hierarchy7NameText);
        hierarchyTextViews.add(hierarchy8NameText);
        
        hierarchyLabelViews = new ArrayList<TextView>();
        hierarchyLabelViews.add(hierarchy1LabelText);
        hierarchyLabelViews.add(hierarchy2LabelText);
        hierarchyLabelViews.add(hierarchy3LabelText);
        hierarchyLabelViews.add(hierarchy4LabelText);
        hierarchyLabelViews.add(hierarchy5LabelText);
        hierarchyLabelViews.add(hierarchy6LabelText);
        hierarchyLabelViews.add(hierarchy7LabelText);
        hierarchyLabelViews.add(hierarchy8LabelText);
        
        
        hierarchyLabelExtIdViews = new ArrayList<TextView>();
        hierarchyLabelExtIdViews.add(hierarchy1LabelExtIdText);
        hierarchyLabelExtIdViews.add(hierarchy2LabelExtIdText);
        hierarchyLabelExtIdViews.add(hierarchy3LabelExtIdText);
        hierarchyLabelExtIdViews.add(hierarchy4LabelExtIdText);
        hierarchyLabelExtIdViews.add(hierarchy5LabelExtIdText);
        hierarchyLabelExtIdViews.add(hierarchy6LabelExtIdText);
        hierarchyLabelExtIdViews.add(hierarchy7LabelExtIdText);
        hierarchyLabelExtIdViews.add(hierarchy8LabelExtIdText);
        
        hierarchyExtIdViews = new ArrayList<TextView>();
        hierarchyExtIdViews.add(hierarchy1ExtIdText);
        hierarchyExtIdViews.add(hierarchy2ExtIdText);
        hierarchyExtIdViews.add(hierarchy3ExtIdText);
        hierarchyExtIdViews.add(hierarchy4ExtIdText);
        hierarchyExtIdViews.add(hierarchy5ExtIdText);
        hierarchyExtIdViews.add(hierarchy6ExtIdText);
        hierarchyExtIdViews.add(hierarchy7ExtIdText);
        hierarchyExtIdViews.add(hierarchy8ExtIdText);
        
        hierarchy1Btn.getBackground();        
        setHierarchyButtonLabels();
        
        hideIndividualInfo();
    }
    
    private void setHierarchyButtonLabels(){
        Cursor c = Queries.getAllHierarchyLevels(getActivity().getContentResolver());
        List<LocationHierarchyLevel> lhll = Converter.toLocationHierarchyLevelList(c); 
        c.close();
        
        int levelNumbers = lhll.size()-1;
        int startLevel = 1;
        for(int i = 0; i < levelNumbers; i++){
        	hierarchyButtons.get(i).setText(lhll.get(startLevel).getName());
        	hierarchyLabelViews.get(i).setText(lhll.get(startLevel).getName());
        	hierarchyButtons.get(i).setVisibility(View.VISIBLE);
        	hierarchyTextViews.get(i).setVisibility(View.VISIBLE);
        	hierarchyLabelViews.get(i).setVisibility(View.VISIBLE);
        	hierarchyLabelExtIdViews.get(i).setVisibility(View.VISIBLE);
        	hierarchyExtIdViews.get(i).setVisibility(View.VISIBLE);
        	startLevel++;
        }     	
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

    private void setHierarchy5() {
        LocationHierarchy hierarchy5 = locationVisit.getHierarchy5();
        if (hierarchy5 == null) {
        	hierarchy5 = LocationHierarchy.emptyHierarchy();
        }

        hierarchy5NameText.setText(hierarchy5.getName());
        hierarchy5ExtIdText.setText(hierarchy5.getExtId());
    }
    
    private void setHierarchy6() {
        LocationHierarchy hierarchy6 = locationVisit.getHierarchy6();
        if (hierarchy6 == null) {
        	hierarchy6 = LocationHierarchy.emptyHierarchy();
        }

        hierarchy6NameText.setText(hierarchy6.getName());
        hierarchy6ExtIdText.setText(hierarchy6.getExtId());
    }
    
    private void setHierarchy7() {
        LocationHierarchy hierarchy7 = locationVisit.getHierarchy7();
        if (hierarchy7 == null) {
        	hierarchy7 = LocationHierarchy.emptyHierarchy();
        }

        hierarchy7NameText.setText(hierarchy7.getName());
        hierarchy7ExtIdText.setText(hierarchy7.getExtId());
    }
    
    private void setHierarchy8() {
        LocationHierarchy hierarchy8 = locationVisit.getHierarchy8();
        if (hierarchy8 == null) {
        	hierarchy8 = LocationHierarchy.emptyHierarchy();
        }

        hierarchy8NameText.setText(hierarchy8.getName());
        hierarchy8ExtIdText.setText(hierarchy8.getExtId());
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
		} else if  (id == R.id.hierarchy5Btn) {
			listener.onHierarchy5();
		} else if (id == R.id.hierarchy6Btn) {
			listener.onHierarchy6();
		} else if (id == R.id.hierarchy7Btn) {
			listener.onHierarchy7();
		} else if (id == R.id.hierarchy8Btn) {
			listener.onHierarchy8();
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
        registerHierarchy5Listener(stateMachine);
        registerHierarchy6Listener(stateMachine);
        registerHierarchy7Listener(stateMachine);
        registerHierarchy8Listener(stateMachine);
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
                hierarchy5Btn.setEnabled(true);
                hierarchy6Btn.setEnabled(true);
                hierarchy7Btn.setEnabled(true);
                hierarchy8Btn.setEnabled(true);
                roundBtn.setEnabled(true);
                searchlBtn.setVisibility(8);
            }
        });
    }

    private void registerEventListener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Event", new StateListener() {
            public void onEnterState() {
                setIndividual();
                unhideIndividualInfo();
            }

            public void onExitState() {
            	hideIndividualInfo();
            }
        });
    }

    private void registerVisitListener(StateMachine stateMachine) {
        stateMachine.registerListener("Create Visit", new StateListener() {
            public void onEnterState() {
            	locationBtn.setEnabled(true);
            	locationBtn.setBackgroundColor(Color.LTGRAY);
            }

            public void onExitState() {
                hierarchy1Btn.setEnabled(false);
                hierarchy2Btn.setEnabled(false);
                hierarchy3Btn.setEnabled(false);
                hierarchy4Btn.setEnabled(false);
                hierarchy5Btn.setEnabled(false);
                hierarchy6Btn.setEnabled(false);
                hierarchy7Btn.setEnabled(false);
                hierarchy8Btn.setEnabled(false);
                roundBtn.setEnabled(false);
                locationBtn.setEnabled(false);
                locationBtn.setBackgroundColor(Color.DKGRAY);
                searchlBtn.setVisibility(8);
            }
        });
    }
    
    private void hideIndividualInfo(){
        individualBtn.setVisibility(View.GONE);
        individualBtn.setEnabled(false);
        individualBtn.setBackgroundColor(Color.DKGRAY);
        
        individualExtIdText.setVisibility(View.GONE);
        individualFirstNameText.setVisibility(View.GONE);
        individualLastNameText.setVisibility(View.GONE);
        individualDobText.setVisibility(View.GONE);
        
        individualExtId.setVisibility(View.GONE);
        individualFirstName.setVisibility(View.GONE);
        individualLastName.setVisibility(View.GONE);
        individualDob.setVisibility(View.GONE);
    }
    
    private void unhideIndividualInfo(){
        individualBtn.setVisibility(View.VISIBLE);
        individualBtn.setEnabled(true);
        individualBtn.setBackgroundColor(Color.LTGRAY);
        
        individualExtIdText.setVisibility(View.VISIBLE);
        individualFirstNameText.setVisibility(View.VISIBLE);
        individualLastNameText.setVisibility(View.VISIBLE);
        individualDobText.setVisibility(View.VISIBLE);
        
        individualExtId.setVisibility(View.VISIBLE);
        individualFirstName.setVisibility(View.VISIBLE);
        individualLastName.setVisibility(View.VISIBLE);
        individualDob.setVisibility(View.VISIBLE);
    }    

    private void registerIndividualListener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Individual", new StateListener() {
            public void onEnterState() {
                setIndividual();
//                individualBtn.setEnabled(true);
//                individualBtn.setBackgroundColor(Color.LTGRAY);
                unhideIndividualInfo();
            }

            public void onExitState() {
//                individualBtn.setEnabled(false);
//                individualBtn.setBackgroundColor(Color.DKGRAY);
            	hideIndividualInfo();
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

    private void registerHierarchy8Listener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Hierarchy 8", new StateListener() {
            public void onEnterState() {
                resetToDefaultState(10, false);
                hierarchy8Btn.setEnabled(true);
                hierarchy8Btn.setBackgroundColor(Color.LTGRAY);
            }

            public void onExitState() {
                setHierarchy8();
                hierarchy8Btn.setBackgroundColor(Color.DKGRAY);
            }
        });
    }
    
    private void registerHierarchy7Listener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Hierarchy 7", new StateListener() {
            public void onEnterState() {
                resetToDefaultState(9, false);
                hierarchy7Btn.setEnabled(true);
                hierarchy7Btn.setBackgroundColor(Color.LTGRAY);
            }

            public void onExitState() {
                setHierarchy7();
                hierarchy7Btn.setBackgroundColor(Color.DKGRAY);
            }
        });
    }
    
    private void registerHierarchy6Listener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Hierarchy 6", new StateListener() {
            public void onEnterState() {
                resetToDefaultState(8, false);
                hierarchy6Btn.setEnabled(true);
                hierarchy6Btn.setBackgroundColor(Color.LTGRAY);
            }

            public void onExitState() {
                setHierarchy6();
                hierarchy6Btn.setBackgroundColor(Color.DKGRAY);
            }
        });
    }
    
    private void registerHierarchy5Listener(StateMachine stateMachine) {
        stateMachine.registerListener("Select Hierarchy 5", new StateListener() {
            public void onEnterState() {
                resetToDefaultState(7, false);
                hierarchy5Btn.setEnabled(true);
                hierarchy5Btn.setBackgroundColor(Color.LTGRAY);
            }

            public void onExitState() {
                setHierarchy5();
                hierarchy5Btn.setBackgroundColor(Color.DKGRAY);
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
        case 7:
            hierarchy5Btn.setEnabled(enabled);
            searchlBtn.setVisibility(8);
            setHierarchy5();
        case 8:
            hierarchy6Btn.setEnabled(enabled);
            setHierarchy6();
        case 9:
            hierarchy7Btn.setEnabled(enabled);
            setHierarchy7();
        case 10:
            hierarchy8Btn.setEnabled(enabled);
            setHierarchy8();
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
		setHierarchy5();
		setHierarchy6();
		setHierarchy7();
		setHierarchy8();
		setRound();
		setLocation();
		setIndividual();
	}
}

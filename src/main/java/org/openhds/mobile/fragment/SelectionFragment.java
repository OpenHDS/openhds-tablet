package org.openhds.mobile.fragment;

import org.openhds.mobile.R;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.LocationVisit;
import org.openhds.mobile.model.Round;
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
import android.widget.TextView;

public class SelectionFragment extends Fragment implements OnClickListener {

    public static interface Listener {
        void onRegion();

        void onSubRegion();

        void onVillage();

        void onLocation();

        void onRound();

        void onIndividual();
    }

    private Listener listener;
    private LocationVisit locationVisit;

    private Button regionBtn, subRegionBtn, villageBtn, locationBtn, roundBtn, individualBtn;

    private TextView loginGreetingText, regionNameText, regionExtIdText, subRegionNameText, subRegionExtIdText,
            villageNameText, villageExtIdText, roundNumberText, roundStartDateText, roundEndDateText, locationNameText,
            locationExtIdText, locationLatitudeText, locationLongitudeText, individualFirstNameText,
            individualLastNameText, individualExtIdText, individualDobText;

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

        regionBtn = (Button) view.findViewById(R.id.regionBtn);
        regionBtn.setOnClickListener(this);
        regionNameText = (TextView) view.findViewById(R.id.regionNameText);
        regionExtIdText = (TextView) view.findViewById(R.id.regionExtIdText);

        subRegionBtn = (Button) view.findViewById(R.id.subRegionBtn);
        subRegionBtn.setOnClickListener(this);
        subRegionNameText = (TextView) view.findViewById(R.id.subRegionNameText);
        subRegionExtIdText = (TextView) view.findViewById(R.id.subRegionExtIdText);

        villageBtn = (Button) view.findViewById(R.id.villageBtn);
        villageBtn.setOnClickListener(this);
        villageNameText = (TextView) view.findViewById(R.id.villageNameText);
        villageExtIdText = (TextView) view.findViewById(R.id.villageExtIdText);

        locationBtn = (Button) view.findViewById(R.id.locationBtn);
        locationBtn.setOnClickListener(this);
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
    }

    private void setRegion() {
        LocationHierarchy region = locationVisit.getRegion();
        if (region == null) {
            region = LocationHierarchy.emptyHierarchy();
        }

        regionNameText.setText(region.getName());
        regionExtIdText.setText(region.getExtId());
    }

    private void setSubRegion() {
        LocationHierarchy subRegion = locationVisit.getSubRegion();
        if (subRegion == null) {
            subRegion = LocationHierarchy.emptyHierarchy();
        }

        subRegionNameText.setText(subRegion.getName());
        subRegionExtIdText.setText(subRegion.getExtId());
    }

    private void setVillage() {
        LocationHierarchy village = locationVisit.getVillage();
        if (village == null) {
            village = LocationHierarchy.emptyHierarchy();
        }

        villageNameText.setText(village.getName());
        villageExtIdText.setText(village.getExtId());
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
        switch (view.getId()) {
        case R.id.regionBtn:
            listener.onRegion();
            break;
        case R.id.subRegionBtn:
            listener.onSubRegion();
            break;
        case R.id.villageBtn:
            listener.onVillage();
            break;
        case R.id.locationBtn:
            listener.onLocation();
            break;
        case R.id.roundBtn:
            listener.onRound();
            break;
        case R.id.individualBtn:
            listener.onIndividual();
            break;
        }
    }

    public void setLocationVisit(LocationVisit locationVisit) {
        this.locationVisit = locationVisit;
        loginGreetingText.setText("Hello, " + locationVisit.getFieldWorker().getFirstName() + " "
                + locationVisit.getFieldWorker().getLastName());
    }

    public void registerTransitions(StateMachine stateMachine) {
        registerRegionListener(stateMachine);
        registerSubregionListener(stateMachine);
        registerVillageListener(stateMachine);
        registerRoundListener(stateMachine);
        registerLocationListener(stateMachine);
        registerVisitListener(stateMachine);
        registerIndividualListener(stateMachine);
        registerEventListener(stateMachine);
        registerFinishVisitListener(stateMachine);
    }

    private void registerFinishVisitListener(StateMachine stateMachine) {
        stateMachine.registerListener(State.FINISH_VISIT, new StateListener() {
            public void onEnterState() {
            }

            public void onLeaveState() {
                regionBtn.setEnabled(true);
                subRegionBtn.setEnabled(true);
                villageBtn.setEnabled(true);
                roundBtn.setEnabled(true);
            }
        });
    }

    private void registerEventListener(StateMachine stateMachine) {
        stateMachine.registerListener(State.SELECT_EVENT, new StateListener() {
            public void onEnterState() {
                setIndividual();
            }

            public void onLeaveState() {
            }
        });
    }

    private void registerVisitListener(StateMachine stateMachine) {
        stateMachine.registerListener(State.CREATE_VISIT, new StateListener() {
            public void onEnterState() {
            }

            public void onLeaveState() {
                regionBtn.setEnabled(false);
                subRegionBtn.setEnabled(false);
                villageBtn.setEnabled(false);
                roundBtn.setEnabled(false);
                locationBtn.setEnabled(false);
            }
        });
    }

    private void registerIndividualListener(StateMachine stateMachine) {
        stateMachine.registerListener(State.SELECT_INDIVIDUAL, new StateListener() {
            public void onEnterState() {
                setIndividual();
                individualBtn.setEnabled(true);
            }

            public void onLeaveState() {
                individualBtn.setEnabled(false);
            }
        });
    }

    private void registerLocationListener(StateMachine stateMachine) {
        stateMachine.registerListener(State.SELECT_LOCATION, new StateListener() {
            public void onEnterState() {
                resetToDefaultState(4, false);
                locationBtn.setEnabled(true);
            }

            public void onLeaveState() {
                setLocation();
            }
        });
    }

    private void registerRoundListener(StateMachine stateMachine) {
        stateMachine.registerListener(State.SELECT_ROUND, new StateListener() {
            public void onEnterState() {
                resetToDefaultState(3, false);
                roundBtn.setEnabled(true);

            }

            public void onLeaveState() {
                setRound();
            }
        });
    }

    private void registerVillageListener(StateMachine stateMachine) {
        stateMachine.registerListener(State.SELECT_VILLAGE, new StateListener() {
            public void onEnterState() {
                resetToDefaultState(2, false);
                villageBtn.setEnabled(true);
            }

            public void onLeaveState() {
                setVillage();
            }
        });
    }

    private void registerSubregionListener(StateMachine stateMachine) {
        stateMachine.registerListener(State.SELECT_SUBREGION, new StateListener() {
            public void onEnterState() {
                resetToDefaultState(1, false);
                subRegionBtn.setEnabled(true);
            }

            public void onLeaveState() {
                setSubRegion();
            }
        });
    }

    private void registerRegionListener(StateMachine stateMachine) {
        stateMachine.registerListener(State.SELECT_REGION, new StateListener() {
            public void onEnterState() {
                resetToDefaultState(0, false);
                regionBtn.setEnabled(true);
            }

            public void onLeaveState() {
                setRegion();
            }
        });
    }

    private void resetToDefaultState(int level, boolean enabled) {
        switch (level) {
        case 0:
            regionBtn.setEnabled(enabled);
            setRegion();
        case 1:
            subRegionBtn.setEnabled(enabled);
            setSubRegion();
        case 2:
            villageBtn.setEnabled(enabled);
            setVillage();
        case 3:
            roundBtn.setEnabled(enabled);
            setRound();
        case 4:
            locationBtn.setEnabled(enabled);
            setLocation();
        case 5:
            individualBtn.setEnabled(enabled);
            setIndividual();
        }
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
}

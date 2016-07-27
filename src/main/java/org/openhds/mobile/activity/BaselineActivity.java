package org.openhds.mobile.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;

import org.openhds.mobile.FormsProviderAPI;
import org.openhds.mobile.InstanceProviderAPI;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.database.BaselineUpdate;
import org.openhds.mobile.database.DeathUpdate;
import org.openhds.mobile.database.HouseholdUpdate;
import org.openhds.mobile.database.InternalInMigrationUpdate;
import org.openhds.mobile.database.LocationUpdate;
import org.openhds.mobile.database.MembershipUpdate;
import org.openhds.mobile.database.OutMigrationUpdate;
import org.openhds.mobile.database.PregnancyObservationUpdate;
import org.openhds.mobile.database.PregnancyOutcomeUpdate;
import org.openhds.mobile.database.RelationshipUpdate;
import org.openhds.mobile.database.Updatable;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.fragment.EventFragment;
import org.openhds.mobile.fragment.ProgressFragment;
import org.openhds.mobile.fragment.SelectionFragment;
import org.openhds.mobile.fragment.ValueFragment;
import org.openhds.mobile.listener.OdkFormLoadListener;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.FilledForm;
import org.openhds.mobile.model.Form;
import org.openhds.mobile.model.FormFiller;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.LocationHierarchyLevel;
import org.openhds.mobile.model.LocationVisit;
import org.openhds.mobile.model.PregnancyOutcome;
import org.openhds.mobile.model.Round;
import org.openhds.mobile.model.Settings;
import org.openhds.mobile.model.SocialGroup;
import org.openhds.mobile.model.StateMachine;
import org.openhds.mobile.task.OdkGeneratedFormLoadTask;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * UpdateActivity mediates the interaction between the 3 column fragments. The
 * buttons in the left most column drive a state machine while the user
 * interacts with the application.
 */
public class BaselineActivity extends Activity implements ValueFragment.ValueListener, LoaderCallbacks<Cursor>,
EventFragment.Listener, SelectionFragment.Listener, ValueFragment.OnlyOneEntryListener, DialogInterface.OnCancelListener {

    private SelectionFragment sf;
    private ValueFragment vf;
    private EventFragment ef;
    private ProgressFragment progressFragment;
    private MenuItem  menuItemForm;

    // loader ids
    private static final int SOCIAL_GROUP_AT_LOCATION = 0;
    private static final int SOCIAL_GROUP_FOR_INDIVIDUAL = 10;
    private static final int SOCIAL_GROUP_FOR_EXT_INMIGRATION = 20;
    
    // activity request codes for onActivityResult
    private static final int SELECTED_XFORM = 1;
    private static final int CREATE_LOCATION = 10;
    private static final int FILTER_RELATIONSHIP = 20;
    private static final int FILTER_LOCATION = 30;
    private static final int FILTER_FORM = 35;
    private static final int FILTER_INMIGRATION = 40;
    private static final int FILTER_BIRTH_FATHER = 45;
    protected static final int FILTER_INMIGRATION_MOTHER = 60;
    protected static final int FILTER_INMIGRATION_FATHER = 70;
    protected static final int FILTER_INDIV_VISIT = 75;
    protected static final int FILTER_SOCIALGROUP = 80;
    
    private static int MINIMUM_HOUSEHOLD_AGE;
    private static final int DEFAULT_MINIMUM_HOUSEHOLD_AGE = 14;
    
    private static String VISIT_LEVEL;
    private static final String DEFAULT_VISIT_LEVEL = "location";
    
    // the uri of the last viewed xform
    private Uri contentUri;

    // status flags indicating a dialog, used for restoring the activity
    private boolean formUnFinished = false;
    private boolean xFormNotFound = false;

    private AlertDialog householdDialog;

    private final FormFiller formFiller = new FormFiller();
    private StateMachine stateMachine;
    
    private LocationVisit locationVisit = new LocationVisit();
    private FilledForm filledForm;
    private AlertDialog xformUnfinishedDialog;
    private boolean showingProgress;
    private Updatable updatable;
    private boolean extInm;
    private int levelNumbers;
    private String parentExtId;
    private boolean hhCreation;
    private String jrFormId;
    private boolean newIndividual;
    private boolean outMigration;
    
    //State machine states  
	public static final String SELECT_HIERARCHY_1 = "Select Hierarchy 1";
	public static final String SELECT_HIERARCHY_2 = "Select Hierarchy 2";
	public static final String SELECT_HIERARCHY_3 = "Select Hierarchy 3";
	public static final String SELECT_HIERARCHY_4 = "Select Hierarchy 4";
	public static final String SELECT_HIERARCHY_5 = "Select Hierarchy 5";
	public static final String SELECT_HIERARCHY_6 = "Select Hierarchy 6";
	public static final String SELECT_HIERARCHY_7 = "Select Hierarchy 7";
	public static final String SELECT_HIERARCHY_8 = "Select Hierarchy 8";
	public static final String SELECT_ROUND = "Select Round";
	public static final String SELECT_LOCATION = "Select Location";
	public static final String CREATE_VISIT = "Create Visit";
	public static final String SELECT_INDIVIDUAL = "Select Individual";
	public static final String SELECT_EVENT = "Select Event";
	public static final String FINISH_VISIT = "Finish Visit";
	public static final String INMIGRATION = "Inmigration";
	
	private static final List<String> stateSequence = new ArrayList<String>();
//	private static final Map<String, Integer> stateLabels = new HashMap<String, Integer>();
	static {
		stateSequence.add(SELECT_HIERARCHY_1);
		stateSequence.add(SELECT_HIERARCHY_2);
		stateSequence.add(SELECT_HIERARCHY_3);
		stateSequence.add(SELECT_HIERARCHY_4);
		stateSequence.add(SELECT_HIERARCHY_5);
		stateSequence.add(SELECT_HIERARCHY_6);
		stateSequence.add(SELECT_HIERARCHY_7);
		stateSequence.add(SELECT_HIERARCHY_8);
		stateSequence.add(SELECT_ROUND);
		stateSequence.add(SELECT_LOCATION);
		stateSequence.add(CREATE_VISIT);
		stateSequence.add(SELECT_INDIVIDUAL);
		stateSequence.add(SELECT_EVENT);
		stateSequence.add(FINISH_VISIT);
		stateSequence.add(INMIGRATION);


	}    
	


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

      	 Cursor curs = Queries.getAllHierarchyLevels(getContentResolver());
         List<LocationHierarchyLevel> lhll = Converter.toLocationHierarchyLevelList(curs); 
         curs.close();
         
         levelNumbers = lhll.size()-1;
        setContentView(R.layout.main);
        this.setTitle("Baseline");
        
        FieldWorker fw = (FieldWorker) getIntent().getExtras().getSerializable("fieldWorker");
        locationVisit.setFieldWorker(fw);

        vf = new ValueFragment();
        vf.addOnlyOneEntryListener(this);
        
        Cursor startCursor = Queries.getStartHierarchyLevel(getContentResolver(), "2");
        if (startCursor.moveToNext()) {
        	vf.setSTART_HIERARCHY_LEVEL_NAME(startCursor.getString(startCursor.getColumnIndex(OpenHDS.HierarchyLevels.COLUMN_LEVEL_NAME)));
        }
        startCursor.close();
        FragmentTransaction txn = getFragmentManager().beginTransaction();
        txn.add(R.id.middle_col, vf).commit();

        sf = (SelectionFragment) getFragmentManager().findFragmentById(R.id.selectionFragment);
        ef = (EventFragment) getFragmentManager().findFragmentById(R.id.eventFragment);
        //Turn on baseline
        ef.setBaseLine();
        
        ActionBar actionBar = getActionBar();
        actionBar.show();        
        
        if(savedInstanceState == null){
            //Create state machine
            stateMachine = new StateMachine(new LinkedHashSet<String>(stateSequence), stateSequence.get(0)); //Pass in LinkedHashSet instead of normal HashSet to preserve  
            
            registerTransitions();
	        sf.setLocationVisit(locationVisit);
	        ef.setLocationVisit(locationVisit);   
	        
	        String state = "Select Hierarchy 1";
	        stateMachine.transitionInSequence(state);
        }
        else{
        	String state = (String)savedInstanceState.getSerializable("currentState");
        	stateMachine = new StateMachine(new LinkedHashSet<String>(stateSequence), stateSequence.get(0));
        	
            locationVisit = (LocationVisit) savedInstanceState.getSerializable("locationvisit");

            String uri = savedInstanceState.getString("uri");
            if (uri != null)
                contentUri = Uri.parse(uri);

            if (savedInstanceState.getBoolean("xFormNotFound"))
                createXFormNotFoundDialog();
            if (savedInstanceState.getBoolean("unfinishedFormDialog"))
                createUnfinishedFormDialog();

            registerTransitions();
            sf.setLocationVisit(locationVisit);
            ef.setLocationVisit(locationVisit);
            
            //Restore last state
            stateMachine.transitionInSequence(state);
        	
        }

            Cursor c = Queries.getAllSettings(getContentResolver());
     		Settings settings = Converter.convertToSettings(c); 
     		c.close();
     		MINIMUM_HOUSEHOLD_AGE = settings.getMinimumAgeOfHouseholdHead() == 0 ? DEFAULT_MINIMUM_HOUSEHOLD_AGE : settings.getMinimumAgeOfHouseholdHead();
     		VISIT_LEVEL = settings.getVisitLevel()==null ? DEFAULT_VISIT_LEVEL : settings.getVisitLevel();
     		locationVisit.setVisitLevel(VISIT_LEVEL);
    }
    
    /**
     * At any given point in time, the screen can be rotated. This method is
     * responsible for saving the screen state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("locationvisit", locationVisit);
        outState.putString("currentState", stateMachine.getState().toString());
        outState.putBoolean("unfinishedFormDialog", formUnFinished);
        outState.putBoolean("xFormNotFound", xFormNotFound);

        if (contentUri != null)
            outState.putString("uri", contentUri.toString());
    }

   
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    }

    /**
     * The main menu, showing multiple options
     */
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.formmenu, menu);
        this.menuItemForm = menu.getItem(0);
        menu.getItem(0).setVisible(false);
        super.onCreateOptionsMenu(menu);
        return true;
	}

    /**
     * Defining what happens when a main menu item is selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.extra_forms) {
			createFormMenu();
			return true;
		} else if (itemId == R.id.sync_database) {
			createSyncDatabaseMenu();
			return true;
		}
        return super.onOptionsItemSelected(item);
    }
    
    @Override
	public void onResume()
	{
	    super.onResume();
	    hideProgressFragment();
	    restoreState();
	} 
    
    private void restoreState(){
	    if(stateMachine != null && stateMachine.getState() != ""){
	    	String currentState = stateMachine.getState();
	    	stateMachine.transitionTo(currentState);
	    }
    }
    
    /**
     * Display dialog when user clicks on back button
     */    
	@Override
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
	    		.setTitle(getString(R.string.exit_confirmation_title))
	           .setMessage(getString(R.string.exiting_lbl))
	           .setCancelable(false)
	           .setPositiveButton(getString(R.string.yes_lbl), new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   try{
	                    BaselineActivity.this.finish();
	            	   }
	            	   catch(Exception e){}
	               }
	           })
	           .setNegativeButton(getString(R.string.no_lbl), null)
	           .show();
	}

    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	ContentResolver resolver = getContentResolver();
    	Cursor cursor = null;
        switch (requestCode) {
        case SELECTED_XFORM:
        	handleXformResult(resultCode, data);
            break;
        case FILTER_FORM:
        	 if (resultCode != RESULT_OK) {
                 return;
             }
        	Form form =(Form) data.getExtras().getSerializable("form");
        	SocialGroup sg = null;
        	cursor = Queries.getSocialGroupsByIndividualExtId(resolver,locationVisit.getSelectedIndividual().getExtId());
        	if (cursor.moveToFirst()) {
        	sg = Converter.convertToSocialGroup(cursor);
        	locationVisit.getLocation().setHead(sg.getGroupHead());
        	}
        	filledForm = formFiller.fillExtraForm(locationVisit, form.getName(), sg);
        	cursor.close();
        	loadForm(SELECTED_XFORM);
        	break;
        case FILTER_BIRTH_FATHER:
            handleFatherBirthResult(resultCode, data);
            break;
        case CREATE_LOCATION:
            handleLocationCreateResult(resultCode, data);
            break;
        case FILTER_RELATIONSHIP:
            handleFilterRelationshipResult(resultCode, data);
            break;
        case FILTER_LOCATION:
            if (resultCode != RESULT_OK) {
                return;
            }
        	Location location1 = (Location) data.getExtras().getSerializable("location");
        	locationVisit.setLocation(location1);
        	vf.onLoaderReset(null);
            transitionToCreateVisit();
            break;
        case FILTER_SOCIALGROUP:
            if (resultCode != RESULT_OK) {
                return;
            }
        	SocialGroup socialGroup = (SocialGroup) data.getExtras().getSerializable("socialGroup");
        	vf.onLoaderReset(null);
            filledForm = formFiller.appendSocialGroup(socialGroup, filledForm);
            loadForm(SELECTED_XFORM);
            break;            
        case FILTER_INMIGRATION:
            handleFilterInMigrationResult(resultCode, data);
            break;
        case FILTER_INMIGRATION_MOTHER:
            handleFilterMother(resultCode, data);
            break;
        case FILTER_INDIV_VISIT:
            handleFilterIndivVisit(resultCode, data);
            break;
        case FILTER_INMIGRATION_FATHER:
            handleFilterFather(resultCode, data);
            break;
        }
    }
    
    private void transitionToCreateVisit(){
    	stateMachine.transitionTo("Create Visit"); 	
    	onCreateVisit();
    }

    private void handleFilterIndivVisit(int resultCode, Intent data) { 	
    		if (resultCode != RESULT_OK) {
                return;
            }

            if (locationVisit.getVisitLevel()==null) {
            	locationVisit.setVisitLevel(VISIT_LEVEL);
            }
            
            Individual individual = (Individual) data.getExtras().getSerializable("individual");
            if (individual!=null) 
            filledForm.setIntervieweeId(individual.getExtId());	
            loadForm(SELECTED_XFORM);
	}

	private void handleFatherBirthResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        Individual individual = (Individual) data.getExtras().getSerializable("individual");
        new CreatePregnancyOutcomeTask(individual).execute();
    }

    private void handleLocationCreateResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            showProgressFragment();
            new CheckLocationFormStatus(getContentResolver(), contentUri).execute();
        } else {
            Toast.makeText(this, getString(R.string.odk_problem_lbl), Toast.LENGTH_LONG).show();
        }
        
        if (locationVisit.getVisitLevel()==null) {
        	locationVisit.setVisitLevel(VISIT_LEVEL);
        }
    }

    /**
     * This differs from {@link BaselineActivity.CheckFormStatus} in that, upon
     * creating a new location, the user is automatically forwarded to creating
     * a visit. This happens because the user could in theory create a location,
     * and then skip the visit.
     */
    class CheckLocationFormStatus extends AsyncTask<Void, Void, Boolean> {

        private ContentResolver resolver;
        private Uri contentUri;

        public CheckLocationFormStatus(ContentResolver resolver, Uri contentUri) {
            this.resolver = resolver;
            this.contentUri = contentUri;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Cursor cursor = resolver.query(contentUri, new String[] { InstanceProviderAPI.InstanceColumns.STATUS,
                    InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH },
                    InstanceProviderAPI.InstanceColumns.STATUS + "=?",
                    new String[] { InstanceProviderAPI.STATUS_COMPLETE }, null);
            if (cursor.moveToNext()) {
                String filepath = cursor.getString(cursor
                        .getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                LocationUpdate update = new LocationUpdate();
                update.updateDatabase(resolver, filepath, jrFormId);
                cursor.close();
                return true;
            } else {
                cursor.close();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            hideProgressFragment();

            if (result) {
            	//Handle new Location, load list and select first entry
            	String locationExtId = locationVisit.getLocation().getExtId();
            	if(locationExtId.length() > 0){
            		vf.loadFilteredLocationById(locationExtId);
            		vf.selectItemNoInList(0);
            	}
            } else {
                createUnfinishedFormDialog();
                
                //Reset location and display text after cancellation
                locationVisit.setLocation(null);
                sf.setAll();
            }
        }
    }

    private void handleFilterInMigrationResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        showProgressFragment();
        Individual individual = (Individual) data.getExtras().getSerializable("individual");
        //extInm= false;


        new CreateInternalInMigrationTask(individual).execute();
        locationVisit.setSelectedIndividual(individual);

        stateMachine.transitionTo("Inmigration");
        
    }

    private class CreateInternalInMigrationTask extends AsyncTask<Void, Void, Void> {

        private Individual individual;
        
        public CreateInternalInMigrationTask(Individual individual) {
            this.individual = individual;
        }

        @Override
        protected Void doInBackground(Void... params) {
            filledForm = formFiller.fillInternalInMigrationForm(locationVisit, individual);
            updatable = new InternalInMigrationUpdate();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            hideProgressFragment();
            loadForm(SELECTED_XFORM);
        }
    }

    
    private void handleFilterRelationshipResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        Individual individual = (Individual) data.getExtras().getSerializable("individual");
        filledForm.setIndividualB(individual.getExtId());

        loadForm(SELECTED_XFORM);
    }

    private void handleXformResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            showProgressFragment();
            new CheckFormStatus(getContentResolver(), contentUri).execute();
        } else {
            Toast.makeText(this, getString(R.string.odk_problem_lbl), Toast.LENGTH_LONG).show();
        }
    }

    private void handleFilterFather(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        Individual individual = (Individual) data.getExtras().getSerializable("individual");
        filledForm.setFatherExtId(individual.getExtId());
        filledForm.setIndividualLastName(individual.getLastName());
        filledForm.setIndividualMiddleName(individual.getFirstName());
        loadForm(SELECTED_XFORM);
    }

    private void handleFilterMother(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        Individual individual = (Individual) data.getExtras().getSerializable("individual");
        filledForm.setMotherExtId(individual.getExtId());

        buildFatherDialog();
    }

    private void showProgressFragment() {
        if (showingProgress) {
            return;
        }

        if (progressFragment == null) {
            progressFragment = ProgressFragment.newInstance();
        }

        showingProgress = true;
        FragmentTransaction txn = getFragmentManager().beginTransaction();
        txn.remove(progressFragment);
        txn.add(R.id.middle_col, progressFragment).commit();
    }

    void hideProgressFragment() {
        if (!showingProgress) {
            return;
        }

        showingProgress = false;
        FragmentTransaction txn = getFragmentManager().beginTransaction();
        
        if(progressFragment != null){
        	txn.remove(progressFragment).commitAllowingStateLoss();
        	progressFragment = null;
        }
        
        if (!vf.isAdded()) {
        	txn.add(R.id.middle_col, vf).commitAllowingStateLoss();
        } else {
        	txn.show(vf);
        }
        
    }

    /**
     * AsyncTask that attempts to get the status of the form that the user just
     * filled out. In ODK, when a form is saved and marked as complete, its
     * status is set to {@link InstanceProviderAPI.STATUS_COMPLETE}. If the user
     * leaves the form in ODK before saving it, the status will not be set to
     * complete. Alternatively, the user could save the form, but not mark it as
     * complete. Since there is no way to tell the difference between the user
     * leaving the form without completing, or saving without marking as
     * complete, we enforce that the form be marked as complete before the user
     * can continue with update events. They have 2 options: go back to the form
     * and save it as complete, or delete the previously filled form.
     */
    class CheckFormStatus extends AsyncTask<Void, Void, Boolean> {

        private ContentResolver resolver;
        private Uri contentUri;

        public CheckFormStatus(ContentResolver resolver, Uri contentUri) {
            this.resolver = resolver;
            this.contentUri = contentUri;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Cursor cursor = resolver.query(contentUri, new String[] { InstanceProviderAPI.InstanceColumns.STATUS,
                    InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH },
                    InstanceProviderAPI.InstanceColumns.STATUS + "=?",
                    new String[] { InstanceProviderAPI.STATUS_COMPLETE }, null);
            if (cursor.moveToNext()) {
                String filepath = cursor.getString(cursor
                        .getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
                if(updatable != null){
                	updatable.updateDatabase(getContentResolver(), filepath, jrFormId);
                	updatable = null;
                }
                cursor.close();
                return true;
            } else {
                cursor.close();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            hideProgressFragment();

            if (result) {
            	if (stateMachine.getState()=="Inmigration") {
            		stateMachine.transitionTo("Select Event");
            		if (extInm)
                		onFinishExternalInmigration();
            	} else if (stateMachine.getState()=="Select Individual") {
            		if (extInm)
                		onFinishExternalInmigration();
            		//Select newly created indiv.
                    selectIndividual();
            	}
            	else if(stateMachine.getState() == "Select Event"){
            		if(hhCreation){
            			onFinishedHouseHoldCreation();
            		}
            		else if(outMigration){
            			onClearIndividual();
            		}
            	}
            	else {
            		stateMachine.transitionTo("Select Individual");
            	}
            } else {
                createUnfinishedFormDialog();
            }
        }
    }
    
	private void onFinishedHouseHoldCreation() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.baseline_lbl));
        alertDialogBuilder.setOnCancelListener(this);
        alertDialogBuilder.setMessage(getString(R.string.finish_household_creation_msg));
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {			
        				public void onClick(DialogInterface dialog, int which) {
        					if(newIndividual){
        			        	onMembership();
        			        	newIndividual = false;
        			        }	
        				}
        			});        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();		
                
        hhCreation = false;
	}    

    /**
     * Creates the 'Configure Server' option in the action menu.
     */
    private void createFormMenu() {
        Intent i = new Intent(this, FilterFormActivity.class);
        i.putExtra("location", locationVisit);
        startActivityForResult(i, FILTER_FORM);
    }

    /**
     * Creates the 'Sync Database' option in the action menu.
     */
    private void createSyncDatabaseMenu() {
        //Intent i = new Intent(this, SyncDatabaseActivity.class);
        //startActivity(i);
    }

    /**
     * Method used for starting the activity for filtering for individuals
     */
    private void startFilterActivity(int requestCode) {
    	Intent i =null;
    	if (requestCode==FILTER_INDIV_VISIT) {
            i = new Intent(this, FilterVisitActivity.class);
    	} 
    	else if(requestCode == FILTER_SOCIALGROUP){
    		i = new Intent(this, FilterSocialGroupActivity.class);
    	}
    	else {
    		i = new Intent(this, FilterActivity.class);
    	}
    	

        i.putExtra("hierarchy1", locationVisit.getHierarchy1());
        i.putExtra("hierarchy2", locationVisit.getHierarchy2());
        i.putExtra("hierarchy3", locationVisit.getHierarchy3());
        i.putExtra("hierarchy4", locationVisit.getHierarchy4());
        i.putExtra("hierarchy5", locationVisit.getHierarchy5());
        i.putExtra("hierarchy6", locationVisit.getHierarchy6());
        i.putExtra("hierarchy7", locationVisit.getHierarchy7());
        i.putExtra("hierarchy8", locationVisit.getHierarchy8());
        

        Location loc = locationVisit.getLocation();
        if (loc == null) {
            loc = Location.emptyLocation();
        }
        i.putExtra("location", loc);

        switch (requestCode) {
        case FILTER_INMIGRATION_MOTHER:
            i.putExtra("requireGender", "F");
            break;
        case FILTER_INMIGRATION_FATHER:
            i.putExtra("requireGender", "M");
            break;
        case FILTER_BIRTH_FATHER:
            i.putExtra("requireGender", "M");
        case FILTER_INMIGRATION:
            i.putExtra("img", "IMG");
        }

        startActivityForResult(i, requestCode);
    }
    
    
    /**
     * Method used for starting the activity for filtering for Locations
     */
    private void startFilterLocActivity(int requestCode) {
        Intent i = new Intent(this, FilterLocationActivity.class);
        i.putExtra("hierarchy1", locationVisit.getHierarchy1());
        i.putExtra("hierarchy2", locationVisit.getHierarchy2());
        i.putExtra("hierarchy3", locationVisit.getHierarchy3());
        i.putExtra("hierarchy4", locationVisit.getHierarchy4());
        i.putExtra("hierarchy5", locationVisit.getHierarchy5());
        i.putExtra("hierarchy6", locationVisit.getHierarchy6());
        i.putExtra("hierarchy7", locationVisit.getHierarchy7());
        i.putExtra("hierarchy8", locationVisit.getHierarchy8());

        Location loc = locationVisit.getLocation();
        if (loc == null) {
            loc = Location.emptyLocation();
        }
        i.putExtra("location", loc);


        startActivityForResult(i, requestCode);
    }    
    
    
    
    

    private void loadHierarchy1ValueData() {
        vf.loadLocationHierarchy();
    }

    private void loadHierarchy2ValueData() {
        vf.loadHierarchy2(locationVisit.getHierarchy1().getExtId());
    }
    
    private void loadHierarchy3ValueData() {
        vf.loadHierarchy3(locationVisit.getHierarchy2().getExtId());
    }

    private void loadHierarchy4ValueData() {
        vf.loadHierarchy4(locationVisit.getHierarchy3().getExtId());
    }

    private void loadHierarchy5ValueData() {
        vf.loadHierarchy5(locationVisit.getHierarchy4().getExtId());
    }
    
    private void loadHierarchy6ValueData() {
        vf.loadHierarchy6(locationVisit.getHierarchy5().getExtId());
    }
    
    private void loadHierarchy7ValueData() {
        vf.loadHierarchy7(locationVisit.getHierarchy6().getExtId());
    }
    
    private void loadHierarchy8ValueData() {
        vf.loadHierarchy8(locationVisit.getHierarchy7().getExtId());
    }
    
    private void loadLocationValueData() {
        vf.loadLocations(parentExtId);
    }


    private void loadIndividualValueData() {
        vf.loadIndividuals(locationVisit,VISIT_LEVEL);
    }

    private void createUnfinishedFormDialog() {
        formUnFinished = true;
        if (xformUnfinishedDialog == null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getString(R.string.warning_lbl));
            alertDialogBuilder.setMessage(getString(R.string.update_unfinish_msg1));
            alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.setOnCancelListener(this);
            alertDialogBuilder.setPositiveButton(getString(R.string.update_unfinish_pos_button), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    formUnFinished = false;
                    xformUnfinishedDialog.hide();
                    getContentResolver().delete(contentUri, InstanceProviderAPI.InstanceColumns.STATUS + "=?",
                            new String[] { InstanceProviderAPI.STATUS_INCOMPLETE });
                }
            });
            alertDialogBuilder.setNegativeButton(getString(R.string.update_unfinish_neg_button), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    formUnFinished = false;
                    xformUnfinishedDialog.hide();
                    startActivityForResult(new Intent(Intent.ACTION_EDIT, contentUri), SELECTED_XFORM);
                }
            });
            xformUnfinishedDialog = alertDialogBuilder.create();
        }

        xformUnfinishedDialog.show();
    }

    private void createXFormNotFoundDialog() {
        xFormNotFound = true;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
          alertDialogBuilder.setTitle(getString(R.string.warning_lbl));
          alertDialogBuilder.setOnCancelListener(this);
          alertDialogBuilder.setMessage(getString(R.string.update_xform_not_found_msg));
          alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                xFormNotFound = false;
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void onCreateLocation() {
        showProgressFragment();
        new GenerateLocationTask().execute();
    }

    private class GenerateLocationTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            locationVisit.createLocation(getContentResolver());
            filledForm = formFiller.fillLocationForm(locationVisit);
            updatable = new LocationUpdate();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            hideProgressFragment();
            loadForm(CREATE_LOCATION);
        }

    }

    public void onCreateVisit() {
    	locationVisit.createVisit(getContentResolver());
    	
    	stateMachine.transitionTo("Select Individual");
    }



    public void onFinishVisit() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.visit_lbl));
        alertDialogBuilder.setMessage(getString(R.string.update_finish_visit_msg));
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setOnCancelListener(this);
        alertDialogBuilder.setPositiveButton(getString(R.string.yes_lbl), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	if(menuItemForm != null) {
                  	menuItemForm.setVisible(false);
                 }    
            	locationVisit = locationVisit.completeVisit();
                sf.setLocationVisit(locationVisit);
                ef.setLocationVisit(locationVisit);
                stateMachine.transitionTo("Finish Visit");
                stateMachine.transitionTo("Select Location");
                vf.onLoaderReset(null);
                }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel_lbl), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				reloadState();
			}
		});
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void onHousehold() {
        showProgressFragment();
        new CreateSocialGroupTask().execute();
    }

    private class CreateSocialGroupTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            SocialGroup sg = locationVisit.createSocialGroup(getContentResolver());
            if (sg==null){
            	//this.cancel(true);
            	//hideProgressFragment();
            	//onSGexists();
            } else {
            	filledForm = formFiller.fillSocialGroupForm(locationVisit, sg);
            	updatable = new HouseholdUpdate();
            }
            
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        	hideProgressFragment();
        	 SocialGroup sg = locationVisit.createSocialGroup(getContentResolver());
             if (sg==null){            	
            	onSGexists();
            	this.cancel(true);  	
             } else {
                 //Check if selected individual meets required age to be the Head of a SG. If not, display a msg and don't proceed.
                 Individual individual = locationVisit.getSelectedIndividual();
                 boolean meetsMinimumAge = individualMeetsMinimumAge(individual);
                 if(meetsMinimumAge){
                 	hhCreation = true;
                 	loadForm(SELECTED_XFORM);
                 }
                 else{
                 	Toast.makeText(BaselineActivity.this, getString(R.string.younger_than_required_age_for_hoh), Toast.LENGTH_LONG).show();
                 }
             }
        }
    }
    
    public void onSGexists() {
    	 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
         alertDialogBuilder.setTitle(getString(R.string.socialgroup_lbl));
         alertDialogBuilder.setMessage(getString(R.string.update_on_sgexists_msg));
         alertDialogBuilder.setCancelable(true);
         alertDialogBuilder.setOnCancelListener(this);
         alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int which) {
	        		reloadState();
	        	}
	        });
         AlertDialog alertDialog = alertDialogBuilder.create();
         
         alertDialog.show();         
    }

    public void onMembership() {
        filledForm = formFiller.fillMembershipForm(locationVisit);
        updatable = new MembershipUpdate();
        showProgressFragment();
        getLoaderManager().restartLoader(SOCIAL_GROUP_AT_LOCATION, null, this);
    }

    public void onRelationship() {
        filledForm = formFiller.fillRelationships(locationVisit);
        updatable = new RelationshipUpdate();
        startFilterActivity(FILTER_RELATIONSHIP);
    }
    
    public void onInMigration(){
    	throw new UnsupportedOperationException("Method not used in Baseline.");
    }

    public void onBaseline() {
        createBaselineFormDialog();
    }

    private void createBaselineFormDialog() {
    	showProgressFragment();
    	extInm= true;
    	newIndividual = true;
    	new CreateBaselineTask().execute();
    }

    private class CreateBaselineTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String id = locationVisit.generateIndividualId(getContentResolver());
            filledForm = formFiller.fillBaseline(locationVisit, id);
            updatable = new BaselineUpdate();            
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            hideProgressFragment();
            buildMotherDialog();
         }


    }
    
    private void selectIndividual(){
        String indExtId = filledForm.getIndividualExtId();
        if(indExtId.length() > 0){
        	vf.onLoaderReset(null);
        	vf.loadFilteredIndividualById(indExtId);
        	vf.selectItemNoInList(0);
        }  
    }
    
	private void onFinishExternalInmigration() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.baseline_lbl));
        alertDialogBuilder.setMessage(getString(R.string.update_finish_ext_inmigration_msg));
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setOnCancelListener(this);
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {			
        				public void onClick(DialogInterface dialog, int which) {
        					if(newIndividual){
        			        	onMembership();		        
        			        }				
        				}
        			});        
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();		
                
        extInm = false;
	}

    private void buildMotherDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.mother_lbl));
        alertDialogBuilder.setMessage(getString(R.string.update_build_mother_msg));
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setOnCancelListener(this);
        alertDialogBuilder.setPositiveButton(getString(R.string.yes_lbl), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startFilterActivity(FILTER_INMIGRATION_MOTHER);
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.no_lbl), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                filledForm.setMotherExtId("UNK");
                buildFatherDialog();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void buildFatherDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
       alertDialogBuilder.setTitle(getString(R.string.father_lbl));
        alertDialogBuilder.setMessage(getString(R.string.update_build_father_msg));
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setOnCancelListener(this);
        alertDialogBuilder.setPositiveButton(getString(R.string.yes_lbl), new DialogInterface.OnClickListener() {
        	  public void onClick(DialogInterface dialog, int which) {
                startFilterActivity(FILTER_INMIGRATION_FATHER);
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.no_lbl), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                filledForm.setFatherExtId("UNK");
                loadForm(SELECTED_XFORM);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void onOutMigration() {
        showProgressFragment();
        new CreateOutMigrationTask().execute();
    }

    private class CreateOutMigrationTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            filledForm = formFiller.fillOutMigrationForm(locationVisit);
            updatable = new OutMigrationUpdate();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            hideProgressFragment();
    		outMigration = true;
            loadForm(SELECTED_XFORM);
        }
    }

    public void onPregnancyRegistration() {
        showProgressFragment();
        new CreatePregnancyObservationTask().execute();
    }

    private class CreatePregnancyObservationTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            filledForm = formFiller.fillPregnancyRegistrationForm(locationVisit);
            updatable = new PregnancyObservationUpdate();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            hideProgressFragment();
            loadForm(SELECTED_XFORM);
        }
    }

    /**
     * The pregnancy outcome flow is as follows: <br />
     * 1. Prompt user for the number of live births. This indicates how many
     * child ids will be generated. <br />
     * 2. Prompt user for the father. We attempt to determine the father by
     * looking at any relationships the mother has. The user also has the option
     * of searching for the father as well. <br />
     * 3. Prompt for the social group to use. In this scenario, a search is made
     * for all memberships present at a location.
     */
    public void onPregnancyOutcome() {
        buildPregnancyLiveBirthCountDialog();
    }

    private void buildPregnancyLiveBirthCountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setOnCancelListener(this);
        builder.setTitle(getString(R.string.update_build_pregnancy_lbr_count_msg)).setCancelable(true)
                .setItems(new String[] {"1", "2", "3", "4" }, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showProgressFragment();
                        new PregnancyOutcomeFatherSelectionTask(which+1).execute();
                    }
                });
        builder.show();
    }

    private class PregnancyOutcomeFatherSelectionTask extends AsyncTask<Void, Void, Individual> {

        private int liveBirthCount;

        public PregnancyOutcomeFatherSelectionTask(int liveBirthCount) {
            this.liveBirthCount = liveBirthCount;
        }

        @Override
        protected Individual doInBackground(Void... params) {
            PregnancyOutcome pregOut = locationVisit.createPregnancyOutcome(getContentResolver(), liveBirthCount);
            filledForm = formFiller.fillPregnancyOutcome(locationVisit, pregOut);
            updatable = new PregnancyOutcomeUpdate();
            final Individual father = locationVisit.determinePregnancyOutcomeFather(getContentResolver());
            return father;
        }

        @Override
        protected void onPostExecute(final Individual father) {
            hideProgressFragment();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BaselineActivity.this);
            alertDialogBuilder.setTitle(getString(R.string.update_pregoutcome_choose_father));
            alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.setOnCancelListener(BaselineActivity.this);
            alertDialogBuilder.setNegativeButton(getString(R.string.cancel_lbl), new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int which) {
	        		reloadState();
	        	}
	        });

            if (father != null) {
                String fatherName = father.getFullName() + " (" + father.getExtId() + ")";
                String items[] = { fatherName, getString(R.string.update_pregoutcome_search_hdss), getString(R.string.update_pregoutcome_father_not_found) };
                alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int choice) {
                        if (choice == 0) {
                            new CreatePregnancyOutcomeTask(father).execute();
                        } else if (choice == 1) {
                            // choose father
                            startFilterActivity(FILTER_BIRTH_FATHER);
                        } else if (choice == 2) {
                            new CreatePregnancyOutcomeTask(null).execute();
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.fatherNotFound), Toast.LENGTH_LONG).show();
                String items[] = { getString(R.string.update_pregoutcome_search_hdss), getString(R.string.update_pregoutcome_not_within_hdss) };
                alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int choice) {
                        if (choice == 0) {
                            startFilterActivity(FILTER_BIRTH_FATHER);
                        } else if (choice == 1) {
                            new CreatePregnancyOutcomeTask(null).execute();
                        }
                    }
                });
            }

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    private class CreatePregnancyOutcomeTask extends AsyncTask<Void, Void, Void> {

        private Individual father;

        public CreatePregnancyOutcomeTask(Individual father) {
            this.father = father;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String fatherId = father == null ? "UNK" : father.getExtId();
            formFiller.appendFatherId(filledForm, fatherId);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            hideProgressFragment();
            loadSocialGroupsForIndividual();
        }
    }

    public void onDeath() {
        showProgressFragment();
        new CreateDeathTask().execute();
    }
    
    private class CreateDeathTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
        	SocialGroup sg = null;
        	ContentResolver resolver = getContentResolver();
        	Cursor cursor = Queries.getSocialGroupsByIndividualExtId(resolver,locationVisit.getSelectedIndividual().getExtId());
        	if (cursor.moveToFirst()) {
        	sg = Converter.convertToSocialGroup(cursor);
        	locationVisit.getLocation().setHead(sg.getGroupHead());
        	}
            filledForm = formFiller.fillDeathForm(locationVisit, sg);
            
            updatable = new DeathUpdate();
            cursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            hideProgressFragment();
            loadForm(SELECTED_XFORM);
        }
    }

    private void loadSocialGroupsForIndividual() {
        showProgressFragment();
        getLoaderManager().restartLoader(SOCIAL_GROUP_FOR_INDIVIDUAL, null, this);
    }

    public void onClearIndividual() {
        locationVisit.setSelectedIndividual(null);
        stateMachine.transitionTo("Select Individual");
        
        if(this.menuItemForm != null) {
        	this.menuItemForm.setVisible(false);
        }
    }

    public void loadForm(final int requestCode) {
        new OdkGeneratedFormLoadTask(getBaseContext(), filledForm, new OdkFormLoadListener() {
            public void onOdkFormLoadSuccess(Uri contentUri) {
            	Cursor cursor = getCursorForFormsProvider(filledForm.getFormName());
                if (cursor.moveToFirst()) {
                    jrFormId = cursor.getString(0);
                }
                BaselineActivity.this.contentUri = contentUri;
                startActivityForResult(new Intent(Intent.ACTION_EDIT, contentUri), requestCode);
            }

            public void onOdkFormLoadFailure() {
                createXFormNotFoundDialog();
            }
        }).execute();
    }

    public void onHierarchy1() {
        locationVisit.clearLevelsBelow(0);
        stateMachine.transitionTo("Select Hierarchy 1");
        
    	ContentResolver resolver = getContentResolver();
    	Cursor cursor = null;
    	cursor = Queries.getHierarchysByLevel(resolver, vf.getSTART_HIERARCHY_LEVEL_NAME());
    	
    	if(cursor.getCount() == 1){
    		cursor.moveToNext();
    		LocationHierarchy currentLocationHierarchy = Converter.convertToHierarchy(cursor);
    		cursor.close();   
    		onHierarchy1Selected(currentLocationHierarchy);
    	}
    	else{
    		cursor.close();   
    		loadHierarchy1ValueData();
    		vf.onLoaderReset(null); 
    	} 
    }

    public void onHierarchy2() {
        locationVisit.clearLevelsBelow(1);
        stateMachine.transitionTo("Select Hierarchy 2");

    	ContentResolver resolver = getContentResolver();
    	Cursor cursor = null;
    	String parentExtId = locationVisit.getHierarchy1().getExtId();
    	cursor = Queries.getHierarchysByParent(resolver, parentExtId);
    	
    	if(cursor.getCount() == 1){
    		cursor.moveToNext();
    		LocationHierarchy currentLocationHierarchy = Converter.convertToHierarchy(cursor);
    		cursor.close();   
    		onHierarchy2Selected(currentLocationHierarchy);
    	}
    	else{
    		cursor.close();   
    		loadHierarchy2ValueData();
    		vf.onLoaderReset(null);
    	} 
    }
    
    public void onHierarchy3() {
        locationVisit.clearLevelsBelow(2);
        stateMachine.transitionTo("Select Hierarchy 3");

    	ContentResolver resolver = getContentResolver();
    	Cursor cursor = null;
    	String parentExtId = locationVisit.getHierarchy2().getExtId();
    	cursor = Queries.getHierarchysByParent(resolver, parentExtId);
    	
    	if(cursor.getCount() == 1){
    		cursor.moveToNext();
    		LocationHierarchy currentLocationHierarchy = Converter.convertToHierarchy(cursor);
    		cursor.close();   
    		onHierarchy3Selected(currentLocationHierarchy);
    	}
    	else{
    		cursor.close();   
    		loadHierarchy3ValueData();
    		vf.onLoaderReset(null);
    	}   
    }

    public void onHierarchy4() {
        locationVisit.clearLevelsBelow(3);
        stateMachine.transitionTo("Select Hierarchy 4");

    	ContentResolver resolver = getContentResolver();
    	Cursor cursor = null;
    	String parentExtId = locationVisit.getHierarchy3().getExtId();
    	cursor = Queries.getHierarchysByParent(resolver, parentExtId);
    	
    	if(cursor.getCount() == 1){
    		cursor.moveToNext();
    		LocationHierarchy currentLocationHierarchy = Converter.convertToHierarchy(cursor);
    		cursor.close();   
    		onHierarchy4Selected(currentLocationHierarchy);
    	}
    	else{
    		cursor.close();
    		loadHierarchy4ValueData();
    		vf.onLoaderReset(null);
    	}          
    }
    
	
	public void onHierarchy5() {
		 locationVisit.clearLevelsBelow(4);
	        stateMachine.transitionTo("Select Hierarchy 5");

	    	ContentResolver resolver = getContentResolver();
	    	Cursor cursor = null;
	    	String parentExtId = locationVisit.getHierarchy4().getExtId();
	    	cursor = Queries.getHierarchysByParent(resolver, parentExtId);
	    	
	    	if(cursor.getCount() == 1){
	    		cursor.moveToNext();
	    		LocationHierarchy currentLocationHierarchy = Converter.convertToHierarchy(cursor);
	    		cursor.close();   
	    		onHierarchy5Selected(currentLocationHierarchy);
	    	}
	    	else{
	    		cursor.close();
	    		loadHierarchy5ValueData();
	    		vf.onLoaderReset(null);
	    	}          
		
	}
    
	
	public void onHierarchy6() {
		 locationVisit.clearLevelsBelow(5);
	        stateMachine.transitionTo("Select Hierarchy 6");

	    	ContentResolver resolver = getContentResolver();
	    	Cursor cursor = null;
	    	String parentExtId = locationVisit.getHierarchy5().getExtId();
	    	cursor = Queries.getHierarchysByParent(resolver, parentExtId);
	    	
	    	if(cursor.getCount() == 1){
	    		cursor.moveToNext();
	    		LocationHierarchy currentLocationHierarchy = Converter.convertToHierarchy(cursor);
	    		cursor.close();   
	    		onHierarchy6Selected(currentLocationHierarchy);
	    	}
	    	else{
	    		cursor.close();
	    		loadHierarchy6ValueData();
	    		vf.onLoaderReset(null);
	    	}          
				
	}

	
	public void onHierarchy7() {
		 locationVisit.clearLevelsBelow(6);
	        stateMachine.transitionTo("Select Hierarchy 7");

	    	ContentResolver resolver = getContentResolver();
	    	Cursor cursor = null;
	    	String parentExtId = locationVisit.getHierarchy6().getExtId();
	    	cursor = Queries.getHierarchysByParent(resolver, parentExtId);
	    	
	    	if(cursor.getCount() == 1){
	    		cursor.moveToNext();
	    		LocationHierarchy currentLocationHierarchy = Converter.convertToHierarchy(cursor);
	    		cursor.close();   
	    		onHierarchy7Selected(currentLocationHierarchy);
	    	}
	    	else{
	    		cursor.close();
	    		loadHierarchy7ValueData();
	    		vf.onLoaderReset(null);
	    	}          
	}

	public void onHierarchy8() {
		 locationVisit.clearLevelsBelow(7);
	        stateMachine.transitionTo("Select Hierarchy 8");

	    	ContentResolver resolver = getContentResolver();
	    	Cursor cursor = null;
	    	String parentExtId = locationVisit.getHierarchy7().getExtId();
	    	cursor = Queries.getHierarchysByParent(resolver, parentExtId);
	    	
	    	if(cursor.getCount() == 1){
	    		cursor.moveToNext();
	    		LocationHierarchy currentLocationHierarchy = Converter.convertToHierarchy(cursor);
	    		cursor.close();   
	    		onHierarchy8Selected(currentLocationHierarchy);
	    	}
	    	else{
	    		cursor.close();
	    		loadHierarchy8ValueData();
	    		vf.onLoaderReset(null);
	    	}          
		
	}

    public void onLocation() {
    	
        locationVisit.clearLevelsBelow(9);
        stateMachine.transitionTo("Select Location");
        

    	ContentResolver resolver = getContentResolver();
    	Cursor cursor = null;
    	if (levelNumbers==2) {
    		parentExtId = locationVisit.getHierarchy2().getExtId();
    	} else if (levelNumbers==3) {
        	parentExtId = locationVisit.getHierarchy3().getExtId(); 
    	} else if (levelNumbers==4) {
    		parentExtId = locationVisit.getHierarchy4().getExtId();
    	} else if (levelNumbers==5) {
    		parentExtId = locationVisit.getHierarchy5().getExtId();
    	} else if (levelNumbers==6) {
    		parentExtId = locationVisit.getHierarchy6().getExtId();
    	} else if (levelNumbers==7) {
    		parentExtId = locationVisit.getHierarchy7().getExtId();
    	} else if (levelNumbers==8) {
    		parentExtId = locationVisit.getHierarchy8().getExtId();
    	}
    	cursor = Queries.getLocationsByHierachy(resolver, parentExtId);
    	
    	if(cursor.getCount() == 1){
    		cursor.moveToNext();
    		Location location = Converter.convertToLocation(cursor);
    		onLocationSelected(location);
    	}
    	else{
    		loadLocationValueData();
    	}
    	cursor.close();
    }

    public void onRound() {
        locationVisit.clearLevelsBelow(8);
        stateMachine.transitionTo("Select Round");
        
    	ContentResolver resolver = getContentResolver();
    	Cursor cursor = null;
        cursor = Queries.allRounds(resolver);
        int rows = cursor.getCount();
        boolean baselineFound = false;
        Round round = null;
        if(rows > 0){        	
        	while(cursor.moveToNext()){
        		round = Converter.convertToRound(cursor);
        		
        		if(round.getRoundNumber().equalsIgnoreCase("0")){
        			baselineFound = true;
        			break;
        		}
        	}
        	
            if(baselineFound){
            	if(round != null){
            		onRoundSelected(round);
            	}
            }
            else{
            	Toast.makeText(this, getString(R.string.couldnt_find_baseline_lbl), Toast.LENGTH_LONG).show();
            	
            	cursor.moveToFirst();
            	round = Converter.convertToRound(cursor);
            	onRoundSelected(round);
            }        	
        }
        else{
        	Toast.makeText(this, getString(R.string.no_round_info_lbl), Toast.LENGTH_LONG).show();
        }
                
        vf.onLoaderReset(null);
        cursor.close();        
       
    }

    public void onIndividual() {
        locationVisit.clearLevelsBelow(10);
        loadIndividualValueData();
    }

    public void onHierarchy1Selected(LocationHierarchy hierarchy) {
        locationVisit.setHierarchy1(hierarchy);
        stateMachine.transitionTo("Select Hierarchy 2");
 //       updateButtons(0);
        onHierarchy2();
    }

    private void registerTransitions() {
        sf.registerTransitions(stateMachine);
        ef.registerTransitions(stateMachine);
    }

    public void onHierarchy2Selected(LocationHierarchy subregion) {
        locationVisit.setHierarchy2(subregion);
        stateMachine.transitionTo("Select Hierarchy 3");
  //      updateButtons(1);
        onHierarchy3();
    }
    

    public void onHierarchy3Selected(LocationHierarchy hierarchy) {
        locationVisit.setHierarchy3(hierarchy);
        stateMachine.transitionTo("Select Hierarchy 4");
   //     updateButtons(2);
        if (levelNumbers==3) {
        	onRound();	
        } else {
        	onHierarchy4();
        }
    }

    
    public void onHierarchy4Selected(LocationHierarchy village) {
        locationVisit.setHierarchy4(village);
        //stateMachine.transitionTo("Select Round");
   //     updateButtons(3);
        if (levelNumbers==4) {
        	onRound();	
        } else {
        	onHierarchy5();
        }
    }
    
    
	public void onHierarchy5Selected(LocationHierarchy hierarchy5) {
        locationVisit.setHierarchy5(hierarchy5);
 //       updateButtons(4);
        if (levelNumbers==5) {
        	onRound();	
        } else {
        	onHierarchy6();
        }
	}

	
	public void onHierarchy6Selected(LocationHierarchy hierarchy6) {
        locationVisit.setHierarchy6(hierarchy6);
  //      updateButtons(5);
        if (levelNumbers==6) {
        	onRound();	
        } else {
        	onHierarchy7();
        }		
	}

	
	public void onHierarchy7Selected(LocationHierarchy hierarchy7) {
        locationVisit.setHierarchy7(hierarchy7);
   //     updateButtons(6);
        if (levelNumbers==7) {
        	onRound();	
        } else {
        	onHierarchy8();
        }		
	}
	

	
	public void onHierarchy8Selected(LocationHierarchy hierarchy8) {
	     locationVisit.setHierarchy8(hierarchy8);
	//        updateButtons(7);
        	onRound();	
	}


    
    public void onRoundSelected(Round round) {
        locationVisit.setRound(round);
        stateMachine.transitionTo("Select Location");
    }

    public void onLocationSelected(Location location) {
        locationVisit.setLocation(location);
//        stateMachine.transitionTo("Create Visit");
        transitionToCreateVisit();
    }

    public void onIndividualSelected(Individual individual) {
        locationVisit.setSelectedIndividual(individual);
        stateMachine.transitionTo("Select Event");
        
        //Display Extra Forms Menu
        if(this.menuItemForm != null) {
        	this.menuItemForm.setVisible(true);
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = null;
        switch (id) {
        case SOCIAL_GROUP_AT_LOCATION:
        case SOCIAL_GROUP_FOR_EXT_INMIGRATION:
            uri = OpenHDS.SocialGroups.CONTENT_LOCATION_ID_URI_BASE.buildUpon()
                    .appendPath(locationVisit.getLocation().getExtId()).build();
            break;
        case SOCIAL_GROUP_FOR_INDIVIDUAL:
            uri = OpenHDS.SocialGroups.CONTENT_INDIVIDUAL_ID_URI_BASE.buildUpon()
                    .appendPath(locationVisit.getSelectedIndividual().getExtId()).build();
            break;
        }

        return new CursorLoader(this, uri, null, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        hideProgressFragment();
        
    	restoreState();
    	
        if (cursor.getCount() == 1 && loader.getId() == SOCIAL_GROUP_FOR_INDIVIDUAL) {
            cursor.moveToFirst();
            appendSocialGroupFromCursor(cursor);
            return;
        }
        if(loader.getId() == SOCIAL_GROUP_AT_LOCATION){
        	handleSocialGroup(loader, cursor);
        }
    }
    
    private void handleSocialGroup(Loader<Cursor> loader, Cursor cursor){	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	if(cursor.getCount() > 0){
    		builder.setTitle(getString(R.string.select_household_lbl));
        	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor,
        			new String[] { OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME,
        			OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID }, new int[] { android.R.id.text1,
        			android.R.id.text2 }, 0)
			{
			    @Override
			    public View getView(int position, View convertView, ViewGroup parent)
			    {
			        final View row = super.getView(position, convertView, parent);
			        TextView text1 = (TextView) row.findViewById(android.R.id.text1);
      			    TextView text2 = (TextView) row.findViewById(android.R.id.text2);
      			  
      			    text1.setTextColor(Color.BLACK);
      			    text2.setTextColor(Color.BLACK);
			        if (position % 2 == 0)
			            row.setBackgroundResource(android.R.color.darker_gray);
			        else
			            row.setBackgroundResource(android.R.color.background_light);
			        return row;
			    }
			};
			
        	builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
        			public void onClick(DialogInterface dialog, int which) {
        				Cursor cursor = (Cursor) householdDialog.getListView().getItemAtPosition(which);
        				appendSocialGroupFromCursor(cursor);
        			}
        	});     		
    	}
    	else{
    		builder.setTitle(getString(R.string.select_household_lbl));
    		builder.setMessage(getString(R.string.search_for_household_lbl));
    	}
    	
    	builder.setNegativeButton(getString(R.string.cancel_lbl),new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, just close
				// the dialog box and do nothing
				dialog.cancel();
			}
		});   	
    	builder.setPositiveButton(getString(R.string.create_lbl), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				onHousehold();
			}
		});           	
		builder.setNeutralButton(getString(R.string.search_lbl), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				searchSocialGroup();
			}
		});     
		builder.setOnCancelListener(this);
        householdDialog = builder.create();
        householdDialog.show();    	
    }
    
    private void searchSocialGroup(){ 	
    	startFilterActivity(FILTER_SOCIALGROUP);
    }

    private void appendSocialGroupFromCursor(Cursor cursor) {
        SocialGroup sg = Converter.convertToSocialGroup(cursor);
        filledForm = formFiller.appendSocialGroup(sg, filledForm);
        loadForm(SELECTED_XFORM);
    }

    public void onLoaderReset(Loader<Cursor> arg0) {
    	if (householdDialog!=null){ 
    		householdDialog.dismiss();
    		householdDialog = null;
    	}
    }

	public void onFilterLocation() {
		startFilterLocActivity(FILTER_LOCATION);	
		
	}
    private Cursor getCursorForFormsProvider(String name) {
    	ContentResolver resolver = getContentResolver();
        return resolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, new String[] {
                FormsProviderAPI.FormsColumns.JR_FORM_ID, FormsProviderAPI.FormsColumns.FORM_FILE_PATH },
                FormsProviderAPI.FormsColumns.JR_FORM_ID + " like ?", new String[] { name + "%" }, null);
    }
    
	public void handleResult(Entity entity) {
		if(entity == Entity.INDIVIDUAL){
			vf.selectItemNoInList(0);
		}
	}

	
	public void onLocationGeoPoint() {
		// TODO Auto-generated method stub
		
	}
	
    private boolean individualMeetsMinimumAge(Individual indiv) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date dob = formatter.parse(indiv.getDob());
            Calendar cal = Calendar.getInstance();
            cal.setTime(dob);
            if ((new GregorianCalendar().get(Calendar.YEAR) - cal.get(Calendar.YEAR)) > MINIMUM_HOUSEHOLD_AGE) {
                return true;
            }
        } catch (Exception e) {
            // no dob or malformed
            return true;
        }

        return false;
    }

	public void onChangeHouseholdHead() {
		// TODO Auto-generated method stub
		
	}   

	//Dialog cancelled by pressing the back button, refresh state
	public void onCancel(DialogInterface arg0) {
		reloadState();
	}
	
	private void reloadState(){
		stateMachine.transitionTo(stateMachine.getState());
	}
}

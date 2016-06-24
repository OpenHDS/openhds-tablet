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
import org.openhds.mobile.database.DeathOfHoHUpdate;
import org.openhds.mobile.database.DeathUpdate;
import org.openhds.mobile.database.ExternalInMigrationUpdate;
import org.openhds.mobile.database.ExtraFormUpdate;
import org.openhds.mobile.database.HeadOfHouseholdUpdate;
import org.openhds.mobile.database.HouseholdUpdate;
import org.openhds.mobile.database.IndividualVisitedUpdate;
import org.openhds.mobile.database.InternalInMigrationUpdate;
import org.openhds.mobile.database.LocationUpdate;
import org.openhds.mobile.database.MembershipUpdate;
import org.openhds.mobile.database.OutMigrationUpdate;
import org.openhds.mobile.database.PregnancyObservationUpdate;
import org.openhds.mobile.database.PregnancyOutcomeUpdate;
import org.openhds.mobile.database.RelationshipUpdate;
import org.openhds.mobile.database.Updatable;
import org.openhds.mobile.database.VisitUpdate;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.fragment.EventFragment;
import org.openhds.mobile.fragment.ProgressFragment;
import org.openhds.mobile.fragment.SelectionFragment;
import org.openhds.mobile.fragment.ValueFragment;
import org.openhds.mobile.fragment.ValueFragment.Displayed;
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
import org.openhds.mobile.model.Visit;
import org.openhds.mobile.task.OdkGeneratedFormLoadTask;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * UpdateActivity mediates the interaction between the 3 column fragments. The
 * buttons in the left most column drive a state machine while the user
 * interacts with the application.
 */
public class UpdateActivity extends Activity implements ValueFragment.ValueListener, LoaderCallbacks<Cursor>,
        EventFragment.Listener, SelectionFragment.Listener, ValueFragment.OnlyOneEntryListener, DialogInterface.OnCancelListener {

    private SelectionFragment sf;
    private ValueFragment vf;
    private EventFragment ef;
    private ProgressFragment progressFragment;
    private MenuItem menuItemForm;

    // loader ids
    private static final int SOCIAL_GROUP_AT_LOCATION = 5;
    private static final int SOCIAL_GROUP_FOR_INDIVIDUAL = 10;
    private static final int SOCIAL_GROUP_FOR_EXT_INMIGRATION = 20;
    private static final int INDIVIDUALS_IN_SOCIAL_GROUP = 30;
    private static final int INDIVIDUALS_IN_SOCIAL_GROUP_ACTIVE = 40;
    
    // activity request codes for onActivityResult
    private static final int SELECTED_XFORM = 1;
    private static final int CREATE_LOCATION = 10;
    private static final int FILTER_RELATIONSHIP = 20;
    private static final int FILTER_LOCATION = 30;
    private static final int FILTER_FORM = 35;
    private static final int FILTER_INMIGRATION = 40;
    private static final int FILTER_BIRTH_FATHER = 45;
    private static final int LOCATION_GEOPOINT = 50;
    protected static final int FILTER_INMIGRATION_MOTHER = 60;
    protected static final int FILTER_INMIGRATION_FATHER = 70;
    protected static final int FILTER_INDIV_VISIT = 75;
    protected static final int FILTER_SOCIALGROUP = 80;
    
    private static int MINIMUM_HOUSEHOLD_AGE;
    private static final int DEFAULT_MINIMUM_HOUSEHOLD_AGE = 14;
    
    private static int MINIMUM_PARENTHOOD_AGE;
    private static final int DEFAULT_MINIMUM_PARENTHOOD_AGE = 12;
    
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
    private boolean deathCreation;
    private boolean inChangeHoH;
    private boolean outMigration;
    private String jrFormId;
    
	private ProgressDialog progress;
    
    //State machine stuff
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
	private int CREATING_NEW_LOCATION = 0;
	private int RETURNING_TO_DSS = 0;
	
	private static final List<String> stateSequence = new ArrayList<String>();

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
        
        ActionBar actionBar = getActionBar();
        actionBar.show();        
        
        if(savedInstanceState == null){
            //Create state machine
            stateMachine = new StateMachine(new LinkedHashSet<String>(stateSequence), stateSequence.get(0));
            
            registerTransitions();
	        sf.setLocationVisit(locationVisit);
	        ef.setLocationVisit(locationVisit);   
	        
	        String state = "Select Hierarchy 1";
	        stateMachine.transitionInSequence(state);
        }
        else{
        	String state = (String)savedInstanceState.getSerializable("currentState");
        	stateMachine = new StateMachine(new LinkedHashSet<String>(stateSequence), state);
        	
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
        
        // SET MINIMUM HOH AGE
		android.database.Cursor c = Queries.getAllSettings(getContentResolver());
		Settings settings = Converter.convertToSettings(c); 
		c.close();
		MINIMUM_HOUSEHOLD_AGE = settings.getMinimumAgeOfHouseholdHead() == 0 ? DEFAULT_MINIMUM_HOUSEHOLD_AGE : settings.getMinimumAgeOfHouseholdHead();
		MINIMUM_PARENTHOOD_AGE = settings.getMinimumAgeOfParents() == 0 ? DEFAULT_MINIMUM_PARENTHOOD_AGE : settings.getMinimumAgeOfParents();
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
    
    private void restoreState(){
	    if(stateMachine != null && stateMachine.getState() != ""){
	    	String currentState = stateMachine.getState();
	    	stateMachine.transitionTo(currentState);
	    }
    }
    
    @Override
    protected void onStart() {
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
    
    @Override
	public void onResume()
	{
	    super.onResume();
	    hideProgressFragment();
	    dismissLoadingDialog();
	    restoreState();
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
	                    UpdateActivity.this.finish();
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
            updatable = new ExtraFormUpdate();
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
            stateMachine.transitionTo("Create Visit");
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
            vf.onLoaderReset(null);
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
        case LOCATION_GEOPOINT:
            if (resultCode == RESULT_OK) {
                String extId = data.getExtras().getString("extId");
                // a few things need to happen here:
                // * get the location by extId
                cursor = Queries.getLocationByExtId(resolver, extId);
                Location location = Converter.toLocation(cursor);
               
                // * figure out the parent location hierarchy
                cursor = Queries.getHierarchyByExtId(resolver, location.getHierarchy());
                LocationHierarchy subvVllage = Converter.toHierarhcy(cursor, true);
                

                cursor = Queries.getHierarchyByExtId(resolver, subvVllage.getParent());
                LocationHierarchy village = Converter.toHierarhcy(cursor, true);
                
                cursor = Queries.getHierarchyByExtId(resolver, village.getParent());
                LocationHierarchy district = Converter.toHierarhcy(cursor, true);
                
                cursor = Queries.getHierarchyByExtId(resolver, district.getParent());
                LocationHierarchy region = Converter.toHierarhcy(cursor, true);
                
                cursor = Queries.allRounds(resolver);
                Round round = Converter.convertToRound(cursor);
                
                locationVisit.setHierarchy1(region);
                locationVisit.setHierarchy2(district);
                locationVisit.setHierarchy3(village);
                locationVisit.setHierarchy4(subvVllage);
                locationVisit.setRound(round);
                locationVisit.setLocation(location);
                
                
                sf.setLocationVisit(locationVisit);
                sf.setAll();
            	vf.onLoaderReset(null);
                stateMachine.transitionTo("Create Visit");
                cursor.close();
            }
        }
    }
    
	public void showLoadingDialog() {

	    if (progress == null) {
	        progress = new ProgressDialog(this);
	        progress.setTitle(getString(R.string.loading_lbl));
	        progress.setMessage(getString(R.string.please_wait_lbl));
	    }
	    progress.show();
	}

	public void dismissLoadingDialog() {

	    if (progress != null && progress.isShowing()) {
	        progress.dismiss();
	    }
	}    

    private void handleFilterIndivVisit(int resultCode, Intent data) { 	
    		SocialGroup sg;
    		if (resultCode != RESULT_OK) {
                return;
            }

            Individual individual = (Individual) data.getExtras().getSerializable("individual");
            if(individual != null){
            	ContentResolver resolver = getContentResolver();
            	Cursor cursor = Queries.getSocialGroupsByIndividualExtId(resolver,individual.getExtId());
            	if (cursor.moveToFirst()) {
            		SocialGroup socialGroup = Converter.convertToSocialGroup(cursor);
            		sg = socialGroup;
            		locationVisit.setSocialgroup(sg);
            	}
            	cursor.close();
            }

            if (locationVisit.getVisitLevel()==null) {
            	locationVisit.setVisitLevel(VISIT_LEVEL);
            }
            locationVisit.createVisit(getContentResolver());
            filledForm = formFiller.fillVisitForm(locationVisit);

            if (individual!=null){
            	filledForm.setIntervieweeId(individual.getExtId());
            }else{
            	filledForm.setIntervieweeId("UNK");
            }

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
    }

    /**
     * This differs from {@link UpdateActivity.CheckFormStatus} in that, upon
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
            		buildNewLocDialog();
            	}
            } else {
                createUnfinishedFormDialog();
                
                //Reset location and display text after cancellation
                locationVisit.setLocation(null);
                sf.setAll();
            }            
        }


    }
	private void buildNewLocDialog() {
		// check if new location and new people
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setOnCancelListener(this);
        alertDialogBuilder.setTitle(getString(R.string.dialog_new_loc));
        alertDialogBuilder.setMessage(getString(R.string.select_any_sg_hdss));
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setPositiveButton(getString(R.string.yes_lbl), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	CREATING_NEW_LOCATION = 0;
        		onCreateVisit();	
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.no_lbl), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	CREATING_NEW_LOCATION = 1;
        		onCreateVisit();	
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();			
	}
    private void handleFilterInMigrationResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        showProgressFragment();
        Individual individual = (Individual) data.getExtras().getSerializable("individual");

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

        Individual individualB = (Individual) data.getExtras().getSerializable("individual");
        filledForm.setIndividualB(individualB.getExtId());

        if (individualB.getExtId().equalsIgnoreCase(filledForm.getIndividualA())){
        	//Cant create an relationship between an individual and it self
        	Toast.makeText(UpdateActivity.this, getString(R.string.cant_create_relationship_lbl) , Toast.LENGTH_LONG).show();
        	return;
        }
        
        if(individualB.getGender().equalsIgnoreCase(locationVisit.getSelectedIndividual().getGender())){
        	Toast.makeText(UpdateActivity.this, getString(R.string.cant_create_relsamesex_lbl) , Toast.LENGTH_LONG).show();
        	return;        	
        }
        
        loadForm(SELECTED_XFORM);
    } 

    private void handleXformResult(int resultCode, Intent data) {		
        if (resultCode == RESULT_OK) {
            showProgressFragment();
            new CheckFormStatus(getContentResolver(), contentUri).execute();
        } else {
            Toast.makeText(this, getString(R.string.odk_problem_lbl), Toast.LENGTH_LONG).show();
    		deathCreation = false;
    		extInm= false;
    		updatable = null;
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
        txn.add(R.id.middle_col, progressFragment, "progressFragment").commit();
    }

    void hideProgressFragment() {
        if (!showingProgress) {
            return;
        }

        showingProgress = false;
        FragmentManager fm = getFragmentManager();
        Fragment frag = fm.findFragmentByTag("progressFragment");
        
        if (frag!=null && frag instanceof ProgressFragment)
        {
        	FragmentTransaction tr = fm.beginTransaction();
        	tr.remove(frag).commitAllowingStateLoss();
        }
        
        FragmentTransaction txn = getFragmentManager().beginTransaction();
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
                try{
	                if(updatable != null){
	                	updatable.updateDatabase(getContentResolver(), filepath, jrFormId);
	                	
	                	/*Set value of real visit read out from from*/
	                	if(updatable instanceof VisitUpdate){
	                		Visit visit = ((VisitUpdate)updatable).getVisit();
	                		locationVisit.getVisit().setRealVisit(visit.getRealVisit());
	                	}
	                	updatable = null;
	                }
                }finally{
                	try{
                		cursor.close();
                	}catch(Exception e){
                		e.printStackTrace();
                	}
                }
                return true;
            } else {
            	try{
            		cursor.close();
            	}catch(Exception e){
            		e.printStackTrace();
            	}            	
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
            		selectIndividual();
            	} else if (stateMachine.getState() == UpdateActivity.SELECT_INDIVIDUAL && inChangeHoH) {
            		//Stay in same state
            	} else if (stateMachine.getState()=="Select Individual") {
            		if (extInm)
                		onFinishExternalInmigration();
            		selectIndividual();
            	}else if (stateMachine.getState()=="Select Event") {
            		if (hhCreation)
            			onFinishedHouseHoldCreation();
            		else if(deathCreation){            			
            			onClearIndividual();
            		}
            		else if(outMigration){
            			onClearIndividual();
            		}
            	}
            	else if(stateMachine.getState() == CREATE_VISIT){
            		Visit visit = locationVisit.getVisit();
            		if(visit != null && visit.getRealVisit()){
            			stateMachine.transitionTo(SELECT_INDIVIDUAL);
            		}
            		else{
            			onFinishVisit();
            		}
            	}
            	else {
            		stateMachine.transitionTo("Select Individual");
            	}
            } else {
            	if (stateMachine.getState()=="Inmigration") {
            		locationVisit.setSelectedIndividual(null);
            		stateMachine.transitionTo("Select Individual");
            	}
                createUnfinishedFormDialog();
            }  
    		deathCreation = false;
    		extInm = false;
    		inChangeHoH = false;
    		outMigration = false;
        }
    }
    
	private void onFinishedHouseHoldCreation() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.householdBtn_lbl));
        alertDialogBuilder.setMessage(getString(R.string.finish_household_creation_msg));
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setPositiveButton("Ok", null);
        AlertDialog alertDialog = alertDialogBuilder.create();
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
            i.putExtra("minimumAge", MINIMUM_PARENTHOOD_AGE);
        case FILTER_INMIGRATION:
            i.putExtra("img", "IMG");
        }

         if (CREATING_NEW_LOCATION == 1) {
        	handleFilterIndivVisit(RESULT_OK, i);
        	return;
         }
         
         if (RETURNING_TO_DSS == 1) {
        	  i.putExtra("img", "IMG_RETURN");
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
        vf.loadIndividuals(locationVisit, VISIT_LEVEL);
    }

    private void createUnfinishedFormDialog() {
        formUnFinished = true;
        if (xformUnfinishedDialog == null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getString(R.string.warning_lbl));
            alertDialogBuilder.setMessage(getString(R.string.update_unfinish_msg1));
            alertDialogBuilder.setCancelable(true);
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
          alertDialogBuilder.setMessage(getString(R.string.update_xform_not_found_msg));
          alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                xFormNotFound = false;
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void onLocationGeoPoint() {
        Intent intent = new Intent(getApplicationContext(), ShowMapActivity.class);
        startActivityForResult(intent, LOCATION_GEOPOINT);
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
        new CreateVisitTask().execute();
    }

    private class CreateVisitTask extends AsyncTask<Void, Void, Void> {

    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		showProgressFragment();
    	}
    	
        @Override
        protected Void doInBackground(Void... params) {
            updatable = new VisitUpdate();
        	startFilterActivity(FILTER_INDIV_VISIT);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        	super.onPostExecute(result);
            hideProgressFragment();
            CREATING_NEW_LOCATION = 0;
        }
    }

    public void onFinishVisit() {
        /* If real visit, run validation*/
    	if(locationVisit.getVisit().getRealVisit()){
    		validateVisit();
    	}
    	/* Else just finish visit since nobody's there to interview*/
    	else{
    		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    		alertDialogBuilder.setTitle(getString(R.string.finishVisit_title));
    		alertDialogBuilder.setMessage(getString(R.string.visitFinished_lbl));
    		alertDialogBuilder.setCancelable(false);
    		alertDialogBuilder.setPositiveButton(getString(R.string.ok_lbl),
    				new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int which) {
    						finishVisit();
    					}
    				});
    		AlertDialog alertDialog = alertDialogBuilder.create();
    		alertDialog.show();
    	}
    }
    
    private void validateVisit(){
    	Cursor curs = null;
    	
    	if(VISIT_LEVEL.equalsIgnoreCase("location")){
    		curs = Queries.getActiveIndividualsByResidency(getContentResolver(), locationVisit.getLocation().getExtId());    		
    	}
    	else{
	    	if(locationVisit.getSocialgroup() != null)
	    		curs = Queries.getActiveIndividualsByResidencySG(getContentResolver(), locationVisit.getLocation().getExtId(), locationVisit.getSocialgroup().getExtId());
	    	else
	    		curs = Queries.getActiveIndividualsByResidency(getContentResolver(), locationVisit.getLocation().getExtId());    		
    	}
    	    	
    	final List<Individual> individualList = Converter.toIndividualList(curs);
    	int individualCount = individualList.size();
    	
    	int visitedIndividualsCount = 0;
    	    	
    	for(int i = 0 ; i < individualCount; i++){
    		Individual individual = individualList.get(i);
    		if(individual.getVisited().equalsIgnoreCase("Yes")){
    			visitedIndividualsCount++;
    		}
    	}
    	    	
    	final boolean notAllIndividualsVisited = visitedIndividualsCount < individualCount;
    	String message;
    	if(notAllIndividualsVisited){
    		message = getString(R.string.update_finish_not_all_visited_msg);
    	}
    	else{
    		message = getString(R.string.update_finish_visit_msg);
    	}
    	   		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getString(R.string.visit_lbl));
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setOnCancelListener(this);
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.setPositiveButton(getString(R.string.yes_lbl),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (notAllIndividualsVisited) {
							for (Individual ind : individualList) {
								if (!ind.getVisited().equalsIgnoreCase("Yes"))
									setIndividualVisitedFlag(ind);
							}
						}
						finishVisit();
					}
				});
		alertDialogBuilder.setNegativeButton(getString(R.string.cancel_lbl),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				reloadState();
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
    }
    
    private void finishVisit(){
    	if(menuItemForm != null) {
          	menuItemForm.setVisible(false);
    	}    
    	locationVisit = locationVisit.completeVisit();
        sf.setLocationVisit(locationVisit);
        ef.setLocationVisit(locationVisit);
        stateMachine.transitionTo("Finish Visit");
        stateMachine.transitionTo("Select Location");
        vf.onLoaderReset(null);
        vf.setListCurrentlyDisplayed(Displayed.LOCATION);
    }
    
    private void setIndividualVisitedFlag(Individual individiual){
    	IndividualVisitedUpdate update = new IndividualVisitedUpdate();
    	update.updateDatabase(getContentResolver(), individiual);
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
            } else {
            	filledForm = formFiller.fillSocialGroupForm(locationVisit, sg);
            	updatable = new HouseholdUpdate();
            }
            
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        	 SocialGroup sg = locationVisit.createSocialGroup(getContentResolver());
             if (sg==null){
            	onSGexists();
            	this.cancel(true);
             	hideProgressFragment();
             	
             } else {
            	hideProgressFragment();
            	
                //Check if selected individual meets required age to be the Head of a SG. If not, display a msg and don't proceed.
                Individual individual = locationVisit.getSelectedIndividual();
                boolean meetsMinimumAge = individualMeetsMinimumAge(individual);
                if(meetsMinimumAge){
                	hhCreation = true;
                	loadForm(SELECTED_XFORM);
                }
                else{
                	Toast.makeText(UpdateActivity.this, getString(R.string.younger_than_required_age_for_hoh), Toast.LENGTH_LONG).show();
                }
             }
        }
    }
    
    public void onSGexists() {
    	 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
         alertDialogBuilder.setTitle(getString(R.string.socialgroup_lbl));
         alertDialogBuilder.setOnCancelListener(this);
         alertDialogBuilder.setMessage(getString(R.string.update_on_sgexists_msg));
         alertDialogBuilder.setCancelable(true);
         alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int which) {
	        		reloadState();
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

    public void onMembership() {
    	hhCreation=false;
        filledForm = formFiller.fillMembershipForm(locationVisit);
        updatable = new MembershipUpdate();
        getLoaderManager().restartLoader(SOCIAL_GROUP_AT_LOCATION, null, this);
    }

    public void onRelationship() {
        filledForm = formFiller.fillRelationships(locationVisit);
        updatable = new RelationshipUpdate();
        startFilterActivity(FILTER_RELATIONSHIP);
    }
    
    public void onBaseline(){
    	//We call onMigration
    	onInMigration();
    }

    public void onInMigration() {
    	RETURNING_TO_DSS = 0;
        createInMigrationFormDialog();
    }

    private void createInMigrationFormDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.in_migration_lbl));
        alertDialogBuilder.setMessage(getString(R.string.update_create_inmigration_msg));
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setOnCancelListener(this);
        alertDialogBuilder.setPositiveButton(getString(R.string.update_create_inmigration_pos_button), new DialogInterface.OnClickListener() {
        	 public void onClick(DialogInterface dialog, int which) {
            	extInm= true;
            	startFilterActivity(FILTER_INMIGRATION);
     
            }
        });
             alertDialogBuilder.setNegativeButton(getString(R.string.update_create_inmigration_neg_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                showProgressFragment();
                extInm= true;
                new CreateExternalInmigrationTask().execute();

            }
        });
             
             alertDialogBuilder.setNeutralButton(getString(R.string.update_create_inmigration_neutral_button), new DialogInterface.OnClickListener() {
            	 public void onClick(DialogInterface dialog, int which) {
            	 extInm= true;
            	 RETURNING_TO_DSS = 1;
            	 startFilterActivity(FILTER_INMIGRATION);
            	 }
            	 });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private class CreateExternalInmigrationTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String id = locationVisit.generateIndividualId(getContentResolver());
            filledForm = formFiller.fillExternalInmigration(locationVisit, id);
            updatable = new ExternalInMigrationUpdate();            
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
        if(indExtId != null && indExtId.length() > 0){
        	vf.onLoaderReset(null);
        	vf.loadFilteredIndividualById(indExtId);
        	vf.selectItemNoInList(0);
        }    	
    }
    
	private void onFinishExternalInmigration() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.in_migration_lbl));
        alertDialogBuilder.setMessage(getString(R.string.update_finish_ext_inmigration_msg));
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setPositiveButton("Ok", null);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();		
        extInm= false;
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

    	private SocialGroup sg;
        @Override
        protected Void doInBackground(Void... params) {
            filledForm = formFiller.fillOutMigrationForm(locationVisit);
            updatable = new OutMigrationUpdate();
            
            Individual individual = locationVisit.getSelectedIndividual();
            if(individual != null){
            	ContentResolver resolver = getContentResolver();
            	Cursor cursor = Queries.getSocialGroupsByIndividualExtId(resolver,individual.getExtId());
            	if (cursor.moveToFirst()) {
            		SocialGroup socialGroup = Converter.convertToSocialGroup(cursor);
            		this.sg = socialGroup;
            		locationVisit.getLocation().setHead(sg.getGroupHead());
            	}
            	cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            hideProgressFragment();
            
            if(this.sg != null){
            	if(locationVisit.getSelectedIndividual().getExtId().equalsIgnoreCase(sg.getGroupHead())){
            		Toast.makeText(UpdateActivity.this, getString(R.string.is_migrating_hoh_warning_lbl) , Toast.LENGTH_LONG).show();
            	}
            	else{
            		//Toast.makeText(UpdateActivity.this, getString(R.string.is_not_migrating_hoh_warning_lbl), Toast.LENGTH_LONG).show();
            	}
            }
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
        builder.setTitle(getString(R.string.update_build_pregnancy_lbr_count_msg)).setCancelable(true)
                .setItems(new String[] {"1", "2", "3", "4" }, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showProgressFragment();
                        new PregnancyOutcomeFatherSelectionTask(which+1).execute();
                    }
                });
        builder.setOnCancelListener(this);
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

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UpdateActivity.this);
            alertDialogBuilder.setTitle(getString(R.string.update_pregoutcome_choose_father));
            alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.setCancelable(true);
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

    	private SocialGroup sg;
    	
        @Override
        protected Void doInBackground(Void... params) {
        	ContentResolver resolver = getContentResolver();
        	Cursor cursor = Queries.getSocialGroupsByIndividualExtId(resolver,locationVisit.getSelectedIndividual().getExtId());
        	if (cursor.moveToFirst()) {
        		SocialGroup socialGroup = Converter.convertToSocialGroup(cursor);
        		this.sg = socialGroup;
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
        	if(this.sg != null){
        		deathCreation = true;
        		if(locationVisit.getSelectedIndividual().getExtId().equalsIgnoreCase(locationVisit.getLocation().getHead())){
	    			updatable = new DeathOfHoHUpdate();
	    	        Bundle bundle = new Bundle();
	    	        bundle.putString("sg", sg.getExtId());
	    	        bundle.putString("hohExtId", sg.getGroupHead());
	    	        UpdateActivity.this.getLoaderManager().restartLoader(INDIVIDUALS_IN_SOCIAL_GROUP_ACTIVE, bundle, UpdateActivity.this);
	    	        filledForm = formFiller.fillDeathOfHouseholdForm(locationVisit, sg);
	    		}
	    		else{
	    			loadForm(SELECTED_XFORM);
	    		}
        	}
    		else{
    			Toast.makeText(UpdateActivity.this, getString(R.string.create_membership), Toast.LENGTH_LONG).show();
    		}       
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
                UpdateActivity.this.contentUri = contentUri;
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
        if(rows > 0){        
        	int highestRoundNumber = -1;
        	Round latestRound = null;
        	while(cursor.moveToNext()){
        		Round currentRound = Converter.convertToRound(cursor);
        		String roundNumberString = currentRound.getRoundNumber();
        		try{
        			int currentRoundNumber = Integer.parseInt(roundNumberString);
        			if(currentRoundNumber > highestRoundNumber){
        				latestRound = currentRound;
        				highestRoundNumber = currentRoundNumber;
        			}
        		}
        		catch(NumberFormatException nfe){}
        	}   
        	
    		if(highestRoundNumber == 0){
    			Toast.makeText(this, getString(R.string.round_number_found_lbl), Toast.LENGTH_LONG).show();
    		}
    		else if(highestRoundNumber > 0){
    			onRoundSelected(latestRound);
    		}
    		else{
    			Toast.makeText(this, getString(R.string.couldnt_parse_roundnr_lbl), Toast.LENGTH_LONG).show();
    		}
        }
        else{
        	Toast.makeText(this, getString(R.string.no_round_info_found_lbl), Toast.LENGTH_LONG).show();
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
        updateButtons(0);
        onHierarchy2();
    }

    private void registerTransitions() {
        sf.registerTransitions(stateMachine);
        ef.registerTransitions(stateMachine);
    }

    public void onHierarchy2Selected(LocationHierarchy subregion) {
        locationVisit.setHierarchy2(subregion);
        stateMachine.transitionTo("Select Hierarchy 3");
        updateButtons(1);
        if (levelNumbers==2) {
        	stateMachine.transitionTo("Select Round");
        	onRound();	
        } else {
            stateMachine.transitionTo("Select Hierarchy 3");
        	onHierarchy3();
        }   
    }
    
    public void onHierarchy3Selected(LocationHierarchy hierarchy) {
        locationVisit.setHierarchy3(hierarchy);
        stateMachine.transitionTo("Select Hierarchy 4");
        updateButtons(2);
        if (levelNumbers==3) {
        	stateMachine.transitionTo("Select Round");
        	onRound();	
        } else {
            stateMachine.transitionTo("Select Hierarchy 4");
        	onHierarchy4();
        }   
    }
    
    public void onHierarchy4Selected(LocationHierarchy village) {
        locationVisit.setHierarchy4(village);
        updateButtons(3);
        if (levelNumbers==4) {
        	stateMachine.transitionTo("Select Round");
        	onRound();	
        } else {
            stateMachine.transitionTo("Select Hierarchy 5");
        	onHierarchy5();
        }
    }
        
	public void onHierarchy5Selected(LocationHierarchy hierarchy5) {
        locationVisit.setHierarchy5(hierarchy5);
        updateButtons(4);
        if (levelNumbers==5) {
        	stateMachine.transitionTo("Select Round");   	
        	onRound();	
        } else {
        	stateMachine.transitionTo("Select Hierarchy 6");
        	onHierarchy6();
        }
	}
	
	public void onHierarchy6Selected(LocationHierarchy hierarchy6) {
        locationVisit.setHierarchy6(hierarchy6);
        updateButtons(5);
        if (levelNumbers==6) {
        	stateMachine.transitionTo("Select Round"); 
        	onRound();	
        } else {
        	stateMachine.transitionTo("Select Hierarchy 7");
        	onHierarchy7();
        }		
	}
	
	public void onHierarchy7Selected(LocationHierarchy hierarchy7) {
        locationVisit.setHierarchy7(hierarchy7);
        updateButtons(6);
        if (levelNumbers==7) {
        	stateMachine.transitionTo("Select Round"); 
        	onRound();	
        } else {
        	stateMachine.transitionTo("Select Hierarchy 8");
        	onHierarchy8();
        }		
	}
		
	public void onHierarchy8Selected(LocationHierarchy hierarchy8) {
	     locationVisit.setHierarchy8(hierarchy8);
        	onRound();	
	}
        
    private void updateButtons(int level){
    }

    public void onRoundSelected(Round round) {
        locationVisit.setRound(round);
        stateMachine.transitionTo("Select Location");
    }

    public void onLocationSelected(Location location) {
        locationVisit.setLocation(location);
        stateMachine.transitionTo("Create Visit");
    }

    public void onIndividualSelected(Individual individual) {
        locationVisit.setSelectedIndividual(individual);
        stateMachine.transitionTo("Select Event");
        
        if(this.menuItemForm != null) {
        	this.menuItemForm.setVisible(true);
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {	
    	
    	showLoadingDialog();
    	
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
	        case INDIVIDUALS_IN_SOCIAL_GROUP:
	        {
	        	String sg = args.getString("sg");
	        	String hohExtId = args.getString("hohExtId");
	            uri = OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE;
	            String where = "((" + OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID + " = ? ) AND (" + OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID + " != ? ))";
	            String[] criteria = new String[] { sg, hohExtId };
	            return new CursorLoader(this, uri, null, where, criteria, null);
	        }
	        case INDIVIDUALS_IN_SOCIAL_GROUP_ACTIVE:
	        {	        	
	        	Cursor cursor = Queries.getSocialGroupsByIndividualExtId(getContentResolver(), locationVisit.getSelectedIndividual().getExtId());	        	
	        	if(cursor.moveToNext()){	        	
	        		int columnIndex = cursor.getColumnIndex("_id");
	        		int extIdIndex = cursor.getColumnIndex("extId");
	        		if(columnIndex > -1  && extIdIndex > -1) {
	        			String extId = cursor.getString(extIdIndex);
	        			cursor.close();
	        			
	        			uri = OpenHDS.Individuals.CONTENT_SG_ACTIVE_URI_BASE.buildUpon().appendPath(extId).build();
	        			String where = "s." + OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID + " != ?";
	    	            String[] criteria = new String[] { locationVisit.getSelectedIndividual().getExtId() };	    	            
	    	            return new CursorLoader(this, uri, null, where, criteria, null);
	        		}
	        	}       	
	        }
        }

        return new CursorLoader(this, uri, null, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {      
    	
    	hideProgressFragment();
    	
    	restoreState();
    	
    	dismissLoadingDialog();
    	    	
        if(loader.getId() == -1 ) return;
        
        if (cursor.getCount() == 1 && loader.getId() == SOCIAL_GROUP_FOR_INDIVIDUAL) {
            cursor.moveToFirst();
            appendSocialGroupFromCursor(cursor);
            return;
        } else if(loader.getId() == SOCIAL_GROUP_AT_LOCATION){
        	handleSocialGroup(loader, cursor);
        }
        
        else if(loader.getId() == INDIVIDUALS_IN_SOCIAL_GROUP_ACTIVE){  
        	List<Individual> uniqueIndividuals = new ArrayList<Individual>();
        	
	        if(cursor.moveToNext()){
		        List<String> uniqueExtIds = new ArrayList<String>();     
		        List<Individual> uniqueIndividualsOverMinimumAge = new ArrayList<Individual>();
		        
		        
	        	while(!cursor.isAfterLast()){
	        		String individualExtId = cursor.getString(cursor.getColumnIndex(OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID));
	        		
					if(!uniqueExtIds.contains(individualExtId)){
						uniqueExtIds.add(individualExtId);
		        		Cursor individualCursor = Queries.getIndividualByExtId(this.getContentResolver(), individualExtId);
		        		if(individualCursor.moveToNext()){
	        				Individual individual = Converter.convertToIndividual(individualCursor);
	        				if(individual != null && individualMeetsMinimumAge(individual)){
	        					uniqueIndividualsOverMinimumAge.add(individual);
	        				}
	        				if(individual != null){
	        					uniqueIndividuals.add(individual);
	        				}
		        		}
		        		individualCursor.close();		        		
					}
	        		cursor.moveToNext();
	        	}
	        	   	
	        	final List<Individual> list = uniqueIndividualsOverMinimumAge; 
	        	final List<Individual> list2 = uniqueIndividuals;
        		@SuppressWarnings({ "unchecked", "rawtypes" })
				ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, list) {
        			@Override
      			  	public View getView(int position, View convertView, android.view.ViewGroup parent) {
        				View view = super.getView(position, convertView, parent);
        				TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        				TextView text2 = (TextView) view.findViewById(android.R.id.text2);

        				text1.setTextColor(Color.BLACK);
        				text1.setText(list.get(position).getFirstName() + " " +list.get(position).getLastName());
        				text2.setText("(" + list.get(position).getExtId() + ")");
        				return view;
      			  	}
      			};
    	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	        builder.setTitle(getString(R.string.pls_select_new_hoh_lbl));
    	        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
    	            public void onClick(DialogInterface dialog, int which) {
    	            	ListView lw = ((AlertDialog)dialog).getListView();    	            	
    	            	Object checkedItem = lw.getItemAtPosition(which);
    	            	
    	            	Individual newHoh = null;
    	            	List<Individual> members = new ArrayList<Individual>();
    	            	
    	            	if(checkedItem instanceof Individual){
    	            		newHoh = (Individual)checkedItem;
    	            	}
    	            	
    	            	//Remove selected individual from list
    	            	Individual selectedIndividual = null;
    	            	for(Individual ind : list2){
    	            		if(newHoh != null && newHoh.getExtId().equalsIgnoreCase(ind.getExtId())){
    	            			selectedIndividual = ind;
    	            		}
    	            	}
    	            	if(selectedIndividual != null){
    	            		list2.remove(selectedIndividual);
    	            		members = list2;
    	            	}
    	            	
	            		selectedNewHoh(newHoh, members);
    	            }
    	        });
    	        builder.setNegativeButton(getString(R.string.cancel_lbl), new DialogInterface.OnClickListener() {
    	        	public void onClick(DialogInterface dialog, int which) {
    	        		deathCreation = false;
    	        	}
    	        });
    	        AlertDialog dlg = builder.create();
    	        dlg.show();      
	        }
	        else{    	        
            	ContentResolver resolver = getContentResolver();
            	Cursor c_cursor = Queries.getSocialGroupsByIndividualExtId(resolver,locationVisit.getSelectedIndividual().getExtId());
            	SocialGroup socialGroup = null;
            	if (c_cursor.moveToFirst()) {
            		socialGroup = Converter.convertToSocialGroup(c_cursor);
            		locationVisit.getLocation().setHead(socialGroup.getGroupHead());
            	}
            	
                filledForm = formFiller.fillDeathForm(locationVisit, socialGroup);    
                updatable = new DeathUpdate();
                c_cursor.close();
                
                loadForm(SELECTED_XFORM);
	        }
	        cursor.close();
	        getLoaderManager().destroyLoader(loader.getId());
        }
        else{
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle(getString(R.string.update_load_finished_select_hh_msg));
	        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor,
	                new String[] { OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME,
	                        OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID }, new int[] { android.R.id.text1,
	                        android.R.id.text2 }, 0);
	        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
	
	            public void onClick(DialogInterface dialog, int which) {
	                Cursor cursor = (Cursor) householdDialog.getListView().getItemAtPosition(which);
	                appendSocialGroupFromCursor(cursor);
	            }
	        });
	        builder.setNegativeButton(getString(R.string.cancel_lbl), null);
	        householdDialog = builder.create();
	        householdDialog.show();
        }
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
    
    private void selectedNewHoh(final Individual newHoh, final List<Individual> members){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.update_load_finished_select_hh_msg));
        if(newHoh != null){
        	builder.setMessage(getString(R.string.selected_new_hoh_lbl)  + " " + newHoh.getFirstName() + " " + newHoh.getLastName() +  " (" + getString(R.string.with_extid_lbl)  + " " + newHoh.getExtId() + ")");
        }
        builder.setNegativeButton(getString(R.string.cancel_lbl), new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        		if(deathCreation){ //We are in Death of HoH mode
        			deathCreation = false;
        		}
        		else{ //We are in Change HoH mode
        			stateMachine.transitionTo(UpdateActivity.SELECT_INDIVIDUAL);
        		}
        	}
        });
        builder.setPositiveButton(R.string.continue_lbl, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	filledForm.setIndividualA(newHoh.getExtId());
            	filledForm.setHouseHoldMembers(members);
            	if(!deathCreation){ //We are in Change HoH mode
            		inChangeHoH = true;
            	}
            	loadForm(SELECTED_XFORM);
            }
        });
        householdDialog = builder.create();
        householdDialog.show();  
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

	 private void handleSocialGroup(Loader<Cursor> loader, Cursor cursor){	
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	
	    	if(cursor.getCount() > 0){
	    		builder.setTitle(getString(R.string.select_household_lbl));
	        	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor,
	        			new String[] { OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME,
	        			OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID }, new int[] { android.R.id.text1,
	        			android.R.id.text2 }, 0 )
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
	        householdDialog = builder.create();
	        householdDialog.show();    	
	    }
	 
	    private void searchSocialGroup(){ 	
	    	startFilterActivity(FILTER_SOCIALGROUP);
	    }
	    
		public void onChangeHouseholdHead() {
			if(locationVisit.getSocialgroup() != null){
				showProgressFragment();
				new CreateChangeHoHTask().execute();
			}
			else{
				Toast.makeText(this, "No Household defined", Toast.LENGTH_LONG).show();
				reloadState();
			}
		}
		
		private class CreateChangeHoHTask extends AsyncTask<Void, Void, Boolean> {
	    	private SocialGroup sg;
	    	
			@Override
			protected Boolean doInBackground(Void... arg0) {				
				boolean foundHoH = false;
	        	Cursor cursor = Queries.getSocialGroupByName(getContentResolver(), locationVisit.getSocialgroup().getGroupName());
	        	if (cursor.moveToFirst()) {
	        		SocialGroup socialGroup = Converter.convertToSocialGroup(cursor);
	        		this.sg = socialGroup;
	        		if(sg != null && sg.getGroupHead().trim().length() > 0 && locationVisit.getLocation() != null){
	        			String hohextId = sg.getGroupHead();
	        			locationVisit.getLocation().setHead(hohextId);
	    	            filledForm = formFiller.fillChangeHoHForm(locationVisit, sg);
	    	            filledForm.setIndividualExtId(hohextId);
	    	            updatable = new HeadOfHouseholdUpdate();
	    	            foundHoH = true;
	        		}
	        	}   
	            cursor.close();
				return foundHoH;
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				hideProgressFragment();				
				if(result){
	    			changeHoH(sg);
				}
				else{
					Toast.makeText(UpdateActivity.this, getString(R.string.no_hoh_found), Toast.LENGTH_LONG).show();
				}
			}
		}
		
		private void changeHoH(SocialGroup sg){  
        	Cursor cursor = Queries.getActiveIndividualsByResidency(getContentResolver(), locationVisit.getLocation().getExtId());
        	List<Individual> allIndividuals = Converter.toIndividualList(cursor);
	        List<Individual> individualsEligible = new ArrayList<Individual>();
	        List<Individual> individualsActive = new ArrayList<Individual>();
	        
        	for(Individual individual : allIndividuals){
        		if(individual != null){      					
        			//Dont add the current head of household
        			if(individual.getExtId().equalsIgnoreCase(sg.getGroupHead())){
        				individualsActive.add(individual); // Active
        				String info = individual.getFirstName() + " " + individual.getLastName() + " (" + individual.getExtId() + ")";
                    	Toast.makeText(UpdateActivity.this, getString(R.string.current_hoh)+ info, Toast.LENGTH_LONG).show();
        				continue;
        			}
        			        			
        			if(!"DTH".equals(individual.getEndType()) || !"OMG".equals(individual.getEndType())){ //These should never be the case, since we load only Active individuals
        				individualsActive.add(individual);
        				if(individualMeetsMinimumAge(individual)){
        					individualsEligible.add(individual);
        				}
        			}
        		}	
        		cursor.moveToNext();
        	}     	   	
        	cursor.close();
        	
        	if(individualsEligible.size() == 0){
        		Toast.makeText(this, "No valid successor found!", Toast.LENGTH_LONG).show();
        		reloadState();
        		return;
        	}
        	       	
        	final List<Individual> list = individualsEligible; 
        	final List<Individual> list2 = individualsActive; 
    		@SuppressWarnings({ "unchecked", "rawtypes" })
			ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, list) {
    			@Override
  			  	public View getView(int position, View convertView, android.view.ViewGroup parent) {
    				View view = super.getView(position, convertView, parent);
    				TextView text1 = (TextView) view.findViewById(android.R.id.text1);
    				TextView text2 = (TextView) view.findViewById(android.R.id.text2);

    				Individual ind = list.get(position);
    				text1.setTextColor(Color.BLACK);
    				text1.setText(ind.getFirstName() + " " + ind.getLastName());
    				text2.setText("(" + ind.getExtId() + ")");
    				return view;
  			  	}
  			};
  			  		    
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setOnCancelListener(this);
	        builder.setTitle(getString(R.string.change_household_head_select_lbl) + " (" + sg.getGroupHead() + ")");
	        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int which) {
	        		ListView lw = ((AlertDialog)dialog).getListView();    	            	
	            	Object checkedItem = lw.getItemAtPosition(which);
	            	       		            	
	            	if(checkedItem instanceof Individual){
	            		//Remove selected new HoH from members list
	            		Individual newHoH = (Individual)checkedItem;
		            	list2.remove(newHoH); 	
		                //Set new HoH in form
		                filledForm.setIndividualA(newHoH.getExtId());
		                filledForm.setIndividualFirstName(newHoH.getFirstName() + " " + newHoH.getLastName());                
		            	selectedNewHoh(newHoH, list2);
	            	}	            	
	            }
	        });
	        builder.setNegativeButton(getString(R.string.cancel_lbl), new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int which) {
	        		stateMachine.transitionTo(UpdateActivity.SELECT_INDIVIDUAL);
	        	}
	        });
	        AlertDialog dlg = builder.create();
	        dlg.show();  
	    }

		//Dialog cancelled by pressing the back button, refresh state
		public void onCancel(DialogInterface arg0) {
			reloadState();
		}
		
		private void reloadState(){
			stateMachine.transitionTo(stateMachine.getState());
		}
}

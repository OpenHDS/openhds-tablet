package org.openhds.mobile.fragment;

import java.util.ArrayList;
import java.util.List;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.activity.FilterSocialGroupActivity;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.LocationVisit;
import org.openhds.mobile.model.Round;
import org.openhds.mobile.model.SocialGroup;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ValueFragment is responsible for showing a list of entities, and then
 * notifying the activity using this fragment which entity has been selected. An
 * entity can be defined as: Region, Sub Region, Village, Round, Location and
 * Individual
 */
public class ValueFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private static String START_HIERARCHY_LEVEL_NAME; //"Region";
    
    // loader identifiers
    private static final int HIERARCHY_LOADER = 9990;
    private static final int REGION_LOADER = 9991;
    private static final int ROUND_LOADER = 9992;
    private static final int LOCATION_LOADER = 9993;
    private static final int INDIVIDUAL_LOADER = 9994;
    private static final int INDIVIDUAL_FILTER_LOADER = 9995;
    private static final int INDIMG_FILTER_LOADER = 9996;
    private static final int INDIVIDUAL18_FILTER_LOADER = 9997;
    private static final int INDIVIDUAL_FILTER_ID_LOADER = 9998; 
    private static final int LOCATION_FILTER_ID_LOADER = 9999;
    private static final int SOCIALGROUP_FILTER_LOADER = 99910;
	private static final int INDIVIDUAL_LOADER2 = 99911;


    // create the column mappings so they don't need to be recreated on every load
    private static final String[] HIERARCHY_COLUMNS = new String[] { OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME,
            OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID};
    private static final String[] ROUNDS_COLUMNS = new String[] { OpenHDS.Rounds.COLUMN_ROUND_NUMBER,
            OpenHDS.Rounds.COLUMN_ROUND_STARTDATE};
    private static final String[] LOCATION_COLUMNS = new String[] { OpenHDS.Locations.COLUMN_LOCATION_NAME,
            OpenHDS.Locations.COLUMN_LOCATION_EXTID};
    private static final String[] INDIVIDUAL_COLUMNS = new String[] { OpenHDS.Individuals.COLUMN_INDIVIDUAL_FULLNAME,
            OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID, OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB, OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID};
    private static final String[] SOCIALGROUP_COLUMNS = new String[] { OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME,
        OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID};    

    private static final int[] VIEW_BINDINGS = new int[] { android.R.id.text1, android.R.id.text2 };
    private static final int[] VIEW_BINDINGSI = new int[] { android.R.id.text1, android.R.id.text2,R.id.text3,R.id.text4};



    private SimpleCursorAdapter adapter;

    // since this fragment displays different types of entities, it needs to
    // keep track of which one is currently showing
    private Displayed listCurrentlyDisplayed;
    
    private ValueListener listener;
    
    private OnlyOneEntryListener onlyOneEntryListener;

    public enum Displayed {
        NONE, HIERARCHY_1, HIERARCHY_2, HIERARCHY_3, HIERARCHY_4, HIERARCHY_5, HIERARCHY_6, HIERARCHY_7, HIERARCHY_8, ROUND, LOCATION, INDIVIDUAL, SOCIALGROUP;
    }

    public interface ValueListener {
        void onHierarchy1Selected(LocationHierarchy hierarchy);

        void onHierarchy2Selected(LocationHierarchy subregion);

        void onHierarchy3Selected(LocationHierarchy hierarchy);
        
        void onHierarchy4Selected(LocationHierarchy village);
        
        void onHierarchy5Selected(LocationHierarchy hierarchy5);

        void onHierarchy6Selected(LocationHierarchy hierarchy6);

        void onHierarchy7Selected(LocationHierarchy hierarchy7);
        
        void onHierarchy8Selected(LocationHierarchy hierarchy8);

        void onRoundSelected(Round round);

        void onLocationSelected(Location location);

        void onIndividualSelected(Individual individual);
    }
    
    public interface OnlyOneEntryListener {
    	public enum Entity { INDIVIDUAL };
    	void handleResult(Entity entity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
        adapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item, null, HIERARCHY_COLUMNS,
                VIEW_BINDINGS, 0);
        
        setListAdapter(adapter);
        
        // Start out with a progress indicator.

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(-1, null, this);
        
        listCurrentlyDisplayed = Displayed.NONE;
    }

    
    public void setSTART_HIERARCHY_LEVEL_NAME(
			String sTART_HIERARCHY_LEVEL_NAME) {
		START_HIERARCHY_LEVEL_NAME = sTART_HIERARCHY_LEVEL_NAME;
	}
    
    public String getSTART_HIERARCHY_LEVEL_NAME(){
    	return START_HIERARCHY_LEVEL_NAME;
    }
    

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        try {
            listener = (ValueListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }
	
	public void addOnlyOneEntryListener(OnlyOneEntryListener listener){
		this.onlyOneEntryListener = listener;
	}

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = (Cursor) adapter.getItem(position);

        switch (listCurrentlyDisplayed) {
        case HIERARCHY_1:
            LocationHierarchy region = Converter.convertToHierarchy(cursor);
            listener.onHierarchy1Selected(region);
            break;
        case HIERARCHY_2:
            LocationHierarchy subregion = Converter.convertToHierarchy(cursor);
            listener.onHierarchy2Selected(subregion);
            break;
        case HIERARCHY_3:
            LocationHierarchy hierarchy = Converter.convertToHierarchy(cursor);
            listener.onHierarchy3Selected(hierarchy);
            break;
        case HIERARCHY_4:
            LocationHierarchy village = Converter.convertToHierarchy(cursor);
            listener.onHierarchy4Selected(village);
            break;
        case HIERARCHY_5:
            LocationHierarchy hierarchy5 = Converter.convertToHierarchy(cursor);
            listener.onHierarchy5Selected(hierarchy5);
            break;
        case HIERARCHY_6:
            LocationHierarchy hierarchy6 = Converter.convertToHierarchy(cursor);
            listener.onHierarchy6Selected(hierarchy6);
            break;
        case HIERARCHY_7:
            LocationHierarchy hierarchy7 = Converter.convertToHierarchy(cursor);
            listener.onHierarchy7Selected(hierarchy7);
            break;
        case HIERARCHY_8:
            LocationHierarchy hierarchy8 = Converter.convertToHierarchy(cursor);
            listener.onHierarchy8Selected(hierarchy8);
            break;
        case ROUND:
            Round round = Converter.convertToRound(cursor);
            listener.onRoundSelected(round);
            break;
        case LOCATION:
            Location location = Converter.convertToLocation(cursor);
            listener.onLocationSelected(location);
            break;
        case INDIVIDUAL:
            Individual individual = Converter.convertToIndividual(cursor);
            listener.onIndividualSelected(individual);
            break;
	    case SOCIALGROUP:
	    	SocialGroup sg = Converter.convertToSocialGroup(cursor);
	    	
	    	//Cast listener to FilterSocialGroupActivity
	        try {
	            FilterSocialGroupActivity filterSocialGroup = (FilterSocialGroupActivity)listener;
	            filterSocialGroup.onSocialGroupSelected(sg);
	        } catch (ClassCastException e) {
	            throw new ClassCastException("Could not cast listener to FilterSocialGroupActivity.");
	        }
	        break;
		default:
			break;
	    }        

        adapter.swapCursor(null);
    }

    public void loadLocationHierarchy() {
        listCurrentlyDisplayed = Displayed.HIERARCHY_1;
        getLoaderManager().restartLoader(HIERARCHY_LOADER, null, this);
    }

    public void loadHierarchy2(String parentExtId) {
        listCurrentlyDisplayed = Displayed.HIERARCHY_2;
        loadHierarchyItemsFromParent(parentExtId);
    }
    
    public void loadHierarchy3(String extId) {
        listCurrentlyDisplayed = Displayed.HIERARCHY_3;
        loadHierarchyItemsFromParent(extId);
    }

    private void loadHierarchyItemsFromParent(String parentExtId) {
        Bundle bundle = new Bundle();
        bundle.putString("parentExtId", parentExtId);
        getLoaderManager().restartLoader(REGION_LOADER, bundle, this);
    }

    /**
     * Load a village optionally filtered by a parent external id
     * 
     * @param parentExtId
     *            the parent ext it to filter on, or null to list all villages
     */
    public void loadHierarchy4(String parentExtId) {
        listCurrentlyDisplayed = Displayed.HIERARCHY_4;
        loadHierarchyItemsFromParent(parentExtId);
    }

    public void loadHierarchy5(String parentExtId) {
        listCurrentlyDisplayed = Displayed.HIERARCHY_5;
        loadHierarchyItemsFromParent(parentExtId);
    }
    
    public void loadHierarchy6(String parentExtId) {
        listCurrentlyDisplayed = Displayed.HIERARCHY_6;
        loadHierarchyItemsFromParent(parentExtId);
    }
    
    public void loadHierarchy7(String parentExtId) {
        listCurrentlyDisplayed = Displayed.HIERARCHY_7;
        loadHierarchyItemsFromParent(parentExtId);
    }
    
    public void loadHierarchy8(String parentExtId) {
        listCurrentlyDisplayed = Displayed.HIERARCHY_8;
        loadHierarchyItemsFromParent(parentExtId);
    }
    
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
    	
    	if(arg0 == -1) return null;
    	setListShown(false);
    	
        switch (arg0) {
	        case HIERARCHY_LOADER:
	            adapter.changeCursorAndColumns(null, HIERARCHY_COLUMNS, VIEW_BINDINGS);
	            return new CursorLoader(getActivity(), OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE, null,
	                    OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL + " = ?",
	                    new String[] { START_HIERARCHY_LEVEL_NAME }, null);
	        case REGION_LOADER:
	            adapter.changeCursorAndColumns(null, HIERARCHY_COLUMNS, VIEW_BINDINGS);
	            return buildRegionCursorLoader(arg1);
	        case ROUND_LOADER:
	            adapter.changeCursorAndColumns(null, ROUNDS_COLUMNS, VIEW_BINDINGS);
	            return new CursorLoader(getActivity(), OpenHDS.Rounds.CONTENT_ID_URI_BASE, null, null, null, null);
	        case LOCATION_LOADER:
	            adapter.changeCursorAndColumns(null, LOCATION_COLUMNS, VIEW_BINDINGS);
	            return buildLocationCursorLoader(arg1);
	        case INDIVIDUAL_LOADER:
	            adapter.changeCursorAndColumns(null, INDIVIDUAL_COLUMNS, VIEW_BINDINGSI);
	            return new CursorLoader(getActivity(), OpenHDS.Individuals.CONTENT_SG_ACTIVE_URI_BASE, null,
	                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE + " = ? AND " + OpenHDS.Individuals.COLUMN_RESIDENCE_END_TYPE +"='NA'",
	                    new String[] { arg1.getString("locationExtId")}, null);
	        case INDIVIDUAL_LOADER2:
	            adapter.changeCursorAndColumns(null, INDIVIDUAL_COLUMNS, VIEW_BINDINGSI);
	            if (arg1.getString("socialGroupExtId")!=null) {
	            	return new CursorLoader(getActivity(), OpenHDS.Individuals.CONTENT_SG_ACTIVE_URI_BASE, null,
	                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE + " = ? AND " + OpenHDS.Individuals.COLUMN_RESIDENCE_END_TYPE +"='NA' AND (" + OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID +"= ? OR " + OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID +" is NULL)",
	                    new String[] { arg1.getString("locationExtId") , arg1.getString("socialGroupExtId")}, null);
	            } else {
	            	return new CursorLoader(getActivity(), OpenHDS.Individuals.CONTENT_SG_ACTIVE_URI_BASE, null,
		                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE + " = ? AND " + OpenHDS.Individuals.COLUMN_RESIDENCE_END_TYPE +"='NA'",
		                    new String[] { arg1.getString("locationExtId")}, null);
	            }
	        case INDIVIDUAL_FILTER_LOADER:
	        {
	            adapter.changeCursorAndColumns(null, INDIVIDUAL_COLUMNS, VIEW_BINDINGSI);
	
	            String filter = buildFilter(arg1);
	            String[] args = buildArguments(arg1);
	
	            return new CursorLoader(getActivity(), OpenHDS.Individuals.CONTENT_SG_ACTIVE_URI_BASE, null, filter, args,
	                    OpenHDS.Individuals._ID + " ASC");
	        }
	        case INDIVIDUAL18_FILTER_LOADER:
	        {
	            adapter.changeCursorAndColumns(null, INDIVIDUAL_COLUMNS, VIEW_BINDINGSI);
	
	            String filter2 = buildFilter(arg1);
	            String[] args2 = buildArguments(arg1);
	            if (filter2.length()>0) {
	            	filter2 = filter2 + " AND ";
	            }   
	            //filter2 = filter2 + "(strftime('%Y', date('now')) - substr(dob,7))>13";
	            filter2 = filter2 + "(strftime('%Y', date('now')) - substr(dob,7))>13 AND " + OpenHDS.Individuals.COLUMN_RESIDENCE_END_TYPE +"!='OMG'";
	            return new CursorLoader(getActivity(), OpenHDS.Individuals.CONTENT_SG_ACTIVE_URI_BASE, null, filter2, args2,
	                    OpenHDS.Individuals._ID + " ASC");
	        }
	        case INDIMG_FILTER_LOADER:
	        {
	            adapter.changeCursorAndColumns(null, INDIVIDUAL_COLUMNS, VIEW_BINDINGSI);
	
	            String img = (String) arg1.get("img");
	            arg1.remove("img");

	            String filter1 = img.equals("IMG") ? buildFilter(arg1) : buildFilterOMG(arg1);
	            String[] args1 = buildArguments(arg1);
	
	            return new CursorLoader(getActivity(), OpenHDS.Individuals.CONTENT_SG_ACTIVE_URI_BASE, null, filter1, args1,
	                    OpenHDS.Individuals._ID + " ASC");
	        }
	        case INDIVIDUAL_FILTER_ID_LOADER:
	        {
	            adapter.changeCursorAndColumns(null, INDIVIDUAL_COLUMNS, VIEW_BINDINGSI);
	
	            String filter3 = OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID + " = ?";
	            String[] args3 = new String[] { arg1.getString("extId") };
	            
	            CursorLoader cl = new CursorLoader(getActivity(), OpenHDS.Individuals.CONTENT_SG_ACTIVE_URI_BASE, null, filter3, args3,
	                    OpenHDS.Individuals._ID + " ASC");
	
	            return cl;   
	        }
	        case LOCATION_FILTER_ID_LOADER:
	        {
	            adapter.changeCursorAndColumns(null, LOCATION_COLUMNS, VIEW_BINDINGS);
	
	            String filter = OpenHDS.Locations.COLUMN_LOCATION_EXTID + " = ?";
	            String[] args = new String[] { arg1.getString("extId") };
	            
	            CursorLoader cl = new CursorLoader(getActivity(), OpenHDS.Locations.CONTENT_ID_URI_BASE, null, filter, args,
	                    OpenHDS.Locations._ID + " ASC");
	
	            return cl;                   
	        }
	        case SOCIALGROUP_FILTER_LOADER:
	        {
	            adapter.changeCursorAndColumns(null, SOCIALGROUP_COLUMNS, VIEW_BINDINGS);
	
	            String filter = "";
	            String[] args = new String[]{};
	            
	            String extId = arg1.getString("extId");
	            String groupName = arg1.getString("groupName");
	            
	            StringBuilder filterBuilder = new StringBuilder();
	            List<String> argumentList = new ArrayList<String>();
	            
	            if(extId.length() > 0){
	            	argumentList.add(extId + "%");
	            	filterBuilder.append("upper(" + OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID + ")" + " LIKE upper(?)" );
	            }
	            
	            if(groupName.length() > 0){
	            	argumentList.add(groupName + "%");
	            	
	            	if(extId.length() > 0){
	            		filterBuilder.append(" AND ");
	            	}
	            	filterBuilder.append("upper(" +OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME + ")" + " LIKE upper(?)" );
	            }
	            
	            filter = filterBuilder.toString();
	            args = argumentList.toArray(new String[] {});
	            
	            CursorLoader cl = new CursorLoader(getActivity(), OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, null, filter, args,
	                    OpenHDS.SocialGroups._ID + " ASC");
	
	            return cl;                   
	        }	        
        }

        return null;
    }

    private Loader<Cursor> buildLocationCursorLoader(Bundle arg1) {
        if (TextUtils.isEmpty(arg1.getString("hierarchyExtId"))) {
            return buildCursorLoader(OpenHDS.Locations.CONTENT_ID_URI_BASE, null, null);
        }else {
            return buildCursorLoader(OpenHDS.Locations.CONTENT_ID_URI_BASE, OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY
                    + " = ?", new String[] { arg1.getString("hierarchyExtId") });
        }
    }

    private Loader<Cursor> buildRegionCursorLoader(Bundle arg1) {
        if (TextUtils.isEmpty(arg1.getString("parentExtId"))) {
            return buildCursorLoader(OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE, null, null);
        } else {
            return buildCursorLoader(OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE,
                    OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT + " = ?",
                    new String[] { arg1.getString("parentExtId") });
        }
    }

    private Loader<Cursor> buildCursorLoader(Uri uri, String where, String[] args) {
        return new CursorLoader(getActivity(), uri, null, where, args, null);
    }

    /**
     * Builds an array of strings that will be used as the arguments to an SQL
     * query
     * 
     * @param arg1
     * @return
     */
    private String[] buildArguments(Bundle arg1) {
        List<String> args = new ArrayList<String>();

        if (!TextUtils.isEmpty(arg1.getString("location"))) {
            args.add(arg1.getString("location") + "%");
        }
        if (!TextUtils.isEmpty(arg1.getString("firstName"))) {
            args.add("%" + arg1.getString("firstName") + "%");
        }
        if (!TextUtils.isEmpty(arg1.getString("lastName"))) {
            args.add("%" + arg1.getString("lastName") + "%");
        }
        if (!TextUtils.isEmpty(arg1.getString("gender"))) {
            args.add(arg1.getString("gender"));
        }

        return args.toArray(new String[] {});
    }

    /**
     * Constructs the filtering SQL clause for getting a list of individuals
     * 
     * @param arg1
     *            bundle which contains possible filtering options
     * @return
     */
    private String buildFilter(Bundle arg1) {
        StringBuilder builder = new StringBuilder();

        if (!TextUtils.isEmpty(arg1.getString("location"))) {
            builder.append(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE + " LIKE ?");
        }
        if (!TextUtils.isEmpty(arg1.getString("firstName"))) {
            if (builder.length() > 0)
                builder.append(" AND ");
            builder.append(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRSTNAME + " LIKE ?");
        }
        if (!TextUtils.isEmpty(arg1.getString("lastName"))) {
            if (builder.length() > 0)
                builder.append(" AND ");
            builder.append(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LASTNAME + " LIKE ?");
        }
        if (!TextUtils.isEmpty(arg1.getString("gender"))) {
            if (builder.length() > 0)
                builder.append(" AND ");
            builder.append(OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER + " = ?");
        }
        if (builder.length() > 0)
            builder.append(" AND ");
            
            builder.append(OpenHDS.Individuals.COLUMN_RESIDENCE_END_TYPE).append("!='DTH'");
        
        return builder.toString();
    }

    
    /**
     * Constructs the filtering SQL clause for getting a list of individuals out migrated
     *
     * @param arg1
     * bundle which contains possible filtering options
     * @return
     */
     private String buildFilterOMG(Bundle arg1) {
     StringBuilder builder = new StringBuilder();

     if (!TextUtils.isEmpty(arg1.getString("location"))) {
     builder.append(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE + " LIKE ?");
     }
     if (!TextUtils.isEmpty(arg1.getString("firstName"))) {
     if (builder.length() > 0)
     builder.append(" AND ");
     builder.append(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRSTNAME + " LIKE ?");
     }
     if (!TextUtils.isEmpty(arg1.getString("lastName"))) {
     if (builder.length() > 0)
     builder.append(" AND ");
     builder.append(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LASTNAME + " LIKE ?");
     }
     if (!TextUtils.isEmpty(arg1.getString("gender"))) {
     if (builder.length() > 0)
     builder.append(" AND ");
     builder.append(OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER + " = ?");
     }
     if (builder.length() > 0)
     builder.append(" AND ");

     builder.append(OpenHDS.Individuals.COLUMN_RESIDENCE_END_TYPE).append("='OMG'");

     return builder.toString();
     }
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(adapter != null && cursor != null){
        	
        	if(listCurrentlyDisplayed == Displayed.LOCATION && cursor.getColumnIndex("name") == -1){
//        		System.out.println("Display Location, but cursor doesnt contain index with columnname 'name'");
        	}
        	else
        	{
        		adapter.swapCursor(cursor);
        	}
        	
            if(listCurrentlyDisplayed != null){
    	        //Show different messages depending on currently displayed list
    	        if (listCurrentlyDisplayed.equals(Displayed.INDIVIDUAL)) {
    	        	if(cursor.getCount() == 0){
    	        			Toast.makeText(getActivity(), getString(R.string.no_individuals_found), Toast.LENGTH_LONG).show();
    	        		}
    	        		else if(cursor.getCount() == 1){
    	        			if(onlyOneEntryListener != null)
    	        				onlyOneEntryListener.handleResult(OnlyOneEntryListener.Entity.INDIVIDUAL);
    	        		}
    	        		
    	        		 adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
     	        	        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
     	        	          if (view.getId() == android.R.id.text2)
     	        	            { 
     	        	        	  int visitedIndex = cursor.getColumnIndex("visited");
     	        	              if (visitedIndex>-1) {
	     	        	        	  String visited = cursor.getString(visitedIndex);
	     	        	              int extIdIndex = cursor.getColumnIndex("extId");
	    	        	              String extId = cursor.getString(extIdIndex);
	     	        	              if (visited!=null && visited.equalsIgnoreCase("Yes")) {
	     	        	              TextView indiv = (TextView)view;
	     	        	              indiv.setTextColor(Color.GREEN);
	     	        	              indiv.setText(extId);
     	        	              
	     	        	              }
	     	        	              else {
	     	        	                  TextView extIdView = (TextView)view.findViewById(android.R.id.text2);
	     	        	                  extIdView.setText(extId);
	     	        	              }
     	        	              }
     	        	             return true;

     	        	        }
     	        	          return false;}

     	        	    });
    	        	}
            }        	
        }
        
        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }

    }

    public void onLoaderReset(Loader<Cursor> arg0) {
    	adapter.setViewBinder(null);
        adapter.swapCursor(null);
    }

    public Displayed getListCurrentlyDisplayed() {
		return listCurrentlyDisplayed;
	}

	public void setListCurrentlyDisplayed(Displayed listCurrentlyDisplayed) {
		this.listCurrentlyDisplayed = Displayed.LOCATION;
	}

	/**
     * Loads all rounds
     */
    public void loadRounds() {
        listCurrentlyDisplayed = Displayed.ROUND;
        getLoaderManager().restartLoader(ROUND_LOADER, null, this);
    }

    /**
     * Loads a list of locations that can optionally be filtered by a hierarchy
     * ext id
     * 
     * @param hierarchyExtId
     *            the hierarchy to filter, or null to get a list of all
     *            locations
     */
    public void loadLocations(String hierarchyExtId) {
        listCurrentlyDisplayed = Displayed.LOCATION;
        Bundle bundle = new Bundle();
        bundle.putString("hierarchyExtId", hierarchyExtId);
        getLoaderManager().restartLoader(LOCATION_LOADER, bundle, this);
    }
    
    
    /**
     * Loads a location based on the extId
     * 
     * @param ExtId
     *            the extId to filter
     */
    public void loadLocation(String ExtId) {
        listCurrentlyDisplayed = Displayed.LOCATION;
        Bundle bundle = new Bundle();
        bundle.putString("extId", ExtId);
        getLoaderManager().restartLoader(LOCATION_LOADER, bundle, this);
    }

    /**
     * Load a list of individuals based on their current residency
     * 
     * @param locationVisit
     *            filter by the location ext id (current residency) of the
     *            individual
     * @param vISIT_LEVEL 
     */
    public void loadIndividuals(LocationVisit locationVisit, String visitLevel) {
        listCurrentlyDisplayed = Displayed.INDIVIDUAL;
        Bundle bundle = new Bundle();
        bundle.putString("locationExtId", locationVisit.getLocation().getExtId());
        if (locationVisit.getSocialgroup()!=null) {
        	bundle.putString("socialGroupExtId", locationVisit.getSocialgroup().getExtId());
        }
        if (visitLevel.equalsIgnoreCase("location")) {
        	getLoaderManager().restartLoader(INDIVIDUAL_LOADER, bundle, this);
        } else {
        	getLoaderManager().restartLoader(INDIVIDUAL_LOADER2, bundle, this);
        }
    }

    /**
     * Loads a list of individuals that are filtered by the arguments
     * 
     * @param location
     *            the location id to filter, or null to ignore filtering on
     *            location
     * @param firstName
     *            matches on first name of individual (using SQL LIKE), null to
     *            ignore first name matching
     * @param lastName
     *            matches on last name of individual (using SQL LIKE), null to
     *            ignore last name matching
     * @param gender
     *            filters by individual gender, null to ignore gender filtering
     */
    public void loadFilteredIndividuals(String location, String firstName, String lastName, String gender) {
        listCurrentlyDisplayed = Displayed.INDIVIDUAL;
        Bundle bundle = new Bundle();
        bundle.putString("location", location);
        bundle.putString("firstName", firstName);
        bundle.putString("lastName", lastName);
        bundle.putString("gender", gender);
        getLoaderManager().restartLoader(INDIVIDUAL_FILTER_LOADER, bundle, this);
    }

    public void loadFilteredIndividuals2(String location, String firstName, String lastName, String gender) {
        listCurrentlyDisplayed = Displayed.INDIVIDUAL;
        Bundle bundle = new Bundle();
        bundle.putString("location", location);
        bundle.putString("firstName", firstName);
        bundle.putString("lastName", lastName);
        bundle.putString("gender", gender);
        getLoaderManager().restartLoader(INDIVIDUAL18_FILTER_LOADER, bundle, this);
    }

	public void loadAllFilteredIndividuals(String location, String firstName,
			String lastName, String gender, String img) {
		  listCurrentlyDisplayed = Displayed.INDIVIDUAL;
	        Bundle bundle = new Bundle();
	        bundle.putString("location", location);
	        bundle.putString("firstName", firstName);
	        bundle.putString("lastName", lastName);
	        bundle.putString("gender", gender);
	        bundle.putString("img", img);
	        getLoaderManager().restartLoader(INDIMG_FILTER_LOADER, bundle, this);	
	}
	
    /**
     * Load a single individual based on his id
     * 
     * @param individialExtId
     *            filter by the ext id (userid) of the individual
     */	
	public void loadFilteredIndividualById(String individialExtId){
		listCurrentlyDisplayed = Displayed.INDIVIDUAL;
		Bundle bundle = new Bundle();
		bundle.putString("extId", individialExtId);
		getLoaderManager().restartLoader(INDIVIDUAL_FILTER_ID_LOADER, bundle, this);	
	}
	
    /**
     * Load a single individual based on his id
     * 
     * @param individialExtId
     *            filter by the ext id (userid) of the individual
     */	
	public void loadFilteredLocationById(String locationExtId){
		listCurrentlyDisplayed = Displayed.LOCATION;
		Bundle bundle = new Bundle();
		bundle.putString("extId", locationExtId);
		getLoaderManager().restartLoader(LOCATION_FILTER_ID_LOADER, bundle, this);	
	}	
	
    /**
     * Load a social groups based on their extId and / or groupname
     * 
     * @param extId
     *            filter by the ext id (userid) of the social group
     * @param groupName
     *            filter by the group name (groupname) of the social group     *            
     */	
	public void loadFilteredSocialGroups(String extId, String groupName) {
		listCurrentlyDisplayed = Displayed.SOCIALGROUP;
        Bundle bundle = new Bundle();
        bundle.putString("extId", extId);
        bundle.putString("groupName", groupName);
        getLoaderManager().restartLoader(SOCIALGROUP_FILTER_LOADER, bundle, this);	
	}	
	
	public void selectItemNoInList(int position){
		final int mActivePosition = position;
    	
		    getListView().postDelayed(new Runnable() {
		        public void run() {
		        	ListView listView = getListView(); // Save a local reference rather than calling `getListView()` three times
		            if(listView.getCount() > 0){
		            	listView.setSelection(mActivePosition);
		            	listView.performItemClick(listView.getChildAt(0), mActivePosition, mActivePosition);
		            }
		        }
		    }, 500);		
	}
}

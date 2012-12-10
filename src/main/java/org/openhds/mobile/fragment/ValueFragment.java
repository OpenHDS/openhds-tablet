package org.openhds.mobile.fragment;

import java.util.ArrayList;
import java.util.List;

import org.openhds.mobile.Converter;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.Round;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * ValueFragment is responsible for showing a list of entities, and then
 * notifying the activity using this fragment which entity has been selected. An
 * entity can be defined as: Region, Sub Region, Village, Round, Location and
 * Individual
 */
public class ValueFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private static final String START_HIERARCHY_LEVEL_NAME = "Ward";
    // loader identifiers
    private static final int HIERARCHY_LOADER = 0;
    private static final int REGION_LOADER = 1;
    private static final int ROUND_LOADER = 2;
    private static final int LOCATION_LOADER = 3;
    private static final int INDIVIDUAL_LOADER = 4;
    private static final int INDIVIDUAL_FILTER_LOADER = 5;

    // create the column mappings so they don't need to be recreated on every
    // load
    private static final String[] HIERARCHY_COLUMNS = new String[] { OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME,
            OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID };
    private static final String[] ROUNDS_COLUMNS = new String[] { OpenHDS.Rounds.COLUMN_ROUND_NUMBER,
            OpenHDS.Rounds.COLUMN_ROUND_STARTDATE };
    private static final String[] LOCATION_COLUMNS = new String[] { OpenHDS.Locations.COLUMN_LOCATION_NAME,
            OpenHDS.Locations.COLUMN_LOCATION_EXTID };
    private static final String[] INDIVIDUAL_COLUMNS = new String[] { OpenHDS.Individuals.COLUMN_INDIVIDUAL_FULLNAME,
            OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID };

    private static final int[] VIEW_BINDINGS = new int[] { android.R.id.text1, android.R.id.text2 };

    private SimpleCursorAdapter adapter;

    // since this fragment displays different types of entities, it needs to
    // keep track of which one is currently showing
    private Displayed listCurrentlyDisplayed;
    private ValueListener listener;

    private enum Displayed {
        HIERARCHY_1, HIERARCHY_2, HIERARCHY_3, HIERARCHY_4, ROUND, LOCATION, INDIVIDUAL;
    }

    public interface ValueListener {
        void onHierarchy1Selected(LocationHierarchy hierarchy);

        void onHierarchy2Selected(LocationHierarchy subregion);

        void onHierarchy3Selected(LocationHierarchy hierarchy);
        
        void onHierarchy4Selected(LocationHierarchy village);

        void onRoundSelected(Round round);

        void onLocationSelected(Location location);

        void onIndividualSelected(Individual individual);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (ValueListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }

        adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_2, null, HIERARCHY_COLUMNS,
                VIEW_BINDINGS, 0);
        setListAdapter(adapter);
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

    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
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
            adapter.changeCursorAndColumns(null, INDIVIDUAL_COLUMNS, VIEW_BINDINGS);
            return new CursorLoader(getActivity(), OpenHDS.Individuals.CONTENT_ID_URI_BASE, null,
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE + " = ?",
                    new String[] { arg1.getString("locationExtId") }, null);
        case INDIVIDUAL_FILTER_LOADER:
            adapter.changeCursorAndColumns(null, INDIVIDUAL_COLUMNS, VIEW_BINDINGS);

            String filter = buildFitler(arg1);
            String[] args = buildArguments(arg1);

            return new CursorLoader(getActivity(), OpenHDS.Individuals.CONTENT_ID_URI_BASE, null, filter, args,
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID + " ASC");
        }

        return null;
    }

    private Loader<Cursor> buildLocationCursorLoader(Bundle arg1) {
        if (TextUtils.isEmpty(arg1.getString("hierarchyExtId"))) {
            return buildCursorLoader(OpenHDS.Locations.CONTENT_ID_URI_BASE, null, null);
        } else {
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
            args.add(arg1.getString("location"));
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
    private String buildFitler(Bundle arg1) {
        StringBuilder builder = new StringBuilder();

        if (!TextUtils.isEmpty(arg1.getString("location"))) {
            builder.append(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE + " = ?");
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

        return builder.toString();
    }

    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        adapter.swapCursor(arg1);
    }

    public void onLoaderReset(Loader<Cursor> arg0) {
        adapter.swapCursor(null);
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
     * Load a list of individuals based on their current residency
     * 
     * @param extId
     *            filter by the location ext id (current residency) of the
     *            individual
     */
    public void loadIndividuals(String extId) {
        listCurrentlyDisplayed = Displayed.INDIVIDUAL;
        Bundle bundle = new Bundle();
        bundle.putString("locationExtId", extId);
        getLoaderManager().restartLoader(INDIVIDUAL_LOADER, bundle, this);
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

}

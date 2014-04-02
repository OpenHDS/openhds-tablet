package org.openhds.mobile.fragment;

import java.util.ArrayList;
import java.util.List;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.database.queries.Converter;
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
 * ValueLocFragment is responsible for showing a list of locations, and then
 * notifying the activity using this fragment which entity has been selected.
 */
public class ValueLocFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private static final int LOCATION_LOADER = 3;
    private static final int LOCATION_FILTER_LOADER = 5;


    // create the column mappings so they don't need to be recreated on every
    // load
    private static final String[] HIERARCHY_COLUMNS = new String[] { OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME,
           OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID};
    private static final String[] LOCATION_COLUMNS = new String[] { OpenHDS.Locations.COLUMN_LOCATION_NAME,
            OpenHDS.Locations.COLUMN_LOCATION_EXTID};

    private static final int[] VIEW_BINDINGS = new int[] { android.R.id.text1, android.R.id.text2 };

    private SimpleCursorAdapter adapter;

    // since this fragment displays different types of entities, it needs to
    // keep track of which one is currently showing
    private Displayed listCurrentlyDisplayed;
    private ValueListener listener;

    private enum Displayed {
        LOCATION;
    }

    public interface ValueListener {
        void onHierarchy1Selected(LocationHierarchy hierarchy);

        void onHierarchy2Selected(LocationHierarchy subregion);

        void onHierarchy3Selected(LocationHierarchy hierarchy);
        
        void onHierarchy4Selected(LocationHierarchy village);

        void onRoundSelected(Round round);

        void onLocationSelected(Location location);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (ValueListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }

        adapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item, null, HIERARCHY_COLUMNS,
                VIEW_BINDINGS, 0);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = (Cursor) adapter.getItem(position);

        switch (listCurrentlyDisplayed) {
        case LOCATION:
            Location location = Converter.convertToLocation(cursor);
            listener.onLocationSelected(location);
            break;
        }

        adapter.swapCursor(null);
    }

     public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        switch (arg0) {
        case LOCATION_LOADER:
            adapter.changeCursorAndColumns(null, LOCATION_COLUMNS, VIEW_BINDINGS);
            return buildLocationCursorLoader(arg1);
    
        case LOCATION_FILTER_LOADER:
            adapter.changeCursorAndColumns(null, LOCATION_COLUMNS, VIEW_BINDINGS);

            String filter = buildFitler(arg1);
            String[] args = buildArguments(arg1);

            return new CursorLoader(getActivity(), OpenHDS.Locations.CONTENT_ID_URI_BASE, null, filter, args,
                    OpenHDS.Locations.COLUMN_LOCATION_EXTID + " ASC");
        }

        return null;
    }

    private Loader<Cursor> buildLocationCursorLoader(Bundle arg1) {
        if (TextUtils.isEmpty(arg1.getString("extId"))) {
            return buildCursorLoader(OpenHDS.Locations.CONTENT_ID_URI_BASE, null, null);
        } else {
            return buildCursorLoader(OpenHDS.Locations.CONTENT_ID_URI_BASE, OpenHDS.Locations.COLUMN_LOCATION_EXTID
                    + " = ?", new String[] { arg1.getString("extId") });
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
     * Loads a list of Locations that are filtered by the arguments
     * 
     * @param location
     *            the location id to filter, or null to ignore filtering on
     *            location
     */
    public void loadFilteredLocations(String location) {
        listCurrentlyDisplayed = Displayed.LOCATION;
        Bundle bundle = new Bundle();
        bundle.putString("extId", location);
        getLoaderManager().restartLoader(LOCATION_LOADER, bundle, this);
    }

}

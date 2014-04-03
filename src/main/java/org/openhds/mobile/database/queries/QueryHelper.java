package org.openhds.mobile.database.queries;

import java.util.ArrayList;
import java.util.List;

import org.openhds.mobile.activity.CensusActivity;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;

import android.content.ContentResolver;
import android.database.Cursor;

public class QueryHelper {

	public static final String REGION_HIERARCHY_LEVEL_NAME = "Region";
	public static final String MAP_AREA_HIERARCHY_LEVEL_NAME = "MapArea";
	public static final String SECTOR_HIERARCHY_LEVEL_NAME = "Sector";

	public static List<QueryResult> getAll(ContentResolver contentResolver, String state) {

		if (state.equals(CensusActivity.REGION_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver, REGION_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResults(cursor, state);
		} else if (state.equals(CensusActivity.HOUSEHOLD_STATE)) {
			Cursor cursor = Queries.getAllLocations(contentResolver);
			return getLocationQueryResults(cursor, state);
		}

		return null;
	}

	public static List<QueryResult> getChildren(ContentResolver contentResolver, QueryResult qr,
			String childState) {
		String state = qr.getState();

		if (state.equals(CensusActivity.REGION_STATE) || state.equals(CensusActivity.MAP_AREA_STATE)) {
			Cursor cursor = Queries.getHierarchysByParent(contentResolver, qr.getExtId());
			return getHierarchyQueryResults(cursor, childState);
		} else if (state.equals(CensusActivity.SECTOR_STATE)) {
			Cursor cursor = Queries.getLocationsByHierachy(contentResolver, qr.getExtId());
			return getLocationQueryResults(cursor, childState);
		} else if (state.equals(CensusActivity.HOUSEHOLD_STATE)) {
			Cursor cursor = Queries.getIndividualsByResidency(contentResolver, qr.getExtId());
			return getIndividualQueryResults(cursor, childState);
		}

		return null;
	}

	private static List<QueryResult> getHierarchyQueryResults(Cursor cursor, String state) {
		List<QueryResult> results = new ArrayList<QueryResult>();

		if (null == cursor) {
			return results;
		}

		for (LocationHierarchy lh : Converter.toHierarchyList(cursor)) {
			QueryResult qr = new QueryResult();
			qr.setExtId(lh.getExtId());
			qr.setName(lh.getName());
			qr.setState(state);
			results.add(qr);
		}

		return results;
	}

	private static List<QueryResult> getLocationQueryResults(Cursor cursor, String state) {
		List<QueryResult> results = new ArrayList<QueryResult>();

		if (null == cursor) {
			return results;
		}

		for (Location location : Converter.toLocationList(cursor)) {
			QueryResult qr = new QueryResult();
			qr.setExtId(location.getExtId());
			qr.setName(location.getName());
			qr.setState(state);
			results.add(qr);
		}

		return results;
	}

	private static List<QueryResult> getIndividualQueryResults(Cursor cursor, String state) {
		List<QueryResult> results = new ArrayList<QueryResult>();

		if (null == cursor) {
			return results;
		}

		for (Individual individual : Converter.toIndividualList(cursor)) {
			QueryResult qr = new QueryResult();
			qr.setExtId(individual.getExtId());
			qr.setName(individual.getFullName());
			qr.setState(state);
			results.add(qr);
		}

		return results;
	}
}

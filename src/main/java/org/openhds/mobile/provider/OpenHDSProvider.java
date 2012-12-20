package org.openhds.mobile.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.sqlcipher.SQLException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import net.sqlcipher.database.SQLiteQueryBuilder;

import org.openhds.mobile.OpenHDS;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

/**
 * ContentProvider for OpenHDS <br />
 * This class is based on the NotPadProvider sample in the Android SDK
 */
public class OpenHDSProvider extends ContentProvider {
    private static final String TAG = "OpenHDSProvider";

    private static final String DATABASE_PASSWORD_KEY = "database-password";
    private static final String DATABASE_SHARED_PREF = "openhds-provider";
    
    private static final String DATABASE_NAME = "openhds.db";
    private static final int DATABASE_VERSION = 12;

    private static HashMap<String, String> individualsProjectionMap;
    private static HashMap<String, String> locationsProjectionMap;
    private static HashMap<String, String> hierarchyitemsProjectionMap;
    private static HashMap<String, String> roundsProjectionMap;
    private static HashMap<String, String> visitsProjectionMap;
    private static HashMap<String, String> relationshipsProjectionMap;
    private static HashMap<String, String> fieldworkersProjectionMap;
    private static HashMap<String, String> socialgroupsProjectionMap;
    private static HashMap<String, String> socialgroupsJoinProjectionMap;
    private static HashMap<String, String> individualgroupsProjectionMap;

    private static final int INDIVIDUALS = 1;
    private static final int INDIVIDUAL_ID = 2;
    private static final int INDIVIDUAL_SG = 19;
    private static final int LOCATIONS = 3;
    private static final int LOCATION_ID = 4;
    private static final int HIERARCHYITEMS = 5;
    private static final int HIERARCHYITEM_ID = 6;
    private static final int ROUNDS = 7;
    private static final int ROUND_ID = 8;
    private static final int VISITS = 9;
    private static final int VISIT_ID = 10;
    private static final int RELATIONSHIPS = 11;
    private static final int RELATIONSHIP_ID = 12;
    private static final int FIELDWORKERS = 13;
    private static final int FIELDWORKER_ID = 14;
    private static final int SOCIALGROUPS = 15;
    private static final int SOCIALGROUPS_BY_LOCATION = 20;
    private static final int SOCIALGROUPS_BY_INDIVIDUAL = 21;
    private static final int SOCIALGROUP_ID = 16;
    private static final int INDIVIDUALGROUPS = 17;
    private static final int INDIVIDUALGROUP_ID = 18;

    private static final UriMatcher sUriMatcher;
    private DatabaseHelper mOpenHelper;

    private String password;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "individuals", INDIVIDUALS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "individuals/sg/*", INDIVIDUAL_SG);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "individuals/#", INDIVIDUAL_ID);

        sUriMatcher.addURI(OpenHDS.AUTHORITY, "locations", LOCATIONS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "locations/#", LOCATION_ID);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "hierarchyitems", HIERARCHYITEMS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "hierarchyitems/#", HIERARCHYITEM_ID);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "rounds", ROUNDS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "rounds/#", ROUND_ID);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "visits", VISITS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "visits/#", VISIT_ID);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "relationships", RELATIONSHIPS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "relationships/#", RELATIONSHIP_ID);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "fieldworkers", FIELDWORKERS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "fieldworkers/#", FIELDWORKER_ID);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "socialgroups", SOCIALGROUPS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "socialgroups/location/*", SOCIALGROUPS_BY_LOCATION);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "socialgroups/individual/*", SOCIALGROUPS_BY_INDIVIDUAL);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "socialgroups/#", SOCIALGROUP_ID);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "individualgroups", INDIVIDUALGROUPS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, "individualgroups/#", INDIVIDUALGROUP_ID);

        individualsProjectionMap = new HashMap<String, String>();
        individualsProjectionMap.put(OpenHDS.Individuals._ID, OpenHDS.Individuals._ID);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRSTNAME,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRSTNAME);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LASTNAME,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_LASTNAME);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER);
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE);
        // special case to display individuals first name and last name on the
        // value fragment
        individualsProjectionMap.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FULLNAME,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRSTNAME + " || ' ' || "
                        + OpenHDS.Individuals.COLUMN_INDIVIDUAL_LASTNAME + " as "
                        + OpenHDS.Individuals.COLUMN_INDIVIDUAL_FULLNAME);

        locationsProjectionMap = new HashMap<String, String>();
        locationsProjectionMap.put(OpenHDS.Locations._ID, OpenHDS.Locations._ID);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_EXTID, OpenHDS.Locations.COLUMN_LOCATION_EXTID);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY,
                OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_LATITUDE,
                OpenHDS.Locations.COLUMN_LOCATION_LATITUDE);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE,
                OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE);
        locationsProjectionMap.put(OpenHDS.Locations.COLUMN_LOCATION_NAME, OpenHDS.Locations.COLUMN_LOCATION_NAME);

        hierarchyitemsProjectionMap = new HashMap<String, String>();
        hierarchyitemsProjectionMap.put(OpenHDS.HierarchyItems._ID, OpenHDS.HierarchyItems._ID);
        hierarchyitemsProjectionMap.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID,
                OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID);
        hierarchyitemsProjectionMap.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL,
                OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL);
        hierarchyitemsProjectionMap.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME,
                OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME);
        hierarchyitemsProjectionMap.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT,
                OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT);

        roundsProjectionMap = new HashMap<String, String>();
        roundsProjectionMap.put(OpenHDS.Rounds._ID, OpenHDS.Rounds._ID);
        roundsProjectionMap.put(OpenHDS.Rounds.COLUMN_ROUND_STARTDATE, OpenHDS.Rounds.COLUMN_ROUND_STARTDATE);
        roundsProjectionMap.put(OpenHDS.Rounds.COLUMN_ROUND_ENDDATE, OpenHDS.Rounds.COLUMN_ROUND_ENDDATE);
        roundsProjectionMap.put(OpenHDS.Rounds.COLUMN_ROUND_NUMBER, OpenHDS.Rounds.COLUMN_ROUND_NUMBER);

        visitsProjectionMap = new HashMap<String, String>();
        visitsProjectionMap.put(OpenHDS.Visits._ID, OpenHDS.Visits._ID);
        visitsProjectionMap.put(OpenHDS.Visits.COLUMN_VISIT_DATE, OpenHDS.Visits.COLUMN_VISIT_DATE);
        visitsProjectionMap.put(OpenHDS.Visits.COLUMN_VISIT_EXTID, OpenHDS.Visits.COLUMN_VISIT_EXTID);
        visitsProjectionMap.put(OpenHDS.Visits.COLUMN_VISIT_LOCATION, OpenHDS.Visits.COLUMN_VISIT_LOCATION);
        visitsProjectionMap.put(OpenHDS.Visits.COLUMN_VISIT_ROUND, OpenHDS.Visits.COLUMN_VISIT_ROUND);

        relationshipsProjectionMap = new HashMap<String, String>();
        relationshipsProjectionMap.put(OpenHDS.Relationships._ID, OpenHDS.Relationships._ID);
        relationshipsProjectionMap.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A,
                OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A);
        relationshipsProjectionMap.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B,
                OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B);
        relationshipsProjectionMap.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE,
                OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE);

        fieldworkersProjectionMap = new HashMap<String, String>();
        fieldworkersProjectionMap.put(OpenHDS.FieldWorkers._ID, OpenHDS.FieldWorkers._ID);
        fieldworkersProjectionMap.put(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_EXTID,
                OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_EXTID);
        fieldworkersProjectionMap.put(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_FIRSTNAME,
                OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_FIRSTNAME);
        fieldworkersProjectionMap.put(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_LASTNAME,
                OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_LASTNAME);
        fieldworkersProjectionMap.put(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_PASSWORD,
                OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_PASSWORD);

        socialgroupsProjectionMap = new HashMap<String, String>();
        socialgroupsProjectionMap.put(OpenHDS.SocialGroups._ID, OpenHDS.SocialGroups._ID);
        socialgroupsProjectionMap.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID,
                OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID);
        socialgroupsProjectionMap.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPHEAD,
                OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPHEAD);
        socialgroupsProjectionMap.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME,
                OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME);

        // A duplicate of the default social group projection map but is used in
        // join query. The join query requires the
        // columns be explicit with the table alias
        socialgroupsJoinProjectionMap = new HashMap<String, String>();
        socialgroupsJoinProjectionMap.put(OpenHDS.SocialGroups._ID, "s." + OpenHDS.SocialGroups._ID);
        socialgroupsJoinProjectionMap.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID, "s."
                + OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID);
        socialgroupsJoinProjectionMap.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPHEAD, "s."
                + OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPHEAD);
        socialgroupsJoinProjectionMap.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME, "s."
                + OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME);
        socialgroupsJoinProjectionMap.put(OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID, "x."
                + OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID);

        individualgroupsProjectionMap = new HashMap<String, String>();
        individualgroupsProjectionMap.put(OpenHDS.IndividualGroups._ID, OpenHDS.IndividualGroups._ID);
        individualgroupsProjectionMap.put(OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID,
                OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID);
        individualgroupsProjectionMap.put(OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID,
                OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID);
    }

    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + OpenHDS.Individuals.TABLE_NAME + " (" + OpenHDS.Individuals._ID
                    + " INTEGER PRIMARY KEY," + OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRSTNAME + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_LASTNAME + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER + " TEXT,"
                    + OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE + " TEXT);");

            db.execSQL("CREATE TABLE " + OpenHDS.Locations.TABLE_NAME + " (" + OpenHDS.Locations._ID
                    + " INTEGER PRIMARY KEY," + OpenHDS.Locations.COLUMN_LOCATION_EXTID + " TEXT NOT NULL,"
                    + OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY + " TEXT NOT NULL,"
                    + OpenHDS.Locations.COLUMN_LOCATION_LATITUDE + " TEXT,"
                    + OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE + " TEXT," + OpenHDS.Locations.COLUMN_LOCATION_NAME
                    + " TEXT NOT NULL);");

            db.execSQL("CREATE TABLE " + OpenHDS.HierarchyItems.TABLE_NAME + " (" + OpenHDS.HierarchyItems._ID
                    + " INTEGER PRIMARY KEY," + OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID + " TEXT NOT NULL,"
                    + OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL + " TEXT NOT NULL,"
                    + OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME + " TEXT NOT NULL,"
                    + OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT + " TEXT NOT NULL);");

            db.execSQL("CREATE TABLE " + OpenHDS.Rounds.TABLE_NAME + " (" + OpenHDS.Rounds._ID
                    + " INTEGER PRIMARY KEY," + OpenHDS.Rounds.COLUMN_ROUND_ENDDATE + " TEXT NOT NULL,"
                    + OpenHDS.Rounds.COLUMN_ROUND_NUMBER + " TEXT NOT NULL," + OpenHDS.Rounds.COLUMN_ROUND_STARTDATE
                    + " TEXT NOT NULL);");

            db.execSQL("CREATE TABLE " + OpenHDS.Visits.TABLE_NAME + " (" + OpenHDS.Visits._ID
                    + " INTEGER PRIMARY KEY," + OpenHDS.Visits.COLUMN_VISIT_DATE + " TEXT NOT NULL,"
                    + OpenHDS.Visits.COLUMN_VISIT_EXTID + " TEXT NOT NULL," + OpenHDS.Visits.COLUMN_VISIT_LOCATION
                    + " TEXT NOT NULL," + OpenHDS.Visits.COLUMN_VISIT_ROUND + " TEXT NOT NULL);");

            db.execSQL("CREATE TABLE " + OpenHDS.Relationships.TABLE_NAME + " (" + OpenHDS.Relationships._ID
                    + " INTEGER PRIMARY KEY," + OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A
                    + " TEXT NOT NULL," + OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B + " TEXT NOT NULL,"
                    + OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE + " TEXT NOT NULL);");

            db.execSQL("CREATE TABLE " + OpenHDS.FieldWorkers.TABLE_NAME + " (" + OpenHDS.FieldWorkers._ID
                    + " INTEGER PRIMARY KEY," + OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_EXTID + " TEXT NOT NULL,"
                    + OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_FIRSTNAME + " TEXT NOT NULL,"
                    + OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_LASTNAME + " TEXT NOT NULL,"
                    + OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_PASSWORD + " TEXT NOT NULL);");

            db.execSQL("CREATE TABLE " + OpenHDS.SocialGroups.TABLE_NAME + " (" + OpenHDS.SocialGroups._ID
                    + " INTEGER PRIMARY KEY," + OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID + " TEXT NOT NULL,"
                    + OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPHEAD + " TEXT NOT NULL,"
                    + OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME + " TEXT NOT NULL);");

            db.execSQL("CREATE TABLE " + OpenHDS.IndividualGroups.TABLE_NAME + " (" + OpenHDS.IndividualGroups._ID
                    + " INTEGER PRIMARY KEY," + OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID + " TEXT NOT NULL,"
                    + OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID + " TEXT NOT NULL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.IndividualGroups.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.SocialGroups.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.FieldWorkers.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Relationships.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Rounds.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Individuals.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Visits.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.HierarchyItems.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Locations.TABLE_NAME);
            onCreate(db);
        }
    }

    /**
     * 
     * Initializes the provider by creating a new DatabaseHelper. onCreate() is
     * called automatically when Android creates the provider in response to a
     * resolver request from a client.
     */
    @Override
    public boolean onCreate() {

        // Creates a new helper object. Note that the database itself isn't
        // opened until
        // something tries to access it, and it's only created if it doesn't
        // already exist.
        mOpenHelper = new DatabaseHelper(getContext());
        SQLiteDatabase.loadLibs(getContext());
        SharedPreferences sp = getContext().getSharedPreferences(DATABASE_SHARED_PREF, Context.MODE_PRIVATE);
        password = sp.getString(DATABASE_PASSWORD_KEY, "");
        if (password.isEmpty()) {
            password = UUID.randomUUID().toString();
            Editor editor = sp.edit();
            editor.putString(DATABASE_PASSWORD_KEY, password);
            editor.commit();
        }

        // Assumes that any failures will be reported by a thrown exception.
        return true;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int inserted = -1;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase(password);
        db.beginTransaction();
        try {
            inserted = super.bulkInsert(uri, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return inserted;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case INDIVIDUALS:
            qb.setTables(OpenHDS.Individuals.TABLE_NAME);
            qb.setProjectionMap(individualsProjectionMap);
            break;
        case INDIVIDUAL_ID:
            qb.setTables(OpenHDS.Individuals.TABLE_NAME);
            qb.setProjectionMap(individualsProjectionMap);
            qb.appendWhere(OpenHDS.Individuals._ID + "="
                    + uri.getPathSegments().get(OpenHDS.Individuals.NOTE_ID_PATH_POSITION));
            break;
        case INDIVIDUAL_SG:
            qb.setTables(OpenHDS.SocialGroups.TABLE_NAME + " s inner join " + OpenHDS.IndividualGroups.TABLE_NAME
                    + " x on s." + OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID + " = x."
                    + OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID);
            qb.setProjectionMap(socialgroupsJoinProjectionMap);
            break;
        case LOCATIONS:
            qb.setTables(OpenHDS.Locations.TABLE_NAME);
            qb.setProjectionMap(locationsProjectionMap);
            break;
        case LOCATION_ID:
            qb.setTables(OpenHDS.Locations.TABLE_NAME);
            qb.setProjectionMap(locationsProjectionMap);
            qb.appendWhere(OpenHDS.Locations._ID + "="
                    + uri.getPathSegments().get(OpenHDS.Locations.NOTE_ID_PATH_POSITION));
            break;
        case HIERARCHYITEMS:
            qb.setTables(OpenHDS.HierarchyItems.TABLE_NAME);
            qb.setProjectionMap(hierarchyitemsProjectionMap);
            break;
        case HIERARCHYITEM_ID:
            qb.setTables(OpenHDS.HierarchyItems.TABLE_NAME);
            qb.setProjectionMap(hierarchyitemsProjectionMap);
            qb.appendWhere(OpenHDS.HierarchyItems._ID + "="
                    + uri.getPathSegments().get(OpenHDS.HierarchyItems.NOTE_ID_PATH_POSITION));
            break;
        case ROUNDS:
            qb.setTables(OpenHDS.Rounds.TABLE_NAME);
            qb.setProjectionMap(roundsProjectionMap);
            break;
        case ROUND_ID:
            qb.setTables(OpenHDS.Rounds.TABLE_NAME);
            qb.setProjectionMap(roundsProjectionMap);
            qb.appendWhere(OpenHDS.Rounds._ID + "=" + uri.getPathSegments().get(OpenHDS.Rounds.ID_PATH_POSITION));
            break;
        case VISITS:
            qb.setTables(OpenHDS.Visits.TABLE_NAME);
            qb.setProjectionMap(visitsProjectionMap);
            break;
        case VISIT_ID:
            qb.setTables(OpenHDS.Visits.TABLE_NAME);
            qb.setProjectionMap(visitsProjectionMap);
            qb.appendWhere(OpenHDS.Visits._ID + "=" + uri.getPathSegments().get(OpenHDS.Visits.ID_PATH_POSITION));
            break;
        case RELATIONSHIPS:
            qb.setTables(OpenHDS.Relationships.TABLE_NAME);
            qb.setProjectionMap(relationshipsProjectionMap);
            break;
        case RELATIONSHIP_ID:
            qb.setTables(OpenHDS.Relationships.TABLE_NAME);
            qb.setProjectionMap(relationshipsProjectionMap);
            qb.appendWhere(OpenHDS.Relationships._ID + "="
                    + uri.getPathSegments().get(OpenHDS.Relationships.ID_PATH_POSITION));
            break;
        case FIELDWORKERS:
            qb.setTables(OpenHDS.FieldWorkers.TABLE_NAME);
            qb.setProjectionMap(fieldworkersProjectionMap);
            break;
        case FIELDWORKER_ID:
            qb.setTables(OpenHDS.FieldWorkers.TABLE_NAME);
            qb.setProjectionMap(fieldworkersProjectionMap);
            qb.appendWhere(OpenHDS.FieldWorkers._ID + "="
                    + uri.getPathSegments().get(OpenHDS.FieldWorkers.ID_PATH_POSITION));
            break;
        case SOCIALGROUPS:
            qb.setTables(OpenHDS.SocialGroups.TABLE_NAME);
            qb.setProjectionMap(socialgroupsProjectionMap);
            break;
        case SOCIALGROUPS_BY_LOCATION:
            qb.setTables(OpenHDS.SocialGroups.TABLE_NAME);
            qb.setProjectionMap(socialgroupsProjectionMap);
            selectionArgs = addSocialGroupExtIds(qb,
                    uri.getPathSegments().get(OpenHDS.SocialGroups.LOCATION_PATH_POSITION));
            break;
        case SOCIALGROUPS_BY_INDIVIDUAL:
            qb.setTables(OpenHDS.SocialGroups.TABLE_NAME + " s inner join " + OpenHDS.IndividualGroups.TABLE_NAME
                    + " x on s." + OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID + " = x."
                    + OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID);
            qb.setProjectionMap(socialgroupsJoinProjectionMap);
            qb.appendWhere("x." + OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID + " = '"
                    + uri.getPathSegments().get(OpenHDS.SocialGroups.LOCATION_PATH_POSITION) + "'");
            sortOrder = "s." + OpenHDS.SocialGroups._ID;
            break;
        case SOCIALGROUP_ID:
            qb.setTables(OpenHDS.SocialGroups.TABLE_NAME);
            qb.setProjectionMap(socialgroupsProjectionMap);
            qb.appendWhere(OpenHDS.SocialGroups._ID + "="
                    + uri.getPathSegments().get(OpenHDS.SocialGroups.ID_PATH_POSITION));
            break;
        case INDIVIDUALGROUPS:
            qb.setTables(OpenHDS.IndividualGroups.TABLE_NAME);
            qb.setProjectionMap(individualgroupsProjectionMap);
            break;
        case INDIVIDUALGROUP_ID:
            qb.setTables(OpenHDS.IndividualGroups.TABLE_NAME);
            qb.setProjectionMap(individualgroupsProjectionMap);
            qb.appendWhere(OpenHDS.IndividualGroups._ID + "="
                    + uri.getPathSegments().get(OpenHDS.IndividualGroups.ID_PATH_POSITION));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = OpenHDS.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase(password);

        Cursor c = qb.query(db, // The database to query
                projection, // The columns to return from the query
                selection, // The columns for the where clause
                selectionArgs, // The values for the where clause
                null, // don't group the rows
                null, // don't filter by row groups
                orderBy // The sort order
                );

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    private String[] addSocialGroupExtIds(SQLiteQueryBuilder qb, String string) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase(password);
        // get all individuals at location
        Cursor c = db.query(OpenHDS.Individuals.TABLE_NAME,
                new String[] { OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID },
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE + " = ?", new String[] { string }, null, null, null);

        // iterate over all individuals and collect their memberships
        // this results in a subset of households at the location
        Set<String> socialGroupExtIds = new HashSet<String>();
        while (c.moveToNext()) {
            Cursor c2 = db.query(OpenHDS.IndividualGroups.TABLE_NAME,
                    new String[] { OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID },
                    OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID + " = ?", new String[] { c.getString(0) }, null,
                    null, null);
            while (c2.moveToNext()) {
                socialGroupExtIds.add(c2.getString(0));
            }
            c2.close();
        }
        c.close();

        // generate the SQL IN clause with the subset of social group ids
        StringBuilder placeholders = new StringBuilder();
        if (socialGroupExtIds.size() > 0) {
            placeholders.append("?");
        }

        for (int i = 1; i < socialGroupExtIds.size(); i++) {
            placeholders.append(",?");
        }

        qb.appendWhere(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID + " IN (" + placeholders.toString() + ")");
        return socialGroupExtIds.toArray(new String[] {});
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case INDIVIDUALS:
            return OpenHDS.Individuals.CONTENT_TYPE;
        case INDIVIDUAL_ID:
            return OpenHDS.Individuals.CONTENT_ITEM_TYPE;
        case INDIVIDUAL_SG:
            return OpenHDS.SocialGroups.CONTENT_TYPE;
        case LOCATIONS:
            return OpenHDS.Locations.CONTENT_TYPE;
        case LOCATION_ID:
            return OpenHDS.Locations.CONTENT_ITEM_TYPE;
        case HIERARCHYITEMS:
            return OpenHDS.HierarchyItems.CONTENT_TYPE;
        case HIERARCHYITEM_ID:
            return OpenHDS.HierarchyItems.CONTENT_ITEM_TYPE;
        case ROUNDS:
            return OpenHDS.Rounds.CONTENT_TYPE;
        case ROUND_ID:
            return OpenHDS.Rounds.CONTENT_ITEM_TYPE;
        case VISITS:
            return OpenHDS.Visits.CONTENT_TYPE;
        case VISIT_ID:
            return OpenHDS.Visits.CONTENT_ITEM_TYPE;
        case RELATIONSHIPS:
            return OpenHDS.Relationships.CONTENT_TYPE;
        case RELATIONSHIP_ID:
            return OpenHDS.Relationships.CONTENT_ITEM_TYPE;
        case FIELDWORKERS:
            return OpenHDS.FieldWorkers.CONTENT_TYPE;
        case FIELDWORKER_ID:
            return OpenHDS.FieldWorkers.CONTENT_ITEM_TYPE;
        case SOCIALGROUPS:
            return OpenHDS.SocialGroups.CONTENT_TYPE;
        case SOCIALGROUP_ID:
            return OpenHDS.SocialGroups.CONTENT_ITEM_TYPE;
        case INDIVIDUALGROUPS:
            return OpenHDS.IndividualGroups.CONTENT_TYPE;
        case INDIVIDUALGROUP_ID:
            return OpenHDS.IndividualGroups.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        String table;
        Uri contentUriBase;

        switch (sUriMatcher.match(uri)) {
        case INDIVIDUALS:
            table = OpenHDS.Individuals.TABLE_NAME;
            contentUriBase = OpenHDS.Individuals.CONTENT_ID_URI_BASE;
            break;
        case LOCATIONS:
            table = OpenHDS.Locations.TABLE_NAME;
            contentUriBase = OpenHDS.Locations.CONTENT_ID_URI_BASE;
            break;
        case HIERARCHYITEMS:
            table = OpenHDS.HierarchyItems.TABLE_NAME;
            contentUriBase = OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE;
            break;
        case ROUNDS:
            table = OpenHDS.Rounds.TABLE_NAME;
            contentUriBase = OpenHDS.Rounds.CONTENT_ID_URI_BASE;
            break;
        case VISITS:
            table = OpenHDS.Visits.TABLE_NAME;
            contentUriBase = OpenHDS.Visits.CONTENT_ID_URI_BASE;
            break;
        case RELATIONSHIPS:
            table = OpenHDS.Relationships.TABLE_NAME;
            contentUriBase = OpenHDS.Relationships.CONTENT_ID_URI_BASE;
            break;
        case FIELDWORKERS:
            table = OpenHDS.FieldWorkers.TABLE_NAME;
            contentUriBase = OpenHDS.FieldWorkers.CONTENT_ID_URI_BASE;
            break;
        case SOCIALGROUPS:
            table = OpenHDS.SocialGroups.TABLE_NAME;
            contentUriBase = OpenHDS.SocialGroups.CONTENT_ID_URI_BASE;
            break;
        case INDIVIDUALGROUPS:
            table = OpenHDS.IndividualGroups.TABLE_NAME;
            contentUriBase = OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE;
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase(password);

        long rowId = db.insert(table, null, values);

        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(contentUriBase, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase(password);
        String finalWhere;

        int count;

        switch (sUriMatcher.match(uri)) {
        case INDIVIDUALS:
            count = db.delete(OpenHDS.Individuals.TABLE_NAME, where, whereArgs);
            break;
        case INDIVIDUAL_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.Individuals.NOTE_ID_PATH_POSITION, where);
            count = db.delete(OpenHDS.Individuals.TABLE_NAME, finalWhere, whereArgs);
            break;
        case LOCATIONS:
            count = db.delete(OpenHDS.Locations.TABLE_NAME, where, whereArgs);
            break;
        case LOCATION_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.Locations.NOTE_ID_PATH_POSITION, where);
            count = db.delete(OpenHDS.Locations.TABLE_NAME, finalWhere, whereArgs);
            break;
        case HIERARCHYITEMS:
            count = db.delete(OpenHDS.HierarchyItems.TABLE_NAME, where, whereArgs);
            break;
        case HIERARCHYITEM_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.HierarchyItems.NOTE_ID_PATH_POSITION, where);
            count = db.delete(OpenHDS.HierarchyItems.TABLE_NAME, finalWhere, whereArgs);
            break;
        case ROUNDS:
            count = db.delete(OpenHDS.Rounds.TABLE_NAME, where, whereArgs);
            break;
        case ROUND_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.Rounds.ID_PATH_POSITION, where);
            count = db.delete(OpenHDS.Rounds.TABLE_NAME, finalWhere, whereArgs);
            break;
        case VISITS:
            count = db.delete(OpenHDS.Visits.TABLE_NAME, where, whereArgs);
            break;
        case VISIT_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.Visits.ID_PATH_POSITION, where);
            count = db.delete(OpenHDS.Visits.TABLE_NAME, finalWhere, whereArgs);
            break;
        case RELATIONSHIPS:
            count = db.delete(OpenHDS.Relationships.TABLE_NAME, where, whereArgs);
            break;
        case RELATIONSHIP_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.Relationships.ID_PATH_POSITION, where);
            count = db.delete(OpenHDS.Relationships.TABLE_NAME, finalWhere, whereArgs);
            break;
        case FIELDWORKERS:
            count = db.delete(OpenHDS.FieldWorkers.TABLE_NAME, where, whereArgs);
            break;
        case FIELDWORKER_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.FieldWorkers.ID_PATH_POSITION, where);
            count = db.delete(OpenHDS.FieldWorkers.TABLE_NAME, finalWhere, whereArgs);
            break;
        case SOCIALGROUPS:
            count = db.delete(OpenHDS.SocialGroups.TABLE_NAME, where, whereArgs);
            break;
        case SOCIALGROUP_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.SocialGroups.ID_PATH_POSITION, where);
            count = db.delete(OpenHDS.SocialGroups.TABLE_NAME, finalWhere, whereArgs);
            break;
        case INDIVIDUALGROUPS:
            count = db.delete(OpenHDS.IndividualGroups.TABLE_NAME, where, whereArgs);
            break;
        case INDIVIDUALGROUP_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.IndividualGroups.ID_PATH_POSITION, where);
            count = db.delete(OpenHDS.IndividualGroups.TABLE_NAME, finalWhere, whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    private String buildFinalWhere(Uri uri, int pathPosition, String where) {
        String finalWhere;
        finalWhere = BaseColumns._ID + " = " + uri.getPathSegments().get(pathPosition);

        if (where != null) {
            finalWhere = finalWhere + " AND " + where;
        }
        return finalWhere;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase(password);
        int count;
        String finalWhere;

        switch (sUriMatcher.match(uri)) {
        case INDIVIDUALS:
            count = db.update(OpenHDS.Individuals.TABLE_NAME, values, where, whereArgs);
            break;
        case INDIVIDUAL_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.Individuals.NOTE_ID_PATH_POSITION, where);
            count = db.update(OpenHDS.Individuals.TABLE_NAME, values, finalWhere, whereArgs);
            break;
        case LOCATIONS:
            count = db.update(OpenHDS.Locations.TABLE_NAME, values, where, whereArgs);
            break;
        case LOCATION_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.Locations.NOTE_ID_PATH_POSITION, where);
            count = db.update(OpenHDS.Locations.TABLE_NAME, values, finalWhere, whereArgs);
            break;
        case HIERARCHYITEMS:
            count = db.update(OpenHDS.HierarchyItems.TABLE_NAME, values, where, whereArgs);
            break;
        case HIERARCHYITEM_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.HierarchyItems.NOTE_ID_PATH_POSITION, where);
            count = db.update(OpenHDS.HierarchyItems.TABLE_NAME, values, finalWhere, whereArgs);
            break;
        case ROUNDS:
            count = db.update(OpenHDS.Rounds.TABLE_NAME, values, where, whereArgs);
            break;
        case ROUND_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.Rounds.ID_PATH_POSITION, where);
            count = db.update(OpenHDS.Rounds.TABLE_NAME, values, finalWhere, whereArgs);
            break;
        case VISITS:
            count = db.update(OpenHDS.Visits.TABLE_NAME, values, where, whereArgs);
            break;
        case VISIT_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.Visits.ID_PATH_POSITION, where);
            count = db.update(OpenHDS.Visits.TABLE_NAME, values, finalWhere, whereArgs);
            break;
        case RELATIONSHIPS:
            count = db.update(OpenHDS.Relationships.TABLE_NAME, values, where, whereArgs);
            break;
        case RELATIONSHIP_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.Relationships.ID_PATH_POSITION, where);
            count = db.update(OpenHDS.Relationships.TABLE_NAME, values, finalWhere, whereArgs);
            break;
        case FIELDWORKERS:
            count = db.update(OpenHDS.FieldWorkers.TABLE_NAME, values, where, whereArgs);
            break;
        case FIELDWORKER_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.FieldWorkers.ID_PATH_POSITION, where);
            count = db.update(OpenHDS.FieldWorkers.TABLE_NAME, values, finalWhere, whereArgs);
            break;
        case SOCIALGROUPS:
            count = db.update(OpenHDS.SocialGroups.TABLE_NAME, values, where, whereArgs);
            break;
        case SOCIALGROUP_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.SocialGroups.ID_PATH_POSITION, where);
            count = db.update(OpenHDS.SocialGroups.TABLE_NAME, values, finalWhere, whereArgs);
            break;
        case INDIVIDUALGROUPS:
            count = db.update(OpenHDS.IndividualGroups.TABLE_NAME, values, where, whereArgs);
            break;
        case INDIVIDUALGROUP_ID:
            finalWhere = buildFinalWhere(uri, OpenHDS.IndividualGroups.ID_PATH_POSITION, where);
            count = db.update(OpenHDS.IndividualGroups.TABLE_NAME, values, finalWhere, whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}

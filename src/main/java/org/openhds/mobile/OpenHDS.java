package org.openhds.mobile;

import android.net.Uri;
import android.provider.BaseColumns;

public class OpenHDS {
    public static final String AUTHORITY = "org.openhds.Application";

    private OpenHDS() {
    }

    public static final String DEFAULT_SORT_ORDER = "_id ASC";

    public static final class Individuals implements BaseColumns {

        private Individuals() {
        }

        public static final String TABLE_NAME = "individuals";
        private static final String SCHEME = "content://";

        private static final String PATH_NOTES = "/individuals";
        private static final String PATH_NOTE_ID = "/individuals/";
        private static final String PATH_SG = "/individuals/sg/";

        public static final int NOTE_ID_PATH_POSITION = 1;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
        public static final Uri CONTENT_SG_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_SG);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.individual";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.individual";

        public static final String COLUMN_INDIVIDUAL_EXTID = "extId";
        public static final String COLUMN_INDIVIDUAL_FIRSTNAME = "firstName";
        public static final String COLUMN_INDIVIDUAL_LASTNAME = "lastName";
        public static final String COLUMN_INDIVIDUAL_FULLNAME = "fullname";
        public static final String COLUMN_INDIVIDUAL_DOB = "dob";
        public static final String COLUMN_INDIVIDUAL_GENDER = "gender";
        public static final String COLUMN_INDIVIDUAL_MOTHER = "mother";
        public static final String COLUMN_INDIVIDUAL_FATHER = "father";
        public static final String COLUMN_INDIVIDUAL_RESIDENCE = "currentResidence";
    }

    public static final class Locations implements BaseColumns {
        public static final String TABLE_NAME = "locations";
        private static final String SCHEME = "content://";

        private static final String PATH_NOTES = "/locations";
        private static final String PATH_NOTE_ID = "/locations/";

        public static final int NOTE_ID_PATH_POSITION = 1;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.location";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.location";

        public static final String COLUMN_LOCATION_EXTID = "extId";
        public static final String COLUMN_LOCATION_NAME = "name";
        public static final String COLUMN_LOCATION_LATITUDE = "latitude";
        public static final String COLUMN_LOCATION_LONGITUDE = "longitude";
        public static final String COLUMN_LOCATION_HIERARCHY = "hierarchy";
    }

    public static final class HierarchyItems implements BaseColumns {
        public static final String TABLE_NAME = "hierarchyitems";
        private static final String SCHEME = "content://";

        private static final String PATH_NOTES = "/hierarchyitems";
        private static final String PATH_NOTE_ID = "/hierarchyitems/";

        public static final int NOTE_ID_PATH_POSITION = 1;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.hierarchyitem";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.hierarchyitem";

        public static final String COLUMN_HIERARCHY_EXTID = "extId";
        public static final String COLUMN_HIERARCHY_NAME = "name";
        public static final String COLUMN_HIERARCHY_PARENT = "parent";
        public static final String COLUMN_HIERARCHY_LEVEL = "level";
    }

    public static final class Rounds implements BaseColumns {
        public static final String TABLE_NAME = "rounds";
        private static final String SCHEME = "content://";

        private static final String PATH_NOTES = "/rounds";
        private static final String PATH_NOTE_ID = "/rounds/";

        public static final int ID_PATH_POSITION = 1;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.round";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.round";

        public static final String COLUMN_ROUND_STARTDATE = "startDate";
        public static final String COLUMN_ROUND_ENDDATE = "endDate";
        public static final String COLUMN_ROUND_NUMBER = "roundNumber";
    }

    public static final class Visits implements BaseColumns {
        public static final String TABLE_NAME = "visits";
        private static final String SCHEME = "content://";

        private static final String PATH_NOTES = "/visits";
        private static final String PATH_NOTE_ID = "/visits/";

        public static final int ID_PATH_POSITION = 1;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.visit";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.visit";

        public static final String COLUMN_VISIT_EXTID = "extId";
        public static final String COLUMN_VISIT_ROUND = "round";
        public static final String COLUMN_VISIT_DATE = "date";
        public static final String COLUMN_VISIT_LOCATION = "location";
    }

    public static final class Relationships implements BaseColumns {
        public static final String TABLE_NAME = "relationships";
        private static final String SCHEME = "content://";

        private static final String PATH_NOTES = "/relationships";
        private static final String PATH_NOTE_ID = "/relationships/";

        public static final int ID_PATH_POSITION = 1;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.relationship";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.relationship";

        public static final String COLUMN_RELATIONSHIP_INDIVIDUAL_A = "individualA";
        public static final String COLUMN_RELATIONSHIP_INDIVIDUAL_B = "individualB";
        public static final String COLUMN_RELATIONSHIP_STARTDATE = "startDate";
    }

    public static final class FieldWorkers implements BaseColumns {
        public static final String TABLE_NAME = "fieldworkers";
        private static final String SCHEME = "content://";

        private static final String PATH_NOTES = "/fieldworkers";
        private static final String PATH_NOTE_ID = "/fieldworkers/";

        public static final int ID_PATH_POSITION = 1;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.fieldworker";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.fieldworker";

        public static final String COLUMN_FIELDWORKER_EXTID = "extId";
        public static final String COLUMN_FIELDWORKER_PASSWORD = "password";
        public static final String COLUMN_FIELDWORKER_FIRSTNAME = "firstName";
        public static final String COLUMN_FIELDWORKER_LASTNAME = "lastName";
    }

    public static final class SocialGroups implements BaseColumns {
        public static final String TABLE_NAME = "socialgroups";
        private static final String SCHEME = "content://";

        private static final String PATH_NOTES = "/socialgroups";
        private static final String PATH_LOCATION_ID = "/socialgroups/location/";
        private static final String PATH_INDIVIDUAL_ID = "/socialgroups/individual/";
        private static final String PATH_NOTE_ID = "/socialgroups/";

        public static final int ID_PATH_POSITION = 1;
        public static final int LOCATION_PATH_POSITION = 2;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
        public static final Uri CONTENT_LOCATION_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_LOCATION_ID);
        public static final Uri CONTENT_LOCATION_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_LOCATION_ID + "/*");
        public static final Uri CONTENT_INDIVIDUAL_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_INDIVIDUAL_ID);
        public static final Uri CONTENT_INDIVIDUAL_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_INDIVIDUAL_ID + "/*");

        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.socialgroups";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.socialgroups";

        public static final String COLUMN_SOCIALGROUP_EXTID = "extId";
        public static final String COLUMN_SOCIALGROUP_GROUPNAME = "groupName";
        public static final String COLUMN_SOCIALGROUP_GROUPHEAD = "groupHead";
    }
    
    public static final class IndividualGroups implements BaseColumns {
        public static final String TABLE_NAME = "individualgroups";
        private static final String SCHEME = "content://";

        private static final String PATH_NOTES = "/individualgroups";
        private static final String PATH_NOTE_ID = "/individualgroups/";

        public static final int ID_PATH_POSITION = 2;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.individualgroups";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.individualgroups";

    	public static final String COLUMN_INDIVIDUALUUID = "individual_extId";
    	public static final String COLUMN_SOCIALGROUPUUID = "socialgroup_extId";  
    }
}

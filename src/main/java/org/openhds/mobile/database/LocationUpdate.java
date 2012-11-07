package org.openhds.mobile.database;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Location;

import android.content.ContentResolver;
import android.content.ContentValues;

public class LocationUpdate implements Updatable {

    private String locationExtId;
    private String hierarchy;
    private String name;

    public LocationUpdate(Location location) {
        this.locationExtId = location.getExtId();
        this.hierarchy = location.getHierarchy();
        this.name = location.getName();
    }

    public void updateDatabase(ContentResolver resolver) {
        ContentValues cv = new ContentValues();
        
        cv.put(OpenHDS.Locations.COLUMN_LOCATION_EXTID, locationExtId);
        cv.put(OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY, hierarchy);
        cv.put(OpenHDS.Locations.COLUMN_LOCATION_NAME, "");
        
        cv.put(OpenHDS.Locations.COLUMN_LOCATION_LATITUDE, "");
        cv.put(OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE, "");

        resolver.insert(OpenHDS.Locations.CONTENT_ID_URI_BASE, cv);
    }

}

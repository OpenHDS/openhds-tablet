package org.openhds.mobile.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FormXmlReader;
import org.openhds.mobile.model.Location;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;

public class LocationUpdate implements Updatable {

    public void updateDatabase(ContentResolver resolver, String filepath) {
        Location location = readLocationFromXml(filepath);

        if (location == null) {
            Log.e(LocationUpdate.class.getName(), "Was not able to read location from XML file");
            return;
        }

        writeLocationToDatabase(resolver, location);
    }

    private void writeLocationToDatabase(ContentResolver resolver, Location location) {
        ContentValues cv = new ContentValues();

        cv.put(OpenHDS.Locations.COLUMN_LOCATION_EXTID, location.getExtId());
        cv.put(OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY, location.getHierarchy());
        cv.put(OpenHDS.Locations.COLUMN_LOCATION_NAME, location.getName());

        cv.put(OpenHDS.Locations.COLUMN_LOCATION_LATITUDE, "");
        cv.put(OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE, "");

        resolver.insert(OpenHDS.Locations.CONTENT_ID_URI_BASE, cv);
    }

    private Location readLocationFromXml(String filepath) {
        File file = new File(filepath);
        FormXmlReader reader = new FormXmlReader();
        try {
            return reader.readLocation(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

}

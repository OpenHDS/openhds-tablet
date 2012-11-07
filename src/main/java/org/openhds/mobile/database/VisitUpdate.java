package org.openhds.mobile.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FormXmlReader;
import org.openhds.mobile.model.Visit;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;

public class VisitUpdate implements Updatable {

    public void updateDatabase(ContentResolver resolver, String filepath) {
        FormXmlReader xmlReader = new FormXmlReader();
        try {
            Visit visit = xmlReader.readVisit(new FileInputStream(new File(filepath)));
            
            if (visit == null) {
                return;
            }
            
            ContentValues cv = new ContentValues();
            cv.put(OpenHDS.Visits.COLUMN_VISIT_EXTID, visit.getExtId());
            cv.put(OpenHDS.Visits.COLUMN_VISIT_LOCATION, visit.getLocation());
            cv.put(OpenHDS.Visits.COLUMN_VISIT_DATE, visit.getDate());
            cv.put(OpenHDS.Visits.COLUMN_VISIT_ROUND, visit.getRound());

            resolver.insert(OpenHDS.Visits.CONTENT_ID_URI_BASE, cv);
        } catch (FileNotFoundException e) {
            Log.e(VisitUpdate.class.getName(), "Could not read Visit XML file");
        }
    }
}

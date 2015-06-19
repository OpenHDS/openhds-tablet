package org.openhds.mobile.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FormXmlReader;
import org.openhds.mobile.model.PregnancyObservation;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class PregnancyObservationUpdate implements Updatable {

    public void updateDatabase(ContentResolver resolver, String filepath,String jrFormId) {
        FormXmlReader xmlReader = new FormXmlReader();
        try {
            PregnancyObservation pregnObs = xmlReader.readPregnancyObservation(new FileInputStream(new File(filepath)), jrFormId);

            if (pregnObs == null) {
                return;
            }

            ContentValues cv = new ContentValues();
            cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_VISITED, "Yes");


            Cursor cursor = resolver.query(OpenHDS.Individuals.CONTENT_ID_URI_BASE,
                    new String[] { OpenHDS.Individuals._ID }, OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID + " = ?",
                    new String[] { pregnObs.getMother().getExtId() }, null);
            if (cursor.moveToNext()) {
                Uri uri = ContentUris.withAppendedId(OpenHDS.Individuals.CONTENT_ID_URI_BASE, cursor.getLong(0));
                resolver.update(uri, cv, null, null);
            }
            
            cursor.close();
            

        } catch (FileNotFoundException e) {
            Log.e(InternalInMigrationUpdate.class.getName(), "Could not read In Migration XML file");
        }
    }
}

package org.openhds.mobile.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FormXmlReader;
import org.openhds.mobile.model.Individual;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;

public class ExternalInMigrationUpdate implements Updatable {

    public void updateDatabase(ContentResolver resolver, String filepath) {
        FormXmlReader xmlReader = new FormXmlReader();
        try {
            Individual individual = xmlReader.readInMigration(new FileInputStream(new File(filepath)));
            
            if (individual == null) {
                return;
            }
            
            ContentValues cv = new ContentValues();
            cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB, individual.getDob());
            cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID, individual.getExtId());
            cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER, individual.getFather());
            cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRSTNAME, individual.getFirstName());
            cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER, individual.getGender());
            cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LASTNAME, individual.getLastName());
            cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER, individual.getMother());
            cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE, individual.getCurrentResidence());
            
            resolver.insert(OpenHDS.Individuals.CONTENT_ID_URI_BASE, cv);
        } catch (FileNotFoundException e) {
            Log.e(ExternalInMigrationUpdate.class.getName(), "Could not read In Migration XML file");
        }
    }

}

package org.openhds.mobile.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FormXmlReader;
import org.openhds.mobile.model.Relationship;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;

public class RelationshipUpdate implements Updatable {

    public void updateDatabase(ContentResolver resolver, String filepath) {
        FormXmlReader xmlReader = new FormXmlReader();
        try {
            Relationship rel = xmlReader.readRelationship(new FileInputStream(new File(filepath)));

            if (rel == null) {
                return;
            }

            ContentValues cv = new ContentValues();
            cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A, rel.getIndividualA());
            cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B, rel.getIndividualB());
            cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE, rel.getStartDate());
            resolver.insert(OpenHDS.Relationships.CONTENT_ID_URI_BASE, cv);
        } catch (FileNotFoundException e) {
            Log.e(VisitUpdate.class.getName(), "Could not read Relationship XML file");
        }
    }

}

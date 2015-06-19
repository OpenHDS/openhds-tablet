package org.openhds.mobile.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FormXmlReader;
import org.openhds.mobile.model.Relationship;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class RelationshipUpdate implements Updatable {

    public void updateDatabase(ContentResolver resolver, String filepath, String jrFormId) {
        FormXmlReader xmlReader = new FormXmlReader();
        try {
            Relationship rel = xmlReader.readRelationship(new FileInputStream(new File(filepath)), jrFormId);

            if (rel == null) {
                return;
            }

            ContentValues cv = new ContentValues();
            cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A, rel.getIndividualA());
            cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B, rel.getIndividualB());
            cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE, rel.getStartDate());
            resolver.insert(OpenHDS.Relationships.CONTENT_ID_URI_BASE, cv);
            
            cv.clear();

            cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_VISITED, "Yes");
            Cursor cursor = resolver.query(OpenHDS.Individuals.CONTENT_ID_URI_BASE,
                    new String[] { OpenHDS.Individuals._ID }, OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID + " = ?",
                    new String[] { rel.getIndividualA() }, null);
            if (cursor.moveToNext()) {
                Uri uri = ContentUris.withAppendedId(OpenHDS.Individuals.CONTENT_ID_URI_BASE, cursor.getLong(0));
                resolver.update(uri, cv, null, null);
            }
            cursor.close();
            
        } catch (FileNotFoundException e) {
            Log.e(VisitUpdate.class.getName(), "Could not read Relationship XML file");
        }
    }

}

package org.openhds.mobile.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.DeathOfHeadOfHousehold;
import org.openhds.mobile.model.FormXmlReader;
import org.openhds.mobile.model.Individual;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DeathOfHoHUpdate implements Updatable {

	public void updateDatabase(ContentResolver resolver, String filepath,String jrFormId) {
        FormXmlReader xmlReader = new FormXmlReader();
        try {
        	DeathOfHeadOfHousehold dHoh = xmlReader.readDeathOfHeadOfHousehold(new FileInputStream(new File(filepath)), jrFormId );
            Individual individual = dHoh.getOldHoh();

            if (individual == null) {
                return;
            }
            
            //Put Residency type to Death
            ContentValues cv = new ContentValues();
            cv.put(OpenHDS.Individuals.COLUMN_RESIDENCE_END_TYPE, "DTH");
            Cursor cursor = resolver.query(OpenHDS.Individuals.CONTENT_ID_URI_BASE,
                    new String[] { OpenHDS.Individuals._ID }, OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID + " = ?",
                    new String[] { individual.getExtId() }, null);
            if (cursor.moveToNext()) {
                Uri uri = ContentUris.withAppendedId(OpenHDS.Individuals.CONTENT_ID_URI_BASE, cursor.getLong(0));
                resolver.update(uri, cv, null, null);
            }
            cursor.close();
            
            //Change HoH
            ContentValues contentValues = new ContentValues();
            Cursor hhToRem = resolver.query(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, 
            		new String[] { OpenHDS.SocialGroups._ID }, 
            		OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID + " = ?",
            		new String[] { dHoh.getHouseHoldExtId() }, null);
            
            if(hhToRem.moveToNext()){
            	contentValues.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPHEAD, dHoh.getNewHoh().getExtId());
            	Uri uri = ContentUris.withAppendedId(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, hhToRem.getLong(0));
            	resolver.update(uri, contentValues, null, null);
            }           
            hhToRem.close();
            
        } catch (FileNotFoundException e) {
            Log.e(DeathOfHoHUpdate.class.getName(), "Could not read Death XML file");
        }
    }

}

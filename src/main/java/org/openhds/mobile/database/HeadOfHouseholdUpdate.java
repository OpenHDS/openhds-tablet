package org.openhds.mobile.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.ChangeHeadOfHousehold;
import org.openhds.mobile.model.FormXmlReader;
import org.openhds.mobile.model.Individual;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class HeadOfHouseholdUpdate implements Updatable {

	public void updateDatabase(ContentResolver resolver, String filepath,String jrFormId) {
        FormXmlReader xmlReader = new FormXmlReader();
        try {
        	ChangeHeadOfHousehold cHoh = xmlReader.readChangeHeadOfHousehold(new FileInputStream(new File(filepath)), jrFormId );
            Individual individual = cHoh.getOldHoh();

            if (individual == null) {
                return;
            }
                        
            //Change HoH
            ContentValues contentValues = new ContentValues();
            Cursor hhToRem = resolver.query(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, 
            		new String[] { OpenHDS.SocialGroups._ID }, 
            		OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID + " = ?",
            		new String[] { cHoh.getHouseHoldExtId() }, null);
            
            if(hhToRem.moveToNext()){
            	contentValues.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPHEAD, cHoh.getNewHoh().getExtId());
            	Uri uri = ContentUris.withAppendedId(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, hhToRem.getLong(0));
            	resolver.update(uri, contentValues, null, null);
            }           
            hhToRem.close();
            
        } catch (FileNotFoundException e) {
            Log.e(HeadOfHouseholdUpdate.class.getName(), "Could not read Change HoH XML file");
        }
    }

}

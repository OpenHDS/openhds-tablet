package org.openhds.mobile.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FormXmlReader;
import org.openhds.mobile.model.Membership;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class MembershipUpdate implements Updatable {

    public void updateDatabase(ContentResolver resolver, String filepath,String jrFormId) {
    	 FormXmlReader xmlReader = new FormXmlReader();
         try {
             Membership membership = xmlReader.readMembership(new FileInputStream(new File(filepath)), jrFormId);
             
             if (membership == null) {
                 return;
             }
             
             ContentValues cv = new ContentValues();
             cv.put(OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID, membership.getIndExtId());
             cv.put(OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID, membership.getGroupextId());
             
             Cursor cursor = resolver.query(OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE,
	                    new String[] { OpenHDS.IndividualGroups._ID }, OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID + " = ?",
	                    new String[] { membership.getIndExtId() }, null);
	            if (cursor.moveToNext()) {
	                Uri uri = ContentUris.withAppendedId(OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE, cursor.getLong(0));
	                resolver.update(uri, cv, null, null);
	            } else {
	            	resolver.insert(OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE, cv);
	            }
	            
	            cursor.close();
	            
	            cv.clear();
	            cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_VISITED, "Yes");
	            cursor = resolver.query(OpenHDS.Individuals.CONTENT_ID_URI_BASE,
	                    new String[] { OpenHDS.Individuals._ID }, OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID + " = ?",
	                    new String[] { membership.getIndExtId() }, null);
	            if (cursor.moveToNext()) {
	                Uri uri = ContentUris.withAppendedId(OpenHDS.Individuals.CONTENT_ID_URI_BASE, cursor.getLong(0));
	                resolver.update(uri, cv, null, null);
	            }
	            cursor.close();
         } catch (FileNotFoundException e) {
             Log.e(MembershipUpdate.class.getName(), "Could not read Membership XML file");
         }
     }
    }



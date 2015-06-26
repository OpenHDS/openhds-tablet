package org.openhds.mobile.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FormXmlReader;
import org.openhds.mobile.model.SocialGroup;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class HouseholdUpdate implements Updatable {

    public void updateDatabase(ContentResolver resolver, String filepath,String jrFormId) {
        try {
            File file = new File(filepath);
            FormXmlReader reader = new FormXmlReader();

            SocialGroup sg = reader.readSocialGroup(new FileInputStream(file), jrFormId);
            
            // creat the social group
            ContentValues cv = new ContentValues();
            cv.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID, sg.getExtId());
            cv.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPHEAD, sg.getGroupHead());
            cv.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME, sg.getGroupName());
            
            resolver.insert(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, cv);
            
            // create the membership for the head to the social group
            cv.clear();
            cv.put(OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID, sg.getGroupHead());
            cv.put(OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID, sg.getExtId());
            
            //resolver.insert(OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE, cv);
            Cursor cursor = resolver.query(OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE,
                    new String[] { OpenHDS.IndividualGroups._ID }, OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID + " = ?",
                    new String[] {sg.getGroupHead()}, null);
            if (cursor.moveToNext()) {
                Uri uri = ContentUris.withAppendedId(OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE, cursor.getLong(0));
                resolver.update(uri, cv, null, null);
            } else {
            	resolver.insert(OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE, cv);
            }
            
            
        } catch (FileNotFoundException e) {
            Log.e(HouseholdUpdate.class.getName(), "Could not find Household XML file");
        }
    }

}

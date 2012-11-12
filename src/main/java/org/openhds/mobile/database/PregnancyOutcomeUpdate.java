package org.openhds.mobile.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FormXmlReader;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.PregnancyOutcome;
import org.openhds.mobile.model.SocialGroup;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class PregnancyOutcomeUpdate implements Updatable {

    public void updateDatabase(ContentResolver resolver, String filepath) {
        FormXmlReader xmlReader = new FormXmlReader();
        try {
            PregnancyOutcome pregOut = xmlReader.readPregnancyOutcome(new FileInputStream(new File(filepath)));

            if (pregOut == null) {
                return;
            }

            // figure out the residency to stick the child
            Cursor mother = resolver.query(OpenHDS.Individuals.CONTENT_ID_URI_BASE,
                    new String[] { OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE },
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID + " = ?",
                    new String[] { pregOut.getMother().getExtId() }, null);
            if (!mother.moveToNext()) {
                // should never happen - but if it does it means the user changed the mother id
                // therefore there is no way of assigning a proper residency to the child
                mother.close();
                return;
            }
            
            String residency = mother.getString(0);
            mother.close();
            
            // insert children
            for(Individual child : pregOut.getChildren()) {
                ContentValues cv = new ContentValues();
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB, child.getDob());
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID, child.getExtId());
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER, child.getFather());
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRSTNAME, child.getFirstName());
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER, child.getGender());
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LASTNAME, child.getLastName());
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER, child.getMother());
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE, residency);
                
                resolver.insert(OpenHDS.Individuals.CONTENT_ID_URI_BASE, cv);
                
                // now the membership
                List<SocialGroup> groups = child.getSocialGroups();
                if (groups.size() == 0) {
                    continue;
                }
                SocialGroup sg = groups.get(0);
                cv.clear();
                cv.put(OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID, child.getExtId());
                cv.put(OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID, sg.getExtId());
                
                resolver.insert(OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE, cv);
            }

        } catch (FileNotFoundException e) {
            Log.e(VisitUpdate.class.getName(), "Could not read Visit XML file");
        }
    }

}

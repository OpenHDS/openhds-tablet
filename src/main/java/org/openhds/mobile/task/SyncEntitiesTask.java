package org.openhds.mobile.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.listener.CollectEntitiesListener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * AsyncTask responsible for downloading the OpenHDS "database", that is a
 * subset of the OpenHDS database records. It does the downloading
 * incrementally, by downloading parts of the data one at a time. For example,
 * it gets all locations and then retrieves all individuals. Ordering is
 * somewhat important here, because the database has a few foreign key
 * references that must be satisfied (e.g. individual references a location
 * location)
 */
public class SyncEntitiesTask extends AsyncTask<Void, Integer, Boolean> {

    private static final String API_PATH = "/api/rest";

    private CollectEntitiesListener listener;
    private ContentResolver resolver;

    private UsernamePasswordCredentials creds;
    private ProgressDialog dialog;
    private HttpGet httpGet;
    private HttpClient client;

    private String baseurl;
    private String username;
    private String password;

    String lastExtId;

    private final List<ContentValues> values = new ArrayList<ContentValues>();
    private final ContentValues[] emptyArray = new ContentValues[] {};

    private State state;
    private Entity entity;

    private enum State {
        DOWNLOADING, SAVING
    }

    private enum Entity {
        LOCATION_HIERARCHY, LOCATION, ROUND, VISIT, RELATIONSHIP, INDIVIDUAL, SOCIALGROUP
    }

    public SyncEntitiesTask(String url, String username, String password, ProgressDialog dialog, Context context,
            CollectEntitiesListener listener) {
        this.baseurl = url;
        this.username = username;
        this.password = password;
        this.dialog = dialog;
        this.listener = listener;
        this.resolver = context.getContentResolver();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        StringBuilder builder = new StringBuilder();
        switch (state) {
        case DOWNLOADING:
            builder.append("Downloading ");
            break;
        case SAVING:
            builder.append("Saving ");
            break;
        }

        switch (entity) {
        case INDIVIDUAL:
            builder.append(" Individuals.");
            break;
        case LOCATION:
            builder.append(" Locations.");
            break;
        case LOCATION_HIERARCHY:
            builder.append(" Location Hierarchy.");
            break;
        case RELATIONSHIP:
            builder.append(" Relationships.");
            break;
        case ROUND:
            builder.append(" Rounds.");
            break;
        case SOCIALGROUP:
            builder.append(" Social Groups.");
            break;
        case VISIT:
            builder.append(" Visits.");
            break;
        }

        if (values.length > 0) {
            builder.append(" Saved " + values[0] + " items");
        }

        dialog.setMessage(builder.toString());
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        creds = new UsernamePasswordCredentials(username, password);

        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 100000);
        HttpConnectionParams.setSoTimeout(httpParameters, 100000);
        HttpConnectionParams.setSocketBufferSize(httpParameters, 240);
        client = new DefaultHttpClient(httpParameters);

        // at this point, we don't care to be smart about which data to
        // download, we simply download it all
        deleteAllTables();

        try {
            entity = Entity.LOCATION_HIERARCHY;
            processUrl(baseurl + API_PATH + "/locationhierarchies");

            entity = Entity.LOCATION;
            processUrl(baseurl + API_PATH + "/locations/cached");

            entity = Entity.ROUND;
            processUrl(baseurl + API_PATH + "/rounds");

             entity = Entity.VISIT;
             processUrl(baseurl + API_PATH + "/visits/cached");
            
             entity = Entity.RELATIONSHIP;
             processUrl(baseurl + API_PATH + "/relationships/cached");

            entity = Entity.INDIVIDUAL;
            processUrl(baseurl + API_PATH + "/individuals/cached");

             entity = Entity.SOCIALGROUP;
             processUrl(baseurl + API_PATH + "/socialgroups/cached");
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private void deleteAllTables() {
        // ordering is somewhat important during delete. a few tables have
        // foreign keys
        resolver.delete(OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE, null, null);
        resolver.delete(OpenHDS.Rounds.CONTENT_ID_URI_BASE, null, null);
        resolver.delete(OpenHDS.Visits.CONTENT_ID_URI_BASE, null, null);
        resolver.delete(OpenHDS.Relationships.CONTENT_ID_URI_BASE, null, null);
        resolver.delete(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, null, null);
        resolver.delete(OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE, null, null);
        resolver.delete(OpenHDS.Individuals.CONTENT_ID_URI_BASE, null, null);
        resolver.delete(OpenHDS.Locations.CONTENT_ID_URI_BASE, null, null);
    }

    private void processUrl(String url) throws Exception {
        state = State.DOWNLOADING;
        publishProgress();

        httpGet = new HttpGet(url);
        processResponse();
    }

    private void processResponse() throws Exception {
        InputStream inputStream = getResponse();
        if (inputStream != null)
            processXMLDocument(inputStream);
    }

    private InputStream getResponse() throws AuthenticationException, ClientProtocolException, IOException {
        HttpResponse response = null;

        httpGet.addHeader(new BasicScheme().authenticate(creds, httpGet));
        httpGet.addHeader("content-type", "application/xml");
        response = client.execute(httpGet);

        HttpEntity entity = response.getEntity();
        return entity.getContent();
    }

    private void processXMLDocument(InputStream content) throws Exception {
        state = State.SAVING;

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);

        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new InputStreamReader(content));

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT && !isCancelled()) {
            String name = null;

            switch (eventType) {
            case XmlPullParser.START_TAG:
                name = parser.getName();
                if (name.equalsIgnoreCase("count")) {
                    parser.next();
                    int cnt = Integer.parseInt(parser.getText());
                    publishProgress(cnt);
                    parser.nextTag();
                } else if (name.equalsIgnoreCase("individuals")) {
                    processIndividualParams(parser);
                } else if (name.equalsIgnoreCase("locations")) {
                    processLocationParams(parser);
                } else if (name.equalsIgnoreCase("locationhierarchies")) {
                    processHierarchyParams(parser);
                } else if (name.equalsIgnoreCase("rounds")) {
                    processRoundParams(parser);
                } else if (name.equalsIgnoreCase("visits")) {
                    processVisitParams(parser);
                } else if (name.equalsIgnoreCase("socialgroups")) {
                    processSocialGroupParams(parser);
                } else if (name.equalsIgnoreCase("relationships")) {
                    processRelationshipParams(parser);
                }
                break;
            }
            eventType = parser.next();
        }
    }

    private void processHierarchyParams(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.nextTag();

        values.clear();
        while (notEndOfXmlDoc("locationHierarchies", parser)) {
            ContentValues cv = new ContentValues();

            parser.nextTag();
            cv.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID, parser.nextText());

            parser.nextTag(); // <level>
            parser.next(); // <keyIdentifier>
            parser.nextText();
            parser.nextTag(); // <name>
            cv.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL, parser.nextText());

            parser.next(); // </level>
            parser.nextTag();
            cv.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME, parser.nextText());

            parser.next(); // <parent>
            parser.nextTag(); // <extId>
            cv.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT, parser.nextText());

            values.add(cv);

            parser.nextTag(); // </parent>
            parser.nextTag(); // </hierarchy>
            parser.nextTag(); // <hierarchy> or </hiearchys>
        }

        if (!values.isEmpty()) {
            resolver.bulkInsert(OpenHDS.HierarchyItems.CONTENT_URI, values.toArray(emptyArray));
        }
    }

    private boolean notEndOfXmlDoc(String element, XmlPullParser parser) throws XmlPullParserException {
        return !element.equals(parser.getName()) && parser.getEventType() != XmlPullParser.END_TAG && !isCancelled();
    }

    private void processLocationParams(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.nextTag();
        int locationCount = 0;
        
        values.clear();
        while (notEndOfXmlDoc("locations", parser)) {
            // skip collected by
            parser.nextTag(); // <collectedBy>
            parser.nextTag(); // <extId>
            parser.nextText();
            parser.nextTag(); // </collectedBy>

            // skip accuracy
            parser.nextTag();
            parser.nextText();

            // skip altitude
            parser.nextTag();
            parser.nextText();

            ContentValues cv = new ContentValues();

            parser.nextTag();
            cv.put(OpenHDS.Locations.COLUMN_LOCATION_EXTID, parser.nextText());

            parser.nextTag();
            cv.put(OpenHDS.Locations.COLUMN_LOCATION_LATITUDE, parser.nextText());

            parser.nextTag(); // <locationLevel>
            parser.nextTag();
            cv.put(OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY, parser.nextText());
            parser.nextTag(); // </locationLevel>

            parser.nextTag();
            cv.put(OpenHDS.Locations.COLUMN_LOCATION_NAME, parser.nextText());

            // skip location type
            parser.nextTag();
            parser.nextText();

            parser.nextTag();
            cv.put(OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE, parser.nextText());

            values.add(cv);
            
            locationCount += 1;

            parser.nextTag(); // </location>
            parser.nextTag(); // <location> or </locations>
            
            if (locationCount % 100 == 0) {
                persistLocations();
                values.clear();
                publishProgress(locationCount);
            }
        }

        persistLocations();
    }

    private void persistLocations() {
        if (!values.isEmpty()) {
            resolver.bulkInsert(OpenHDS.Locations.CONTENT_ID_URI_BASE, values.toArray(emptyArray));
        }
    }

    private void processIndividualParams(XmlPullParser parser) throws XmlPullParserException, IOException {
        int individualsParsed = 0;
        parser.nextTag();

        values.clear();
        List<ContentValues> individualSocialGroups = new ArrayList<ContentValues>();
        while (notEndOfXmlDoc("individuals", parser)) {
            try {
                ContentValues cv = new ContentValues();

                // memberships
                parser.nextTag();
                parser.nextTag();
                List<String> groups = null;
                if (parser.getEventType() != XmlPullParser.END_TAG) {
                    // memberships are present
                    groups = parseMembershipExtIds(parser);
                }

                // residencies
                parser.nextTag();
                parser.nextTag();
                if (parser.getEventType() != XmlPullParser.END_TAG) {
                    parser.nextTag(); // <location>
                    parser.nextTag(); // <extId>
                    cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE, parser.nextText());
                    parser.nextTag(); // </location>
                    parser.nextTag(); // </residency>
                    parser.nextTag(); // </residencies>
                }

                parser.nextTag();
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB, parser.nextText());

                parser.nextTag();
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID, parser.nextText());
                lastExtId = cv.getAsString(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID);

                // father
                parser.nextTag(); // <father>
                parser.nextTag(); // <memberships>
                parser.nextTag(); // </memberships>
                parser.nextTag(); // <residencies>
                parser.nextTag(); // </residencies>
                parser.nextTag(); // <extId>
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER, parser.nextText());
                parser.nextTag(); // </father>

                parser.nextTag();
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRSTNAME, parser.nextText());

                parser.nextTag();
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER, parser.nextText());

                parser.nextTag();
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LASTNAME, parser.nextText());

                parser.nextTag();
                if ("middlename".equalsIgnoreCase(parser.getName())) {
                    parser.nextText();
                }

                // mother
                parser.nextTag(); // <mother>
                parser.nextTag(); // <memberships>
                parser.nextTag(); // </memberships>
                parser.nextTag(); // <residencies>
                parser.nextTag(); // </residencies>
                parser.nextTag(); // <extId>
                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER, parser.nextText());
                parser.nextTag(); // </mother>

                values.add(cv);

                if (groups != null) {
                    for (String item : groups) {
                        ContentValues socialGroups = new ContentValues();
                        socialGroups.put(OpenHDS.IndividualGroups.COLUMN_INDIVIDUALUUID,
                                cv.getAsString(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID));
                        socialGroups.put(OpenHDS.IndividualGroups.COLUMN_SOCIALGROUPUUID, item);
                        individualSocialGroups.add(socialGroups);
                    }
                }

                individualsParsed += 1;

                parser.nextTag(); // </individual>
                parser.nextTag(); // </individuals> or <individual>

                if (individualsParsed % 100 == 0) {
                    persistParsedIndividuals(individualSocialGroups);
                    values.clear();
                    individualSocialGroups.clear();
                    publishProgress(individualsParsed);
                }
            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage());
            }
        }

        persistParsedIndividuals(individualSocialGroups);
    }

    private void persistParsedIndividuals(List<ContentValues> individualSocialGroups) {
        if (!values.isEmpty()) {
            resolver.bulkInsert(OpenHDS.Individuals.CONTENT_ID_URI_BASE, values.toArray(emptyArray));
        }

        if (!individualSocialGroups.isEmpty()) {
            resolver.bulkInsert(OpenHDS.IndividualGroups.CONTENT_ID_URI_BASE,
                    individualSocialGroups.toArray(emptyArray));
        }
    }

    private List<String> parseMembershipExtIds(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<String> groups = new ArrayList<String>();

        while (!"memberships".equalsIgnoreCase(parser.getName())) {
            parser.nextTag(); // <socialGroup>
            parser.nextTag(); // <extId>
            groups.add(parser.nextText());
            parser.nextTag(); // <socialGroup>
            parser.nextTag(); // <bIsToA>
            parser.nextText();
            parser.nextTag(); // </membership>
            parser.nextTag(); // <membership> or </memberships>
        }

        return groups;
    }

    private void processRoundParams(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.nextTag();

        values.clear();
        while (notEndOfXmlDoc("rounds", parser)) {
            ContentValues cv = new ContentValues();

            parser.nextTag();
            cv.put(OpenHDS.Rounds.COLUMN_ROUND_ENDDATE, parser.nextText());

            // skip <remarks />
            parser.nextTag();
            parser.nextText();
            
            parser.nextTag();
            cv.put(OpenHDS.Rounds.COLUMN_ROUND_NUMBER, parser.nextText());

            parser.nextTag();
            cv.put(OpenHDS.Rounds.COLUMN_ROUND_STARTDATE, parser.nextText());

            // skip <uuid />
            parser.nextTag();
            parser.nextText();

            values.add(cv);

            parser.nextTag(); // </round>
            parser.nextTag(); // </rounds> or <round>
        }

        if (!values.isEmpty()) {
            resolver.bulkInsert(OpenHDS.Rounds.CONTENT_ID_URI_BASE, values.toArray(emptyArray));
        }
    }

    private void processVisitParams(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.nextTag();

        values.clear();
        while (notEndOfXmlDoc("visits", parser)) {
            // skip collected by
            parser.nextTag(); // <collectedBy>
            parser.nextTag(); // <extId>
            parser.nextText();
            parser.nextTag(); // </collectedBy>

            ContentValues cv = new ContentValues();

            parser.nextTag();
            cv.put(OpenHDS.Visits.COLUMN_VISIT_EXTID, parser.nextText());

            parser.nextTag();
            cv.put(OpenHDS.Visits.COLUMN_VISIT_ROUND, parser.nextText());

            parser.nextTag();
            cv.put(OpenHDS.Visits.COLUMN_VISIT_DATE, parser.nextText());

            parser.nextTag(); // <visitLocation>
            parser.nextTag();
            cv.put(OpenHDS.Visits.COLUMN_VISIT_LOCATION, parser.nextText());
            parser.nextTag(); // </visitLocation>

            values.add(cv);

            parser.nextTag(); // </visit>
            parser.nextTag(); // </visits> or <visit>
        }

        if (!values.isEmpty()) {
            resolver.bulkInsert(OpenHDS.Visits.CONTENT_ID_URI_BASE, values.toArray(emptyArray));
        }
    }

    private void processSocialGroupParams(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.nextTag();

        values.clear();
        while (notEndOfXmlDoc("socialGroups", parser)) {
            ContentValues cv = new ContentValues();

            parser.nextTag();
            cv.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID, parser.nextText());

            parser.nextTag(); // <groupHead>
            parser.nextTag(); // <memberships>
            parser.nextTag(); // </memberships>
            parser.nextTag(); // <residencies>
            parser.nextTag(); // </residencies>
            parser.nextTag(); // <extId>
            cv.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPHEAD, parser.nextText());
            parser.nextTag(); // </groupHead>

            parser.nextTag();
            cv.put(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME, parser.nextText());

            values.add(cv);

            parser.nextTag(); // </socialGroup>
            parser.nextTag(); // </socialGroups> or <socialGroup>
        }

        if (!values.isEmpty()) {
            resolver.bulkInsert(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, values.toArray(emptyArray));
        }
    }

    private void processRelationshipParams(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.nextTag();

        values.clear();
        while (notEndOfXmlDoc("relationships", parser)) {
            ContentValues cv = new ContentValues();

            parser.nextTag(); // <individualA>
            parser.nextTag(); // <memberships>
            parser.nextTag(); // </memberships>
            parser.nextTag(); // <residencies>
            parser.nextTag(); // <residencies>
            parser.nextTag(); // <extId>
            cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A, parser.nextText());
            parser.nextTag(); // </individualA>

            parser.nextTag(); // <individualB>
            parser.nextTag(); // <memberships>
            parser.nextTag(); // </memberships>
            parser.nextTag(); // <residencies>
            parser.nextTag(); // <residencies>
            parser.nextTag();
            cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B, parser.nextText());
            parser.nextTag(); // </individualB>

            parser.nextTag();
            cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE, parser.nextText());

            parser.nextTag(); // <aIsToB>
            parser.nextText();

            values.add(cv);

            parser.nextTag(); // </relationship>
            parser.nextTag(); // </relationships> or <relationship>
        }

        if (!values.isEmpty()) {
            resolver.bulkInsert(OpenHDS.Relationships.CONTENT_ID_URI_BASE, values.toArray(emptyArray));
        }
    }

    protected void onPostExecute(final Boolean result) {
        listener.collectionComplete(result);
    }
}

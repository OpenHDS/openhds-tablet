package org.openhds.mobile.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openhds.mobile.FormsProviderAPI;
import org.openhds.mobile.InstanceProviderAPI;
import org.openhds.mobile.listener.OdkFormLoadListener;
import org.openhds.mobile.model.Child;
import org.openhds.mobile.model.FilledForm;
import org.openhds.mobile.model.FilledParams;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

public class OdkGeneratedFormLoadTask extends AsyncTask<Void, Void, Boolean> {

    private OdkFormLoadListener listener;
    private ContentResolver resolver;
    private Uri odkUri;
    private FilledForm filledForm;

    public OdkGeneratedFormLoadTask(ContentResolver resolver, FilledForm filledForm, OdkFormLoadListener listener) {
        this.listener = listener;
        this.resolver = resolver;
        this.filledForm = filledForm;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        Cursor cursor = getCursorForFormsProvider(filledForm.getFormName());
        if (cursor.moveToFirst()) {
            String jrFormId = cursor.getString(0);
            String formFilePath = cursor.getString(1);
            String xml = processXml(jrFormId, formFilePath);

            File targetFile = saveFile(xml);
            if (targetFile != null) {
                return writeContent(targetFile, filledForm.getFormName(), jrFormId);
            }
        }
        cursor.close();

        return false;
    }

    private Cursor getCursorForFormsProvider(String name) {
        return resolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, new String[] {
                FormsProviderAPI.FormsColumns.JR_FORM_ID, FormsProviderAPI.FormsColumns.FORM_FILE_PATH },
                FormsProviderAPI.FormsColumns.JR_FORM_ID + " like ?", new String[] { name + "%" }, null);
    }

    private String processXml(String jrFormId, String formFilePath) {

        StringBuilder sbuilder = new StringBuilder();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new FileInputStream(formFilePath));

            Node node = doc.getElementsByTagName("data").item(0);
            sbuilder.append("<data id=\"" + jrFormId + "\">" + "\r\n");

            processNodeChildren(node, sbuilder);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return sbuilder.toString();
    }

    private void processNodeChildren(Node node, StringBuilder sbuilder) {
        NodeList childElements = node.getChildNodes();

        List<String> params = FilledParams.getParamsArray();
        for (int i = 0; i < childElements.getLength(); i++) {
            Node n = childElements.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String name = n.getNodeName();

                if (params.contains(name)) {
                    if (name.equals(FilledParams.visitId)) {
                        sbuilder.append(filledForm.getVisitExtId() == null ? "<visitId />" + "\r\n" : "<visitId>"
                                + filledForm.getVisitExtId() + "</visitId>" + "\r\n");
                    } else if (name.equals(FilledParams.roundNumber)) {
                        sbuilder.append(filledForm.getRoundNumber() == null ? "<roundNumber />" + "\r\n"
                                : "<roundNumber>" + filledForm.getRoundNumber() + "</roundNumber>" + "\r\n");
                    } else if (name.equals(FilledParams.visitDate)) {
                        sbuilder.append(filledForm.getVisitDate() == null ? "<visitDate />" + "\r\n" : "<visitDate>"
                                + filledForm.getVisitDate() + "</visitDate>" + "\r\n");
                    } else if (name.equals(FilledParams.individualId)) {
                        sbuilder.append(filledForm.getIndividualExtId() == null ? "<individualId />" + "\r\n"
                                : "<individualId>" + filledForm.getIndividualExtId() + "</individualId>" + "\r\n");
                    } else if (name.equals(FilledParams.motherId)) {
                        sbuilder.append(filledForm.getMotherExtId() == null ? "<motherId />" + "\r\n" : "<motherId>"
                                + filledForm.getMotherExtId() + "</motherId>" + "\r\n");
                    } else if (name.equals(FilledParams.fatherId)) {
                        sbuilder.append(filledForm.getFatherExtId() == null ? "<fatherId />" + "\r\n" : "<fatherId>"
                                + filledForm.getFatherExtId() + "</fatherId>" + "\r\n");
                    } else if (name.equals(FilledParams.firstName)) {
                        sbuilder.append(filledForm.getIndividualFirstName() == null ? "<firstName />" + "\r\n"
                                : "<firstName>" + filledForm.getIndividualFirstName() + "</firstName>" + "\r\n");
                    } else if (name.equals(FilledParams.lastName)) {
                        sbuilder.append(filledForm.getIndividualLastName() == null ? "<lastName />" + "\r\n"
                                : "<lastName>" + filledForm.getIndividualLastName() + "</lastName>" + "\r\n");
                    } else if (name.equals(FilledParams.gender)) {
                        sbuilder.append(filledForm.getIndividualGender() == null ? "<gender />" + "\r\n" : "<gender>"
                                + (filledForm.getIndividualGender().equalsIgnoreCase("Male") ? "1" : "2") + "</gender>"
                                + "\r\n");
                    } else if (name.equals(FilledParams.dob)) {
                        sbuilder.append(filledForm.getIndividualDob() == null ? "<dob />" + "\r\n" : "<dob>"
                                + filledForm.getIndividualDob() + "</dob>" + "\r\n");
                    } else if (name.equals(FilledParams.locationId)) {
                        sbuilder.append(filledForm.getLocationId() == null ? "<locationId />" + "\r\n" : "<locationId>"
                                + filledForm.getLocationId() + "</locationId>" + "\r\n");
                    } else if (name.equals(FilledParams.houseName)) {
                        sbuilder.append(filledForm.getHouseName() == null ? "<houseName />" + "\r\n" : "<houseName>"
                                + filledForm.getHouseName() + "</houseName>" + "\r\n");
                    } else if (name.equals(FilledParams.hierarchyId)) {
                        sbuilder.append(filledForm.getHierarchyId() == null ? "<hierarchyId />" + "\r\n"
                                : "<hierarchyId>" + filledForm.getHierarchyId() + "</hierarchyId>" + "\r\n");
                    } else if (name.equals(FilledParams.latlong))
                        sbuilder.append("<latlong />" + "\r\n");
                    else if (name.equals(FilledParams.householdId)) {
                        sbuilder.append(filledForm.getHouseholdId() == null ? "<householdId />" + "\r\n"
                                : "<householdId>" + filledForm.getHouseholdId() + "</householdId>" + "\r\n");
                    } else if (name.equals(FilledParams.householdName)) {
                        sbuilder.append(filledForm.getHouseholdName() == null ? "<householdName />" + "\r\n"
                                : "<householdName>" + filledForm.getHouseholdName() + "</householdName>" + "\r\n");
                    } else if (name.equals(FilledParams.fieldWorkerId)) {
                        sbuilder.append(filledForm.getFieldWorkerId() == null ? "<fieldWorkerId />" + "\r\n"
                                : "<fieldWorkerId>" + filledForm.getFieldWorkerId() + "</fieldWorkerId>" + "\r\n");
                    } else if (name.equals(FilledParams.individualA)) {
                        sbuilder.append(filledForm.getIndividualA() == null ? "<individualA />" + "\r\n" : "<individualA>"
                                + filledForm.getIndividualA() + "</individualA>" + "\r\n");
                    } else if (name.equals(FilledParams.individualB)) {
                        sbuilder.append(filledForm.getIndividualB() == null ? "<individualB />" + "\r\n" : "<individualB>"
                                + filledForm.getIndividualB() + "</individualB>" + "\r\n");
                    } else if (name.equals(FilledParams.migrationType)) {
                        sbuilder.append(filledForm.getMigrationType() == null ? "<migrationType />" + "\r\n" : "<migrationType>"
                                + filledForm.getMigrationType() + "</migrationType>" + "\r\n");
                    }
                } else if (name.equalsIgnoreCase("outcomes")) {
                    // special case handling for pregnancy outcomes
                    for(Child child : filledForm.getPregOutcomeChildren()) {
                        sbuilder.append("<outcomes>\r\n");
                        sbuilder.append("<outcomeType>LBR</outcomeType>\r\n");
                        sbuilder.append("<childId>" + child.getId() + "</childId>\r\n");
                        sbuilder.append("<firstName />\r\n");
                        sbuilder.append("<lastName />\r\n");
                        sbuilder.append("<gender />\r\n");
                        sbuilder.append("<dateOfBirth />\r\n");
                        sbuilder.append("<partialDate />\r\n");
                        sbuilder.append("<socialGroupId>" + filledForm.getHouseholdId() + "</socialGroupId>\r\n");
                        sbuilder.append("<relationshipToGroupHead />\r\n");
                        sbuilder.append("</outcomes>\r\n");
                    }
                } else {
                    if (!n.hasChildNodes())
                        sbuilder.append("<" + name + " />" + "\r\n");
                    else {
                        sbuilder.append("<" + name + ">" + "\r\n");
                        processNodeChildren(n, sbuilder);
                    }
                }
            }
        }
        sbuilder.append("</" + node.getNodeName() + ">" + "\r\n");
    }

    private File saveFile(String xml) {
        File root = Environment.getExternalStorageDirectory();
        String destinationPath = root.getAbsolutePath() + File.separator + "Android" + File.separator + "data"
                + File.separator + "org.openhds.mobile" + File.separator + "files";

        File baseDir = new File(destinationPath);
        if (!baseDir.exists()) {
            boolean created = baseDir.mkdirs();
            if (!created) {
                return null;
            }
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
        df.setTimeZone(TimeZone.getDefault());
        String date = df.format(new Date());

        destinationPath += File.separator + date + ".xml";
        File targetFile = new File(destinationPath);
        if (!targetFile.exists()) {
            try {
                FileWriter writer = new FileWriter(targetFile);
                writer.write(xml);
                writer.close();
            } catch (IOException e) {
                return null;
            }
        }
        return targetFile;
    }

    private boolean writeContent(File targetFile, String displayName, String formId) {

        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, targetFile.getAbsolutePath());
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, displayName);
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, formId);
        odkUri = resolver.insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);
        if (odkUri == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if (result)
            listener.onOdkFormLoadSuccess(odkUri);
        else
            listener.onOdkFormLoadFailure();
    }
}

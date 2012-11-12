package org.openhds.mobile.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FormXmlReader {

    private XPath xpath = XPathFactory.newInstance().newXPath();
    private DocumentBuilder builder;

    public Location readLocation(InputStream is) {
        try {
            Location location = new Location();

            Document doc = buildDocument(is);
            location.setName(xpath.evaluate("/data/locationName/text()", doc));
            location.setExtId(xpath.evaluate("/data/locationId/text()", doc));
            location.setHierarchy(xpath.evaluate("/data/hierarchyId/text()", doc));

            return location;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }

        return null;
    }

    private Document buildDocument(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        if (builder == null) {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        
        Document doc = builder.parse(is);
        return doc;
    }

    public Visit readVisit(InputStream input) {
        try {
            Visit visit = new Visit();
            Document doc = buildDocument(input);
            visit.setDate(xpath.evaluate("/data/visitDate/text()", doc));
            visit.setExtId(xpath.evaluate("/data/visitId/text()", doc));
            visit.setLocation(xpath.evaluate("/data/locationId/text()", doc));
            visit.setRound(xpath.evaluate("/data/roundNumber/text()", doc));
            return visit;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }
        return null;
    }

    public SocialGroup readSocialGroup(InputStream input) {
        try {
            Document doc = buildDocument(input);
            
            SocialGroup sg = new SocialGroup();
            sg.setExtId(xpath.evaluate("/data/householdId/text()", doc));
            sg.setGroupHead(xpath.evaluate("/data/individualId/text()", doc));
            sg.setGroupName(xpath.evaluate("/data/householdName/text()", doc));
            
            return sg;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }
        return null;
    }

    public Individual readInMigration(FileInputStream fileInputStream) {
        try {
            Document doc = buildDocument(fileInputStream);
            
            Individual individual = new Individual();
            individual.setCurrentResidence(xpath.evaluate("/data/locationId/text()", doc));
            individual.setDob(xpath.evaluate("/data/individualInfo/dateOfBirth/text()", doc));
            individual.setExtId(xpath.evaluate("/data/individualInfo/individualId/text()", doc));
            individual.setFather(xpath.evaluate("/data/individualInfo/fatherId/text()", doc));
            individual.setFirstName(xpath.evaluate("/data/individualInfo/firstName/text()", doc));
            individual.setGender(xpath.evaluate("/data/individualInfo/gender/text()", doc));
            individual.setLastName(xpath.evaluate("/data/individualInfo/lastName/text()", doc));
            individual.setMother(xpath.evaluate("/data/individualInfo/motherId/text()", doc));
            
            return individual;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }
        return null;
    }

    public PregnancyOutcome readPregnancyOutcome(FileInputStream fileInputStream) {
        try {
            Document doc = buildDocument(fileInputStream);
            
            PregnancyOutcome pregOutcome = new PregnancyOutcome();
            
            Individual mother = new Individual();
            mother.setExtId(xpath.evaluate("/data/motherId/text()", doc));
            pregOutcome.setMother(mother);
            
            Individual father = new Individual();
            father.setExtId(xpath.evaluate("/data/fatherId/text()", doc));
            pregOutcome.setFather(father);
            
            pregOutcome.setRecordedDate(xpath.evaluate("/data/recordedDate/text()", doc));
            
            // read the children that were born
            NodeList nodeList = (NodeList) xpath.evaluate("//outcomes", doc, XPathConstants.NODESET);
            for(int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String outcomeType = xpath.evaluate("./outcomeType/text()", node);
                if (!"LBR".equalsIgnoreCase(outcomeType)) {
                    continue;
                }
                
                Individual individual = new Individual();
                individual.setDob(pregOutcome.getRecordedDate());
                individual.setExtId(xpath.evaluate("./childId/text()", node));
                individual.setFather(father.getExtId());
                individual.setMother(mother.getExtId());
                individual.setFirstName(xpath.evaluate("./firstName/text()", node));
                individual.setGender(xpath.evaluate("./gender/text()", node));
                individual.setLastName(xpath.evaluate(".//lastName/text()", node));
                SocialGroup group = new SocialGroup();
                group.setExtId(xpath.evaluate("./socialGroupId/text()", node));
                individual.setSocialGroups(Arrays.asList(group));
                
                pregOutcome.addChild(individual);
            }
            
            return pregOutcome;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }
        return null;
    }

    public Relationship readRelationship(FileInputStream fileInputStream) {
        try {
            Document doc = buildDocument(fileInputStream);
            
            Relationship relationship = new Relationship();
            relationship.setIndividualA(xpath.evaluate("/data/individualA/text()", doc));
            relationship.setIndividualB(xpath.evaluate("/data/individualB/text()", doc));
            relationship.setStartDate(xpath.evaluate("/data/startDate/text()", doc));
            
            return relationship;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }
        return null;
    }

}

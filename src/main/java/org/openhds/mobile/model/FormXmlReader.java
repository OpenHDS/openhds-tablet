package org.openhds.mobile.model;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class FormXmlReader {

    XPath xpath = XPathFactory.newInstance().newXPath();

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
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
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

}

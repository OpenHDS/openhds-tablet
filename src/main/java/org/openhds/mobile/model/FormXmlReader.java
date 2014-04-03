package org.openhds.mobile.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public Location readLocation(InputStream is, String jrFormId) {
        try {
            Location location = new Location();
            
            Document doc = buildDocument(is);
            if(xpath.evaluate("/"+jrFormId+"/locationId/text()", doc).length()==0) {
            	jrFormId ="data";
            }
            location.setName(xpath.evaluate("/"+jrFormId+"/locationName/text()", doc));
            location.setExtId(xpath.evaluate("/"+jrFormId+"/locationId/text()", doc));
            location.setHierarchy(xpath.evaluate("/"+jrFormId+"/hierarchyId/text()", doc));

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

    public Visit readVisit(InputStream input, String jrFormId) {
        try {
            Visit visit = new Visit();
            Document doc = buildDocument(input);
            if(xpath.evaluate("/"+jrFormId+"/visitId/text()", doc).length()==0) {
            	jrFormId ="data";
            }
            visit.setDate(xpath.evaluate("/"+jrFormId+"/visitDate/text()", doc));
            visit.setExtId(xpath.evaluate("/"+jrFormId+"/visitId/text()", doc));
            visit.setLocation(xpath.evaluate("/"+jrFormId+"/locationId/text()", doc));
            visit.setRound(xpath.evaluate("/"+jrFormId+"/roundNumber/text()", doc));
            visit.setIntervieweeId(xpath.evaluate("/"+jrFormId+"/intervieweeId/text()", doc));
            return visit;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }
        return null;
    }

    
    public Membership readMembership(InputStream input, String jrFormId) {
        try {
            Membership membership = new Membership();
            Document doc = buildDocument(input);
            if(xpath.evaluate("/"+jrFormId+"/individualId/text()", doc).length()==0) {
            	jrFormId ="data";
            }
            membership.setIndExtId(xpath.evaluate("/"+jrFormId+"/individualId/text()", doc));
            membership.setGroupextId(xpath.evaluate("/"+jrFormId+"/householdId/text()", doc));
           
            return membership;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }
        return null;
    }
    
    
    public SocialGroup readSocialGroup(InputStream input, String jrFormId) {
        try {
            Document doc = buildDocument(input);
            if(xpath.evaluate("/"+jrFormId+"/individualId/text()", doc).length()==0) {
            	jrFormId ="data";
            }
            SocialGroup sg = new SocialGroup();
            sg.setExtId(xpath.evaluate("/"+jrFormId+"/householdId/text()", doc));
            sg.setGroupHead(xpath.evaluate("/"+jrFormId+"/individualId/text()", doc));
            sg.setGroupName(xpath.evaluate("/"+jrFormId+"/householdName/text()", doc));
            
            return sg;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }
        return null;
    }

    public Individual readInMigration(FileInputStream fileInputStream, String jrFormId)  {
        try {
            Document doc = buildDocument(fileInputStream);
            if(xpath.evaluate("/"+jrFormId+"/visitId/text()", doc).length()==0) {
            	jrFormId ="data";
            }
            Individual individual = new Individual();
            individual.setCurrentResidence(xpath.evaluate("/"+jrFormId+"/locationId/text()", doc));
            if(xpath.evaluate("/"+jrFormId+"/individualInfo/dateOfBirth/text()", doc).length()>0) {
            	individual.setDobIn(xpath.evaluate("/"+jrFormId+"/individualInfo/dateOfBirth/text()", doc));
            }
            individual.setExtId(xpath.evaluate("/"+jrFormId+"/individualInfo/individualId/text()", doc));
            individual.setFather(xpath.evaluate("/"+jrFormId+"/individualInfo/fatherId/text()", doc));
            individual.setFirstName(xpath.evaluate("/"+jrFormId+"/individualInfo/firstName/text()", doc));
            individual.setMiddleName(xpath.evaluate("/"+jrFormId+"/individualInfo/middleName/text()", doc));
            individual.setGender(xpath.evaluate("/"+jrFormId+"/individualInfo/gender/text()", doc));
            individual.setLastName(xpath.evaluate("/"+jrFormId+"/individualInfo/lastName/text()", doc));
            individual.setMother(xpath.evaluate("/"+jrFormId+"/individualInfo/motherId/text()", doc));
            
            return individual;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }
        return null;
    }

    public PregnancyOutcome readPregnancyOutcome(FileInputStream fileInputStream, String jrFormId)  {
        try {
            Document doc = buildDocument(fileInputStream);
            
            PregnancyOutcome pregOutcome = new PregnancyOutcome();
            if(xpath.evaluate("/"+jrFormId+"/motherId/text()", doc).length()==0) {
            	jrFormId ="data";
            }
            Individual mother = new Individual();
            mother.setExtId(xpath.evaluate("/"+jrFormId+"/motherId/text()", doc));
            pregOutcome.setMother(mother);
            
            Individual father = new Individual();
            father.setExtId(xpath.evaluate("/"+jrFormId+"/fatherId/text()", doc));
            pregOutcome.setFather(father);
            
            pregOutcome.setRecordedDate(xpath.evaluate("/"+jrFormId+"/recordedDate/text()", doc));
            
            // read the children that were born
            NodeList nodeList = (NodeList) xpath.evaluate("//outcomes", doc, XPathConstants.NODESET);
            for(int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String outcomeType = xpath.evaluate("./outcomeType/text()", node);
                if (!"LBR".equalsIgnoreCase(outcomeType)) {
                    continue;
                } 
                
                Individual individual = new Individual();
                individual.setDobIn(pregOutcome.getRecordedDate());
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

    public Relationship readRelationship(FileInputStream fileInputStream, String jrFormId)  {
        try {
            Document doc = buildDocument(fileInputStream);
            if(xpath.evaluate("/"+jrFormId+"/individualA/text()", doc).length()==0) {
            	jrFormId ="data";
            }
            Relationship relationship = new Relationship();
            relationship.setIndividualA(xpath.evaluate("/"+jrFormId+"/individualA/text()", doc));
            relationship.setIndividualB(xpath.evaluate("/"+jrFormId+"/individualB/text()", doc));
            relationship.setStartDate(xpath.evaluate("/"+jrFormId+"/startDate/text()", doc));
            
            return relationship;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }
        return null;
    }
    
    public Individual readOutMigration(FileInputStream fileInputStream, String jrFormId)  {
        try {
            Document doc = buildDocument(fileInputStream);
            Individual individual = new Individual();
            if(xpath.evaluate("/"+jrFormId+"/individualId/text()", doc).length()==0) {
            	jrFormId ="data";
            }
            individual.setExtId(xpath.evaluate("/"+jrFormId+"/individualId/text()", doc));
            return individual;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }
        return null;
    }

    public Individual readDeath(FileInputStream fileInputStream, String jrFormId) {
        try {
            Document doc = buildDocument(fileInputStream);
            Individual individual = new Individual();
            if(xpath.evaluate("/"+jrFormId+"/individualId/text()", doc).length()==0) {
            	jrFormId ="data";
            }
            individual.setExtId(xpath.evaluate("/"+jrFormId+"/individualId/text()", doc));
            return individual;
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }
        return null;
    }

}

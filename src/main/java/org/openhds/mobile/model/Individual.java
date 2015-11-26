package org.openhds.mobile.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class Individual implements Serializable {

    private static final long serialVersionUID = -799404570247633403L;

    private String extId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private String dob;
    private String mother;
    private String father;
    private String currentResidence;
    private String endType;
    private String visited;

    private List<SocialGroup> socialGroups;

    private static Individual individual;

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    // dates come in from the web service in dd-MM-yyyy format but
    // they must be changed to yyyy-MM-dd for ODK Collect
    public void setDob(String dob) {
        try {
            DateFormat inFormat = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");

            String date = outFormat.format(inFormat.parse(dob));
            this.dob = date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void setDobIn(String dob) {
        try {
            DateFormat outFormat = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd");

            String date = outFormat.format(inFormat.parse(dob));
            this.dob = date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void setDobFromCollect(String dob) {
        try {
            DateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");

            String date = outFormat.format(outFormat.parse(dob));
            this.dob = date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    
    public String getMother() {
        return mother;
    }

    public void setMother(String mother) {
        this.mother = mother;
    }

    public String getFather() {
        return father;
    }

    public void setFather(String father) {
        this.father = father;
    }

    public String getCurrentResidence() {
        return currentResidence;
    }

    public void setCurrentResidence(String currentResidence) {
        this.currentResidence = currentResidence;
    }

    public List<SocialGroup> getSocialGroups() {
        return socialGroups;
    }

    public void setSocialGroups(List<SocialGroup> socialGroups) {
        this.socialGroups = socialGroups;
    }

    public String getFullName() {
        StringBuilder builder = new StringBuilder();
        if (lastName != null) {
            builder.append(lastName);
        }

        if (firstName != null) {
            builder.insert(0, firstName);
        }

        return builder.toString();
    }

    public static Individual emptyIndividual() {
        if (individual == null) {
            individual = new Individual();
          //  individual.setDob("1900-01-01");
            individual.setExtId("");
            individual.setFirstName("");
            individual.setLastName("");
        }

        return individual;
    }

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getEndType() {
		return endType;
	}

	public void setEndType(String endType) {
		this.endType = endType;
	}
	
	public String getVisited() {
		return this.visited;
	}

	public void setVisited(String visited) {
		this.visited = visited==null?"False":visited;
	}
}

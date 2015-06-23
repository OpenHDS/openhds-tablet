package org.openhds.mobile.model;

import java.io.Serializable;

public class Form implements Serializable {

    private static final long serialVersionUID = -2367646883047152268L;

    private String name;
    
    private String gender;
    
    private String indExtId;
  


    public Form() {
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getIndExtId() {
		return indExtId;
	}

	public void setIndExtId(String indExtId) {
		this.indExtId = indExtId;
	}

 }

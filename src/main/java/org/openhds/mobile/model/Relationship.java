package org.openhds.mobile.model;

import java.io.Serializable;

public class Relationship implements Serializable {

	private static final long serialVersionUID = -761650887262715820L;
	
	private String individualA;
	private String individualB;
	private String startDate;
	
	public String getIndividualA() {
		return individualA;
	}
	
	public void setIndividualA(String individualA) {
		this.individualA = individualA;
	}
	
	public String getIndividualB() {
		return individualB;
	}
	
	public void setIndividualB(String individualB) {
		this.individualB = individualB;
	}
	
	// dates come in from the web service in dd-MM-yyyy format
	public String getStartDate() {
		return startDate;
	}
	
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
}

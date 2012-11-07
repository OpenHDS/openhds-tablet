package org.openhds.mobile.model;

import java.io.Serializable;

public class Relationship implements Serializable {

	private static final long serialVersionUID = -761650887262715820L;
	
	private String maleIndividual;
	private String femaleIndividual;
	private String startDate;
	
	public String getMaleIndividual() {
		return maleIndividual;
	}
	
	public void setMaleIndividual(String maleIndividual) {
		this.maleIndividual = maleIndividual;
	}
	
	public String getFemaleIndividual() {
		return femaleIndividual;
	}
	
	public void setFemaleIndividual(String femaleIndividual) {
		this.femaleIndividual = femaleIndividual;
	}
	
	// dates come in from the web service in dd-MM-yyyy format
	public String getStartDate() {
		return startDate;
	}
	
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
}

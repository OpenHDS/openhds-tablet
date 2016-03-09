package org.openhds.mobile.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChangeHeadOfHousehold implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Individual oldHoh;
	private Individual newHoh;
	private String houseHoldExtId;
	private List<Relationship> newRelationships = new ArrayList<Relationship>();
	private String date;
	
	public Individual getOldHoh() {
		return oldHoh;
	}
	public void setOldHoh(Individual oldHoh) {
		this.oldHoh = oldHoh;
	}
	public Individual getNewHoh() {
		return newHoh;
	}
	public void setNewHoh(Individual newHoh) {
		this.newHoh = newHoh;
	}
	public List<Relationship> getNewRelationships() {
		return newRelationships;
	}
	public void setNewRelationships(List<Relationship> newRelationships) {
		this.newRelationships = newRelationships;
	}
	public String getHouseHoldExtId() {
		return houseHoldExtId;
	}
	public void setHouseHoldExtId(String houseHoldExtId) {
		this.houseHoldExtId = houseHoldExtId;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	public void addRelationship(Relationship relationship){
		newRelationships.add(relationship);
	}

}

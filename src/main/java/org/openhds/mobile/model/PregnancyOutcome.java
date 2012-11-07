package org.openhds.mobile.model;

import java.io.Serializable;

public class PregnancyOutcome implements Serializable {

	private static final long serialVersionUID = 4513523640145228695L;
	private Individual mother;
	private Individual father;
	private String child1ExtId;
	private String child2ExtId;
	
	public Individual getMother() {
		return mother;
	}
	
	public void setMother(Individual mother) {
		this.mother = mother;
	}
	
	public Individual getFather() {
		return father;
	}
	
	public void setFather(Individual father) {
		if (father == null)
			this.father = new Individual();
		else
			this.father = father;
	}
	
	public String getChild1ExtId() {
		return child1ExtId;
	}
	
	public void setChild1ExtId(String child1ExtId) {
		this.child1ExtId = child1ExtId;
	}
	
	public String getChild2ExtId() {
		return child2ExtId;
	}
	
	public void setChild2ExtId(String child2ExtId) {
		this.child2ExtId = child2ExtId;
	}
}

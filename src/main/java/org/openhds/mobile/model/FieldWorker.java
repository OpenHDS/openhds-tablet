package org.openhds.mobile.model;

import java.io.Serializable;

public class FieldWorker implements Serializable {
	
	private static final long serialVersionUID = -8973040054481039466L;
	private String extId;
	private String password;
	private String firstName;
	private String lastName;
	
	public FieldWorker() { }
	
	public FieldWorker(String extId, String firstName, String lastName) {
		this.extId = extId;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
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
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}

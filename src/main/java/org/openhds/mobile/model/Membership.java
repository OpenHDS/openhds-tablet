package org.openhds.mobile.model;

import java.io.Serializable;

public class Membership implements Serializable {

	private static final long serialVersionUID = 571090333555561853L;
	
	private String indExtId;
	private String groupextId;
	
	
	public String getIndExtId() {
		return indExtId;
	}
	public void setIndExtId(String indExtId) {
		this.indExtId = indExtId;
	}
	public String getGroupextId() {
		return groupextId;
	}
	public void setGroupextId(String groupextId) {
		this.groupextId = groupextId;
	}
	}

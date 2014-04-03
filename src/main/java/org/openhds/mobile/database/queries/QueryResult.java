package org.openhds.mobile.database.queries;

public class QueryResult {

	private String state;
	private String extId;
	private String name;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getExtId() {
		return extId;
	}

	public void setExtId(String extId) {
		this.extId = extId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "QueryResult[name: " + name + " extId: " + extId + " state: " + state + " ]";
	}
}

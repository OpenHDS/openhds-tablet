package org.openhds.mobile.model;

import java.util.Arrays;
import java.util.List;

/**
 * This class specifies the fields which are prefilled when an xform is loaded.
 * The convention is based on these naming standards, all other fields will be ignored.
 */
public class FilledParams {
	
	public static final String visitId = "visitId";
	public static final String roundNumber = "roundNumber";
	public static final String visitDate = "visitDate";
	public static final String locationId = "locationId";
	public static final String interviewee = "intervieweeId";
	public static final String farmhouse = "farmhouse";


	
	public static final String individualId = "individualId";
	public static final String motherId = "motherId";
	public static final String fatherId = "fatherId";
	public static final String firstName = "firstName";
	public static final String middleName = "middleName";

	public static final String lastName = "lastName";
	public static final String gender = "gender";
	public static final String dob = "dob";
	
	public static final String houseName = "houseName";
	public static final String hierarchyId = "hierarchyId";
	public static final String latlong = "latlong";
	
	public static final String householdId = "householdId";
	public static final String householdName = "householdName";
	public static final String socialGroupType = "socialGroupType";

	public static final String fieldWorkerId = "fieldWorkerId";
	
	public static final String child1Id = "child1Id";
	public static final String child2Id = "child2Id";
	public static final String childFatherId = "childFatherId";
	public static final String childFatherFirstName = "childFatherFirstName";
	public static final String childFatherLastName = "childFatherLastName";
	
	public static final String individualA = "individualA";
	public static final String individualB = "individualB";
	
	public static final String migrationType = "migrationType";
	
	public static final String origin = "origin";

	public static final String start = "start";
	
	public static final String deviceId = "deviceId";
			
	public static final String causeOfDeath = "causeOfDeath";

			
	public static List<String> getParamsArray() {

		return Arrays.asList(visitId, roundNumber, visitDate,interviewee,farmhouse, 
				individualId, motherId, fatherId, firstName, middleName,origin,start,deviceId, causeOfDeath,
				lastName, gender, dob, locationId, houseName,socialGroupType, 
				latlong, householdId, householdName, hierarchyId, 
				fieldWorkerId, child1Id, child2Id, childFatherId,
				childFatherFirstName, childFatherLastName,
				individualA, individualB, migrationType);
	}
}

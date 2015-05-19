package org.openhds.mobile.model;

import java.util.HashMap;
import java.util.Map;

public class Settings {

	private int minimumAgeOfParents;	
	private int minimumAgeOfHouseholdHead;
	private int minMarriageAge;	
	private int minimumAgeOfPregnancy;
	private String dateOfLastSync;
	private String dateOfLastFieldWorkerSync;
	private String dateOfLastFormsSync;
	private String visitLevel;
	
	public final static String MINIMUM_AGE_OF_PARENTS = "minAgeOfParents";
	public final static String MINIMUM_AGE_OF_HOUSEHOLDHEAD = "minAgeOfHouseholdHead";
	public final static String MINIMUM_AGE_OF_MARRIAGE = "minMarriageAge";
	public final static String MINIMUM_AGE_OF_PREGNANCY = "minAgeOfPregnancy";
	public final static String DATE_OF_LAST_SYNC = "dateOfLastSync";
	public final static String DATE_OF_LAST_FW_SYNC = "dateOfLastFieldWorkerSync";
	public final static String DATE_OF_LAST_FORMS_SYNC = "dateOfLastFormsSync";
	public final static String VISIT_LEVEL = "visitLevel";
	
	private Map<String, String> _settingsList;
	
	public Settings(Map<String, String> settingsList){
		
		if(settingsList == null) settingsList = new HashMap<String, String>();
		
		setSettings(settingsList);
		
		this._settingsList = settingsList;
	}

	/**
	 * @return the minimumAgeOfParents
	 */
	public int getMinimumAgeOfParents() {
		return minimumAgeOfParents;
	}

	/**
	 * @param minimumAgeOfParents the minimumAgeOfParents to set
	 */
	public void setMinimumAgeOfParents(int minimumAgeOfParents) {
		this.minimumAgeOfParents = minimumAgeOfParents;
	}

	/**
	 * @return the minimumAgeOfHouseholdHead
	 */
	public int getMinimumAgeOfHouseholdHead() {
		return minimumAgeOfHouseholdHead;
	}

	/**
	 * @param minimumAgeOfHouseholdHead the minimumAgeOfHouseholdHead to set
	 */
	public void setMinimumAgeOfHouseholdHead(int minimumAgeOfHouseholdHead) {
		this.minimumAgeOfHouseholdHead = minimumAgeOfHouseholdHead;
	}

	/**
	 * @return the minMarriageAge
	 */
	public int getMinMarriageAge() {
		return minMarriageAge;
	}

	/**
	 * @param minMarriageAge the minMarriageAge to set
	 */
	public void setMinMarriageAge(int minMarriageAge) {
		this.minMarriageAge = minMarriageAge;
	}

	/**
	 * @return the minimumAgeOfPregnancy
	 */
	public int getMinimumAgeOfPregnancy() {
		return minimumAgeOfPregnancy;
	}

	/**
	 * @param minimumAgeOfPregnancy the minimumAgeOfPregnancy to set
	 */
	public void setMinimumAgeOfPregnancy(int minimumAgeOfPregnancy) {
		this.minimumAgeOfPregnancy = minimumAgeOfPregnancy;
	}
	
	private void setSettings(Map<String, String> settingsList){
		for(String key: settingsList.keySet()){
			if(key.equals(MINIMUM_AGE_OF_MARRIAGE)){
				this.setMinMarriageAge(stringToInt(settingsList.get(key)));
			}else if(key.equals(MINIMUM_AGE_OF_HOUSEHOLDHEAD)){
				this.setMinimumAgeOfHouseholdHead(stringToInt(settingsList.get(key)));
			}else if(key.equals(MINIMUM_AGE_OF_PARENTS)){
				this.setMinimumAgeOfParents(stringToInt(settingsList.get(key)));
			}else if(key.equals(MINIMUM_AGE_OF_PREGNANCY)){
				this.setMinimumAgeOfPregnancy(stringToInt(settingsList.get(key)));
			}else if(key.equals(DATE_OF_LAST_SYNC)){
				this.setDateOfLastSync(settingsList.get(key));
			}else if(key.equals(DATE_OF_LAST_FW_SYNC)){
				this.setDateOfLastFieldWorkerSync(settingsList.get(key));
			}else if(key.equals(DATE_OF_LAST_FORMS_SYNC)){
				this.setDateOfLastFormsSync(settingsList.get(key));
			}else if(key.equals(VISIT_LEVEL)){
				this.setVisitLevel(settingsList.get(key));
			}
		}
	}
	
	private int stringToInt(String inputString){
		int result = -1;
		
		try{
			int foo = Integer.parseInt(inputString);
			result = foo;
		}
		catch(NumberFormatException nfe){
			
		}

		return result;
	}
	
	/**
	 * @return the dateOfLastSync
	 */
	public String getDateOfLastSync() {
		return dateOfLastSync;
	}

	/**
	 * @param dateOfLastSync the dateOfLastSync to set
	 */
	public void setDateOfLastSync(String dateOfLastSync) {
		this.dateOfLastSync = dateOfLastSync;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
    	for(String key: _settingsList.keySet()){
    		String value = _settingsList.get(key);
    		sb.append(key + ": " + value);
    		sb.append(System.getProperty("line.separator"));
    	}
		return sb.toString();
	}

	/**
	 * @return the dateOfLastFieldWorkerSync
	 */
	public String getDateOfLastFieldWorkerSync() {
		System.out.println("getDateOfLastFieldWorkerSync: " + dateOfLastFieldWorkerSync);
		return dateOfLastFieldWorkerSync;
	}

	/**
	 * @param dateOfLastFieldWorkerSync the dateOfLastFieldWorkerSync to set
	 */
	public void setDateOfLastFieldWorkerSync(String dateOfLastFieldWorkerSync) {
		this.dateOfLastFieldWorkerSync = dateOfLastFieldWorkerSync;
	}

	/**
	 * @return the dateOfLastFormsSync
	 */
	public String getDateOfLastFormsSync() {
		return dateOfLastFormsSync;
	}

	/**
	 * @param dateOfLastFormsSync the dateOfLastFormsSync to set
	 */
	public void setDateOfLastFormsSync(String dateOfLastFormsSync) {
		this.dateOfLastFormsSync = dateOfLastFormsSync;
	}

	public String getVisitLevel() {
		return visitLevel;
	}

	public void setVisitLevel(String visitLevel) {
		this.visitLevel = visitLevel;
	}	
}

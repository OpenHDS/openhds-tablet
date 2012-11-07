package org.openhds.mobile.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Supervisory workflow specific
 */
public class FormSubmissionRecord {
	private long id;
	private String formOwnerId;
	private String formType;
	private String partialForm;
	private String saveDate;
	private String odkUri;
	private String formId;
	private int remoteId;
	private boolean completed;
	private boolean needReview;
	private List<String> errors = new ArrayList<String>();

	public void setFormOwnerId(String text) {
		this.formOwnerId = text;
	}

	public void setFormType(String text) {
		this.formType = text;
	}

	public void setPartialForm(String text) {
		this.partialForm = text;
	}
	
	public String getPartialForm() {
		return partialForm;
	}

	public void addErrorMessage(String text) {
		errors.add(text);
	}

	public String getFormOwnerId() {
		return formOwnerId;
	}

	public String getFormType() {
		return formType;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSaveDate() {
		return saveDate;
	}
	
	public void setSaveDate(String dateTime) {
		this.saveDate = dateTime;
	}

	public String getOdkUri() {
		return odkUri;
	}

	public void setOdkUri(String odkUri) {
		this.odkUri = odkUri;
	}

	public long getId() {
		return id;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public int getRemoteId() {
		return remoteId;
	}

	public void setRemoteId(int remoteId) {
		this.remoteId = remoteId;
	}

	public boolean isNeedReview() {
		return needReview;
	}

	public void setNeedReview(boolean needReview) {
		this.needReview = needReview;
	}
}

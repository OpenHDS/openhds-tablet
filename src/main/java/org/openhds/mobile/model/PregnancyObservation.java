package org.openhds.mobile.model;

import java.io.Serializable;

public class PregnancyObservation implements Serializable {

    private static final long serialVersionUID = 4513523640145228695L;
    private Individual mother;
    private String recordedDate;

   
    public Individual getMother() {
        return mother;
    }

   
    public void setMother(Individual mother) {
        this.mother = mother;
    }

 
    public void setRecordedDate(String evaluate) {
        this.recordedDate = evaluate;
    }

    public String getRecordedDate() {
        return recordedDate;
    }

 
}

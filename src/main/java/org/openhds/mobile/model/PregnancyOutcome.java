package org.openhds.mobile.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PregnancyOutcome implements Serializable {

    private static final long serialVersionUID = 4513523640145228695L;
    private Individual mother;
    private Individual father;
    private String recordedDate;

    private List<String> childIds = new ArrayList<String>();
    private List<Individual> children = new ArrayList<Individual>();

    public Individual getMother() {
        return mother;
    }

    public void setMother(Individual mother) {
        this.mother = mother;
    }

    public void addChildId(String childId) {
        childIds.add(childId);
    }

    public List<String> getChildIds() {
        return childIds;
    }

    public void setFather(Individual father) {
        this.father = father;
    }

    public void setRecordedDate(String evaluate) {
        this.recordedDate = evaluate;
    }

    public String getRecordedDate() {
        return recordedDate;
    }

    public void addChild(Individual individual) {
        children.add(individual);
    }

    public List<Individual> getChildren() {
        return children;
    }
}

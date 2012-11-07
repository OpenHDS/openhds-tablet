package org.openhds.mobile.model;

import java.io.Serializable;

public class LocationHierarchy implements Serializable {

    private static final long serialVersionUID = -6370062790248563906L;

    private String extId;
    private String name;
    private String parent;
    private String level;

    private static LocationHierarchy hierarchy;

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

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public static LocationHierarchy emptyHierarchy() {
        if (hierarchy == null) {
            hierarchy = new LocationHierarchy();
            hierarchy.setExtId("");
            hierarchy.setName("");
        }
        
        return hierarchy;
    }
}

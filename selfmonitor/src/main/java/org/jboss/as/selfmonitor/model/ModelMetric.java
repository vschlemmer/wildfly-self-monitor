package org.jboss.as.selfmonitor.model;

import java.util.Objects;

/**
 * A simple class representing metric in the form it is represented in the 
 * configuration of the selfmonitor subsystem
 * 
 * @author Vojtech Schlemmer
 */
public class ModelMetric {

    private String id;
    private String path;
    private boolean enabled;
    private int interval;
    private String type;
    private String description;
    private boolean nillable;
    private String dataType;

    public ModelMetric(String id, String path, boolean enabled, int interval, 
            String type, String description, boolean nillable, String dataType) {
        this.id = id;
        this.path = path;
        this.enabled = enabled;
        this.interval = interval;
        this.type = type;
        this.description = description;
        this.nillable = nillable;
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public String getNameFromId(){
        String[] parts = this.id.split("_");
        return parts[parts.length-1];
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ModelMetric other = (ModelMetric) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ModelMetric{" + "id=" + id + ", path=" + path + ", enabled=" + enabled + ", interval=" + interval + ", type=" + type + ", description=" + description + ", nillable=" + nillable + '}';
    }

    
    
}
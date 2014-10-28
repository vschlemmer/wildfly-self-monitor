package org.jboss.as.selfmonitor.model;

import java.util.Objects;

/**
 * A simple class representing metric in the form it is represented in the 
 * configuration of the selfmonitor subsystem
 * 
 * @author Vojtech Schlemmer
 */
public class ModelMetric {

    private String name;
    private String path;
    private boolean enabled;

    public ModelMetric(String name, String path, boolean enabled) {
        this.name = name;
        this.path = path;
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + Objects.hashCode(this.path);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        final ModelMetric other = (ModelMetric) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        return true;
    }
    
    
}
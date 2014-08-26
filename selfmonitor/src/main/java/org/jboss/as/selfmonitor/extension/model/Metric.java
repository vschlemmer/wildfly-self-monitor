package org.jboss.as.selfmonitor.extension.model;

/**
 *
 * @author Vojtech Schlemmer
 */
public class Metric {

    private String name;
    private String path;

    public Metric(String name, String path) {
        this.name = name;
        this.path = path;
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
    
    
}
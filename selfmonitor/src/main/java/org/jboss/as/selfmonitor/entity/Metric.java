package org.jboss.as.selfmonitor.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Class representing a metric entity and the way it is stored in database - with
 * value and date when the value was captured
 * 
 * @author Vojtech Schlemmer
 */
@Entity
@Table(name = "Metric")
public class Metric implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "metric_name")
    private String name;
    @Column(name = "metric_path")
    private String path;
    @Column(name = "metric_time")
    private long time;
    @Column(name = "metric_value", length=1024)
    private String value;

    public Metric(){
    }
    
    public Metric(String name, String path, long time, String value) {
        this.name = name;
        this.path = path;
        this.time = time;
        this.value = value;
    }
    
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

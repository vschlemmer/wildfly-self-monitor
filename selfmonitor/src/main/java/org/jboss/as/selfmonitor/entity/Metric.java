package org.jboss.as.selfmonitor.entity;

import java.io.Serializable;
import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
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
    @Column(name = "metric_date")
    private Date date;
    @Column(name = "metric_value")
    private String value;

    public Metric(){
    }
    
    public Metric(String name, String path, Date date, String value) {
        this.name = name;
        this.path = path;
        this.date = date;
        this.value = value;
    }
    
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

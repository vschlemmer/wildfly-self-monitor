package org.jboss.as.selfmonitor.storage;

import java.sql.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class providing memory storage of metric records
 * 
 * @author Vojtech Schlemmer
 */
public class MetricsMemoryStorage implements IMetricsStorage {

    private Map<String, Map<Date, Object>> metrics;

    public MetricsMemoryStorage(){
        metrics = new HashMap<>();
        metrics = Collections.synchronizedMap(metrics);
    }
    
    public Map<String, Map<Date, Object>> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Map<Date, Object>> metrics) {
        this.metrics = metrics;
    }
    
    @Override
    public void addMetric(String metricName, String metricPath, Date date, Object value) {
        String metricId = getMetricId(metricName, metricPath);
        addMetricRecord(metricId, date, value);
    }
    
    @Override
    public Map<Date, Object> getMetricRecords(String metricName, String metricPath){
        String metricId = getMetricId(metricName, metricPath);
        return this.metrics.get(metricId);
    }
    
    /**
     * Adds metric with its value and date when it's been captured to the 
     * memory storage
     * 
     * @param metricId metric path along with metric name
     * @param date date when the metric value has been captured
     * @param value  value of the metric
     */
    private void addMetricRecord(String metricId, Date date, Object value){
        if (this.metrics.containsKey(metricId)){
            this.metrics.get(metricId).put(date, value);
        }
        else{
            Map<Date, Object> newRecord = new TreeMap<>();
            newRecord.put(date, value);
            this.metrics.put(metricId, newRecord);
        }
    }
    
    /**
     * Retrieve metric id by simply concatenating its path and name
     * 
     * @param metricName metric name
     * @param metricPath metric path
     * @return id of the metric
     */
    private String getMetricId(String metricName, String metricPath){
        return metricPath + metricName;
    }
    
}

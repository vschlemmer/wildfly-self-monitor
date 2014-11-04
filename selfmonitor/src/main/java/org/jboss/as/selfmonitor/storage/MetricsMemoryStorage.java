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

    private Map<String, Map<Long, Object>> metrics;

    public MetricsMemoryStorage(){
        metrics = new HashMap<>();
        metrics = Collections.synchronizedMap(metrics);
    }
    
    public Map<String, Map<Long, Object>> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Map<Long, Object>> metrics) {
        this.metrics = metrics;
    }
    
    @Override
    public void addMetric(String metricName, String metricPath, long time, Object value) {
        String metricId = getMetricId(metricName, metricPath);
        addMetricRecord(metricId, time, value);
    }
    
    @Override
    public Map<Long, Object> getMetricRecords(String metricName, String metricPath){
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
    private void addMetricRecord(String metricId, long time, Object value){
        if (this.metrics.containsKey(metricId)){
            this.metrics.get(metricId).put(new Long(time), value);
        }
        else{
            Map<Long, Object> newRecord = new TreeMap<>();
            newRecord.put(new Long(time), value);
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

package org.jboss.as.selfmonitor.storage;

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

    private Map<String, Map<Long, String>> metricRecords;

    public MetricsMemoryStorage(){
        metricRecords = new HashMap<>();
        metricRecords = Collections.synchronizedMap(metricRecords);
    }
    
    public Map<String, Map<Long, String>> getMetrics() {
        return metricRecords;
    }

    public void setMetrics(Map<String, Map<Long, String>> metricRecords) {
        this.metricRecords = metricRecords;
    }
    
    @Override
    public void addMetric(String metricId, String metricPath, long time, String value) {
        addMetricRecord(metricId, time, value);
    }
    
    @Override
    public Map<Long, String> getMetricRecords(String metricId){
        return this.metricRecords.get(metricId);
    }
    
    /**
     * Adds metric with its value and date when it's been captured to the 
     * memory storage
     * 
     * @param metricId metric path along with metric name
     * @param date date when the metric value has been captured
     * @param value  value of the metric
     */
    private void addMetricRecord(String metricId, long time, String value){
        if (this.metricRecords.containsKey(metricId)){
            this.metricRecords.get(metricId).put(new Long(time), value);
        }
        else{
            Map<Long, String> newRecord = new TreeMap<>();
            newRecord.put(new Long(time), value);
            this.metricRecords.put(metricId, newRecord);
        }
    }
}

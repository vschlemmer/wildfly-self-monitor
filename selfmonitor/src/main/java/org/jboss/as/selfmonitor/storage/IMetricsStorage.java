package org.jboss.as.selfmonitor.storage;

import java.io.Serializable;
import java.util.Map;

/**
 * Storage of metric records
 * 
 * @author Vojtech Schlemmer
 */
public interface IMetricsStorage extends Serializable {
    
    /**
     * Stores value of a metric
     * 
     * @param metricId Id of the metric
     * @param metricPath Path of the metric
     * @param time Time the metric was captured in seconds
     * @param value Value of the metric
     */
    public void addMetric(String metricId, String metricPath, long time, String value);
    
    /**
     * retrieves values of a given metric
     * 
     * @param metricId Id of the metric
     * @return map where keys are time when metric was captured in seconds 
     * and values are values of the metric at the given time
     */
    public Map<Long, String> getMetricRecords(String metricId);
    
}

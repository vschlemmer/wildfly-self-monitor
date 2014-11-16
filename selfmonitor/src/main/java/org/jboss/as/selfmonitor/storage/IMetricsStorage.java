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
     * @param metricName Name of the metric
     * @param metricPath Path to the metric's resource
     * @param time Time the metric was captured in seconds
     * @param value Value of the metric
     */
    public void addMetric(String metricName, String metricPath, long time, String value);
    
    /**
     * retrieves values of a given metric
     * 
     * @param metricName name of the metric
     * @param metricPath path to the metric's resource
     * @return map where keys are time when metric was captured in seconds 
     * and values are values of the metric at the given time
     */
    public Map<Long, String> getMetricRecords(String metricName, String metricPath);
    
}

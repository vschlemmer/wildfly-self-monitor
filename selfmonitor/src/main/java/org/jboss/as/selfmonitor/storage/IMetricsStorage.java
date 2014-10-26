package org.jboss.as.selfmonitor.storage;

import java.sql.Date;
import java.util.Map;

/**
 *
 * @author Vojtech Schlemmer
 */
public interface IMetricsStorage {
    
    /**
     * Stores value of a metric
     * 
     * @param metricName Name of the metric
     * @param metricPath Path to the metric's resource
     * @param date Time and date the metric was captured
     * @param value Value of the metric
     */
    public void addMetric(String metricName, String metricPath, Date date, Object value);
    
    /**
     * retrieves values of a given metric in time
     * 
     * @param metricName name of the metric
     * @param metricPath path to the metric's resource
     * @return map where keys are time when metric was captured and values 
     *         are values of the metric at the given time
     */
    public Map<Date, Object> getMetricRecords(String metricName, String metricPath);
    
}

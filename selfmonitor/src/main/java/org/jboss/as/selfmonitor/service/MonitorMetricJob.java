package org.jboss.as.selfmonitor.service;

import java.io.IOException;
import java.util.logging.Level;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.selfmonitor.model.MetricPathResolver;
import org.jboss.as.selfmonitor.model.ModelMetric;
import org.jboss.as.selfmonitor.model.ModelScanner;
import org.jboss.as.selfmonitor.storage.IMetricsStorage;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
 
/**
 *
 * @author Vojtech Schlemmer
 */
@DisallowConcurrentExecution
public class MonitorMetricJob implements Job, InterruptableJob {
    
    private final ModelControllerClient client;
    private final IMetricsStorage metricsStorage;
    private boolean interrupted = false;
    private final Logger log = Logger.getLogger(MonitorMetricJob.class);

    public MonitorMetricJob(ModelControllerClient client, IMetricsStorage metricsStorage){
        this.client = client;
        this.metricsStorage = metricsStorage;
    }
    
    @Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
        if(!interrupted){
            storeMetric(context);
        }
	}
 
    /**
     * Stores all enabled metrics in "metrics" property with their values
     * 
     * @param time date and time of capturing the metric's value
     */
    private void storeMetric(JobExecutionContext context){
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        ModelMetric metric = getMetricFromAttribute(dataMap.getString("metricId"));
        long time = System.currentTimeMillis()/1000;
        ModelNode op = new ModelNode();
        op.get(ClientConstants.OP).set(ClientConstants.READ_RESOURCE_OPERATION);
        op.get(ClientConstants.INCLUDE_RUNTIME).set(true);  
        MetricPathResolver.resolvePath(metric.getPath(), op);
        ModelNode returnVal = null;
        if(client != null){
            try {
                returnVal = client.execute(op);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(SelfmonitorService.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            if (returnVal != null){
                Object metricValue = returnVal.get(
                        ClientConstants.RESULT).get(metric.getNameFromId());
                storeMetricValue(metric, metricValue.toString(), time, metricsStorage);
            }
        }
        else{
            log.info("client is null!!");
        }
    }
    
    /**
     * Parses input string representation of a metric (path+attribute name)
     * and creates a ModelMetric instance of it with "enabled" property 
     * set to false
     * 
     * @param attribute attribute to be parsed
     * @return ModelMetric instance of a given attribute with "enabled" 
     * property set to false
     */
    private ModelMetric getMetricFromAttribute(String attribute){
        String[] parts = attribute.split("/");
        String metricId = parts[parts.length-1];
        StringBuilder pathBuilder = new StringBuilder();
        for(int i = 0; i < parts.length-1; i++) {
            pathBuilder.append(parts[i]);
            pathBuilder.append("/");
        }
        String metricPath = pathBuilder.toString();
        //remove the trailing "/"
        int pathLength = metricPath.length();
        if(pathLength > 0){
            metricPath = metricPath.substring(0, pathLength-1);
        }
        String[] idParts = metricId.split("_");
        String metricName = idParts[idParts.length-1];
        ModelScanner scanner = new ModelScanner(client);
        ModelMetric m = scanner.getMetricFromAttribute(metricName, metricPath, 
                metricId, false, 5);
        return m;
    }
    
    /**
     * Stores a metric with its value and time when the value was captured 
     * into the storage
     * 
     * @param metric metric to be stored
     * @param value value of the metric
     * @param time date and time when the metric's value was captured
     */
    private void storeMetricValue(ModelMetric metric, 
            String value, long time, IMetricsStorage metricsStorage){
        metricsStorage.addMetric(metric.getId(), metric.getPath(), time, value);
    }
    
    @Override
    public void interrupt() throws UnableToInterruptJobException {
        interrupted = true;
    }
    
}
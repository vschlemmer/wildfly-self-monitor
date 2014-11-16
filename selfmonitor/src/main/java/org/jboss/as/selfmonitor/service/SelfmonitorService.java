package org.jboss.as.selfmonitor.service;

import org.jboss.as.selfmonitor.model.ModelMetric;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.selfmonitor.model.ModelScanner;
import org.jboss.as.selfmonitor.model.ModelWriter;
import org.jboss.as.selfmonitor.storage.IMetricsStorage;
import org.jboss.as.selfmonitor.storage.MetricsDbStorage;
import org.jboss.as.selfmonitor.storage.MetricsMemoryStorage;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.quartz.Scheduler;

/**
 * Class with selfmonitor service providing the basic functionalities related
 * to monitoring of the server runtime attributes (metrics)
 * 
 * TODO: Add to configuration whether to scan whole model for metrics or not
 * 
 * @author Vojtech Schlemmer
 */
public class SelfmonitorService implements Service<SelfmonitorService>{

    public static final String NAME = "SelfmonitorService01";
    public static final String HOST = "localhost";
    public static final int PORT = 9990;    
    // TODO: put this to subsystem configuration
    public static final String STORAGE_TYPE = "database";
    private final Logger log = Logger.getLogger(SelfmonitorService.class);
    private ModelControllerClient client;
    private static final long STARTUP_TIME = 1000;
    private IMetricsStorage metricsStorage;
    private Set<ModelMetric> metrics = Collections.synchronizedSet(new HashSet<ModelMetric>());
    private Set<String> attributes;
    private Map<String, Scheduler> jobs;
    private boolean initialized = false;
    
    public SelfmonitorService() {
        client = null;
        try {  
            client = ModelControllerClient.Factory.create(
                    InetAddress.getByName(HOST), PORT);
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(
                    SelfmonitorService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static ServiceName createServiceName() {
        return ServiceName.JBOSS.append(NAME);
    }
    
    private Thread OUTPUT = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    if(!initialized){
                        // time needed for client to startup
                        Thread.sleep(STARTUP_TIME);
                        if(STORAGE_TYPE.equals("database")){
                            metricsStorage = new MetricsDbStorage();
                        }
                        else{
                            metricsStorage = new MetricsMemoryStorage();
                        }
                        ModelScanner scanner = new ModelScanner(client);
                        ModelWriter writer = new ModelWriter(client);
                        modelScanAttributes(scanner, writer);
                        jobs = new HashMap<>();
                        int numberOfJobs = initMetricsStoreJobs();
                        log.info("Number of metrics monitored: " + numberOfJobs);
                        initialized = true;
                    }
                    Thread.sleep(10000);
                    for(ModelMetric metric : metrics){
                        if(metric.isEnabled()){
                            MonitorMetricJobHandler.logStoredMetric(log, metric, metricsStorage);
                        }
                    }                    
                } catch (InterruptedException e) {
                    interrupted();
                    break;
                }
            }
        }
        
    };
    
    
    private int initMetricsStoreJobs(){
        int numberOfEnabledMetrics = 0;
        for(ModelMetric metric : metrics){
            if(metric.isEnabled()){
                initSingleMetricJob(metric);
                numberOfEnabledMetrics++;
            }
        }
        return numberOfEnabledMetrics;   
    }
    
    private void initSingleMetricJob(ModelMetric metric){
        Scheduler scheduler = MonitorMetricJobHandler.initSingleMetricJob(
                metric, client, metricsStorage);
        if(scheduler != null){
            jobs.put(metric.getName(), scheduler);
        }
    }
    
    /**
     * Scans the whole resource model and for each node collects it's runtime
     * attributes
     * 
     * @param scanner ModelScanner which provides scanning API
     * @param writer writer that writes each found metric to selfmonitor
     *  subsystem configuration if not already present
     * 
     * @return number of attributes found in the model
     */
    private int modelScanAttributes(ModelScanner scanner, ModelWriter writer){
        log.info("Scanning the whole model for metrics, please wait...");
        attributes = scanner.getModelRuntimeAttributes();
        for(String attribute : attributes){
            ModelMetric m = getMetricFromAttribute(attribute);
            writer.addMetricToModelIfNotPresent(m);
            if(!metrics.contains(m)){
                metrics.add(m);
            }
        }
        log.info("Added " + metrics.size() + " metrics");
        return attributes.size();
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
        String metricName = parts[parts.length-1];
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < parts.length-1; i++) {
            builder.append(parts[i]);
            builder.append("/");
        }
        String metricPath = builder.toString();
        //remove the trailing "/"
        int pathLength = metricPath.length();
        if(pathLength > 0){
            metricPath = metricPath.substring(0, pathLength-1);
        }
        ModelMetric m = new ModelMetric(metricName, metricPath, false, 5);
        return m;
    }
    
    
    
    @Override
    public void start(StartContext sc) throws StartException {
        OUTPUT.start();
    }

    @Override
    public void stop(StopContext sc) {
        OUTPUT.interrupt();
    }

    @Override
    public SelfmonitorService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public Set<ModelMetric> getMetrics() {
        return metrics;
    }

    public void setMetrics(Set<ModelMetric> metrics) {
        this.metrics = metrics;
    }
    
    public void addMetric(ModelMetric metric){
        metrics.add(metric);
        if(initialized){
            initSingleMetricJob(metric);
        }
    }
    
    public void removeMetric(ModelMetric metric){
        metrics.remove(metric);
    }
    
    public void changeMetricEnabled(String metricName, String metricPath, boolean enabled){
        ModelMetric m = getMetric(metricName, metricPath);
        m.setEnabled(enabled);
        if(initialized){
            jobs = MonitorMetricJobHandler.changeMetricEnabled(m, jobs, 
                    client, metricsStorage);
        }
    }
    
    public void changeMetricInterval(String metricName, String metricPath, int interval){
        ModelMetric m = getMetric(metricName, metricPath);
        m.setInterval(interval);
        if(initialized && m.isEnabled() && jobs.containsKey(metricName)){
            MonitorMetricJobHandler.changeJobInterval(jobs, m);
        }
    }
    
    /**
     * Retrieves a metric from "metrics" property according to its name and path
     * 
     * @param metricName name of the metric
     * @param metricPath path of the metric
     * @return metric found or null
     */
    public ModelMetric getMetric(String metricName, String metricPath){
        for(ModelMetric metricIter : metrics){
            if (metricIter.getName().equals(metricName) &&
                metricIter.getPath().equals(metricPath)){
                return metricIter;
            }
        }
        return null;
    }

    public IMetricsStorage getMetricsStorage() {
        return metricsStorage;
    }
    
    public ModelControllerClient getClient(){
        return client;
    }

}

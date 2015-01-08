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
 * @author Vojtech Schlemmer
 */
public class SelfmonitorService implements Service<SelfmonitorService>{

    public static final String NAME = "SelfmonitorService01";
    public static final String HOST = "localhost";
    public static final int PORT = 9990;
    public String storageType = "database";
    private final Logger log = Logger.getLogger(SelfmonitorService.class);
    private ModelControllerClient client;
    private static final long STARTUP_TIME = 10000;
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
    
    private final Thread OUTPUT = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    if(!initialized){
                        // time needed for client to startup
                        Thread.sleep(STARTUP_TIME);
                        if(storageType.equals("database")){
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
                    Thread.sleep(5000);
                    
                } catch (InterruptedException e) {
                    interrupted();
                    break;
                }
            }
        }
        
    };

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }
    
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
            jobs.put(metric.getId(), scheduler);
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
            ModelMetric m = getMetricFromAttribute(attribute, scanner);
            writer.addMetricToModelIfNotPresent(m);
            if(!metrics.contains(m)){
                metrics.add(m);
            }
        }
        log.info("Added " + metrics.size() + " metrics");
        return metrics.size();
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
    private ModelMetric getMetricFromAttribute(String attribute, ModelScanner scanner){
        String[] parts = attribute.split("/");
        String metricName = parts[parts.length-1];
        StringBuilder pathBuilder = new StringBuilder();
        StringBuilder idBuilder = new StringBuilder();
        for(int i = 0; i < parts.length-1; i++) {
            String[] innerParts = parts[i].split("=");
            for (String innerPart : innerParts) {
                idBuilder.append(innerPart);
                idBuilder.append("_");
            }
            pathBuilder.append(parts[i]);
            pathBuilder.append("/");
        }
        String metricPath = pathBuilder.toString();
        String metricId = idBuilder.toString();
        //remove the first "_" from metric id
        int idLength = metricId.length();
        if(idLength > 0){
            metricId = metricId.substring(1, idLength);
        }
        metricId += metricName;
        //remove the trailing "/"
        int pathLength = metricPath.length();
        if(pathLength > 0){
            metricPath = metricPath.substring(0, pathLength-1);
        }
        ModelMetric m = scanner.getMetricFromAttribute(metricName, metricPath, 
                metricId, false, 5);
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
    
    public void changeMetricEnabled(String metricId, boolean enabled){
        ModelMetric m = getMetric(metricId);
        m.setEnabled(enabled);
        if(initialized){
            jobs = MonitorMetricJobHandler.changeMetricEnabled(m, jobs, 
                    client, metricsStorage);
        }
    }
    
    public void changeMetricNillable(String metricId, boolean nillable){
        ModelMetric m = getMetric(metricId);
        m.setNillable(nillable);
    }
    
    public void changeMetricType(String metricId, String type){
        ModelMetric m = getMetric(metricId);
        m.setType(type);
    }
    
    public void changeMetricDescription(String metricId, String description){
        ModelMetric m = getMetric(metricId);
        m.setDescription(description);
    }
    
    public void changeMetricInterval(String metricId, int interval){
        ModelMetric m = getMetric(metricId);
        m.setInterval(interval);
        if(initialized && m.isEnabled() && jobs.containsKey(metricId)){
            MonitorMetricJobHandler.changeJobInterval(jobs, m);
        }
    }
    
    /**
     * Retrieves a metric from "metrics" property according to its name and path
     * 
     * @param metricId id of the metric
     * @return metric found or null
     */
    public ModelMetric getMetric(String metricId){
        for(ModelMetric metricIter : metrics){
            if (metricIter.getId().equals(metricId)){
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

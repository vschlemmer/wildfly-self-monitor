package org.jboss.as.selfmonitor.service;

import org.jboss.as.selfmonitor.model.ModelMetric;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.selfmonitor.SubsystemAdd;
import org.jboss.as.selfmonitor.model.MetricPathResolver;
import org.jboss.as.selfmonitor.model.ModelScanner;
import org.jboss.as.selfmonitor.model.ModelWriter;
import org.jboss.as.selfmonitor.storage.IMetricsStorage;
import org.jboss.as.selfmonitor.storage.MetricsDbStorage;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 *
 * @author Vojtech Schlemmer
 */
public class SelfmonitorService implements Service<SelfmonitorService> {

    public static final String NAME = "SelfmonitorService01";
    private final Logger log = Logger.getLogger(SelfmonitorService.class);
    private ModelControllerClient client;
    private static final long INTERVAL = 5000;
    private IMetricsStorage metricsStorage;
    private Set<ModelMetric> metrics = Collections.synchronizedSet(new HashSet<ModelMetric>());
    private ServiceController jpaService;
    private Set<String> attributes;
    private boolean initialized = false;

    public ServiceController getJpaService() {
        return jpaService;
    }

    public void setJpaService(ServiceController jpaService) {
        this.jpaService = jpaService;
    }
    
    public SelfmonitorService() {
        client = null;
        try {  
            client = ModelControllerClient.Factory.create(
                    InetAddress.getByName("localhost"), 9990);
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(
                    SelfmonitorService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Thread OUTPUT = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    // time needed for client to startup
                    Thread.sleep(1000);
                    if(!initialized){
                        metricsStorage = new MetricsDbStorage();
                        ModelScanner scanner = new ModelScanner(client);
                        ModelWriter writer = new ModelWriter(client);
                        modelScanAttributes2(scanner, writer);
                        initialized = true;
                    }
                    Thread.sleep(INTERVAL);
                    storeMetrics();
                    logStoredMetrics();
                } catch (InterruptedException e) {
                    interrupted();
                    break;
                }
            }
        }
    };
    
    private void modelScanAttributes(ModelScanner scanner, ModelWriter writer){
        log.info("Scanning the whole model for metrics, please wait...");
        long timeStarted = System.currentTimeMillis();
        attributes = scanner.getModelRuntimeAttributes();
        removeAllMetricsFromModel(writer);
        for(String attribute : attributes){
            ModelMetric m = getMetricFromAttribute(attribute);
            if(!metrics.contains(m)){
                this.metrics.add(m);
                writer.addMetricToModel(m);
            }
            else{
                log.info("debug metric already exists: " + m.getPath() + "/" + m.getName());
            }
        }
        long timeEnded = System.currentTimeMillis();
        long resultTime = timeEnded - timeStarted;
        log.info("Metrics added");
        log.info("Time spent: " + resultTime/1000 + "s");
        log.info("Number of scanned attributes: " + attributes.size());
    }
    
    private void modelScanAttributes2(ModelScanner scanner, ModelWriter writer){
        log.info("Scanning the whole model for metrics, please wait...");
        long timeStarted = System.currentTimeMillis();
        attributes = scanner.getModelRuntimeAttributes();
        for(String attribute : attributes){
            ModelMetric m = getMetricFromAttribute(attribute);
            if(!metrics.contains(m)){
                this.metrics.add(m);
                writer.addMetricToModelIfNotPresent(m);
            }
            else{
                log.info("debug metric already exists: " + m.getPath() + "/" + m.getName());
            }
        }
        long timeEnded = System.currentTimeMillis();
        long resultTime = timeEnded - timeStarted;
        log.info("Metrics added");
        log.info("Time spent: " + resultTime/1000 + "s");
        log.info("Number of scanned attributes: " + attributes.size());
    }
    
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
        ModelMetric m = new ModelMetric(metricName, metricPath, false);
        return m;
    }
    
    private void removeAllMetricsFromModel(ModelWriter writer){
        for(String attribute : attributes){
            ModelMetric m = getMetricFromAttribute(attribute);
            writer.removeMetricFromModel(m);
        }
    }
    
    private void storeMetrics(){
        for(ModelMetric metric : metrics){
            if(metric.isEnabled()){
                ModelNode op = new ModelNode();
                op.get(ClientConstants.OP).set(ClientConstants.READ_RESOURCE_OPERATION);
                op.get(ClientConstants.INCLUDE_RUNTIME).set(true);  
                MetricPathResolver.resolvePath(metric.getPath(), op);
                ModelNode returnVal = null;
                if(client != null){
                    try {
                        returnVal = client.execute(op);
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(SubsystemAdd.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                    if (returnVal != null){
                        Object metricValue = returnVal.get("result").get(
                                metric.getName());
                        // debug - remove toString(), inspect the metrics' values
                        storeMetric(metric.getName(), 
                                metric.getPath(), metricValue.toString());
                    }
                }
            }
        }
    }
    
    private void logStoredMetrics(){
        SimpleDateFormat printFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        for(ModelMetric metric : metrics){
            if(metric.isEnabled()){
                log.info("==================================");
                log.info(metric.getName());
                log.info("==================================");
                log.info("Date and time            | value");
                log.info("------------------------------------");
                Map<Date, Object> metricData = metricsStorage.getMetricRecords(
                        metric.getName(), metric.getPath());
                for (Map.Entry<Date, Object> entry : metricData.entrySet()){
                    log.info(entry.getKey() + " " + 
                            printFormat.format(entry.getKey()) + 
                            "  |  " + entry.getValue().toString());
                }
            }
        }
    }
    
    private void storeMetric(String metricName, String metricPath, Object value){
        metricsStorage.addMetric(metricName, metricPath, 
                new Date(System.currentTimeMillis()), value);
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
    }
    
    public void removeMetric(ModelMetric metric){
        metrics.remove(metric);
    }
    
    public ModelMetric getMetric(String metricName, String metricPath){
        for(ModelMetric metricIter : metrics){
            if (metricIter.getName().equals(metricName) &&
                metricIter.getPath().equals(metricPath)){
                return metricIter;
            }
        }
        return null;
    }
    
}

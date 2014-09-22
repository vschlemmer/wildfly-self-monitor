package org.jboss.as.selfmonitor.extension;

import org.jboss.as.selfmonitor.extension.model.Metric;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.selfmonitor.extension.model.MetricPathResolver;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
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
    //TODO: remove INTERVAL and replace with lower level settings
    private static final long INTERVAL = 10000;
    private Set<Metric> metrics = Collections.synchronizedSet(new HashSet<Metric>());
    
    
    public SelfmonitorService(){
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
                    Thread.sleep(INTERVAL);
                    writeMetrics();
                } catch (InterruptedException e) {
                    interrupted();
                    break;
                }
            }
        }
    };
    
    public void writeMetrics(){
        log.info("--------------------------------------------------");
        log.info("METRICS");
        log.info("--------------------------------------------------");
        for(Metric metric : metrics){
            ModelNode op = new ModelNode();
            op.get(ClientConstants.OP).set("read-resource");
            op.get("include-runtime").set(true);  
            MetricPathResolver.resolvePath(
                    metric.getPath(), op);
            ModelNode returnVal = null;
            if(client != null){
                try {
                    returnVal = client.execute(op);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(
                            SubsystemAdd.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (returnVal != null){
                    log.info(metric.getName() + ": " + 
                            returnVal.get("result")
                                    .get(metric.getName())
                                    .asString());
                }
            }
        }
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

    public Set<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(Set<Metric> metrics) {
        this.metrics = metrics;
    }
    
    public void addMetric(Metric metric){
        metrics.add(metric);
    }
    
    public void removeMetric(Metric metric){
        metrics.remove(metric);
    }
    
    public Metric getMetric(Metric metric){
        for(Metric metricIter : metrics){
            if (metricIter.getName().equals(metric.getName()) &&
                metricIter.getPath().equals(metric.getPath())){
                return metricIter;
            }
        }
        return null;
    }
    
}

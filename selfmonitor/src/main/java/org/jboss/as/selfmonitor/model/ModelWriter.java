package org.jboss.as.selfmonitor.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import org.jboss.as.selfmonitor.SelfmonitorExtension;

/**
 * Class providing API for reading and writing metrics from and to 
 * subsystem's configuration
 * 
 * @author Vojtech Schlemmer
 */
public class ModelWriter {
    
    private ModelControllerClient client;
    private Set<ModelMetric> currentMetrics;
    public static final String READ_CHILDREN_NAMES = "read-children-names";
    public static final String METRIC = "metric";
    public String subsystemPath;
    
    public ModelWriter(ModelControllerClient client){
        subsystemPath = SUBSYSTEM + "=" + SelfmonitorExtension.SUBSYSTEM_NAME;
        this.client = client;
        this.currentMetrics = getCurrentMetrics();
    }
    
    /**
     * Retrieves metrics currently present in the selfmonitor configuration
     * 
     * @return set of metrics
     */
    private Set<ModelMetric> getCurrentMetrics(){
        Set<ModelMetric> currMetr = new HashSet<>();
        ModelNode op = new ModelNode();
        MetricPathResolver.resolvePath(subsystemPath, op);
        op.get(ClientConstants.OP).set(READ_CHILDREN_NAMES);
        op.get(ClientConstants.CHILD_TYPE).set(METRIC);
        ModelNode childrenNames = null;
        try {
            childrenNames = client.execute(op).get(ClientConstants.RESULT);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(
                ModelWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(validateNode(childrenNames)){
            ModelNode returnValue;
            for(ModelNode childName : childrenNames.asList()){
                String metricName = childName.asString();
                ModelNode opResult = new ModelNode();
                MetricPathResolver.resolvePath(
                        subsystemPath + "/metric=" + metricName, opResult);
                opResult.get(ClientConstants.OP).set(
                        ClientConstants.READ_RESOURCE_OPERATION);
                returnValue = null;
                try {
                    returnValue = client.execute(opResult).get(
                            ClientConstants.RESULT);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(
                        ModelWriter.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(validateNode(returnValue)){
                    String metricPath = returnValue.get("path").asString();
                    boolean metricEnabled = returnValue.get("enabled").asBoolean();
                    currMetr.add(new ModelMetric(metricName, metricPath, 
                            metricEnabled));
                }
            }
        }
        return currMetr;
    }
    
    /**
     * Adds metric to subsystem's configuration
    private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(MetricPathResolver.class);
    
     * 
     * @param metric metric to be added
     */
    public void addMetricToModel(ModelMetric metric){
        addMetric(metric);
    }
    
    /**
     * Adds metric to subsystem's configuration in case it's not present
     * 
     * @param metric 
     */
    public void addMetricToModelIfNotPresent(ModelMetric metric){
        if(!currentMetrics.contains(metric)){
            addMetric(metric);
        }
    }
    
    /**
     * Performs the operation of adding a metric to subsystem's configuration
     * TODO: think about some optimalization, it takes about 25s to
     * add 291 metrics
     * 
     * @param metric metric to be added
     */
    private void addMetric(ModelMetric metric){
//        log.info("subsystem path: " + subsystemPath);
        ModelNode op = new ModelNode();
        String addMetricPath = subsystemPath + "/metric=" + metric.getName();
        MetricPathResolver.resolvePath(addMetricPath, op);
        op.get(ClientConstants.OP).set(ClientConstants.ADD);
        op.get(SelfmonitorExtension.PATH).set(metric.getPath());
        op.get(SelfmonitorExtension.ENABLED).set(
                metric.isEnabled() ? "true" : "false");
        ModelNode returnVal = null;
        try {
            returnVal = client.execute(op);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(
                    ModelWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        currentMetrics.add(metric);
    }
    
    /**
     * Removes metric from subsystem's configuration
     * 
     * @param metric metric to be removed
     */
    public void removeMetricFromModel(ModelMetric metric){
        ModelNode op = new ModelNode();
        MetricPathResolver.resolvePath(subsystemPath, op);
        op.get(ClientConstants.OP).set(ClientConstants.REMOVE_OPERATION);
        ModelNode returnVal = null;
        if(client != null){
            try {
                returnVal = client.execute(op);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ModelScanner.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Validates model's node - node is valid if is neither null nor empty nor "undefined"
     * 
     * @param node node to be validated
     * @return true if node is valid, false otherwise
     */
    private boolean validateNode(ModelNode node){
        if(node != null){
            if(!node.asString().equals("undefined") &&
               !node.asString().equals("")){
                return true;
            }
        }
        return false;
    }
}

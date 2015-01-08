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
    
    private final ModelControllerClient client;
    private final Set<String> currentMetricIds;
    public static final String READ_CHILDREN_NAMES = "read-children-names";
    public static final String METRIC = "metric";
    public String subsystemPath;
    
    public ModelWriter(ModelControllerClient client){
        subsystemPath = SUBSYSTEM + "=" + SelfmonitorExtension.SUBSYSTEM_NAME;
        this.client = client;
        this.currentMetricIds = getCurrentMetricIds();
    }
    
    /**
     * Retrieves metrics currently present in the selfmonitor configuration
     * 
     * @return set of metrics
     */
    public final Set<String> getCurrentMetricIds(){
        Set<String> currMetrIds = new HashSet<>();
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
                String metricId = childName.asString();
                ModelNode opResult = new ModelNode();
                MetricPathResolver.resolvePath(
                        subsystemPath + "/metric=" + metricId, opResult);
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
                    currMetrIds.add(metricId);
                }
            }
        }
        return currMetrIds;
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
        if(!currentMetricIds.contains(metric.getId())){
            addMetric(metric);
        }
    }
    
    /**
     * Performs the operation of adding a metric to subsystem's configuration
     * 
     * @param metric metric to be added
     */
    private void addMetric(ModelMetric metric){
        ModelNode op = new ModelNode();
        String addMetricPath = subsystemPath + "/metric=" + metric.getId();
        MetricPathResolver.resolvePath(addMetricPath, op);
        op.get(ClientConstants.OP).set(ClientConstants.ADD);
        if(metric.getPath() != null && !metric.getPath().equals("")){
            op.get(SelfmonitorExtension.PATH).set(metric.getPath());
        }
        op.get(SelfmonitorExtension.ENABLED).set(String.valueOf(
                metric.isEnabled()));
        op.get(SelfmonitorExtension.INTERVAL).set(String.valueOf(
                metric.getInterval()));
        op.get(SelfmonitorExtension.TYPE).set(String.valueOf(
                metric.getType()));
        op.get(SelfmonitorExtension.DESCRIPTION).set(String.valueOf(
                metric.getDescription()));
        op.get(SelfmonitorExtension.NILLABLE).set(String.valueOf(
                metric.isNillable()));
        op.get(SelfmonitorExtension.DATA_TYPE).set(String.valueOf(
                metric.getDataType()));
        ModelNode returnVal = null;
        try {
            returnVal = client.execute(op);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(
                    ModelWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        currentMetricIds.add(metric.getId());
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
        currentMetricIds.remove(metric.getId());
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

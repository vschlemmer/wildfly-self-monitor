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
import org.jboss.logging.Logger;

/**
 *
 * @author Vojtech Schlemmer
 */
public class ModelWriter {
    
    private ModelControllerClient client;
    private Set<ModelMetric> currentMetrics;
    private final Logger log = Logger.getLogger(ModelWriter.class);
    public static final String READ_CHILDREN_NAMES = "read-children-names";
    public static final String METRIC = "metric";
    
    public ModelWriter(ModelControllerClient client){
        this.client = client;
        this.currentMetrics = getCurrentMetrics();
    }
    
    private Set<ModelMetric> getCurrentMetrics(){
        ModelNode op = new ModelNode();
        String subsystemPath = SUBSYSTEM + "=" + 
                SelfmonitorExtension.SUBSYSTEM_NAME;
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
        Set<ModelMetric> currMetr = new HashSet<>();
        if(validateNode(childrenNames)){
            ModelNode returnValue;
            ModelNode opResult;
            for(ModelNode childName : childrenNames.asList()){
                String metricName = childName.asString();
                opResult = new ModelNode();
                MetricPathResolver.resolvePath(subsystemPath + "/metric=" + metricName, opResult);
                opResult.get(ClientConstants.OP).set(ClientConstants.READ_RESOURCE_OPERATION);
                returnValue = null;
                try {
                    returnValue = client.execute(opResult).get(ClientConstants.RESULT);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(
                        ModelWriter.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(validateNode(childrenNames)){
                    String metricPath = returnValue.get("path").asString();
                    boolean metricEnabled = returnValue.get("enabled").asBoolean();
                    currMetr.add(new ModelMetric(metricName, metricPath, metricEnabled));
//                    log.info("--------------------------");
//                    log.info("name: " + metricName);
//                    log.info("path: " + metricPath);
//                    log.info("enabled: " + metricEnabled);
                }
            }
        }
        return currMetr;
    }
    
    public void addMetricToModel(ModelMetric metric){
        ModelNode op = getModelPath(metric);
        op.get(ClientConstants.OP).set(ClientConstants.ADD);
        op.get(SelfmonitorExtension.PATH).set(metric.getPath());
        op.get(SelfmonitorExtension.ENABLED).set(
                metric.isEnabled() ? "true" : "false");
        ModelNode returnVal = null;
        try {
            returnVal = client.execute(op);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ModelScanner.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addMetricToModelIfNotPresent(ModelMetric metric){
        if(!currentMetrics.contains(metric)){
            log.info("metric NOT monitored: " + metric.getName());
            ModelNode op = getModelPath(metric);
            op.get(ClientConstants.OP).set(ClientConstants.ADD);
            op.get(SelfmonitorExtension.PATH).set(metric.getPath());
            op.get(SelfmonitorExtension.ENABLED).set(
                    metric.isEnabled() ? "true" : "false");
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
        else{
//            log.info("metric monitored: " + metric.getName());
        }
    }
    
    public void removeMetricFromModel(ModelMetric metric){
        ModelNode op = getModelPath(metric);
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
    
    private ModelNode getModelPath(ModelMetric metric){
        ModelNode op = new ModelNode();
        String subsystemPath = SUBSYSTEM + "=" + 
                SelfmonitorExtension.SUBSYSTEM_NAME + "/" + "metric=" +
                metric.getName();
        MetricPathResolver.resolvePath(subsystemPath, op);
        return op;
    }
    
    private boolean validateNode(ModelNode childrenTypes){
        if(childrenTypes != null){
            if(!childrenTypes.asString().equals("undefined") &&
               !childrenTypes.asString().equals("")){
                return true;
            }
        }
        return false;
    }
}

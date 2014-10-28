package org.jboss.as.selfmonitor.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

/**
 * Class providing API for scanning model and retrieving its runtime 
 * attributes (metrics)
 * 
 * @author Vojtech Schlemmer
 */
public class ModelScanner {
    
    private Set<String> runtimeAttributes;
    private ModelControllerClient client;
    public static final String READ_CHILDREN_TYPES = "read-children-types";
    public static final String READ_CHILDREN_NAMES = "read-children-names";
    public static final String ATTRIBUTES_ONLY = "attributes-only";
    
    public ModelScanner(ModelControllerClient client){
        this.client = client;
        runtimeAttributes = new HashSet<>();
    }
    
    /**
     * Retrieves runtime attributes (metrics) from the whole model - first
     * it adds root metrics to "runtimeAttributes" property and then iterates 
     * child types
     * 
     * @return Set of runtime attributes in form of a 
     * string (path + / + attribute name)
     */
    public Set<String> getModelRuntimeAttributes(){
        // add root runtime attributes
        Set<String> localRuntimeAttributes = null;
        try {
            localRuntimeAttributes = getRuntimeAttributes("");
        } catch (IOException ex) {
            Logger.getLogger(ModelScanner.class.getName()).log(Level.SEVERE, null, ex);
        }
        includeLocalRuntimeAttributes(localRuntimeAttributes, "");
        // get root's children types
        ModelNode op = new ModelNode();
        op.get(ClientConstants.OP).set(READ_CHILDREN_TYPES);
        ModelNode childrenTypes = null;
        try {
            childrenTypes = client.execute(op).get(ClientConstants.RESULT);
        } catch (IOException ex) {
            Logger.getLogger(ModelScanner.class.getName()).log(Level.SEVERE, null, ex);
        }
        //iterate root's children types
        if(validateNode(childrenTypes)){
            for(ModelNode childType : childrenTypes.asList()){
                getChildrenAttributes(childType.asString(), "");
            }
        }
        return runtimeAttributes;
    }
    
    /**
     * Iterates all children of a given child type, adds metrics to 
     * "runtimeAttributes" property of each of the child, iterates children
     * types of each child and runs this method recursively
     * 
     * @param childType child type the children of which are iterated
     * @param path path of the child type in context of the model
     */
    private void getChildrenAttributes(String childType, String path){
        // get the children of a given childType
        ModelNode op = new ModelNode();
        MetricPathResolver.resolvePath(path, op);
        op.get(ClientConstants.OP).set(READ_CHILDREN_NAMES);
        op.get(ClientConstants.CHILD_TYPE).set(childType);
        ModelNode childrenNames = null;
        try {
            childrenNames = client.execute(op).get(ClientConstants.RESULT);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(
                ModelScanner.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(validateNode(childrenNames)){
            for(ModelNode childName : childrenNames.asList()){
                // add runtime attributes of each child
                String attrPath = path+"/"+childType+"="+childName.asString();
                Set<String> localRuntimeAttributes = null;
                try {
                    localRuntimeAttributes = getRuntimeAttributes(attrPath);
                } catch (IOException ex) {
                    // TODO: better handle of the case when childName includes "/" or "="
                    continue;
                }
                includeLocalRuntimeAttributes(localRuntimeAttributes, 
                        attrPath + "/");
                // for each child get his children types and run this method recursively
                ModelNode opChildrenTypes = new ModelNode();
                MetricPathResolver.resolvePath(attrPath, opChildrenTypes);
                opChildrenTypes.get(ClientConstants.OP).set(READ_CHILDREN_TYPES);
                ModelNode childrenTypes = null;
                try {
                    childrenTypes = client.execute(opChildrenTypes).get(
                            ClientConstants.RESULT);
                } catch (IOException ex) {
                    Logger.getLogger(ModelScanner.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
                if(validateNode(childrenTypes)){
                    for(ModelNode recursiveChildType : childrenTypes.asList()){
                        getChildrenAttributes(recursiveChildType.asString(), 
                                path+"/"+childType+"="+childName.asString());   
                    }
                }
            }
        }
    }
    
    /**
     * Retrieves metrics of a model's node
     * 
     * @param path path of the node
     * @return set of metrics in form path + "/" + metric name
     * @throws IOException 
     */
    private Set<String> getRuntimeAttributes(String path) throws IOException{
        Set<String> withoutRuntimeAttributes = getAttributes(path, false);
        Set<String> withRuntimeAttributes = getAttributes(path, true);
        withRuntimeAttributes.removeAll(withoutRuntimeAttributes);
        return withRuntimeAttributes;
    }
    
    /**
     * Retrieves attributes of a model's node - either runtime or not
     * 
     * @param path path of the node
     * @param runtime whether runtime or non-runtime attributes should
     * be retrieved
     * @return set of attributes in form path + "/" + metric name
     * @throws IOException 
     */
    private Set<String> getAttributes(String path, boolean runtime) throws IOException{
        ModelNode op = new ModelNode();
        MetricPathResolver.resolvePath(path, op);
        ModelNode attributes = null;
        op.get(ClientConstants.OP).set(ClientConstants.READ_RESOURCE_OPERATION);
        op.get(ATTRIBUTES_ONLY).set(true);
        if(runtime){
            op.get(ClientConstants.INCLUDE_RUNTIME).set(true);
        }
        attributes = client.execute(op).get(ClientConstants.RESULT);
        Set<String> attributesSet = new HashSet<>();
        if(validateNode(attributes)){
            for (Property attribute : attributes.asPropertyList()){
                attributesSet.add(attribute.getName());
            }
        }
        return attributesSet;
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
    
    /**
     * Adds set of metrics to the "runtimeAttributes" property
     * 
     * @param attributes attributes to be added
     * @param path path of the attributes
     */
    private void includeLocalRuntimeAttributes(Set<String> attributes, String path){
        for (String attribute : attributes){
            String newAttribute = path + attribute;
            if(!runtimeAttributes.contains(newAttribute)){
                runtimeAttributes.add(newAttribute);
            }
        }
    }
}

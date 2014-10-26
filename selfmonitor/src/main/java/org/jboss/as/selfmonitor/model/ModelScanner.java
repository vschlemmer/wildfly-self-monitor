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
 *
 * @author Vojtech Schlemmer
 */
public class ModelScanner {
    
    private Set<String> runtimeAttributes;
    private ModelControllerClient client;
    public static final String READ_CHILDREN_TYPES = "read-children-types";
    public static final String READ_CHILDREN_NAMES = "read-children-names";
    public static final String ATTRIBUTES_ONLY = "attributes-only";
    private final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(ModelScanner.class);
    
    public ModelScanner(ModelControllerClient client){
        this.client = client;
        runtimeAttributes = new HashSet<>();
    }
    
    public Set<String> getModelRuntimeAttributes(){
        // add root runtime attributes
        ModelNode op = new ModelNode();
        Set<String> localRuntimeAttributes = null;
        try {
            localRuntimeAttributes = getRuntimeAttributes("");
        } catch (IOException ex) {
            Logger.getLogger(ModelScanner.class.getName()).log(Level.SEVERE, null, ex);
        }
        includeLocalRuntimeAttributes(localRuntimeAttributes, "");
        // get root's children types
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
            //iterate the children of a given childType
            for(ModelNode childName : childrenNames.asList()){
                // add runtime attributes of the child
                String attrPath = path + "/" + childType + "=" + childName.asString();
                Set<String> localRuntimeAttributes = null;
                try {
                    localRuntimeAttributes = getRuntimeAttributes(attrPath);
                } catch (IOException ex) {
                    // continue in case the childName includes "/" or "="
                    continue;
                }
                includeLocalRuntimeAttributes(localRuntimeAttributes, attrPath + "/");
                // for each child get his children types and run this method recursively
                ModelNode opChildrenTypes = new ModelNode();
                MetricPathResolver.resolvePath(attrPath, opChildrenTypes);
                opChildrenTypes.get(ClientConstants.OP).set(READ_CHILDREN_TYPES);
                ModelNode childrenTypes = null;
                try {
                    childrenTypes = client.execute(opChildrenTypes).get(ClientConstants.RESULT);
                } catch (IOException ex) {
                    Logger.getLogger(ModelScanner.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(validateNode(childrenTypes)){
                    for(ModelNode newChildType : childrenTypes.asList()){
                        getChildrenAttributes(newChildType.asString(), 
                                path + "/" + childType + "=" + childName.asString());   
                    }
                }
            }
        }
    }
    
    private Set<String> getRuntimeAttributes(String path) throws IOException{
        Set<String> withoutRuntimeAttributes = getAttributes(path, false);
        Set<String> withRuntimeAttributes = getAttributes(path, true);
        withRuntimeAttributes.removeAll(withoutRuntimeAttributes);
        return withRuntimeAttributes;
    }
    
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
    
    private boolean validateNode(ModelNode childrenTypes){
        if(childrenTypes != null){
            if(!childrenTypes.asString().equals("undefined") &&
               !childrenTypes.asString().equals("")){
                return true;
            }
        }
        return false;
    }
    
    private void includeLocalRuntimeAttributes(Set<String> attributes, String path){
        for (String attribute : attributes){
            String newAttribute = path + attribute;
            if(!runtimeAttributes.contains(newAttribute)){
                runtimeAttributes.add(newAttribute);
            }
        }
    }
}

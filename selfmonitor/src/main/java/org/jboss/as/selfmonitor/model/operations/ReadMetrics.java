package org.jboss.as.selfmonitor.model.operations;

import java.util.HashSet;
import java.util.Set;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.ModelControllerClient;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import org.jboss.as.selfmonitor.model.ModelMetric;
import org.jboss.as.selfmonitor.model.ModelWriter;
import org.jboss.as.selfmonitor.service.SelfmonitorService;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

/**
 *
 * @author Vojtech Schlemmer
 */
public class ReadMetrics implements OperationStepHandler {

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.require(OP_ADDR));
        boolean showEnabled = operation.get("show-enabled").asBoolean();
        ModelWriter writer = new ModelWriter(getClient(context));
        Set<ModelNode> metrics = new HashSet<>();
        for(ModelMetric metric : writer.getCurrentMetrics()){
            if(showEnabled == metric.isEnabled()){
                ModelNode m = new ModelNode();
                m.get("name").set(metric.getName());
                m.get("path").set(metric.getPath());
                m.get("interval").set(metric.getInterval());
                metrics.add(m);
            }
        }
        context.getResult().set(metrics);
        context.stepCompleted();
    }
    
    private ModelControllerClient getClient(OperationContext context){
        return getService(context).getClient();
    }
    
    private SelfmonitorService getService(OperationContext context){
        ServiceController<?> serviceController =  context
                .getServiceRegistry(true)
                .getService(ServiceName.JBOSS.append(SelfmonitorService.NAME));
        return (SelfmonitorService) serviceController.getService();
    }
    
}
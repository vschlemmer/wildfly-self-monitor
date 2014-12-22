package org.jboss.as.selfmonitor.model.operations;

import java.util.HashSet;
import java.util.Set;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.selfmonitor.model.ModelMetric;
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
        boolean showEnabled = operation.get("show-enabled").asBoolean();
        Set<ModelNode> metrics = new HashSet<>();
        for(ModelMetric metric : getService(context).getMetrics()){       
            if(showEnabled == metric.isEnabled()){
                ModelNode m = new ModelNode();
                m.get("id").set(metric.getId());
                m.get("path").set(metric.getPath());
                m.get("interval").set(metric.getInterval());
                m.get("type").set(metric.getType());
                m.get("description").set(metric.getDescription());
                m.get("nillable").set(metric.isNillable());
                metrics.add(m);
            }
        }
        context.getResult().set(metrics);
        context.stepCompleted();
    }
    
    private SelfmonitorService getService(OperationContext context){
        ServiceController<?> serviceController =  context
                .getServiceRegistry(true)
                .getService(ServiceName.JBOSS.append(SelfmonitorService.NAME));
        return (SelfmonitorService) serviceController.getService();
    }
    
}
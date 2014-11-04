package org.jboss.as.selfmonitor.model;

import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.selfmonitor.MetricDefinition;
import org.jboss.as.selfmonitor.SelfmonitorExtension;
import org.jboss.as.selfmonitor.service.SelfmonitorService;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

/**
 * Handler for runtime update of the "enabled" property of a metric
 * 
 * @author Vojtech Schlemmer
 */
public class SelfmonitorIntervalHandler extends AbstractWriteAttributeHandler<Void> {

    public static final SelfmonitorIntervalHandler INSTANCE = new SelfmonitorIntervalHandler();

    private SelfmonitorIntervalHandler() {
        super(MetricDefinition.INTERVAL);
    }
    
    @Override
    protected boolean applyUpdateToRuntime(
            OperationContext context, ModelNode operation, String attributeName,
            ModelNode resolvedValue, ModelNode currentValue, 
            HandbackHolder<Void> handbackHolder) 
            throws OperationFailedException {
        if (attributeName.equals(SelfmonitorExtension.INTERVAL)) {
            ModelNode submodel = context.readResource(
                    PathAddress.EMPTY_ADDRESS).getModel();
            final String metricName = PathAddress.pathAddress(operation.get(
                    ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
            final String metricPath = submodel.get(
                    SelfmonitorExtension.PATH).asString();
            SelfmonitorService service = (SelfmonitorService) context
                    .getServiceRegistry(true)
                    .getRequiredService(ServiceName.JBOSS.append(
                            SelfmonitorService.NAME)).getValue();
            service.changeMetricInterval(metricName, metricPath, resolvedValue.asInt());
            context.completeStep(
                    OperationContext.ResultHandler.NOOP_RESULT_HANDLER);
        }
        return false;
    }

    @Override
    protected void revertUpdateToRuntime(OperationContext oc, ModelNode mn, 
            String string, ModelNode mn1, ModelNode mn2, Void t) 
            throws OperationFailedException {
        
    }
    
}

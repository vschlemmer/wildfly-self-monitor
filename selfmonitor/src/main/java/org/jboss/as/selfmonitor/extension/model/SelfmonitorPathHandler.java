package org.jboss.as.selfmonitor.extension.model;

import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.selfmonitor.extension.MetricDefinition;
import org.jboss.as.selfmonitor.extension.SelfmonitorExtension;
import org.jboss.as.selfmonitor.extension.SelfmonitorService;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

/**
 *
 * @author Vojtech Schlemmer
 */
public class SelfmonitorPathHandler extends AbstractWriteAttributeHandler<Void> {

    public static final SelfmonitorPathHandler INSTANCE = new SelfmonitorPathHandler();

    private SelfmonitorPathHandler() {
        super(MetricDefinition.PATH);
    }
    
    @Override
    protected boolean applyUpdateToRuntime(
            OperationContext context, ModelNode operation, String attributeName,
            ModelNode resolvedValue, ModelNode currentValue, 
            HandbackHolder<Void> handbackHolder) 
            throws OperationFailedException {
         if (attributeName.equals(SelfmonitorExtension.PATH)) {
            final String metricName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
            SelfmonitorService service = (SelfmonitorService) context
                    .getServiceRegistry(true)
                    .getRequiredService(ServiceName.JBOSS.append(SelfmonitorService.NAME)).getValue();
            Metric metricToUpdate = new Metric(metricName, currentValue.asString());
            service.getMetric(metricToUpdate).setPath(resolvedValue.asString());
            context.completeStep();
        }

        return false;
    }

    @Override
    protected void revertUpdateToRuntime(OperationContext oc, ModelNode mn, String string, ModelNode mn1, ModelNode mn2, Void t) throws OperationFailedException {
        
    }
    
}

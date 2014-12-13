package org.jboss.as.selfmonitor;

import org.jboss.as.selfmonitor.service.SelfmonitorService;
import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.selfmonitor.model.ModelMetric;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;

/**
 *
 * @author Vojtech Schlemmer
 */
public class MetricRemove extends AbstractRemoveStepHandler {
    
    public static final MetricRemove INSTANCE = new MetricRemove();
    private final Logger log = Logger.getLogger(MetricRemove.class);

    @Override
    protected void performRuntime(
            OperationContext context, ModelNode operation, ModelNode model) 
            throws OperationFailedException {
        SelfmonitorService service = getSelfmonitorService(
                context.getServiceRegistry(true), 
                SelfmonitorService.NAME);
        String metricId = PathAddress.pathAddress(operation.get(
                ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        ModelMetric metricToRemove = service.getMetric(metricId);
        if (metricToRemove != null){
            service.removeMetric(metricToRemove);
        }
        else{
            log.info("Metric '" + metricId + "' could not be found among metrics.");
        }
    }
    
    private SelfmonitorService getSelfmonitorService(ServiceRegistry registry, 
            String name){
        ServiceController<?> container = registry.getService(
                ServiceName.JBOSS.append(name));
        if (container != null) {
            SelfmonitorService service = (SelfmonitorService)container.getValue();
            return service;
        }
        return null;
    }
    
}

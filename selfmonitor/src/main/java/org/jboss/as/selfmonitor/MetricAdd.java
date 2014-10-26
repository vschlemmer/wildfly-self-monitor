package org.jboss.as.selfmonitor;

import org.jboss.as.selfmonitor.service.SelfmonitorService;
import java.util.List;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import static org.jboss.as.selfmonitor.MetricDefinition.PATH;
import static org.jboss.as.selfmonitor.MetricDefinition.ENABLED;
import org.jboss.as.selfmonitor.model.ModelMetric;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;

/**
 *
 * @author Vojtech Schlemmer
 */
public class MetricAdd extends AbstractAddStepHandler {

    public static final MetricAdd INSTANCE = new MetricAdd();
    
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        PATH.validateAndSet(operation, model);
        ENABLED.validateAndSet(operation, model);
    }
    
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        SelfmonitorService service = getSelfmonitorService(
                context.getServiceRegistry(true), 
                SelfmonitorService.NAME);
        String metricName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        String metricPath = PATH.resolveModelAttribute(context,model).asString();
        boolean enabled = ENABLED.resolveModelAttribute(context,model).asBoolean();
        ModelMetric metricInService = service.getMetric(metricName, metricPath);
        if(metricInService != null){
            service.removeMetric(metricInService);
        }
        ModelMetric metric = new ModelMetric(metricName, metricPath, enabled);
        service.addMetric(metric);
    }
    
    private SelfmonitorService getSelfmonitorService(ServiceRegistry registry, String name){
        ServiceController<?> container = registry.getService(ServiceName.JBOSS.append(name));
        if (container != null) {
            SelfmonitorService service = (SelfmonitorService)container.getValue();
            return service;
        }
        return null;
    }
    
}

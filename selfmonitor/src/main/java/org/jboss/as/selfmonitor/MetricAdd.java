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
import static org.jboss.as.selfmonitor.MetricDefinition.INTERVAL;
import static org.jboss.as.selfmonitor.MetricDefinition.TYPE;
import static org.jboss.as.selfmonitor.MetricDefinition.DESCRIPTION;
import static org.jboss.as.selfmonitor.MetricDefinition.NILLABLE;
import static org.jboss.as.selfmonitor.MetricDefinition.DATA_TYPE;
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
        INTERVAL.validateAndSet(operation, model);
        TYPE.validateAndSet(operation, model);
        DESCRIPTION.validateAndSet(operation, model);
        NILLABLE.validateAndSet(operation, model);
        DATA_TYPE.validateAndSet(operation, model);
    }
    
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        SelfmonitorService service = getSelfmonitorService(
                context.getServiceRegistry(true), 
                SelfmonitorService.NAME);
        String metricId = PathAddress.pathAddress(operation.get(
                ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        String metricPath = PATH.resolveModelAttribute(context,model).asString();
        boolean enabled = ENABLED.resolveModelAttribute(context,model).asBoolean();
        int interval = INTERVAL.resolveModelAttribute(context,model).asInt();
        String type = TYPE.resolveModelAttribute(context,model).asString();
        String description = DESCRIPTION.resolveModelAttribute(context,model).asString();
        boolean nillable = NILLABLE.resolveModelAttribute(context,model).asBoolean();
        String dataType = DATA_TYPE.resolveModelAttribute(context,model).asString();
        ModelMetric metricInService = service.getMetric(metricId);
        if(metricInService != null){
            service.removeMetric(metricInService);
        }
        ModelMetric metric = new ModelMetric(metricId, metricPath, enabled, 
                interval, type, description, nillable, dataType);
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

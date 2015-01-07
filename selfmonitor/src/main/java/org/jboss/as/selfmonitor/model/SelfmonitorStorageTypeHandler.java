package org.jboss.as.selfmonitor.model;

import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.selfmonitor.SelfmonitorExtension;
import org.jboss.as.selfmonitor.SubsystemDefinition;
import org.jboss.as.selfmonitor.service.SelfmonitorService;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

/**
 * Handler for runtime update of the "storage-type" property of the subsystem
 * 
 * @author Vojtech Schlemmer
 */
public class SelfmonitorStorageTypeHandler extends AbstractWriteAttributeHandler<Void> {

    public static final SelfmonitorStorageTypeHandler INSTANCE = new SelfmonitorStorageTypeHandler();

    private SelfmonitorStorageTypeHandler() {
        super(SubsystemDefinition.STORAGE_TYPE);
    }
    
    @Override
    protected boolean applyUpdateToRuntime(
            OperationContext context, ModelNode operation, String attributeName,
            ModelNode resolvedValue, ModelNode currentValue, 
            HandbackHolder<Void> handbackHolder) 
            throws OperationFailedException {
        if (attributeName.equals(SelfmonitorExtension.STORAGE_TYPE)) {
            SelfmonitorService service = (SelfmonitorService) context
                    .getServiceRegistry(true)
                    .getRequiredService(ServiceName.JBOSS.append(
                            SelfmonitorService.NAME)).getValue();
            service.setStorageType(resolvedValue.asString());
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

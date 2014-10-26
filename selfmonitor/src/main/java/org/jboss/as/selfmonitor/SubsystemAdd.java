package org.jboss.as.selfmonitor;

import org.jboss.as.selfmonitor.service.SelfmonitorService;
import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;

import org.jboss.as.selfmonitor.deployment.SubsystemDeploymentProcessor;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;

/**
 * Handler responsible for adding the subsystem resource to the model
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class SubsystemAdd extends AbstractBoottimeAddStepHandler {

    static final SubsystemAdd INSTANCE = new SubsystemAdd();

    private final Logger log = Logger.getLogger(SubsystemAdd.class);

    private SubsystemAdd() {
    }

    /** {@inheritDoc} */
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        log.info("Populating the model");
        model.setEmptyObject();
    }

    /** {@inheritDoc} */
    @Override
    public void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {

        //Add deployment processors here
        //Remove this if you don't need to hook into the deployers, or you can add as many as you like
        //see SubDeploymentProcessor for explanation of the phases
        context.addStep(new AbstractDeploymentChainStep() {
            public void execute(DeploymentProcessorTarget processorTarget) {
                processorTarget.addDeploymentProcessor(SubsystemDeploymentProcessor.PHASE, SubsystemDeploymentProcessor.PRIORITY, new SubsystemDeploymentProcessor());

            }
        }, OperationContext.Stage.RUNTIME);
        
        SelfmonitorService service = new SelfmonitorService();
        
//        service.setServiceNames(context.getServiceRegistry(true).getServiceNames());
        
        ServiceController<SelfmonitorService> controller = context.getServiceTarget()
                .addService(ServiceName.JBOSS.append(SelfmonitorService.NAME), service)
                .addListener(verificationHandler)
                .setInitialMode(Mode.ACTIVE)
                .install();
        newControllers.add(controller);
        
    }
    
//    @Override
//    public void performRuntime(OperationContext context, ModelNode operation, 
//            ModelNode model, ServiceVerificationHandler verificationHandler, 
//            List<ServiceController<?>> newControllers) 
//            throws OperationFailedException {
//        List<ServiceName> serviceNames = context.getServiceRegistry(true).getServiceNames();
//        if(serviceNames != null){
//            for(ServiceName serviceName : serviceNames){
//                System.out.println("serviceName = " + serviceName);
//            }
//        }
//        service.setServiceNames(context.getServiceRegistry(true).getServiceNames());
//    }
}

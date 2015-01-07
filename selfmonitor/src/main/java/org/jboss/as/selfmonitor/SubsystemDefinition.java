package org.jboss.as.selfmonitor;

import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.selfmonitor.model.operations.ReadMetrics;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:tcerar@redhat.com">Tomaz Cerar</a>
 */
public class SubsystemDefinition extends SimpleResourceDefinition {
    public static final SubsystemDefinition INSTANCE = new SubsystemDefinition();
    public static final String READ_METRICS_STRING = "read-metrics";

    private SubsystemDefinition() {
        super(SelfmonitorExtension.SUBSYSTEM_PATH,
                SelfmonitorExtension.getResourceDescriptionResolver(null),
                //We always need to add an 'add' operation
                SubsystemAdd.INSTANCE,
                //Every resource that is added, normally needs a remove operation
                SubsystemRemove.INSTANCE);
    }
    
    public static final SimpleAttributeDefinition SHOW_ENABLED =
    new SimpleAttributeDefinitionBuilder("show-enabled", ModelType.BOOLEAN)
      .setAllowExpression(true)
      .setXmlName("show-enabled")
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(false)
      .build();
    
    // operations
    static final OperationDefinition READ_METRICS = new SimpleOperationDefinitionBuilder(
            READ_METRICS_STRING, SelfmonitorExtension.getResourceDescriptionResolver(null))
            .setRuntimeOnly()
            .addParameter(SHOW_ENABLED)
            .build();
    
    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(SubsystemDefinition.READ_METRICS, new ReadMetrics());
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        
    }
}

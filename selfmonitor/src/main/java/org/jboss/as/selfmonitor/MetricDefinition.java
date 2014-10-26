package org.jboss.as.selfmonitor;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import static org.jboss.as.selfmonitor.SelfmonitorExtension.METRIC;
import static org.jboss.as.selfmonitor.SelfmonitorExtension.METRIC_PATH;
import org.jboss.as.selfmonitor.model.SelfmonitorEnabledHandler;
import org.jboss.as.selfmonitor.model.SelfmonitorPathHandler;
import org.jboss.dmr.ModelType;

/**
 *
 * @author Vojtech Schlemmer
 */
public class MetricDefinition extends SimpleResourceDefinition {
    public static final MetricDefinition INSTANCE = new MetricDefinition();
    
    private MetricDefinition(){
       super(METRIC_PATH, 
               SelfmonitorExtension.getResourceDescriptionResolver(METRIC),
               MetricAdd.INSTANCE,
               MetricRemove.INSTANCE);
    }

    public static final SimpleAttributeDefinition PATH =
    new SimpleAttributeDefinitionBuilder(SelfmonitorExtension.PATH, ModelType.STRING)
      .setAllowExpression(true)
      .setXmlName(SelfmonitorExtension.PATH)
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(false)
      .build();
    
    public static final SimpleAttributeDefinition ENABLED =
    new SimpleAttributeDefinitionBuilder(SelfmonitorExtension.ENABLED, ModelType.BOOLEAN)
      .setAllowExpression(true)
      .setXmlName(SelfmonitorExtension.ENABLED)
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(false)
      .build();
    
    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        //you can register aditional operations here
    }
    
    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration){
       resourceRegistration.registerReadWriteAttribute(PATH, null, SelfmonitorPathHandler.INSTANCE);
       resourceRegistration.registerReadWriteAttribute(ENABLED, null, SelfmonitorEnabledHandler.INSTANCE);
    }
}

package org.jboss.as.selfmonitor.extension;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import static org.jboss.as.selfmonitor.extension.SelfmonitorExtension.METRIC;
import static org.jboss.as.selfmonitor.extension.SelfmonitorExtension.METRIC_PATH;
import org.jboss.as.selfmonitor.extension.model.SelfmonitorPathHandler;
import org.jboss.dmr.ModelType;

/**
 *
 * @author Vojtech Schlemmer
 */
public class MetricDefinition extends SimpleResourceDefinition {
    public static final MetricDefinition INSTANCE = new MetricDefinition();
    
    private MetricDefinition(){
       super(METRIC_PATH, SelfmonitorExtension.getResourceDescriptionResolver(METRIC),MetricAdd.INSTANCE,MetricRemove.INSTANCE);
    }

    public static final SimpleAttributeDefinition PATH =
    new SimpleAttributeDefinitionBuilder(SelfmonitorExtension.PATH, ModelType.STRING)
      .setAllowExpression(true)
      .setXmlName(SelfmonitorExtension.PATH)
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(false)
      .build();
    
    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration){
       resourceRegistration.registerReadWriteAttribute(PATH, null, SelfmonitorPathHandler.INSTANCE);
    }
}

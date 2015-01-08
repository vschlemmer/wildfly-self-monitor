package org.jboss.as.selfmonitor;

import org.jboss.as.selfmonitor.model.operations.MetricReadValues;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import static org.jboss.as.selfmonitor.SelfmonitorExtension.METRIC;
import static org.jboss.as.selfmonitor.SelfmonitorExtension.METRIC_PATH;
import org.jboss.as.selfmonitor.model.SelfmonitorDescriptionHandler;
import org.jboss.as.selfmonitor.model.SelfmonitorEnabledHandler;
import org.jboss.as.selfmonitor.model.SelfmonitorIntervalHandler;
import org.jboss.as.selfmonitor.model.SelfmonitorNillableHandler;
import org.jboss.as.selfmonitor.model.SelfmonitorPathHandler;
import org.jboss.as.selfmonitor.model.SelfmonitorTypeHandler;
import org.jboss.as.selfmonitor.model.operations.MetricReadAllValues;
import org.jboss.as.selfmonitor.model.operations.MetricReadValue;
import org.jboss.as.selfmonitor.model.operations.MetricValueOccurred;
import org.jboss.dmr.ModelType;

/**
 *
 * @author Vojtech Schlemmer
 */
public class MetricDefinition extends SimpleResourceDefinition {
    public static final MetricDefinition INSTANCE = new MetricDefinition();
    public static final String READ_VALUES_STRING = "read-values";
    public static final String READ_VALUE_STRING = "read-value";
    public static final String VALUE_OCCURRED_STRING = "value-occurred";
    public static final String READ_ALL_VALUES_STRING = "read-all-values";
    
    
    private MetricDefinition(){
       super(METRIC_PATH, 
               SelfmonitorExtension.getResourceDescriptionResolver(METRIC),
               MetricAdd.INSTANCE,
               MetricRemove.INSTANCE);
    }

    //attributes
    public static final SimpleAttributeDefinition PATH =
    new SimpleAttributeDefinitionBuilder(SelfmonitorExtension.PATH, ModelType.STRING)
      .setAllowExpression(true)
      .setXmlName(SelfmonitorExtension.PATH)
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(true)
      .build();
    
    public static final SimpleAttributeDefinition ENABLED =
    new SimpleAttributeDefinitionBuilder(SelfmonitorExtension.ENABLED, ModelType.BOOLEAN)
      .setAllowExpression(true)
      .setXmlName(SelfmonitorExtension.ENABLED)
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(false)
      .build();
    
    public static final SimpleAttributeDefinition INTERVAL =
    new SimpleAttributeDefinitionBuilder(SelfmonitorExtension.INTERVAL, ModelType.INT)
      .setAllowExpression(true)
      .setXmlName(SelfmonitorExtension.INTERVAL)
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(false)
      .build();
    
    public static final SimpleAttributeDefinition TYPE =
    new SimpleAttributeDefinitionBuilder(SelfmonitorExtension.TYPE, ModelType.STRING)
      .setAllowExpression(true)
      .setXmlName(SelfmonitorExtension.TYPE)
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(true)
      .build();
    
    public static final SimpleAttributeDefinition DESCRIPTION =
    new SimpleAttributeDefinitionBuilder(SelfmonitorExtension.DESCRIPTION, ModelType.STRING)
      .setAllowExpression(true)
      .setXmlName(SelfmonitorExtension.DESCRIPTION)
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(true)
      .build();
    
    public static final SimpleAttributeDefinition NILLABLE =
    new SimpleAttributeDefinitionBuilder(SelfmonitorExtension.NILLABLE, ModelType.BOOLEAN)
      .setAllowExpression(true)
      .setXmlName(SelfmonitorExtension.NILLABLE)
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(true)
      .build();
    
    public static final SimpleAttributeDefinition DATA_TYPE =
    new SimpleAttributeDefinitionBuilder(SelfmonitorExtension.DATA_TYPE, ModelType.STRING)
      .setAllowExpression(true)
      .setXmlName(SelfmonitorExtension.DATA_TYPE)
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(true)
      .build();
    
    //operations' parameters
    public static final SimpleAttributeDefinition DATE_FROM =
    new SimpleAttributeDefinitionBuilder("date-from", ModelType.STRING)
      .setAllowExpression(true)
      .setXmlName("date-from")
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(false)
      .build();
    
    public static final SimpleAttributeDefinition DATE_TO =
    new SimpleAttributeDefinitionBuilder("date-to", ModelType.STRING)
      .setAllowExpression(true)
      .setXmlName("date-to")
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(false)
      .build();
    
    public static final SimpleAttributeDefinition FUNCTION_TYPE =
    new SimpleAttributeDefinitionBuilder("function-type", ModelType.STRING)
      .setAllowExpression(true)
      .setXmlName("function-type")
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(false)
      .build();
    
    public static final SimpleAttributeDefinition DATE =
    new SimpleAttributeDefinitionBuilder("date", ModelType.STRING)
      .setAllowExpression(true)
      .setXmlName("date")
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(false)
      .build();
    
    public static final SimpleAttributeDefinition VALUE =
    new SimpleAttributeDefinitionBuilder("value", ModelType.STRING)
      .setAllowExpression(true)
      .setXmlName("value")
      .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
      .setAllowNull(false)
      .build();
    
    
    // operations
    static final OperationDefinition READ_VALUES = new SimpleOperationDefinitionBuilder(
            READ_VALUES_STRING, SelfmonitorExtension.getResourceDescriptionResolver(
                    "metric"))
            .setRuntimeOnly()
            .addParameter(DATE_FROM)
            .addParameter(DATE_TO)
            .addParameter(FUNCTION_TYPE)
            .build();
    
    static final OperationDefinition READ_VALUE = new SimpleOperationDefinitionBuilder(
            READ_VALUE_STRING, SelfmonitorExtension.getResourceDescriptionResolver(
                    "metric"))
            .setRuntimeOnly()
            .addParameter(DATE)
            .build();

    static final OperationDefinition VALUE_OCCURRED = new SimpleOperationDefinitionBuilder(
            VALUE_OCCURRED_STRING, SelfmonitorExtension.getResourceDescriptionResolver(
                    "metric"))
            .setRuntimeOnly()
            .addParameter(DATE_FROM)
            .addParameter(DATE_TO)
            .addParameter(VALUE)
            .build();
    
    static final OperationDefinition READ_ALL_VALUES = new SimpleOperationDefinitionBuilder(
            READ_ALL_VALUES_STRING, SelfmonitorExtension.getResourceDescriptionResolver(
                    "metric"))
            .setRuntimeOnly()
            .build();
    
    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(MetricDefinition.READ_VALUES, new MetricReadValues());
        resourceRegistration.registerOperationHandler(MetricDefinition.READ_VALUE, new MetricReadValue());
        resourceRegistration.registerOperationHandler(MetricDefinition.VALUE_OCCURRED, new MetricValueOccurred());
        resourceRegistration.registerOperationHandler(MetricDefinition.READ_ALL_VALUES, new MetricReadAllValues());
    }
    
    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration){
       resourceRegistration.registerReadWriteAttribute(PATH, null, SelfmonitorPathHandler.INSTANCE);
       resourceRegistration.registerReadWriteAttribute(ENABLED, null, SelfmonitorEnabledHandler.INSTANCE);
       resourceRegistration.registerReadWriteAttribute(INTERVAL, null, SelfmonitorIntervalHandler.INSTANCE);
       resourceRegistration.registerReadWriteAttribute(TYPE, null, SelfmonitorTypeHandler.INSTANCE);
       resourceRegistration.registerReadWriteAttribute(DESCRIPTION, null, SelfmonitorDescriptionHandler.INSTANCE);
       resourceRegistration.registerReadWriteAttribute(NILLABLE, null, SelfmonitorNillableHandler.INSTANCE);
    }
}

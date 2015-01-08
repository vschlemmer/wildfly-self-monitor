package org.jboss.as.selfmonitor;

import java.util.Collections;
import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.List;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import org.jboss.dmr.Property;


/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class SelfmonitorExtension implements Extension {
    
    /**
     * The name space used for the {@code substystem} element
     */
    public static final String NAMESPACE = "urn:org.jboss.as.selfmonitor:1.0";

    /**
     * The name of our subsystem within the model.
     */
    public static final String SUBSYSTEM_NAME = "selfmonitor";

    /**
     * The parser used for parsing our subsystem
     */
    private final SubsystemParser parser = new SubsystemParser();

    protected static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(
            SUBSYSTEM, SUBSYSTEM_NAME);
    private static final String RESOURCE_NAME = SelfmonitorExtension.class
            .getPackage().getName() + ".LocalDescriptions";

    public static final String PATH = "path";
    public static final String ENABLED = "enabled";
    public static final String INTERVAL = "interval";
    public static final String TYPE = "metric-type";
    public static final String DATA_TYPE = "data-type";
    public static final String DESCRIPTION = "metric-description";
    public static final String NILLABLE = "nillable";
    protected static final String METRIC = "metric";
    protected static final String METRICS = "metrics";
    protected static final String METRIC_NAME = "name";
    protected static final PathElement METRIC_PATH = PathElement.pathElement(METRIC);
    
    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        String prefix = SUBSYSTEM_NAME + (keyPrefix == null ? "" : "." + keyPrefix);
        return new StandardResourceDescriptionResolver(prefix, RESOURCE_NAME, 
                SelfmonitorExtension.class.getClassLoader(), true, false);
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
    }


    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(
                SubsystemDefinition.INSTANCE);
        ManagementResourceRegistration metric = registration.registerSubModel(
                MetricDefinition.INSTANCE);
        registration.registerOperationHandler(DESCRIBE, 
                GenericSubsystemDescribeHandler.INSTANCE, 
                GenericSubsystemDescribeHandler.INSTANCE, 
                false, 
                OperationEntry.EntryType.PRIVATE);
        subsystem.registerXMLElementWriter(parser);
    }

    private static ModelNode createAddSubsystemOperation() {
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_NAME);
        return subsystem;
    }

    /**
     * The subsystem parser, which uses stax to read and write to and from xml
     */
    private static class SubsystemParser implements XMLStreamConstants, 
            XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeContent(XMLExtendedStreamWriter writer, 
                SubsystemMarshallingContext context) throws XMLStreamException {
            //Write out the main subsystem element
            context.startSubsystemElement(SelfmonitorExtension.NAMESPACE, false);
            
            writer.writeStartElement(METRICS);
            ModelNode node = context.getModelNode();
            ModelNode metric = node.get(METRIC);
            for (Property property : metric.asPropertyList()) {
                //write each child element to xml
                writer.writeStartElement(METRIC);
                writer.writeAttribute(METRIC_NAME, property.getName());
                ModelNode entry = property.getValue();
                MetricDefinition.PATH.marshallAsAttribute(entry, true, writer);
                MetricDefinition.ENABLED.marshallAsAttribute(entry, true, writer);
                MetricDefinition.INTERVAL.marshallAsAttribute(entry, true, writer);
                MetricDefinition.TYPE.marshallAsAttribute(entry, true, writer);
                MetricDefinition.DESCRIPTION.marshallAsAttribute(entry, true, writer);
                MetricDefinition.NILLABLE.marshallAsAttribute(entry, true, writer);
                MetricDefinition.DATA_TYPE.marshallAsAttribute(entry, true, writer);
                writer.writeEndElement();
            }
            //End metrics
            writer.writeEndElement();
            //End subsystem
            writer.writeEndElement();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            // Require no attributes
            ParseUtils.requireNoAttributes(reader);
 
            //Add the main subsystem 'add' operation
            final ModelNode subsystem = new ModelNode();
            subsystem.get(OP).set(ADD);
            subsystem.get(OP_ADDR).set(PathAddress.pathAddress(SUBSYSTEM_PATH).toModelNode());
            list.add(subsystem);
 
            //Read the children
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                
                if (reader.getLocalName().equals(METRICS)) {
                    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                        if (reader.isStartElement()) {
                            readMetric(reader, list);
                        }
                    }
                }
                else {
                    throw ParseUtils.unexpectedElement(reader);
                }
            }
        }
        
        private void readMetric(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            if (!reader.getLocalName().equals(METRIC)) {
                throw ParseUtils.unexpectedElement(reader);
            }
            ModelNode addMetricOperation = new ModelNode();
            addMetricOperation.get(OP).set(ModelDescriptionConstants.ADD);
 
            String name = null;
            String path = null;
            boolean enabled = false;
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                String attr = reader.getAttributeLocalName(i);
                String value = reader.getAttributeValue(i);
                if (attr.equals(METRIC_NAME)) {
                    name = value;
                } else if (attr.equals(PATH)) {
                    MetricDefinition.PATH.parseAndSetParameter(value, addMetricOperation, reader);
                } else if (attr.equals(ENABLED)) {
                    MetricDefinition.ENABLED.parseAndSetParameter(value, addMetricOperation, reader);
                } else if (attr.equals(INTERVAL)) {
                    MetricDefinition.INTERVAL.parseAndSetParameter(value, addMetricOperation, reader);
                } else if (attr.equals(TYPE)) {
                    MetricDefinition.TYPE.parseAndSetParameter(value, addMetricOperation, reader);
                } else if (attr.equals(DESCRIPTION)) {
                    MetricDefinition.DESCRIPTION.parseAndSetParameter(value, addMetricOperation, reader);
                } else if (attr.equals(NILLABLE)) {
                    MetricDefinition.NILLABLE.parseAndSetParameter(value, addMetricOperation, reader);
                } else if (attr.equals(DATA_TYPE)) {
                    MetricDefinition.DATA_TYPE.parseAndSetParameter(value, addMetricOperation, reader);
                } else {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
            ParseUtils.requireNoContent(reader);
            if (name == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton(METRIC_NAME));
            }
 
            //Add the 'add' operation for each 'metric' child
            PathAddress addr = PathAddress.pathAddress(SUBSYSTEM_PATH, PathElement.pathElement(METRIC, name));
            addMetricOperation.get(OP_ADDR).set(addr.toModelNode());
            list.add(addMetricOperation);
        }
    }

}

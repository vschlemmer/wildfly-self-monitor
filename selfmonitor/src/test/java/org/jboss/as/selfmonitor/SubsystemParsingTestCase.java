package org.jboss.as.selfmonitor;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import org.junit.Assert;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import org.jboss.as.selfmonitor.service.SelfmonitorService;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceNotFoundException;


/**
 * Tests all management expects for subsystem, parsing, marshaling, model definition and other
 * Here is an example that allows you a fine grained controler over what is tested and how. So it can give you ideas what can be done and tested.
 * If you have no need for advanced testing of subsystem you look at {@link SubsystemBaseParsingTestCase} that testes same stuff but most of the code
 * is hidden inside of test harness
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class SubsystemParsingTestCase extends AbstractSubsystemTest {

    public SubsystemParsingTestCase() {
        super(SelfmonitorExtension.SUBSYSTEM_NAME, new SelfmonitorExtension());
    }
    
    /**
     * Tests that the xml is parsed into the correct operations
     * @throws java.lang.Exception
     */
    @Test
    public void testParseSubsystem() throws Exception {
        //Parse the subsystem xml into operations
        String subsystemXml = getSubsystemXml();
        List<ModelNode> operations = super.parse(subsystemXml);
        
        ///Check that we have the expected number of operations
        Assert.assertEquals(3, operations.size());
        
        //Check that each operation has the correct content
        ModelNode addSubsystem = operations.get(0);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        PathElement element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(SelfmonitorExtension.SUBSYSTEM_NAME, element.getValue());
        
        // check that metrics were set
        ModelNode addMetrics = operations.get(1);
        Assert.assertEquals(ADD, addMetrics.get(OP).asString());
        PathAddress metricsAddress = PathAddress.pathAddress(addMetrics.get(OP_ADDR));
        Assert.assertEquals(2, metricsAddress.size());
        element = metricsAddress.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(SelfmonitorExtension.SUBSYSTEM_NAME, element.getValue());
        Assert.assertEquals("/subsystem=transactions", addMetrics.get("path").asString());
        Assert.assertEquals("false", addMetrics.get("enabled").asString());
        Assert.assertEquals(5, addMetrics.get("interval").asInt());
        Assert.assertEquals("LONG", addMetrics.get("metric-type").asString());
        Assert.assertEquals("true", addMetrics.get("nillable").asString());
    }

    /**
     * Test that the model created from the xml looks as expected
     * @throws java.lang.Exception
     */
    @Test
    public void testInstallIntoController() throws Exception {
        //Parse the subsystem xml and install into the controller
        String subsystemXml = getSubsystemXml();
        KernelServices services = super.createKernelServicesBuilder(null)
                .setSubsystemXml(subsystemXml).build();

        //Read the whole model and make sure it looks as expected
        ModelNode model = services.readWholeModel();
        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(SelfmonitorExtension.SUBSYSTEM_NAME));
        
        Assert.assertTrue(model.get(SUBSYSTEM, 
                SelfmonitorExtension.SUBSYSTEM_NAME).hasDefined("metric"));
        List<ModelNode> metricList = model.get(SUBSYSTEM, 
                SelfmonitorExtension.SUBSYSTEM_NAME).get("metric").asList();
        Assert.assertEquals(2, metricList.size());
        ModelNode metric = model.get(SUBSYSTEM, SelfmonitorExtension.SUBSYSTEM_NAME, 
                "metric", "subsystem_transactions_number-of-aborted-transactions");
        Assert.assertTrue(metric.hasDefined("path"));
        Assert.assertEquals("/subsystem=transactions", metric.get("path").asString());
        Assert.assertTrue(metric.hasDefined("enabled"));
        Assert.assertTrue(!metric.get("enabled").asBoolean());
        Assert.assertTrue(metric.hasDefined("interval"));
        Assert.assertEquals(5, metric.get("interval").asInt());
        Assert.assertTrue(metric.hasDefined("metric-type"));
        Assert.assertEquals("LONG", metric.get("metric-type").asString());
        Assert.assertTrue(metric.hasDefined("metric-type"));
        Assert.assertEquals("true", metric.get("nillable").asString());
    }

    /**
     * Starts a controller with a given subsystem xml and then checks that a second
     * controller started with the xml marshalled from the first one results in the same model
     * @throws java.lang.Exception
     */
    @Test
    public void testParseAndMarshalModel() throws Exception {
        //Parse the subsystem xml and install into the first controller
        String subsystemXml = getSubsystemXml();
        KernelServices servicesA = super.createKernelServicesBuilder(null)
                .setSubsystemXml(subsystemXml).build();
        //Get the model and the persisted xml from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        String marshalled = servicesA.getPersistedSubsystemXml();

        //Install the persisted xml from the first controller into a second controller
        KernelServices servicesB = super.createKernelServicesBuilder(
                AdditionalInitialization.MANAGEMENT).setSubsystemXml(marshalled).build();
        ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    /**
     * Starts a controller with the given subsystem xml and then checks that a second
     * controller started with the operations from its describe action results in the same model
     * @throws java.lang.Exception
     */
    @Test
    public void testDescribeHandler() throws Exception {
        //Parse the subsystem xml and install into the first controller
        String subsystemXml = getSubsystemXml();
        KernelServices servicesA = super.createKernelServicesBuilder(null)
                .setSubsystemXml(subsystemXml).build();
        //Get the model and the describe operations from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        ModelNode describeOp = new ModelNode();
        describeOp.get(OP).set(DESCRIBE);
        describeOp.get(OP_ADDR).set(
                PathAddress.pathAddress(
                        PathElement.pathElement(SUBSYSTEM, SelfmonitorExtension.SUBSYSTEM_NAME)).toModelNode());
        List<ModelNode> operations = super.checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList();


        //Install the describe options from the first controller into a second controller
        KernelServices servicesB = super.createKernelServicesBuilder(
                AdditionalInitialization.MANAGEMENT)
                .setBootOperations(operations).build();
        ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    /**
     * Tests that the subsystem can be removed
     * @throws java.lang.Exception
     */
    @Test
    public void testSubsystemRemoval() throws Exception {
        //Parse the subsystem xml and install into the first controller
        String subsystemXml = getSubsystemXml();
        KernelServices services = super.createKernelServicesBuilder(null)
                .setSubsystemXml(subsystemXml).build();
        
        final ServiceName serviceName = SelfmonitorService.createServiceName();
        ServiceController<?> selfmonitorService = services.getContainer()
                .getRequiredService(serviceName);
        Assert.assertNotNull(selfmonitorService);
        
        //Checks that the subsystem was removed from the model
        super.assertRemoveSubsystemResources(services);

        //Check that any services that were installed were removed here
        try {
            services.getContainer().getRequiredService(serviceName);
            Assert.fail("Selfmonitor service was not removed.");
        } catch (ServiceNotFoundException ex) {            
            // this is ok
        }
    }
    
    private String getSubsystemXml() {
        File configFile = null;
        try {
            configFile = new File(getClass().getClassLoader().getResource("selfmonitor-subsystem.xml").toURI());
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Configuration file was not found.", ex);
        }
        StringBuilder configBuilder = new StringBuilder();
        try{
            InputStreamReader isr = new FileReader(configFile);
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            while (line != null) {
                configBuilder.append(line.trim());
                line = br.readLine();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Error occurred while loading subsystem configuration.", ex);
        }
        return configBuilder.toString();
    }
}

package org.jboss.as.selfmonitor.model.operations;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import org.jboss.as.controller.client.ModelControllerClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author Vojtech Schlemmer
 */
public class CLIOperationsTest {

    private ModelControllerClient client;
    
    @Before
    public void setupCLI() {
        client = Mockito.mock(ModelControllerClient.class);
        
        
//        try {  
//            client = ModelControllerClient.Factory.create(
//                    InetAddress.getByName("localhost"), 9990);
//        } catch (UnknownHostException ex) {
//            java.util.logging.Logger.getLogger(
//                    CLIOperationsTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    @Test
    public void testReadAllValues(){
        
    }
    
}

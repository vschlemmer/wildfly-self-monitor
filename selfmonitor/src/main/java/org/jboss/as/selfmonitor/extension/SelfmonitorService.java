package org.jboss.as.selfmonitor.extension;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 *
 * @author vojtech
 */
public class SelfmonitorService implements Service<SelfmonitorService> {

    public static final String NAME = "SelfmonitorService01";
    private final Logger log = Logger.getLogger(SubsystemAdd.class);
    private ModelControllerClient client;
    private static final long INTERVAL = 20000;
    
    public SelfmonitorService(){
        client = null;
        try {  
            client = ModelControllerClient.Factory.create(
                    InetAddress.getByName("localhost"), 9999);
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(
                    SelfmonitorService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Thread OUTPUT = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(INTERVAL);
                    writeTransactionsRuntimeAttributes();
                    writeWebRuntimeAttributes();
                } catch (InterruptedException e) {
                    interrupted();
                    break;
                }
            }
        }
    };
    
    public void writeTransactionsRuntimeAttributes(){
        ModelNode returnVal = null;
        try {
            client = ModelControllerClient.Factory.create(
                    InetAddress.getByName("localhost"), 9999);  
            if(client != null){
                ModelNode op = new ModelNode();
                op.get(ClientConstants.OP).set("read-resource");
                op.get("include-runtime").set(true);  
                ModelNode address = op.get("address");  
                address.add("subsystem", "transactions");  
                //address.add("connector", "http"); 
                try {
                    returnVal = client.execute(op);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(
                            SubsystemAdd.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (returnVal != null){
                    log.info("--------------------------------------------------");
                    log.info("TRANSACTIONS RUNTIME ATTRIBUTES");
                    log.info("--------------------------------------------------");
                    log.info("number-of-committed-transactions: " + 
                            returnVal.get("result")
                                    .get("number-of-committed-transactions")
                                    .asString());
                    log.info("number-of-application-rollbacks: " + 
                            returnVal.get("result")
                                    .get("number-of-application-rollbacks")
                                    .asString());
                    log.info("number-of-committed-transactions: " + 
                            returnVal.get("result")
                                    .get("number-of-committed-transactions")
                                    .asString());
                    log.info("number-of-heuristics: " + 
                            returnVal.get("result")
                                    .get("number-of-heuristics")
                                    .asString());
                    log.info("number-of-inflight-transactions: " + 
                            returnVal.get("result")
                                    .get("number-of-inflight-transactions")
                                    .asString());
                    log.info("number-of-nested-transactions: " + 
                            returnVal.get("result")
                                    .get("number-of-nested-transactions")
                                    .asString());
                    log.info("number-of-resource-rollbacks: " + 
                            returnVal.get("result")
                                    .get("number-of-resource-rollbacks")
                                    .asString());
                    log.info("number-of-timed-out-transactions: " + 
                            returnVal.get("result")
                                    .get("number-of-timed-out-transactions")
                                    .asString());
                    log.info("number-of-transactions: " + 
                            returnVal.get("result")
                                    .get("number-of-transactions")
                                    .asString());
                }
            }
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(
                    SubsystemAdd.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void writeWebRuntimeAttributes(){
        ModelNode returnVal = null;
        try {
            client = ModelControllerClient.Factory.create(
                    InetAddress.getByName("localhost"), 9999);  
            if(client != null){
                ModelNode op = new ModelNode();
                op.get(ClientConstants.OP).set("read-resource");
                op.get("include-runtime").set(true);  
                ModelNode address = op.get("address");  
                address.add("subsystem", "web");  
                address.add("connector", "http"); 
                try {
                    returnVal = client.execute(op);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(
                            SubsystemAdd.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (returnVal != null){
                    log.info("--------------------------------------------------");
                    log.info("WEB RUNTIME ATTRIBUTES");
                    log.info("--------------------------------------------------");
                    log.info("bytesReceived: " + 
                            returnVal.get("result")
                                    .get("bytesReceived").asString());
                    log.info("bytesSent: " + 
                            returnVal.get("result")
                                    .get("bytesSent").asString());
                    log.info("errorCount: " + 
                            returnVal.get("result")
                                    .get("errorCount").asString());
                    log.info("executor: " + 
                            returnVal.get("result")
                                    .get("executor").asString());
                    log.info("maxTime: " + 
                            returnVal.get("result")
                                    .get("maxTime").asString());
                    log.info("processingTime: " + 
                            returnVal.get("result")
                                    .get("processingTime").asString());
                    log.info("requestCount: " + 
                            returnVal.get("result")
                                    .get("requestCount").asString());
                }
            }
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(
                    SubsystemAdd.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void start(StartContext sc) throws StartException {
        OUTPUT.start();
    }

    @Override
    public void stop(StopContext sc) {
        OUTPUT.interrupt();
    }

    @Override
    public SelfmonitorService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }
    
}

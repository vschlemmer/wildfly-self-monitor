package org.jboss.as.selfmonitor.model.operations;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.selfmonitor.service.SelfmonitorService;
import org.jboss.as.selfmonitor.storage.IMetricsStorage;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

/**
 *
 * @author Vojtech Schlemmer
 */


public class MetricValueOccurred implements OperationStepHandler {

    protected final String DATE_FORMAT = "yyyy-MM-dd.HH:mm:ss";
    private final Logger log = Logger.getLogger(MetricValueOccurred.class);
    
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.require(OP_ADDR));
        final String metricName = address.getLastElement().getValue();
        String stringDateFrom = operation.get("date-from").asString();
        String stringDateTo = operation.get("date-to").asString();
        String value = operation.get("value").asString();
        Resource resource = context.readResourceFromRoot(address);
        String metricPath = resource.getModel().get("path").asString();
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date dateFrom = null;
        Date dateTo = null;
        Set<ModelNode> metrics;
        String output = "";
        try {
            dateFrom = dateFormat.parse(stringDateFrom);
            dateTo = dateFormat.parse(stringDateTo);
        } catch (ParseException ex) {
            output = "Problem with parsing date. Expecting format " + DATE_FORMAT;
        }
        if(output.length() == 0) {
            metrics = getMetricValues(dateFrom, dateTo, metricName, metricPath, value, context);
            if(metrics.isEmpty()){
                output = "No such value occurrence found";
                context.getResult().set(output);
            }
            else{
                context.getResult().set(metrics);
            }
        }
        else{
            context.getResult().set(output);
        }
        context.stepCompleted();
    }
    
    private Set<ModelNode> getMetricValues(Date dateFrom, Date dateTo, String metricName, 
            String metricPath, String value, OperationContext context){
        IMetricsStorage storage = getStorage(context);
        Set<ModelNode> metricNodes = new HashSet<>();
        Map<Long, String> metricValues = storage.getMetricRecords(
                metricName, metricPath);
        for (Map.Entry<Long, String> entry : metricValues.entrySet()){
            // hack to compare Long values (strips the trailing L if present)
            String entryValue = entry.getValue();
            try{
                String entryNewValue = entryValue;
                if(entryValue.length() > 0 && entryValue.charAt(entryValue.length()-1)=='L') {
                    entryNewValue = entryValue.substring(0, entryValue.length()-1);
                    Long.parseLong(entryNewValue);
                }
                log.info("exception not raised");
                entryValue = entryNewValue;
            } catch(NumberFormatException e){
                log.info("exception raised");
            }
            log.info("value: " + value + ", entryValue: " + entryValue);
            if(entry.getKey().longValue() >= dateFrom.getTime()/1000 &&
               entry.getKey().longValue() <= dateTo.getTime()/1000 &&
               entryValue.equals(value)){
                ModelNode m = new ModelNode();
                Date date = new Date(entry.getKey() * 1000);
                m.get("date").set(date.toString());
                m.get("value").set(entryValue);
                metricNodes.add(m);
            }
        }
        return metricNodes;
    }
    
    private IMetricsStorage getStorage(OperationContext context){
        ServiceController<?> serviceController =  context
                .getServiceRegistry(true)
                .getService(ServiceName.JBOSS.append(SelfmonitorService.NAME));
        SelfmonitorService service = (SelfmonitorService) serviceController.getService();
        return service.getMetricsStorage();
    }
    
}
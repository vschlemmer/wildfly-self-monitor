package org.jboss.as.selfmonitor.model.operations;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import org.jboss.as.selfmonitor.service.SelfmonitorService;
import org.jboss.as.selfmonitor.storage.IMetricsStorage;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

/**
 *
 * @author Vojtech Schlemmer
 */
public class MetricReadValue implements OperationStepHandler {

    protected final String DATE_FORMAT = "yyyy-MM-dd.HH:mm:ss";
    
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.require(OP_ADDR));
        final String metricId = address.getLastElement().getValue();
        String stringDate = operation.get("date").asString();
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date = null;
        String output = "";
        try {
            date = dateFormat.parse(stringDate);
        } catch (ParseException ex) {
            output = "Problem with parsing date. Expecting format " + DATE_FORMAT;
        }
        if(output.length() == 0) {
            String metricValue = getMetricValue(date, metricId, context);
            output += metricValue;
        }
        ModelNode result = new ModelNode().set(output);
        if (result != null) {
            context.getResult().set(result);
        }
        context.stepCompleted();
    }
    
    private String getMetricValue(Date date, String metricId, OperationContext context){
        if(date == null){
            return "Date not specified";
        }
        IMetricsStorage storage = getStorage(context);
        Map<Long, String> metricValues = storage.getMetricRecords(metricId);
        if(metricValues.isEmpty()){
            return "There are currently no stored values of metric " + metricId;
        }
        for (Map.Entry<Long, String> entry : metricValues.entrySet()){
            if(entry.getKey().longValue() == date.getTime()/1000){
                String value = entry.getValue();
                if(value.length() > 0 && value.charAt(value.length()-1)=='L') {
                    value = value.substring(0, value.length()-1);
                }
                return value;
            }
        }
        return "No value found for selected date";
    }
    
    private IMetricsStorage getStorage(OperationContext context){
        ServiceController<?> serviceController =  context
                .getServiceRegistry(true)
                .getService(ServiceName.JBOSS.append(SelfmonitorService.NAME));
        SelfmonitorService service = (SelfmonitorService) serviceController.getService();
        return service.getMetricsStorage();
    }
    
}
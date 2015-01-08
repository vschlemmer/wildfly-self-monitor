package org.jboss.as.selfmonitor.model.operations;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
public class MetricReadValues implements OperationStepHandler {

    protected final String DATE_FORMAT = "yyyy-MM-dd.HH:mm:ss";
    
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.require(OP_ADDR));
        final String metricId = address.getLastElement().getValue();
        String stringDateFrom = operation.get("date-from").asString();
        String stringDateTo = operation.get("date-to").asString();
        String stringFunctionType = operation.get("function-type").asString();
        FunctionType functionType = FunctionType.forKey(stringFunctionType);
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date dateFrom = null;
        Date dateTo = null;
        String output = "";
        try {
            dateFrom = dateFormat.parse(stringDateFrom);
            dateTo = dateFormat.parse(stringDateTo);
        } catch (ParseException ex) {
            output = "Problem with parsing date. Expecting format " + DATE_FORMAT;
        }
        if(output.length() == 0) {
            String metricValue = getMetricValue(dateFrom, dateTo, functionType, 
                    metricId, context);
            output += metricValue;
        }
        ModelNode result = new ModelNode().set(output);
        if (result != null) {
            context.getResult().set(result);
        }
        context.stepCompleted();
    }
    
    private String getMetricValue(Date dateFrom, Date dateTo, FunctionType functionType,
            String metricId, OperationContext context){
        if(functionType == null){
            String returnMessage = "function-type must be one of the following: ";
            for(FunctionType ft : FunctionType.getAllValues()){
                returnMessage += ft.getKey() + "|";
            }
            returnMessage = returnMessage.substring(0, returnMessage.length()-1);
            return returnMessage;
        }
        IMetricsStorage storage = getStorage(context);
        Map<Long, String> metricValues = storage.getMetricRecords(metricId);
        if(metricValues.isEmpty()){
            return "There are currently no stored values of metric " + metricId;
        }
        switch(FunctionType.find(functionType.getId())){
            case AVG:
                return getMetricAverageValue(metricValues, dateFrom, dateTo);
            case MEDIAN:
                return getMetricValue(metricValues, dateFrom, dateTo, FunctionType.forKey("median"));
            case MIN:
                return getMetricValue(metricValues, dateFrom, dateTo, FunctionType.forKey("min"));
            case MAX:
                return getMetricValue(metricValues, dateFrom, dateTo, FunctionType.forKey("max"));
            default:
                return "No results";
        }
    }
    
    private String getMetricAverageValue(Map<Long, String> metricValues, 
            Date dateFrom, Date dateTo){
        long accumulator = 0;
        int counter = 0;
        for (Map.Entry<Long, String> entry : metricValues.entrySet()){
            if(entry.getKey().longValue() >= dateFrom.getTime()/1000 &&
               entry.getKey().longValue() <= dateTo.getTime()/1000){
                String value = entry.getValue();
                if(value.length() > 0 && value.charAt(value.length()-1)=='L') {
                    value = value.substring(0, value.length()-1);
                }
                long addedValue = 0;
                try{
                    addedValue = Long.parseLong(value, 10); 
                }catch(NumberFormatException e){
                    return "Cannot execute this type of operation upon non-numeric attributes";
                }
                accumulator += addedValue;
                counter++;
            }
        }
        if(counter == 0){
            return "No values found between selected dates";
        }
        Long averageValue = new Long(accumulator/counter);
        return averageValue.toString();
    }
    
    private String getMetricValue(Map<Long, String> metricValues, Date dateFrom, 
            Date dateTo, FunctionType functionType){
        Map<Long, Long> metricSortedValues = new TreeMap<>();
        for (Map.Entry<Long, String> entry : metricValues.entrySet()){
            if(entry.getKey().longValue() >= dateFrom.getTime()/1000 &&
               entry.getKey().longValue() <= dateTo.getTime()/1000){
                String value = entry.getValue();
                if(value.length() > 0 && value.charAt(value.length()-1)=='L') {
                    value = value.substring(0, value.length()-1);
                }
                try{
                    metricSortedValues.put(entry.getKey(), Long.parseLong(value, 10));
                } catch(NumberFormatException e){
                    return "Cannot execute this type of operation upon non-numeric attributes";
                }
            }
        }
        List<Long> values = new ArrayList<>(metricSortedValues.values());
        if(values.isEmpty()){
            return "No values stored in this date range";
        }
        Long metricValue = null;
        switch(FunctionType.find(functionType.getId())){
            case MEDIAN:
                metricValue = getMetricMedianValue(metricSortedValues, values);
                break;
            case MIN:
                metricValue = values.get(0);
                break;
            case MAX:
                metricValue = values.get(metricSortedValues.size()-1);
                break;
            default:
                break;
        }
        if(metricValue != null){
            return metricValue.toString();
        }
        else{
            return null;
        }
    }
    
    private Long getMetricMedianValue(Map<Long, Long> metricSortedValues, 
            List<Long> values){
        Long metricValue = null;
        if(metricSortedValues.size()%2 == 0){
            int firstPosition = (metricSortedValues.size()+1)/2;
            int secondPosition = (metricSortedValues.size()-1)/2;
            Long firstValue = values.get(firstPosition);
            Long secondValue = values.get(secondPosition);
            metricValue = new Long((firstValue.longValue()+secondValue.longValue())/2);
        }
        else{
            int medianPosition = (metricSortedValues.size()-1)/2;
            metricValue = (new ArrayList<>(metricSortedValues.values())).get(medianPosition);
        }
        return metricValue;
    }
    
    private IMetricsStorage getStorage(OperationContext context){
        ServiceController<?> serviceController =  context
                .getServiceRegistry(true)
                .getService(ServiceName.JBOSS.append(SelfmonitorService.NAME));
        SelfmonitorService service = (SelfmonitorService) serviceController.getService();
        return service.getMetricsStorage();
    }
    
}
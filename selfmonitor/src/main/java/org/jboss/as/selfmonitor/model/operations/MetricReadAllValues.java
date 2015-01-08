package org.jboss.as.selfmonitor.model.operations;

import java.util.Date;
import java.text.DateFormat;
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
public class MetricReadAllValues implements OperationStepHandler {

    protected final String DATE_FORMAT = "yyyy-MM-dd.HH:mm:ss";
    
    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.require(OP_ADDR));
        final String metricId = address.getLastElement().getValue();
        ModelNode result = new ModelNode();
        Map<Long, String> metricValues = getMetricValues(metricId, context);
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        for (Map.Entry<Long, String> entry : metricValues.entrySet()){
            ModelNode metricNode = new ModelNode();
            Date date = new Date(entry.getKey().longValue()*1000);
            metricNode.get("time").set(dateFormat.format(date));
            metricNode.get("value").set(entry.getValue());
            result.add(metricNode);
        }
        context.getResult().set(result);
        context.stepCompleted();
    }
    
    private Map<Long, String> getMetricValues(String metricId, OperationContext context){
        IMetricsStorage storage = getStorage(context);
        return storage.getMetricRecords(metricId);
    }
    
    private IMetricsStorage getStorage(OperationContext context){
        ServiceController<?> serviceController =  context
                .getServiceRegistry(true)
                .getService(ServiceName.JBOSS.append(SelfmonitorService.NAME));
        SelfmonitorService service = (SelfmonitorService) serviceController.getService();
        return service.getMetricsStorage();
    }
    
}
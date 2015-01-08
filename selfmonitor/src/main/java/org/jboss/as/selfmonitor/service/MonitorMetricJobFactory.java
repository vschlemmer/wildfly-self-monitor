package org.jboss.as.selfmonitor.service;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.selfmonitor.storage.IMetricsStorage;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 *
 * @author Vojtech Schlemmer
 */
public class MonitorMetricJobFactory implements JobFactory {

    private final ModelControllerClient client;
    private final IMetricsStorage metricsStorage;
    
    public MonitorMetricJobFactory(ModelControllerClient client, IMetricsStorage metricsStorage){
        super();
        this.client = client;
        this.metricsStorage = metricsStorage;
    }
    
    @Override
    public Job newJob(TriggerFiredBundle tfb, Scheduler schdlr) throws SchedulerException {
        return new MonitorMetricJob(client, metricsStorage);
    }
    
}

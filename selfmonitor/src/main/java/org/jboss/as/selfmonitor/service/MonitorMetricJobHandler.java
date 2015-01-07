package org.jboss.as.selfmonitor.service;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.selfmonitor.model.ModelMetric;
import org.jboss.as.selfmonitor.storage.IMetricsStorage;
import org.jboss.logging.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author Vojtech Schlemmer
 */
public class MonitorMetricJobHandler {

    public static void changeJobInterval(Map<String, Scheduler> jobs, ModelMetric m){
        Scheduler scheduler = jobs.get(m.getId());
        Trigger oldTrigger = getMetricTrigger(m, scheduler);
        TriggerBuilder tb = null;
        if(oldTrigger != null){
            tb = oldTrigger.getTriggerBuilder();
        }
        else{
            Logger.getLogger(MonitorMetricJobHandler.class).info("oldTrigger of metric " + m.getId() + " not found");
        }
        Trigger newTrigger = null;
        if(tb != null){
            SimpleScheduleBuilder builder = SimpleScheduleBuilder
                    .simpleSchedule()
                    .withIntervalInSeconds(m.getInterval())
                    .repeatForever();
            newTrigger = tb.withSchedule(builder).startNow()
                    .build();
        }
        else{
            Logger.getLogger(MonitorMetricJobHandler.class).info("Trigger builder of metric " + m.getId() + " not found");
        }
        rescheduleJob(oldTrigger, newTrigger, m, scheduler);
    }
    
    public static Trigger getMetricTrigger(ModelMetric m, Scheduler scheduler){
        Trigger trigger = null;
        try {
            trigger = scheduler.getTrigger(TriggerKey.triggerKey(
                    "trigger" + m.getId(), "group" + m.getId()));
        } catch (SchedulerException ex) {
            java.util.logging.Logger.getLogger(MonitorMetricJobHandler.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return trigger;
    }
    
    public static void rescheduleJob(Trigger oldTrigger, Trigger newTrigger,
            ModelMetric m, Scheduler scheduler){
        try {
            if(oldTrigger != null){
                scheduler.interrupt(JobKey.jobKey("job" + m.getId(), 
                        "group" + m.getId()));
                
                scheduler.rescheduleJob(oldTrigger.getKey(), newTrigger);
                scheduler.resumeJob(JobKey.jobKey("job" + m.getId(), 
                        "group" + m.getId()));
            }
        } catch (SchedulerException ex) {
            java.util.logging.Logger.getLogger(MonitorMetricJobHandler.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Scheduler initSingleMetricJob(ModelMetric metric, 
            ModelControllerClient client, IMetricsStorage metricsStorage){
        Trigger trigger = TriggerBuilder
            .newTrigger()
            .withIdentity("trigger" + metric.getId(), "group" + metric.getId())
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(metric.getInterval()).repeatForever())
            .build();
        JobDetail job = JobBuilder.newJob(MonitorMetricJob.class)
            .withIdentity("job" + metric.getId(), "group" + metric.getId())
            .usingJobData("metricId", metric.getPath() + "/" + metric.getId())
            .build();
        Scheduler scheduler = null;
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.setJobFactory(new MonitorMetricJobFactory(client, metricsStorage));
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException ex) {
            java.util.logging.Logger.getLogger(MonitorMetricJobHandler.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return scheduler;
    }
    
    public static Map<String, Scheduler> changeMetricEnabled(
            ModelMetric m, Map<String, Scheduler> jobs, 
            ModelControllerClient client, IMetricsStorage metricsStorage){
        if(m.isEnabled()){
            if(!jobs.containsKey(m.getId())){
                Scheduler scheduler = initSingleMetricJob(m, client, metricsStorage);
                jobs.put(m.getId(), scheduler);
            }
        }
        else{
            removeJob(jobs, m);
        }
        return jobs;
    }
    
    public static Map<String, Scheduler> changeStorageType(
            Map<String, Scheduler> jobs, SelfmonitorService service,
            ModelControllerClient client, IMetricsStorage metricsStorage){
        Map<String, Scheduler> newJobs = new HashMap<>();
        for(Map.Entry<String, Scheduler> entry : jobs.entrySet()){
            Scheduler scheduler = entry.getValue();
            ModelMetric m = service.getMetric(entry.getKey());
            removeJob(jobs, m);
            Scheduler newScheduler = initSingleMetricJob(m, client, metricsStorage);
            newJobs.put(m.getId(), newScheduler);
        }
        return newJobs;
    }
    
    public static void removeJob(Map<String, Scheduler> jobs, ModelMetric m){
        if(jobs.containsKey(m.getId())){
            try {
                jobs.get(m.getId()).deleteJob(JobKey.jobKey(
                        "job" + m.getId(), "group" + m.getId()));
            } catch (SchedulerException ex) {
                java.util.logging.Logger.getLogger(MonitorMetricJobHandler.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            jobs.remove(m.getId());
        }
    }
    
    /**
     * 
     * Retrieves all records of all metrics from the storage and writes it
     * into log
     * @param log
     * @param metric
     * @param metricsStorage
     */
    public static void logStoredMetric(Logger log, ModelMetric metric, IMetricsStorage metricsStorage){
        SimpleDateFormat printFormat = new SimpleDateFormat("HH:mm:ss");
        log.info("==================================");
        log.info(metric.getId());
        log.info("==================================");
        log.info("Date and time     | value");
        log.info("----------------------------------");
        Map<Long, String> metricData = metricsStorage.getMetricRecords(metric.getId());
        if(metricData != null && !metricData.isEmpty()){
            Map<Long, String> sortedMetricData = new TreeMap<>(metricData);
            for (Map.Entry<Long, String> entry : sortedMetricData.entrySet()){
                log.info(printFormat.format(new Date(entry.getKey().longValue() * 1000)) + 
                        "          |  " + entry.getValue());
            }
        }
        else{
            log.info("No values found");
        }
    }
}

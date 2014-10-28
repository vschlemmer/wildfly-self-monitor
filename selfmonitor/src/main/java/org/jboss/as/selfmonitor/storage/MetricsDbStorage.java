package org.jboss.as.selfmonitor.storage;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.jboss.as.selfmonitor.entity.Metric;
import org.jboss.logging.Logger;

/**
 * Class providing database storage of metric records
 * 
 * @author Vojtech Schlemmer
 */
public class MetricsDbStorage implements IMetricsStorage {

    private final Logger log = Logger.getLogger(MetricsDbStorage.class);
    private EntityManager entityManager;
    public static final String PERSISTENCE_UNIT_NAME = "SelfmonitorPU";
    
    public MetricsDbStorage(){
        this.entityManager = createEntityManagerFactory(
                PERSISTENCE_UNIT_NAME).createEntityManager();
    }
    
    /**
     * TODO: delete - for test purposes
     * tests whether the database is initialized properly
     */
    public void initDatabase(){
        if(entityManager != null){
            Metric m = new Metric("testMetric1", "testMetric1", 
                    new Date(System.currentTimeMillis()), null);
            entityManager.getTransaction().begin();
            entityManager.persist(m);
            entityManager.getTransaction().commit();
            String queryString = "SELECT m FROM Metric m WHERE m.name = 'testMetric1'";
            Query q = entityManager.createQuery(queryString, Metric.class);
            List<Metric> metrics = q.getResultList();
            if(metrics.size() > 0){
                log.info("Test metric stored: " + metrics.get(0).getName());
            }
//            entityManager.close();
        }
        else{
            log.info("EntityManager is null");
        }
    }
    
    /**
     * Creates EntityManagerFactory instance for a given persistence unit name
     * 
     * @param persistenceUnit persistence unit for which EntityManagerFactory
     * will be created
     * @return EntityManagerFactory for a given persistence unit
     */
    private EntityManagerFactory createEntityManagerFactory(String persistenceUnit){	
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            return Persistence.createEntityManagerFactory(persistenceUnit);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    @Override
    public void addMetric(String metricName, String metricPath, Date date, Object value) {
        Metric metric = new Metric(metricName, metricPath, date, (String) value);
        entityManager.getTransaction().begin();
        entityManager.persist(metric);
        entityManager.getTransaction().commit();
    }

    @Override
    public Map<Date, Object> getMetricRecords(String metricName, String metricPath) {
        Map<Date, Object> metricRecords = new HashMap<>();
        for (Metric m : retrieveMetricRecords(metricName, metricPath)){
            metricRecords.put(m.getDate(), m.getValue());
        }
        return metricRecords;
    }
    
    /**
     * retrieves values of a given metric name and path from the database
     * @param metricName name of the metric
     * @param metricPath path of the metric
     * @return list of retrieved metrics
     */
    private List<Metric> retrieveMetricRecords(String metricName, String metricPath){
        String queryString = "SELECT m FROM Metric m WHERE m.name = '" +
                metricName + "' AND m.path = '" + metricPath + "'";
        Query q = entityManager.createQuery(queryString, Metric.class);
        List<Metric> metrics = q.getResultList();
        return metrics;
    }
    
}

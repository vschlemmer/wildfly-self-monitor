package org.jboss.as.selfmonitor.storage;

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
    private EntityManagerFactory emf;
    public static final String PERSISTENCE_UNIT_NAME = "SelfmonitorPU";
    
    public MetricsDbStorage(){
        this.emf = createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
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
    public void addMetric(String metricName, String metricPath, long time, String value) {
        Metric metric = new Metric(metricName, metricPath, time, (String) value);
//        log.info("------------------------------");
//        log.info("metricName: " + metricName);
//        log.info("time: " + time);
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(metric);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Override
    public Map<Long, String> getMetricRecords(String metricName, String metricPath) {
        Map<Long, String> metricRecords = new HashMap<>();
        for (Metric m : retrieveMetricRecords(metricName, metricPath)){
            metricRecords.put(new Long(m.getTime()), m.getValue());
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
        EntityManager entityManager = emf.createEntityManager();
        Query q = entityManager.createQuery(queryString, Metric.class);
        List<Metric> metrics = q.getResultList();
        entityManager.close();
        return metrics;
    }
}

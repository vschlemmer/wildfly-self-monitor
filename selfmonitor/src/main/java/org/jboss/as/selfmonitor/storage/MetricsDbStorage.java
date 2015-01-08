package org.jboss.as.selfmonitor.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.jboss.as.selfmonitor.entity.Metric;

/**
 * Class providing database storage of metric records
 * 
 * @author Vojtech Schlemmer
 */
public class MetricsDbStorage implements IMetricsStorage {

    private final EntityManagerFactory emf;
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
        EntityManagerFactory entmf = null;
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            entmf = Persistence.createEntityManagerFactory(persistenceUnit);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
        return entmf;
    }

    @Override
    public void addMetric(String metricId, String metricPath, long time, String value) {
        Metric metric = new Metric(metricId, metricPath, time, (String) value);
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(metric);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Override
    public Map<Long, String> getMetricRecords(String metricId) {
        Map<Long, String> metricRecords = new HashMap<>();
        for (Metric m : retrieveMetricRecords(metricId)){
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
    private List<Metric> retrieveMetricRecords(String metricId){
        String queryString = "SELECT m FROM Metric m WHERE m.name = '" +
                metricId + "'";
        EntityManager entityManager = emf.createEntityManager();
        Query q = entityManager.createQuery(queryString, Metric.class);
        List<Metric> metrics = q.getResultList();
        entityManager.close();
        return metrics;
    }
}

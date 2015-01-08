package org.jboss.as.selfmonitor.storage;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.selfmonitor.entity.Metric;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class PersistenceTest {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
            .addPackage(Metric.class.getPackage())
            .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
            .addAsWebInfResource("jbossas-ds.xml");
    }
 
    private static final String[] METRIC_NAMES = {
        "Metric 1",
        "Metric 2",
        "Metric 3"
    };
    
    @PersistenceContext
    EntityManager em;
    
    @Inject
    UserTransaction utx;
 
    @Before
    public void preparePersistenceTest() throws Exception {
        clearData();
        insertData();
        startTransaction();
    }

    private void clearData() throws Exception {
        utx.begin();
        em.joinTransaction();
        System.out.println("Dumping old records...");
        em.createQuery("delete from Metric").executeUpdate();
        utx.commit();
    }

    private void insertData() throws Exception {
        utx.begin();
        em.joinTransaction();
        System.out.println("Inserting records...");
        for (String name : METRIC_NAMES) {
            Metric metric = new Metric();
            metric.setName(name);
            em.persist(metric);
        }
        utx.commit();
        // clear the persistence context (first-level cache)
        em.clear();
    }

    private void startTransaction() throws Exception {
        utx.begin();
        em.joinTransaction();
    }
    
    @After
    public void commitTransaction() throws Exception {
        utx.commit();
    }
    
    @Test
    public void testFindAllMetrics() throws Exception {
        String fetchAllMetricsJpql = "SELECT m FROM Metric m ORDER BY m.id";
        System.out.println("Selecting (using JPQL)...");
        List<Metric> metrics = em.createQuery(fetchAllMetricsJpql, Metric.class).getResultList();

        // then
        System.out.println("Found " + metrics.size() + " metrics (using JPQL):");
        assertContainsAllMetrics(metrics);
    }
    
    private static void assertContainsAllMetrics(Collection<Metric> retrievedMetrics) {
        Assert.assertEquals(METRIC_NAMES.length, retrievedMetrics.size());
        final Set<String> retrievedMetricNames = new HashSet<String>();
        for (Metric metric : retrievedMetrics) {
            System.out.println("name: " + metric.getName());
            retrievedMetricNames.add(metric.getName());
        }
        Assert.assertTrue(retrievedMetricNames.containsAll(Arrays.asList(METRIC_NAMES)));
    }
}
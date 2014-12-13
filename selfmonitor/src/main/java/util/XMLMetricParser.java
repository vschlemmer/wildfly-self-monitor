package util;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.jboss.as.selfmonitor.model.ModelMetric;
import org.jboss.as.selfmonitor.service.SelfmonitorService;
import org.jboss.logging.Logger;
import org.xml.sax.SAXException;

/**
 *
 * @author Vojtech Schlemmer
 */
public class XMLMetricParser {

    public static final Logger log = Logger.getLogger(XMLMetricParser.class);
    
    public static Set<ModelMetric> parse(File file, Set<ModelMetric> metrics){
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = null;
        try {
            log.info("Parsing JON XML file...");
            sp = spf.newSAXParser();
            XMLMetricHandler handler = new XMLMetricHandler();
            sp.parse(file, handler);
            log.info("Found " + handler.getMetricCount() + " metric elements.");
            Map<String, String> metricDataTypes = handler.getMetricDataTypes();
            pairWithExistingMetrics(metrics, metricDataTypes);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            java.util.logging.Logger.getLogger(SelfmonitorService.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static Set<ModelMetric> pairWithExistingMetrics(Set<ModelMetric> metrics, 
            Map<String, String> metricDataTypes){
        int pairedCounter = 0;
        for(ModelMetric metric : metrics){
            String metricName = metric.getNameFromId();
            if(metricDataTypes.containsKey(metricName)){
                String dataType = metricDataTypes.get(metricName);
                log.info("metricName: " + metricName);
                log.info("metricPath: " + metric.getPath());
                log.info("dataType: " + dataType);
                log.info("-------------------------------");
                pairedCounter++;
            }
        }
        log.info("Paired: " + pairedCounter);
        return null;
    }
    
}

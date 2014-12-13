package util;

import java.util.HashMap;
import java.util.Map;
import org.jboss.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Vojtech Schlemmer
 */
public class XMLMetricHandler extends DefaultHandler {
    
    private int metricCount;
    private final Map<String, String> metricDataTypes;
    public final Logger log = Logger.getLogger(XMLMetricHandler.class);

    public XMLMetricHandler(){
        metricDataTypes = new HashMap<>();
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equalsIgnoreCase("metric")) {
            this.metricCount++;
            String metricName = attributes.getValue("property");
            String dataType = attributes.getValue("dataType");
            if(!metricDataTypes.containsKey(metricName)){
                this.metricDataTypes.put(metricName, dataType);
            }
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        
    }
    
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        
    }
    
    public int getMetricCount() {
        return metricCount;
    }

    public Map<String, String> getMetricDataTypes() {
        return metricDataTypes;
    }
    
}

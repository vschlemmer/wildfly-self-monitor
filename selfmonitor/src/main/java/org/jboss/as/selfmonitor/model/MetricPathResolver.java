package org.jboss.as.selfmonitor.model;

import java.util.Arrays;
import org.jboss.dmr.ModelNode;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author Vojtech Schlemmer
 */
public class MetricPathResolver {

    private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(MetricPathResolver.class);
    
    public static ModelNode resolvePath(String path, ModelNode op){
        ModelNode metricPath = op.get("address");
        if(path.length() <= 1){
            return metricPath;
        }
        String delim = "/";
        String subSectionDelim = "=";
        String[] sectionsRaw;
        String[] sections;
        String[] subSections;
        sectionsRaw = path.split(delim);
        // in case "/" or "=" is the part of the segment of the path
        String delimElement = sectionsRaw[sectionsRaw.length-1];
        if(delimElement.substring(delimElement.length()-1, delimElement.length()).equals("=")){
            sectionsRaw[sectionsRaw.length-1] += "/";
        }
        if (path.substring(0, 1).equals(delim)){
            sections = Arrays.copyOfRange(sectionsRaw, 1, sectionsRaw.length);
        }
        else{
            sections = sectionsRaw;
        }
        for (String section : sections){
            subSections = section.split(subSectionDelim);
            if(subSections.length > 1) {
                metricPath.add(subSections[0], subSections[1]);
            }
        }
        return metricPath;
    }
    
}

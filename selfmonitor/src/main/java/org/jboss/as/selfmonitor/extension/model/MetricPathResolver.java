package org.jboss.as.selfmonitor.extension.model;

import java.util.Arrays;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author Vojtech Schlemmer
 */
public class MetricPathResolver {

    public static ModelNode resolvePath(String path, ModelNode op){
        ModelNode metricPath = op.get("address");
        String delim = "/";
        String subSectionDelim = "=";
        String[] sectionsRaw;
        String[] sections;
        String[] subSections;
        sectionsRaw = path.split(delim);
        if (path.substring(0, 1).equals(delim)){
            sections = Arrays.copyOfRange(sectionsRaw, 1, sectionsRaw.length);
        }
        else{
            sections = sectionsRaw;
        }
        for (String section : sections){
            subSections = section.split(subSectionDelim);
            metricPath.add(subSections[0], subSections[1]);
        }
        return metricPath;
    }
    
}

package org.jboss.as.selfmonitor.model;

import java.util.Arrays;
import org.jboss.dmr.ModelNode;

/**
 * Class for resolving path of the server's resource model
 * 
 * 
 * @author Vojtech Schlemmer
 */
public class MetricPathResolver {

    /**
     * Parses path of the server's resource model and sets this path 
     * to the op attribute
     * 
     * @param path path to be resolved in form /childType=child [(/childType=child)*]
     * @param op node to which the resolved path will be set
     * @return node with path set
     */
    public static ModelNode resolvePath(String path, ModelNode op){
        ModelNode metricPath = op.get("address");
        if(path.length() <= 1){
            metricPath.add("");
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
    
    public static boolean isPathValid(String path){
        String[] parts = path.split("/");
        boolean valid = true;
        for (int i = 0; i < parts.length; i++){
            if(parts[i].length() > 10){
                String invalidPart = parts[i].substring(0, 10);
                if(invalidPart.equals("applies-to")){
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

    public static String createValidPath(String path){
        if(path.length() <= 1){
            return path;
        }
        String[] parts = path.split("/");
        StringBuilder pathBuilder = new StringBuilder();
        boolean valid = true;
        for (int i = 0; i < parts.length; i++){
            if(parts[i].length() > 10){
                String invalidPart = parts[i].substring(0, 10);
                if(invalidPart.equals("applies-to")){
                    valid = false;
                }
            }
            if(!valid){ 
                break;
            }
            pathBuilder.append(parts[i]);
            pathBuilder.append("/");
        }
        String metricPath = pathBuilder.toString();
        String resultPath = metricPath.substring(0, metricPath.length()-1);
        return resultPath;
    }
    
}

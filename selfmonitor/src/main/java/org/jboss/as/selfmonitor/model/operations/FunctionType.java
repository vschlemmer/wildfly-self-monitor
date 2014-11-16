package org.jboss.as.selfmonitor.model.operations;

/**
 *
 * @author Vojtech Schlemmer
 */
public enum FunctionType {
    
    AVG(1, "avg"),
    MEDIAN(2, "median"),
    MIN(3, "min"),
    MAX(4, "max");
    
    private int id;

    private String key;
    
    private FunctionType(int id, String key) {
        this.id = id;
        this.key = key;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public static FunctionType find(int id) {
        for (FunctionType ft : values()) {
            if (ft.getId() == id) {
                return ft;
            }
        }
        return null;
    }

    public static FunctionType forKey(String key) {
        for (FunctionType ft: values()) {
            if (ft.getKey().equalsIgnoreCase(key)) {
                return ft;
            }
        }
        return null;
    }
    
    public static FunctionType[] getAllValues(){
        return values();
    }

}

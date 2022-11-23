package com.inspur.emmcloud.web.plugin.gps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TraceInfo {
    private String uid;
    private List<Map<String,Object>> locations;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<Map<String, Object>> getLocations() {
        if (locations == null){
            locations = new ArrayList<Map<String,Object>>();
        }
        return locations;
    }

    public void setLocations(List<Map<String, Object>> locations) {
        this.locations = locations;
    }

    public void addLocation(Map<String, Object> location) {
        if (locations != null) {
            locations.add(location);
        } else {
            locations = new ArrayList<Map<String, Object>>();
            locations.add(location);
        }
    }

    public void clear() {
        uid = null;
        if (locations != null) {
            locations.clear();
        }
    }
}

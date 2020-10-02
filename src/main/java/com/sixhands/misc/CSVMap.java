package com.sixhands.misc;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Map;

public class CSVMap {
    private Map<String, String> map = new LinkedMap<>();
    public Map<String, String> getMap(){
        return new LinkedMap<>(map);
    }
    public CSVMap putc(String k, Object v, boolean escape){
        map.put(k, v == null ? "null" : (escape ? StringEscapeUtils.escapeCsv(v.toString()) : v.toString()) );
        return this;
    }
    public CSVMap putc(String k, Object v){
        return putc(k,v,true);
    }
    public CSVMap putcAll(Map<String, String> map){
        return putcAll(map,true);
    }
    public CSVMap putcAll(Map<String, String> map, boolean escape){
        Map<String, String> cloned = new LinkedMap<>(map);
        if(escape){
            for (int i = 0; i < cloned.keySet().size(); i++) {
                Map.Entry<String, String> entry = cloned.entrySet().toArray(new Map.Entry[0])[i];
                cloned.put(entry.getKey(), StringEscapeUtils.escapeCsv(entry.getValue()));
            }
        }
        map.putAll(cloned);
        return this;
    }
}

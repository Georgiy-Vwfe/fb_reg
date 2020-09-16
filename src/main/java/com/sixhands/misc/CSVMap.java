package com.sixhands.misc;

import org.apache.commons.collections4.map.LinkedMap;

import java.util.Map;

public class CSVMap extends LinkedMap<String,String> {
    public CSVMap putc(String k, Object v){
        put(k,v==null?"null":v.toString());
        return this;
    }
    public CSVMap putcAll(Map<? extends String, ? extends String> map){
        putAll(map);
        return this;
    }
}

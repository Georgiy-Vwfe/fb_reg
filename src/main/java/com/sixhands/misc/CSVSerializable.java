package com.sixhands.misc;

import java.util.HashMap;
import java.util.Map;

public interface CSVSerializable {
    Map<String,String> csvMap = new HashMap<>();
    Map<String, String> toCSV();
}

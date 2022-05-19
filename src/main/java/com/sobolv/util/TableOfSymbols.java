package com.sobolv.util;

import com.sobolv.entity.Symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TableOfSymbols {
    private static TableOfSymbols instance;

    private final Map<Integer, Symbol> map = new HashMap<>();

    public String mapToString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, Symbol> entry : map.entrySet()) {
            builder.append(entry.getKey() + " : " + entry.getValue() + "\n");
        }
        return builder.toString();
    }

    public void add(Symbol symbol) {
        map.put(map.size() + 1, symbol);
    }

    public Symbol get(int numRow) {
        return map.get(numRow);
    }

    public Set<Map.Entry<Integer, Symbol>> getEntrySet() {
        return map.entrySet();
    }

    public static TableOfSymbols getInstance() {
        if(instance == null){
            instance = new TableOfSymbols();
        }
        return instance;
    }


    public void remove(Symbol symbol) {
        map.remove(symbol);
    }
}

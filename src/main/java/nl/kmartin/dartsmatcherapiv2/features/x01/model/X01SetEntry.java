package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import java.util.Map;

public record X01SetEntry(int setNumber, X01Set set) {
    public X01SetEntry(Map.Entry<Integer, X01Set> mapEntry) {
        this(mapEntry.getKey(), mapEntry.getValue());
    }
}

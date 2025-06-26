package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import java.util.Map;

public record X01LegRoundEntry(int roundNumber, X01LegRound round) {
    public X01LegRoundEntry(Map.Entry<Integer, X01LegRound> mapEntry) {
        this(mapEntry.getKey(), mapEntry.getValue());
    }
}

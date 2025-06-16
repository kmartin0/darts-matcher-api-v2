package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;

public interface IX01MatchWebsocketService {
    void sendX01MatchUpdate(X01Match x01Match);
}
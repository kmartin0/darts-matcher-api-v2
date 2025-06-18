package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import org.bson.types.ObjectId;

public interface IX01MatchWebsocketService {
    X01MatchWebsocketEvent<X01Match> createUpdateMatchEvent(X01Match match);
    X01MatchWebsocketEvent<ObjectId> createDeleteMatchEvent(ObjectId deletedMatchId);
    void broadcastX01MatchUpdate(X01Match match);
    void broadcastX01MatchDelete(ObjectId deletedMatchId);
}
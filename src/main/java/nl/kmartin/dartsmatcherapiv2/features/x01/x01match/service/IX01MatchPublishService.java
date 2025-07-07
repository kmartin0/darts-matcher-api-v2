package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.event.X01MatchEvent;


public interface IX01MatchPublishService {
    void publish(X01MatchEvent event);
}

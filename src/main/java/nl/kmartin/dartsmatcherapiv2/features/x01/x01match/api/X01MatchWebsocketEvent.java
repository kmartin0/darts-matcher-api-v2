package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api;

import nl.kmartin.dartsmatcherapiv2.features.x01.common.WebSocketEvent;

public class X01MatchWebsocketEvent<P> extends WebSocketEvent<X01WebSocketEventType, P> {
    public X01MatchWebsocketEvent(X01WebSocketEventType eventType, P payload) {
        super(eventType, payload);
    }
}
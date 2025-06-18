package nl.kmartin.dartsmatcherapiv2.features.x01.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A generic data class for WebSocket events.
 *
 * @param <E> The type of the event, typically an enum defining the event's nature.
 * @param <P> The type of the payload data associated with the event.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketEvent<E, P> {
    private E eventType;
    private P payload;
}

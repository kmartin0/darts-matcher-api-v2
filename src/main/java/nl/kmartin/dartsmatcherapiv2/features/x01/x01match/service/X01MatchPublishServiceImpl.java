package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.event.X01MatchEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class X01MatchPublishServiceImpl implements IX01MatchPublishService{
    private final ApplicationEventPublisher eventPublisher;

    public X01MatchPublishServiceImpl(ApplicationEventPublisher eventPublisher) {

        this.eventPublisher = eventPublisher;
    }

    public void publish(X01MatchEvent event) {
            // publish the match event
            eventPublisher.publishEvent(event);

    }
}

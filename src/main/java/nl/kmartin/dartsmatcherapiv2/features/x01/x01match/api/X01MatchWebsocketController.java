package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapiv2.common.WebsocketDestinations;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01EditTurn;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Turn;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.event.X01MatchEvent;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service.IX01MatchService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class X01MatchWebsocketController {

    private final IX01MatchService matchService;

    public X01MatchWebsocketController(IX01MatchService matchService) {
        this.matchService = matchService;
    }

    @SubscribeMapping(WebsocketDestinations.X01_GET_MATCH)
    public X01MatchEvent.X01ProcessMatchEvent subscribeX01Match(@DestinationVariable ObjectId matchId) {
        X01Match match = matchService.getMatch(matchId);
        return new X01MatchEvent.X01ProcessMatchEvent(match);
    }

    @MessageMapping(WebsocketDestinations.X01_ADD_TURN)
    public X01MatchEvent.X01AddHumanTurnEvent addTurn(@DestinationVariable ObjectId matchId, @Valid @Payload X01Turn turn) {
        X01Match match = matchService.addTurn(matchId, turn);
        return new X01MatchEvent.X01AddHumanTurnEvent(match);
    }

    @MessageMapping(WebsocketDestinations.X01_EDIT_TURN)
    public X01MatchEvent.X01EditTurnEvent editTurn(@DestinationVariable ObjectId matchId, @Valid @Payload X01EditTurn editTurn) {
        X01Match updatedMatch = matchService.editTurn(matchId, editTurn);
        return new X01MatchEvent.X01EditTurnEvent(updatedMatch);
    }

    @MessageMapping(WebsocketDestinations.X01_DELETE_LAST_TURN)
    public X01MatchEvent.X01DeleteLastTurnEvent deleteLastTurn(@DestinationVariable ObjectId matchId) {
        X01Match updatedMatch = matchService.deleteLastTurn(matchId);
        return new X01MatchEvent.X01DeleteLastTurnEvent(updatedMatch);
    }

    @MessageMapping(WebsocketDestinations.X01_DELETE_MATCH)
    public X01MatchEvent.X01DeleteMatchEvent deleteMatch(@DestinationVariable ObjectId matchId) {
        matchService.deleteMatch(matchId);
        return new X01MatchEvent.X01DeleteMatchEvent(matchId);
    }

    @MessageMapping(WebsocketDestinations.X01_RESET_MATCH)
    public X01MatchEvent.X01ResetMatchEvent resetMatch(@DestinationVariable ObjectId matchId) {
        X01Match resetMatch = matchService.resetMatch(matchId);
        return new X01MatchEvent.X01ResetMatchEvent(resetMatch);
    }

    @MessageMapping(WebsocketDestinations.X01_REPROCESS_MATCH)
    public X01MatchEvent.X01ProcessMatchEvent reprocessMatch(@DestinationVariable ObjectId matchId) {
        X01Match reProcessedMatch = matchService.reprocessMatch(matchId);
        return new X01MatchEvent.X01ProcessMatchEvent(reProcessedMatch);
    }
}
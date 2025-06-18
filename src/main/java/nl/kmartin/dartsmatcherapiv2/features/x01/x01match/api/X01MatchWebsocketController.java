package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot.IX01DartBotService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service.IX01MatchService;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.WebsocketDestinations;
import org.bson.types.ObjectId;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class X01MatchWebsocketController {

    private final IX01MatchService x01MatchService;
    private final IX01MatchWebsocketService x01MatchWebsocketService;
    private final IX01DartBotService x01DartBotService;

    public X01MatchWebsocketController(IX01MatchService x01MatchService,
                                       IX01MatchWebsocketService x01MatchWebsocketService,
                                       IX01DartBotService x01DartBotService) {
        this.x01MatchService = x01MatchService;
        this.x01MatchWebsocketService = x01MatchWebsocketService;
        this.x01DartBotService = x01DartBotService;
    }

    @SubscribeMapping(WebsocketDestinations.X01_GET_MATCH)
    public X01MatchWebsocketEvent<X01Match> subscribeX01Match(@DestinationVariable ObjectId matchId) {
        X01Match match = x01MatchService.getMatch(matchId);
        return x01MatchWebsocketService.createUpdateMatchEvent(match);
    }

    @MessageMapping(WebsocketDestinations.X01_ADD_TURN)
    public X01MatchWebsocketEvent<X01Match> addTurn(@DestinationVariable ObjectId matchId, @Valid @Payload X01Turn turn) {
        X01Match updatedMatch = x01MatchService.addTurn(matchId, turn);
        x01MatchWebsocketService.broadcastX01MatchUpdate(updatedMatch);
        return x01MatchWebsocketService.createUpdateMatchEvent(updatedMatch);
    }

    @MessageMapping(WebsocketDestinations.X01_TURN_DART_BOT)
    public X01MatchWebsocketEvent<X01Match> addDartBotTurn(@DestinationVariable ObjectId matchId) {
        X01Turn dartBotTurn = x01DartBotService.createDartBotTurn(matchId);
        X01Match updatedMatch = x01MatchService.addTurn(matchId, dartBotTurn);
        x01MatchWebsocketService.broadcastX01MatchUpdate(updatedMatch);
        return x01MatchWebsocketService.createUpdateMatchEvent(updatedMatch);
    }

    @MessageMapping(WebsocketDestinations.X01_EDIT_TURN)
    public X01MatchWebsocketEvent<X01Match> editTurn(@DestinationVariable ObjectId matchId, @Valid @Payload X01EditTurn editTurn) {
        X01Match updatedMatch = x01MatchService.editTurn(matchId, editTurn);
        x01MatchWebsocketService.broadcastX01MatchUpdate(updatedMatch);
        return x01MatchWebsocketService.createUpdateMatchEvent(updatedMatch);
    }

    @MessageMapping(WebsocketDestinations.X01_DELETE_LAST_TURN)
    public X01MatchWebsocketEvent<X01Match> deleteLastTurn(@DestinationVariable ObjectId matchId) {
        X01Match updatedMatch = x01MatchService.deleteLastTurn(matchId);
        x01MatchWebsocketService.broadcastX01MatchUpdate(updatedMatch);
        return x01MatchWebsocketService.createUpdateMatchEvent(updatedMatch);
    }

    @MessageMapping(WebsocketDestinations.X01_DELETE_MATCH)
    public X01MatchWebsocketEvent<ObjectId> deleteMatch(@DestinationVariable ObjectId matchId) {
        x01MatchService.deleteMatch(matchId);
        x01MatchWebsocketService.broadcastX01MatchDelete(matchId);
        return x01MatchWebsocketService.createDeleteMatchEvent(matchId);
    }

    @MessageMapping(WebsocketDestinations.X01_RESET_MATCH)
    public X01MatchWebsocketEvent<X01Match> resetMatch(@DestinationVariable ObjectId matchId) {
        X01Match resetMatch = x01MatchService.resetMatch(matchId);
        x01MatchWebsocketService.broadcastX01MatchUpdate(resetMatch);
        return x01MatchWebsocketService.createUpdateMatchEvent(resetMatch);
    }
}
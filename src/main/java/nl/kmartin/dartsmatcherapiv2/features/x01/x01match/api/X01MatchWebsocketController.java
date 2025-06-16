package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01DeleteLastTurn;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01EditTurn;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Turn;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot.IX01DartBotService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service.IX01MatchService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service.X01MatchServiceImpl;
import nl.kmartin.dartsmatcherapiv2.utils.WebsocketDestinations;
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
    public X01Match subscribeX01Match(@DestinationVariable ObjectId matchId) {

        return x01MatchService.getMatch(matchId);
    }

    @MessageMapping(WebsocketDestinations.X01_ADD_TURN)
    public X01Match addTurn(@Valid @Payload X01Turn turn) {
        X01Match updatedMatch = x01MatchService.addTurn(turn);
        x01MatchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }

    @MessageMapping(WebsocketDestinations.X01_TURN_DART_BOT)
    public X01Match addDartBotTurn(@Payload ObjectId matchId) {
        X01Turn dartBotTurn = x01DartBotService.createDartBotTurn(matchId);
        X01Match updatedMatch = x01MatchService.addTurn(dartBotTurn);
        x01MatchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }

    @MessageMapping(WebsocketDestinations.X01_EDIT_TURN)
    public X01Match editTurn(@Valid @Payload X01EditTurn editTurn) {
        X01Match updatedMatch = x01MatchService.editTurn(editTurn);
        x01MatchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }

    @MessageMapping(WebsocketDestinations.X01_DELETE_LAST_TURN)
    public X01Match deleteLastTurn(@Payload X01DeleteLastTurn deleteLastTurn) {
        X01Match updatedMatch = x01MatchService.deleteLastTurn(deleteLastTurn);
        x01MatchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }
}
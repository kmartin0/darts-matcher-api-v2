package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01DeleteLastTurn;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01EditTurn;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Turn;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot.IX01DartBotService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service.X01MatchServiceImpl;
import nl.kmartin.dartsmatcherapiv2.utils.RestEndpoints;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class X01MatchRestController {
//    private final IX01MatchService x01MatchService;
//    private final IX01MatchWebsocketService x01MatchWebsocketService;
//    private final IX01DartBotService x01DartBotService;
//
//    public X01MatchRestController(IX01MatchService x01MatchService, IX01MatchWebsocketService x01MatchWebsocketService, IX01DartBotService x01DartBotService) {
//        this.x01MatchService = x01MatchService;
//        this.x01MatchWebsocketService = x01MatchWebsocketService;
//        this.x01DartBotService = x01DartBotService;
//    }

    private final X01MatchServiceImpl x01MatchService;
    private final IX01MatchWebsocketService x01MatchWebsocketService;
    private final IX01DartBotService x01DartBotService;

    public X01MatchRestController(X01MatchServiceImpl x01MatchService, IX01MatchWebsocketService x01MatchWebsocketService, IX01DartBotService x01DartBotService) {
        this.x01MatchService = x01MatchService;
        this.x01MatchWebsocketService = x01MatchWebsocketService;
        this.x01DartBotService = x01DartBotService;
    }

    @PostMapping(path = RestEndpoints.X01_CREATE_MATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public X01Match createMatch(@Valid @RequestBody X01Match x01Match) {
        X01Match createdMatch = x01MatchService.createMatch(x01Match);
        x01MatchWebsocketService.sendX01MatchUpdate(createdMatch);
        return createdMatch;
    }

    @GetMapping(path = RestEndpoints.X01_GET_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match getMatch(@PathVariable @NotNull ObjectId matchId) {

        return x01MatchService.getMatch(matchId);
    }

    @PostMapping(path = RestEndpoints.X01_ADD_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match addTurn(@Valid @RequestBody X01Turn x01Turn) {
        X01Match updatedMatch = x01MatchService.addTurn(x01Turn);
        x01MatchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }

    @PostMapping(path = RestEndpoints.X01_EDIT_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match editTurn(@Valid @RequestBody X01EditTurn x01EditTurn) {
        X01Match updatedMatch = x01MatchService.addTurn(x01EditTurn);
        x01MatchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }

    @PostMapping(path = RestEndpoints.X01_DELETE_LAST_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match deleteLastTurn(@Valid @RequestBody X01DeleteLastTurn x01DeleteLastTurn) {
        X01Match updatedMatch = x01MatchService.deleteLastTurn(x01DeleteLastTurn);
        x01MatchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }

    @PostMapping(path = RestEndpoints.X01_TURN_DART_BOT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match addDartBotTurn(@RequestBody @NotNull ObjectId matchId) {
        X01Turn dartBotTurn = x01DartBotService.createDartBotTurn(matchId);
        X01Match updatedMatch = x01MatchService.addTurn(dartBotTurn);
        x01MatchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }
}
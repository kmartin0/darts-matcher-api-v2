package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01DeleteLastTurn;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01EditTurn;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Turn;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot.IX01DartBotService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service.IX01MatchService;
import nl.kmartin.dartsmatcherapiv2.utils.RestEndpoints;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class X01MatchRestController {

    private final IX01MatchService matchService;
    private final IX01MatchWebsocketService matchWebsocketService;
    private final IX01DartBotService dartBotService;

    public X01MatchRestController(IX01MatchService matchService,
                                  IX01MatchWebsocketService matchWebsocketService,
                                  IX01DartBotService dartBotService) {
        this.matchService = matchService;
        this.matchWebsocketService = matchWebsocketService;
        this.dartBotService = dartBotService;
    }

    @PostMapping(path = RestEndpoints.X01_CREATE_MATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public X01Match createMatch(@Valid @RequestBody X01Match match) {
        X01Match createdMatch = matchService.createMatch(match);
        matchWebsocketService.sendX01MatchUpdate(createdMatch);
        return createdMatch;
    }

    @GetMapping(path = RestEndpoints.X01_GET_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match getMatch(@PathVariable @NotNull ObjectId matchId) {

        return matchService.getMatch(matchId);
    }

    @PostMapping(path = RestEndpoints.X01_ADD_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match addTurn(@Valid @RequestBody X01Turn turn) {
        X01Match updatedMatch = matchService.addTurn(turn);
        matchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }

    @PostMapping(path = RestEndpoints.X01_EDIT_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match editTurn(@Valid @RequestBody X01EditTurn editTurn) {
        X01Match updatedMatch = matchService.addTurn(editTurn);
        matchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }

    @PostMapping(path = RestEndpoints.X01_DELETE_LAST_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match deleteLastTurn(@Valid @RequestBody X01DeleteLastTurn deleteLastTurn) {
        X01Match updatedMatch = matchService.deleteLastTurn(deleteLastTurn);
        matchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }

    @PostMapping(path = RestEndpoints.X01_TURN_DART_BOT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match addDartBotTurn(@RequestBody @NotNull ObjectId matchId) {
        X01Turn dartBotTurn = dartBotService.createDartBotTurn(matchId);
        X01Match updatedMatch = matchService.addTurn(dartBotTurn);
        matchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }
}
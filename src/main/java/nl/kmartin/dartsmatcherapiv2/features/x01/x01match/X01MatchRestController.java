package nl.kmartin.dartsmatcherapiv2.features.x01.x01match;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.utils.RestEndpoints;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class X01MatchRestController {
    private final IX01MatchService x01MatchService;
    private final IX01MatchWebsocketService x01MatchWebsocketService;

    public X01MatchRestController(final IX01MatchService x01MatchService, IX01MatchWebsocketService x01MatchWebsocketService) {
        this.x01MatchService = x01MatchService;
        this.x01MatchWebsocketService = x01MatchWebsocketService;
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
    public X01Match addTurn(@Valid @RequestBody X01Turn x01Turn) throws IOException {
        X01Match updatedMatch = x01MatchService.addTurn(x01Turn);
        x01MatchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }

    @PostMapping(path = RestEndpoints.X01_EDIT_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match editTurn(@Valid @RequestBody X01EditTurn x01EditTurn) throws IOException {
        X01Match updatedMatch = x01MatchService.editTurn(x01EditTurn);
        x01MatchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }

    @PostMapping(path = RestEndpoints.X01_DELETE_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match deleteTurn(@Valid @RequestBody X01DeleteTurn x01DeleteTurn) {
        X01Match updatedMatch = x01MatchService.deleteTurn(x01DeleteTurn);
        x01MatchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }

    @PostMapping(path = RestEndpoints.X01_DELETE_LEG, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match deleteLeg(@Valid @RequestBody X01DeleteLeg x01DeleteLeg) {
        X01Match updatedMatch = x01MatchService.deleteLeg(x01DeleteLeg);
        x01MatchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }

    @PostMapping(path = RestEndpoints.X01_DELETE_SET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match deleteSet(@Valid @RequestBody X01DeleteSet x01DeleteSet) {
        X01Match updatedMatch = x01MatchService.deleteSet(x01DeleteSet);
        x01MatchWebsocketService.sendX01MatchUpdate(updatedMatch);
        return updatedMatch;
    }
}
package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapiv2.common.RestEndpoints;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01EditTurn;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Turn;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service.IX01MatchService;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class X01MatchRestController {

    private final IX01MatchService matchService;

    public X01MatchRestController(IX01MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping(path = RestEndpoints.X01_CREATE_MATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public X01Match createMatch(@Valid @RequestBody X01Match match) {
        return matchService.createMatch(match);
    }

    @GetMapping(path = RestEndpoints.X01_GET_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match getMatch(@PathVariable ObjectId matchId) {
        return matchService.getMatch(matchId);
    }

    @GetMapping(path = RestEndpoints.X01_MATCH_EXISTS, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void matchExists(@PathVariable ObjectId matchId) {
        matchService.checkMatchExists(matchId);
    }

    @PostMapping(path = RestEndpoints.X01_ADD_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match addTurn(@PathVariable ObjectId matchId, @Valid @RequestBody X01Turn turn) {
        return matchService.addTurn(matchId, turn);
    }

    @PostMapping(path = RestEndpoints.X01_EDIT_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match editTurn(@PathVariable ObjectId matchId, @Valid @RequestBody X01EditTurn editTurn) {
        return matchService.addTurn(matchId, editTurn);
    }

    @PostMapping(path = RestEndpoints.X01_DELETE_LAST_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match deleteLastTurn(@PathVariable ObjectId matchId) {
        return matchService.deleteLastTurn(matchId);
    }

    @PostMapping(path = RestEndpoints.X01_DELETE_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteMatch(@PathVariable ObjectId matchId) {
        matchService.deleteMatch(matchId);
    }

    @PostMapping(path = RestEndpoints.X01_RESET_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match resetMatch(@PathVariable ObjectId matchId) {
        return matchService.resetMatch(matchId);
    }

    @PostMapping(path = RestEndpoints.X01_REPROCESS_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match reprocessMatch(@PathVariable ObjectId matchId) {
        return matchService.reprocessMatch(matchId);
    }
}
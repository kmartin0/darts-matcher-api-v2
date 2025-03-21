package nl.kmartin.dartsmatcherapiv2.features.x01.x01match;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.utils.Endpoints;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class X01MatchController {
    private final IX01MatchService x01MatchService;

    public X01MatchController(final IX01MatchService x01MatchService) {
        this.x01MatchService = x01MatchService;
    }

    @PostMapping(path = Endpoints.CREATE_MATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public X01Match createMatch(@Valid @RequestBody X01Match x01Match) {

        return x01MatchService.createMatch(x01Match);
    }

    @GetMapping(path = Endpoints.GET_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match getMatch(@PathVariable @NotNull ObjectId matchId) {

        return x01MatchService.getMatch(matchId);
    }

    @PostMapping(path = Endpoints.X01_ADD_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match addTurn(@Valid @RequestBody X01Turn x01Turn) throws IOException {

        return x01MatchService.addTurn(x01Turn);
    }

    @PostMapping(path = Endpoints.X01_EDIT_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match editTurn(@Valid @RequestBody X01EditTurn x01EditTurn) throws IOException {

        return x01MatchService.editTurn(x01EditTurn);
    }

    @PostMapping(path = Endpoints.X01_DELETE_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match deleteTurn(@Valid @RequestBody X01DeleteTurn x01DeleteTurn) {

        return x01MatchService.deleteTurn(x01DeleteTurn);
    }

    @PostMapping(path = Endpoints.X01_DELETE_LEG, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match deleteLeg(@Valid @RequestBody X01DeleteLeg x01DeleteLeg) {

        return x01MatchService.deleteLeg(x01DeleteLeg);
    }

    @PostMapping(path = Endpoints.X01_DELETE_SET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match deleteSet(@Valid @RequestBody X01DeleteSet x01DeleteSet) {

        return x01MatchService.deleteSet(x01DeleteSet);
    }
}
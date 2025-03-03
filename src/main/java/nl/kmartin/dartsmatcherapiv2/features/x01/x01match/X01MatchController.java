package nl.kmartin.dartsmatcherapiv2.features.x01.x01match;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.utils.Endpoints;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}

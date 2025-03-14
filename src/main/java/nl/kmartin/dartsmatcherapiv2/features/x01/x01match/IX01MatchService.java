package nl.kmartin.dartsmatcherapiv2.features.x01.x01match;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Turn;
import org.bson.types.ObjectId;

import java.io.IOException;

public interface IX01MatchService {
    X01Match createMatch(@Valid @NotNull X01Match x01Match);

    X01Match getMatch(@NotNull ObjectId matchId) throws ResourceNotFoundException;

    X01Match addTurn(X01Turn x01Turn) throws IOException;
}

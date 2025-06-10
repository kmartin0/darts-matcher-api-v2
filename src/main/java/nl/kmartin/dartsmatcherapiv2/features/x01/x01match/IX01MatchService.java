package nl.kmartin.dartsmatcherapiv2.features.x01.x01match;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import org.bson.types.ObjectId;

import java.io.IOException;

public interface IX01MatchService {
    X01Match createMatch(@Valid @NotNull X01Match x01Match);

    X01Match getMatch(@NotNull ObjectId matchId) throws ResourceNotFoundException;

    X01Match addTurn(X01Turn x01Turn) throws IOException;

    X01Match editTurn(@NotNull @Valid X01EditTurn x01EditTurn) throws IOException;

    X01Match deleteLastTurn(@NotNull @Valid X01DeleteLastTurn x01DeleteLastTurn);
}

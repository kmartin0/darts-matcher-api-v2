package nl.kmartin.dartsmatcherapiv2.features.x01.x01match;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01DeleteLastTurn;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01EditTurn;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Turn;
import org.bson.types.ObjectId;

public interface IX01MatchService {
    X01Match createMatch(@Valid @NotNull X01Match x01Match);

    X01Match getMatch(@NotNull ObjectId matchId) throws ResourceNotFoundException;

    X01Match addTurn(X01Turn x01Turn);

    X01Match editTurn(@NotNull @Valid X01EditTurn x01EditTurn);

    X01Match deleteLastTurn(@NotNull @Valid X01DeleteLastTurn x01DeleteLastTurn);
}

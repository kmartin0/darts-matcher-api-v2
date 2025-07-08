package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01EditTurn;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Turn;
import org.bson.types.ObjectId;

public interface IX01MatchService {
    X01Match createMatch(@NotNull @Valid X01Match match);

    X01Match getMatch(@NotNull ObjectId matchId) throws ResourceNotFoundException;

    void checkMatchExists(ObjectId matchId);

    X01Match addTurn(@NotNull ObjectId matchId, @NotNull @Valid X01Turn turn);

    X01Match editTurn(@NotNull ObjectId matchId, @NotNull @Valid X01EditTurn editTurn);

    X01Match deleteLastTurn(@NotNull ObjectId matchId);

    void deleteMatch(ObjectId matchId);

    X01Match resetMatch(ObjectId matchId);

    X01Match reprocessMatch(ObjectId matchId);
}

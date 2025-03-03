package nl.kmartin.dartsmatcherapiv2.features.x01.x01match;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;

public interface IX01MatchService {
    X01Match createMatch(@Valid X01Match x01Match);
}

package nl.kmartin.dartsmatcherapiv2.features.x01.x01matchsetup;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;

public interface IX01MatchSetupService {
    void setupMatch(@Valid X01Match match);
}

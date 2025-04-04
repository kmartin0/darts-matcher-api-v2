package nl.kmartin.dartsmatcherapiv2.features.dartboard;

import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.Dart;

public interface IDartboardService {
    Dart getScore(Dart target, double offsetR, double offsetTheta);
}

package nl.kmartin.dartsmatcherapiv2.features.x01.x01set;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01SetEntry;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01SetService {
    X01SetEntry createNewSet(int setNumber, List<X01MatchPlayer> players);

    ObjectId calcThrowsFirstInSet(int setNumber, List<X01MatchPlayer> players);
}

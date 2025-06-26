package nl.kmartin.dartsmatcherapiv2.features.x01.x01set;

import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01ValidationUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.TreeMap;

@Service
@Primary
public class X01SetServiceImpl implements IX01SetService {

    /**
     * Creates a new set with the correct starting player.
     *
     * @param setNumber int the sets number
     * @param players   {@link List<X01MatchPlayer>} the list of match players
     * @return {@link X01Set} the created set
     */
    @Override
    public X01Set createNewSet(int setNumber, List<X01MatchPlayer> players) {
        ObjectId throwsFirstInSet = calcThrowsFirstInSet(setNumber, players);
        return new X01Set(setNumber, new TreeMap<>(), throwsFirstInSet, null);
    }

    /**
     * Determines who throws first in a set
     *
     * @param setNumber int the number of the set
     * @param players   {@link List<X01MatchPlayer>} the list of match players
     * @return {@link ObjectId} the player who throws first in the set
     */
    @Override
    public ObjectId calcThrowsFirstInSet(int setNumber, List<X01MatchPlayer> players) {
        if (X01ValidationUtils.isPlayersEmpty(players))
            throw new IllegalArgumentException("Cannot calculate first thrower from a null or empty player list.");

        // Calculate the index of the player that starts the set
        int numOfPlayers = players.size();
        int throwsFirstIndex = (setNumber - 1) % numOfPlayers;

        // Get the first thrower for this set
        return players.get(throwsFirstIndex).getPlayerId();
    }

}

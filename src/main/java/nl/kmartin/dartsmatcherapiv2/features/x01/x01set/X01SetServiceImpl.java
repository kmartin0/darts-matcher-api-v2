package nl.kmartin.dartsmatcherapiv2.features.x01.x01set;

import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01ValidationUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01SetEntry;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.TreeMap;

@Service
public class X01SetServiceImpl implements IX01SetService {

    /**
     * Creates a new set with the correct starting player.
     *
     * @param setNumber int the sets number
     * @param players   {@link List<X01MatchPlayer>} the list of match players
     * @return {@link X01SetEntry} the created set
     */
    @Override
    public X01SetEntry createNewSet(int setNumber, List<X01MatchPlayer> players) {
        ObjectId throwsFirstInSet = calcThrowsFirstInSet(setNumber, players);
        X01Set newSet = new X01Set(new TreeMap<>(), throwsFirstInSet, null);
        return new X01SetEntry(setNumber, newSet);
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

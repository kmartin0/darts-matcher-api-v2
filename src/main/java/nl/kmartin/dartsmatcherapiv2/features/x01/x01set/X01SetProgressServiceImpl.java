package nl.kmartin.dartsmatcherapiv2.features.x01.x01set;

import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01ValidationUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegEntry;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegService;
import nl.kmartin.dartsmatcherapiv2.utils.NumberUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class X01SetProgressServiceImpl implements IX01SetProgressService {

    private final IX01LegService legService;
    private final IX01LegProgressService legProgressService;

    public X01SetProgressServiceImpl(IX01LegService legService, IX01LegProgressService legProgressService) {
        this.legService = legService;
        this.legProgressService = legProgressService;
    }

    /**
     * Find a leg in a set.
     *
     * @param set       {@link X01Set} the set
     * @param legNumber int the leg number that needs to be found
     * @return {@link Optional<X01LegEntry>} the matching leg, empty if no leg is found
     */
    @Override
    public Optional<X01LegEntry> getLeg(X01Set set, int legNumber, boolean throwIfNotFound) {
        ResourceNotFoundException notFoundException = new ResourceNotFoundException(X01Leg.class, legNumber);

        if (X01ValidationUtils.isLegsEmpty(set) || legNumber < 1) {
            if (throwIfNotFound) throw notFoundException;
            else return Optional.empty();
        }

        // Find the first leg in the leg list matching the leg number.
        Optional<X01LegEntry> legEntry = Optional.ofNullable(set.getLegs().get(legNumber))
                .map(leg -> new X01LegEntry(legNumber, leg));

        if (throwIfNotFound && legEntry.isEmpty()) throw notFoundException;

        return legEntry;
    }

    /**
     * Finds the lowest-numbered leg which does not have a winner from a set.
     *
     * @param set {@link X01Set} the set to check
     * @return {@link Optional<X01LegEntry>} empty when all legs have winners. otherwise the lowest leg without winner.
     */
    @Override
    public Optional<X01LegEntry> getCurrentLeg(X01Set set) {
        if (X01ValidationUtils.isLegsEmpty(set)) return Optional.empty();

        return set.getLegs().entrySet().stream()
                .filter(legEntry -> legEntry.getValue().getWinner() == null) // get legs without result
                .findFirst() // Get the lowest numbered leg
                .map(X01LegEntry::new); // Map it to X01LegEntry
    }

    /**
     * Creates the next leg for a list of legs but doesn't exceed the maximum number of legs.
     *
     * @param set              {@link X01Set} the set in which a leg has to be added to.
     * @param players          {@link List<X01MatchPlayer>} the match players.
     * @param bestOfLegs       int the maximum number of legs.
     * @param throwsFirstInSet {@link ObjectId} the player that throws first in the set.
     * @return {@link Optional<X01LegEntry>} the created leg, empty when the maximum number of legs was reached.
     */
    @Override
    public Optional<X01LegEntry> createNextLeg(X01Set set, List<X01MatchPlayer> players, int bestOfLegs, ObjectId throwsFirstInSet) {
        if (set == null) return Optional.empty();

        // Get existing leg numbers
        Set<Integer> existingLetNumbers = getLegNumbers(set);

        // Find the next available leg number (ensure it doesn't exceed the best of legs)
        int nextLegNumber = NumberUtils.findNextNumber(existingLetNumbers, bestOfLegs);
        if (nextLegNumber == -1) return Optional.empty();

        // Create and add the next leg to the legs.
        X01LegEntry newLegEntry = legService.createNewLeg(nextLegNumber, throwsFirstInSet, players);
        set.getLegs().put(newLegEntry.legNumber(), newLegEntry.leg());
        return Optional.of(newLegEntry);
    }

    /**
     * Collects the unique leg numbers from a set
     *
     * @param set {@link X01Set} the set
     * @return {@link Set<Integer>} the leg numbers
     */
    @Override
    public Set<Integer> getLegNumbers(X01Set set) {
        if (X01ValidationUtils.isLegsEmpty(set)) return Collections.emptySet();

        // Map the leg numbers and collect to an integer set
        return set.getLegs().keySet();
    }

    /**
     * Determines if a set is concluded by checking if all players have a result
     *
     * @param set     {@link X01Set} the set that needs to be checked
     * @param players {@link List<X01MatchPlayer>} the list of match players
     * @return boolean if the set is concluded
     */
    @Override
    public boolean isSetConcluded(X01Set set, List<X01MatchPlayer> players) {
        if (set == null || set.getResult() == null || X01ValidationUtils.isPlayersEmpty(players)) return false;

        return players.stream().allMatch(player -> set.getResult().get(player.getPlayerId()) != null);
    }

    /**
     * Removes the last score from a set.
     * While traversing in reverse order also cleans up empty rounds and legs, up to the removed score.
     *
     * @param set {@link X01Set} the set from which to remove the last score
     * @return true if a score was successfully removed; false otherwise
     */
    @Override
    public boolean removeLastScoreFromSet(X01Set set) {
        Iterator<Integer> reverseLegsIterator = set.getLegs().descendingKeySet().iterator();
        while (reverseLegsIterator.hasNext()) {
            X01Leg leg = set.getLegs().get(reverseLegsIterator.next());
            boolean scoreRemoved = legProgressService.removeLastScoreFromLeg(leg);

            if (leg.getRounds().isEmpty()) reverseLegsIterator.remove();
            if (scoreRemoved) return true;
        }

        return false;
    }

}

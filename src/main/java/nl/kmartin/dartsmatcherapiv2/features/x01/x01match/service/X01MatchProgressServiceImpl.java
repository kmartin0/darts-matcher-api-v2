package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01ValidationUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetService;
import nl.kmartin.dartsmatcherapiv2.utils.NumberUtils;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

@Service
public class X01MatchProgressServiceImpl implements IX01MatchProgressService {

    private final IX01SetService setService;
    private final IX01SetProgressService setProgressService;
    private final IX01LegProgressService legProgressService;
    private final IX01LegRoundService legRoundService;

    public X01MatchProgressServiceImpl(IX01SetService setService, IX01SetProgressService setProgressService,
                                       IX01LegProgressService legProgressService, IX01LegRoundService legRoundService) {
        this.setService = setService;
        this.setProgressService = setProgressService;
        this.legProgressService = legProgressService;
        this.legRoundService = legRoundService;
    }

    /**
     * Find a set number in a match.
     *
     * @param match     The match containing the sets
     * @param setNumber int the set number that needs to be found
     * @return {@link Optional<X01SetEntry>} the matching set, empty if no set is found
     */
    @Override
    public Optional<X01SetEntry> getSet(X01Match match, int setNumber, boolean throwIfNotFound) {
        ResourceNotFoundException notFoundException = new ResourceNotFoundException(X01Set.class, setNumber);

        if (X01ValidationUtils.isSetsEmpty(match) || setNumber < 1) {
            if (throwIfNotFound) throw notFoundException;
            else return Optional.empty();
        }

        // Find the first set matching the set number
        Optional<X01SetEntry> setEntry = Optional.ofNullable(match.getSets().get(setNumber))
                .map(set -> new X01SetEntry(setNumber, set));

        if (throwIfNotFound && setEntry.isEmpty()) throw notFoundException;

        return setEntry;
    }

    /**
     * Finds the lowest-numbered set in a match which does not have a result.
     *
     * @param match {@link X01Match} the match to get the current set from
     * @return {@link Optional<X01SetEntry>} empty when all sets have a result. otherwise the lowest set without a result.
     */
    @Override
    public Optional<X01SetEntry> getCurrentSet(X01Match match) {
        if (X01ValidationUtils.isSetsEmpty(match)) return Optional.empty();

        return match.getSets().entrySet().stream()
                .filter(e -> e.getValue().getResult() == null || e.getValue().getResult().isEmpty()) // Set without result
                .findFirst()// Get the lowest numbered set
                .map(X01SetEntry::new);
    }

    /**
     * Creates the next set in a match but doesn't exceed the maximum number of sets.
     *
     * @param match {@link X01Match} the list of sets a set has to be added to.
     * @return {@link Optional<X01SetEntry>} the created set, empty when the maximum number of sets was reached.
     */
    @Override
    public Optional<X01SetEntry> createNextSet(X01Match match) {
        if (match == null) return Optional.empty();

        // Get existing set numbers
        Set<Integer> existingSetNumbers = getSetNumbers(match);

        // Find the next available set number (ensure it doesn't exceed the best of sets)
        int nextSetNumber = NumberUtils.findNextNumber(existingSetNumbers, match.getMatchSettings().getBestOf().getSets());
        if (nextSetNumber == -1) return Optional.empty();

        // Create and add the next set to the sets.
        X01SetEntry newSetEntry = setService.createNewSet(nextSetNumber, match.getPlayers());
        match.getSets().put(newSetEntry.setNumber(), newSetEntry.set());
        return Optional.of(newSetEntry);
    }

    /**
     * Collects the unique set numbers from a match
     *
     * @param match {@link X01Match} the match
     * @return {@link Set<Integer>} the set numbers
     */
    @Override
    public Set<Integer> getSetNumbers(X01Match match) {
        if (X01ValidationUtils.isSetsEmpty(match)) return Collections.emptySet();

        // Map the set numbers and collect to a set of integers
        return match.getSets().keySet();
    }

    /**
     * Finds the current set in play of a match. If the set is not created yet. A new set will be made and added
     * to the match.
     *
     * @param match {@link X01Match} the match for which the current set needs to be determined
     * @return {@link Optional<X01SetEntry>} the current set in play.
     */
    @Override
    public Optional<X01SetEntry> getCurrentSetOrCreate(X01Match match) {
        if (match == null) return Optional.empty();

        // First find the current set in the active list of sets.
        Optional<X01SetEntry> curSet = getCurrentSet(match);

        // If there is no current set and the match isn't concluded, create the next set.
        return curSet.isEmpty() && !isMatchConcluded(match)
                ? createNextSet(match)
                : curSet;
    }

    /**
     * Finds the current leg in play inside a set. If the leg is not created yet. A new leg will be made and
     * added to the current set.
     *
     * @param match      {@link X01Match}  the match for which the current leg needs to be determined
     * @param currentSet {@link X01Set} the current set in play.
     * @return {@link Optional<X01LegEntry>} the current leg in play.
     */
    @Override
    public Optional<X01LegEntry> getCurrentLegOrCreate(X01Match match, X01Set currentSet) {
        if (match == null || currentSet == null) return Optional.empty();

        // First find the current leg in the active list of legs from the current set.
        int bestOfLegs = match.getMatchSettings().getBestOf().getLegs();
        Optional<X01LegEntry> curLegEntry = setProgressService.getCurrentLeg(currentSet);

        // If there is no current leg and the set isn't concluded, create the next leg.
        return curLegEntry.isEmpty() && !setProgressService.isSetConcluded(currentSet, match.getPlayers())
                ? setProgressService.createNextLeg(currentSet, match.getPlayers(), bestOfLegs, currentSet.getThrowsFirst())
                : curLegEntry;
    }

    /**
     * Finds the current leg round in play inside a leg. If the leg round is not created yet.
     * A new leg round will be made and added to the current leg.
     *
     * @param match      {@link X01Match}  the match for which the current leg round needs to be determined
     * @param currentLeg {@link X01Leg} the current leg in play.
     * @return Optional X01LegRound entry for the current leg round in play.
     */
    @Override
    public Optional<X01LegRoundEntry> getCurrentLegRoundOrCreate(X01Match match, X01Leg currentLeg) {
        if (match == null || currentLeg == null) return Optional.empty();

        // First find the current leg round in the active list of rounds from the current leg.
        Optional<X01LegRoundEntry> curLegRoundEntry = legProgressService.getCurrentLegRound(currentLeg, match.getPlayers());

        // If there is no current leg round and the leg isn't concluded, create the next leg round.
        return curLegRoundEntry.isEmpty() && !legProgressService.isLegConcluded(currentLeg)
                ? legProgressService.createNextLegRound(currentLeg)
                : curLegRoundEntry;
    }

    /**
     * Determines if a match is concluded by checking if all players have a match result.
     *
     * @param match {@link X01Match} the match
     * @return boolean if the match is concluded
     */
    @Override
    public boolean isMatchConcluded(X01Match match) {
        if (match == null) return false;

        // When all players have a result the match is concluded
        return match.getPlayers().stream().allMatch(player -> player.getResultType() != null);
    }

    /**
     * Removes the last score from a match.
     * While traversing in reverse order also cleans up empty rounds, legs or sets up to the removed score.
     *
     * @param match {@link X01Match} The match containing the list of sets to remove the last score from.
     */
    @Override
    public void removeLastScoreFromMatch(X01Match match) {
        if (match == null) return;

        // Iterate the sets, legs and rounds in reverse order to remove the last score and empty rounds, legs or sets.
        // Stops after the first removal of a score.
        Iterator<Integer> reverseSetsIterator = match.getSets().descendingKeySet().iterator();
        while (reverseSetsIterator.hasNext()) {
            X01Set set = match.getSets().get(reverseSetsIterator.next());
            boolean scoreRemoved = setProgressService.removeLastScoreFromSet(set);

            if (set.getLegs().isEmpty()) reverseSetsIterator.remove();
            if (scoreRemoved) break;
        }
    }

    /**
     * Update the calculated fields inside the {@link X01MatchProgress} field of a match.
     *
     * @param match {@link X01Match} the match to be updated
     */
    @Override
    public void updateMatchProgress(X01Match match) {
        if (match == null) return;

        // Get the current set, leg and round.
        Optional<X01SetEntry> currentSetEntry = getCurrentSetOrCreate(match);
        Optional<X01LegEntry> currentLegEntry = currentSetEntry.flatMap(setEntry -> getCurrentLegOrCreate(match, setEntry.set()));
        Optional<X01LegRoundEntry> currentLegRoundEntry = currentLegEntry.flatMap(legEntry -> getCurrentLegRoundOrCreate(match, legEntry.leg()));

        // Get the current thrower for the current round
        Optional<ObjectId> throwsFirstInCurrentLeg = currentLegEntry.map(legEntry -> legEntry.leg().getThrowsFirst());
        Optional<ObjectId> currentThrower = currentLegRoundEntry.flatMap(roundEntry ->
                throwsFirstInCurrentLeg.map(throwsFirst ->
                        legRoundService.getCurrentThrowerInRound(roundEntry.round(), throwsFirst, match.getPlayers())
                )
        );

        // Update the match progress with the new state of the match
        match.setMatchProgress(new X01MatchProgress(
                currentSetEntry.map(X01SetEntry::setNumber).orElse(null),
                currentLegEntry.map(X01LegEntry::legNumber).orElse(null),
                currentLegRoundEntry.map(X01LegRoundEntry::roundNumber).orElse(null),
                currentThrower.orElse(null)
        ));
    }

}
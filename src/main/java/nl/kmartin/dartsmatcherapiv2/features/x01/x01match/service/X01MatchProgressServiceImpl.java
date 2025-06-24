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

import java.util.*;
import java.util.stream.Collectors;

@Service
@Primary
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
     * @return {@link Optional<X01Set>} the matching set, empty if no set is found
     */
    @Override
    public Optional<X01Set> getSet(X01Match match, int setNumber, boolean throwIfNotFound) {
        ResourceNotFoundException notFoundException = new ResourceNotFoundException(X01Set.class, setNumber);

        if (X01ValidationUtils.isSetsEmpty(match) || setNumber < 1) {
            if (throwIfNotFound) throw notFoundException;
            else return Optional.empty();
        }

        // Find the first set matching the set number
        Optional<X01Set> set = match.getSets().stream().filter(x01Set -> x01Set.getSet() == setNumber).findFirst();
        if (throwIfNotFound && set.isEmpty()) throw notFoundException;

        return set;
    }

    /**
     * Finds the lowest-numbered set in a match which does not have a result.
     *
     * @param match {@link X01Match} the match to get the current set from
     * @return {@link Optional<X01Set>} empty when all sets have a result. otherwise the lowest set without a result.
     */
    @Override
    public Optional<X01Set> getCurrentSet(X01Match match) {
        if (X01ValidationUtils.isSetsEmpty(match)) return Optional.empty();

        return match.getSets().stream()
                .filter(set -> set.getResult() == null || set.getResult().isEmpty()) // Set without result
                .min(Comparator.comparingInt(X01Set::getSet)); // Get the lowest numbered set
    }

    /**
     * Creates the next set in a match but doesn't exceed the maximum number of sets.
     *
     * @param match {@link X01Match} the list of sets a set has to be added to.
     * @return {@link Optional<X01Set>} the created set, empty when the maximum number of sets was reached.
     */
    @Override
    public Optional<X01Set> createNextSet(X01Match match) {
        if (match == null) return Optional.empty();

        // Get existing set numbers
        Set<Integer> existingSetNumbers = getSetNumbers(match);

        // Find the next available set number (ensure it doesn't exceed the best of sets)
        int nextSetNumber = NumberUtils.findNextNumber(existingSetNumbers, match.getMatchSettings().getBestOf().getSets());
        if (nextSetNumber == -1) return Optional.empty();

        // Create and add the next set to the sets.
        X01Set newSet = setService.createNewSet(nextSetNumber, match.getPlayers());
        match.getSets().add(newSet);
        return Optional.of(newSet);
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

        // Map the set numbers and collect to an set of integers
        return match.getSets().stream()
                .map(X01Set::getSet)
                .collect(Collectors.toSet());
    }

    /**
     * Finds the current set in play of a match. If the set is not created yet. A new set will be made and added
     * to the match.
     *
     * @param match {@link X01Match} the match for which the current set needs to be determined
     * @return {@link Optional<X01Set>} the current set in play.
     */
    @Override
    public Optional<X01Set> getCurrentSetOrCreate(X01Match match) {
        if (match == null) return Optional.empty();

        // First find the current set in the active list of sets.
        Optional<X01Set> curSet = getCurrentSet(match);

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
     * @return {@link Optional<X01Leg>} the current leg in play.
     */
    @Override
    public Optional<X01Leg> getCurrentLegOrCreate(X01Match match, X01Set currentSet) {
        if (match == null || currentSet == null) return Optional.empty();

        // First find the current leg in the active list of legs from the current set.
        int bestOfLegs = match.getMatchSettings().getBestOf().getLegs();
        Optional<X01Leg> curLeg = setProgressService.getCurrentLeg(currentSet);

        // If there is no current leg and the set isn't concluded, create the next leg.
        return curLeg.isEmpty() && !setProgressService.isSetConcluded(currentSet, match.getPlayers())
                ? setProgressService.createNextLeg(currentSet, match.getPlayers(), bestOfLegs, currentSet.getThrowsFirst())
                : curLeg;
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
    public Optional<Map.Entry<Integer, X01LegRound>> getCurrentLegRoundOrCreate(X01Match match, X01Leg currentLeg) {
        if (match == null || currentLeg == null) return Optional.empty();

        // First find the current leg round in the active list of rounds from the current leg.
        Optional<Map.Entry<Integer, X01LegRound>> curLegRound = legProgressService.getCurrentLegRound(currentLeg, match.getPlayers());

        // If there is no current leg round and the leg isn't concluded, create the next leg round.
        return curLegRound.isEmpty() && !legProgressService.isLegConcluded(currentLeg)
                ? legProgressService.createNextLegRound(currentLeg)
                : curLegRound;
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
     * Removes the most last added score from a list of sets.
     * While traversing also cleans up empty rounds, legs or sets.
     *
     * @param match {@link X01Match} The match containing the list of sets to remove the last score from.
     */
    @Override
    public void deleteLastScore(X01Match match) {
        if (match == null) return;

        List<X01Set> setsReverse = new ArrayList<>(match.getSets());
        Collections.reverse(setsReverse);

        // Iterate the sets, legs and rounds in reverse order to remove the last score and empty rounds, legs or sets.
        // Stops after the first removal of a score.
        outer:
        for (X01Set set : setsReverse) {
            List<X01Leg> legsReverse = new ArrayList<>(set.getLegs());
            Collections.reverse(legsReverse);

            for (X01Leg leg : legsReverse) {
                List<Integer> reverseRoundKeys = new ArrayList<>(leg.getRounds().descendingKeySet());

                for (Integer roundKey : reverseRoundKeys) {
                    X01LegRound round = leg.getRounds().get(roundKey);
                    boolean removed = legRoundService.removeLastScoreFromRound(round);
                    if (round.getScores().isEmpty()) leg.getRounds().remove(roundKey);
                    if (leg.getRounds().isEmpty()) set.getLegs().remove(leg);
                    if (set.getLegs().isEmpty()) match.getSets().remove(set);
                    if (removed) break outer;
                }
            }
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

        // Get the current set/leg/round
        X01Set currentSet = getCurrentSetOrCreate(match).orElse(null);
        X01Leg currentLeg = getCurrentLegOrCreate(match, currentSet).orElse(null);

        Map.Entry<Integer, X01LegRound> currentLegRoundEntry = getCurrentLegRoundOrCreate(match, currentLeg).orElse(null);
        X01LegRound currentLegRound = currentLegRoundEntry != null ? currentLegRoundEntry.getValue() : null;

        // Get the current thrower for the current round
        ObjectId throwsFirstInCurrentLeg = currentLeg != null ? currentLeg.getThrowsFirst() : null;
        ObjectId currentThrower = legRoundService.getCurrentThrowerInRound(currentLegRound, throwsFirstInCurrentLeg, match.getPlayers());

        // Update the match progress with the new state of the match
        match.setMatchProgress(new X01MatchProgress(
                currentSet != null ? currentSet.getSet() : null,
                currentLeg != null ? currentLeg.getLeg() : null,
                currentLegRoundEntry != null ? currentLegRoundEntry.getKey() : null,
                currentThrower
        ));
    }

}
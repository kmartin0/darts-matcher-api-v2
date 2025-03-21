package nl.kmartin.dartsmatcherapiv2.features.x01.x01set;

import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegService;
import nl.kmartin.dartsmatcherapiv2.utils.NumberUtils;
import nl.kmartin.dartsmatcherapiv2.utils.StandingsUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class X01SetServiceImpl implements IX01SetService {

    private final IX01LegService legService;

    public X01SetServiceImpl(IX01LegService legService) {
        this.legService = legService;
    }

    /**
     * Finds the lowest-numbered set which does not have a result.
     *
     * @param sets {@link List<X01Set>} the list of sets to check
     * @return {@link Optional<X01Set>} empty when all sets have a result. otherwise the lowest set without a result.
     */
    @Override
    public Optional<X01Set> getCurrentSet(List<X01Set> sets) {
        if (sets == null) return Optional.empty();

        return sets.stream()
                .filter(set -> set.getResult() == null || set.getResult().isEmpty()) // Set without result
                .min(Comparator.comparingInt(X01Set::getSet)); // Get the lowest numbered set
    }

    /**
     * Creates the next sets for a list of sets but doesn't exceed the maximum number of sets.
     *
     * @param sets         {@link List<X01Set>} the list of sets a set has to be added to.
     * @param matchPlayers {@link List<X01MatchPlayer>} the match players.
     * @param bestOfSets   int the maximum number of sets.
     * @return {@link Optional<X01Set>} the created set, empty when the maximum number of sets was reached.
     */
    @Override
    public Optional<X01Set> createNextSet(List<X01Set> sets, List<X01MatchPlayer> matchPlayers, int bestOfSets) {
        if (sets == null) return Optional.empty();

        // Get existing set numbers
        Set<Integer> existingSetNumbers = getSetNumbers(sets);

        // Find the next available set number (ensure it doesn't exceed the best of sets)
        int nextSetNumber = NumberUtils.findNextNumber(existingSetNumbers, bestOfSets);
        if (nextSetNumber == -1) return Optional.empty();

        // Create and add the next set to the sets.
        X01Set newSet = createNewSet(nextSetNumber, matchPlayers);
        sets.add(newSet);
        return Optional.of(newSet);
    }

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
        return new X01Set(setNumber, new ArrayList<>(), throwsFirstInSet, null);
    }

    /**
     * Collects the unique set numbers from a list of sets
     *
     * @param sets {@link List<X01Set>} the list of sets
     * @return {@link Set<Integer>} the set numbers
     */
    @Override
    public Set<Integer> getSetNumbers(List<X01Set> sets) {
        if (sets == null) return null;

        // Map the set numbers and collect to an set of integers
        return sets.stream()
                .map(X01Set::getSet)
                .collect(Collectors.toSet());
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
        if (players == null) return null;

        // Calculate the index of the player that starts the set
        int numOfPlayers = players.size();
        int throwsFirstIndex = (setNumber - 1) % numOfPlayers;

        // Get the first thrower for this set
        return players.get(throwsFirstIndex).getPlayerId();
    }

    /**
     * Determines if a set is concluded by checking if all players have a result
     *
     * @param x01Set  {@link X01Set} the set that needs to be checked
     * @param players {@link List<X01MatchPlayer>} the list of match players
     * @return boolean if the set is concluded
     */
    @Override
    public boolean isSetConcluded(X01Set x01Set, List<X01MatchPlayer> players) {
        if (x01Set == null || x01Set.getResult() == null) return false;

        return players.stream().allMatch(player -> x01Set.getResult().get(player.getPlayerId()) != null);
    }

    /**
     * Determines the number of legs each player has won for a given set
     *
     * @param set     {@link X01Set} the set for which standings need to be calculated
     * @param players {@link List<X01MatchPlayer>} the list of match players
     * @return Map<ObjectId, Long> containing the number of legs each player has won
     */
    @Override
    public Map<ObjectId, Long> getSetStandings(X01Set set, List<X01MatchPlayer> players) {
        if (set == null || players == null) return null;

        // Initialize standings map with all players and 0 wins
        Map<ObjectId, Long> standings = players.stream()
                .collect(Collectors.toMap(MatchPlayer::getPlayerId, player -> 0L));

        // Update the map with the number of wins from the legs for each player
        set.getLegs().stream()
                .filter(x01Leg -> x01Leg.getWinner() != null)  // Filter out legs with no winner
                .forEach(x01Leg -> standings.merge(x01Leg.getWinner(), 1L, Long::sum)); // Increment win count for the winner

        // Return the standings map
        return standings;
    }

    /**
     * Determine the number of legs that are yet to be played in a set
     *
     * @param bestOfLegs int the maximum number of legs going to be played
     * @param x01Set     {@link X01Set} the set for which the remaining legs need to be determined
     * @return int the number of legs can still be played
     */
    @Override
    public int calcRemainingLegs(int bestOfLegs, X01Set x01Set) {
        // If the set or legs don't exist, the remaining legs are the maximum number of legs to be played
        if (x01Set == null || x01Set.getLegs() == null) return bestOfLegs;

        // Count the number of completed legs (legs with a winner)
        long completedLegs = x01Set.getLegs().stream()
                .filter(legService::isLegConcluded)
                .count();

        // Determine the number of remaining legs and return them. Ensuring they are not negative.
        int remainingLegs = bestOfLegs - (int) completedLegs;
        return Math.max(remainingLegs, 0);
    }

    /**
     * Updates the set result for all sets in a list of sets.
     *
     * @param sets       {@link List<X01Set>} the list of sets that needs to be updated
     * @param players    {@link List<X01MatchPlayer>} the list of match players
     * @param bestOfLegs int the best of setting for the legs
     * @param x01        int the x01 setting for the legs
     */
    @Override
    public void updateSetResults(List<X01Set> sets, List<X01MatchPlayer> players, int bestOfLegs, int x01) {
        if (sets == null || players == null) return;

        // For each set update the leg results and after update the set result
        sets.forEach(x01Set -> {
            legService.updateLegResults(x01Set.getLegs(), players, x01);
            updateSetResult(x01Set, bestOfLegs, players);
        });
    }

    /**
     * Updates the set result for a set.
     *
     * @param x01Set     {@link X01Set} the set to be updated
     * @param bestOfLegs int the best of setting for the legs
     * @param players    {@link List<X01MatchPlayer>} the list of match players
     */
    @Override
    public void updateSetResult(X01Set x01Set, int bestOfLegs, List<X01MatchPlayer> players) {
        if (x01Set == null || players == null) return;

        // Get the player(s) that have won the set
        List<ObjectId> setWinners = getSetWinners(x01Set, bestOfLegs, players);

        if (!setWinners.isEmpty()) { // The set has a result
            // If multiple players have won the set, that means they have drawn.
            ResultType winOrDrawType = setWinners.size() > 1 ? ResultType.DRAW : ResultType.WIN;

            // Map and set the player results in the set. Players in the setWinners list get a win/draw. The rest gets a loss
            x01Set.setResult(players.stream()
                    .collect(Collectors.toMap(
                            X01MatchPlayer::getPlayerId,
                            player -> setWinners.contains(player.getPlayerId()) ? winOrDrawType : ResultType.LOSS
                    ))
            );
        } else { // The set has no result
            x01Set.setResult(null);
        }
    }

    /**
     * Find a set number in a list of sets.
     *
     * @param sets      {@link List<X01Set>} the list of legs}
     * @param setNumber int the set number that needs to be found
     * @return {@link Optional<X01Set>} the matching set, empty if no set is found
     */
    public Optional<X01Set> getSet(List<X01Set> sets, int setNumber, boolean throwIfNotFound) {
        ResourceNotFoundException notFoundException = new ResourceNotFoundException(X01Set.class, setNumber);

        if (sets == null || setNumber < 1) {
            if (throwIfNotFound) throw notFoundException;
            else return Optional.empty();
        }

        Optional<X01Set> set = sets.stream().filter(x01Set -> x01Set.getSet() == setNumber).findFirst();

        if (throwIfNotFound && set.isEmpty()) throw notFoundException;

        return set;
    }

    /**
     * Deletes a specific set from the list of sets in the match.
     * If the set exists in the list, it will be removed.
     *
     * @param sets      {@link List<X01Set>} The list of sets in the match.
     * @param setNumber int The number of the set to be deleted.
     */
    public void deleteSet(List<X01Set> sets, int setNumber) {
        if (sets == null) return;

        // Find the set in the list by its number.
        Optional<X01Set> setToDelete = getSet(sets, setNumber, true);

        // If the set is found, remove it from the list.
        setToDelete.ifPresent(sets::remove);
    }

    /**
     * A list of object ids is created containing all players that have won the set. Multiple set winners
     * means a draw has occurred.
     *
     * @param x01Set     {@link X01Set} The set for which the winners are being determined.
     * @param bestOfLegs int the best of setting for the legs
     * @param players    {@link List<X01MatchPlayer>} the list of match players
     * @return {@link List<ObjectId>} containing the IDs of players who won the set. multiple winners indicates a draw.
     */
    private List<ObjectId> getSetWinners(X01Set x01Set, int bestOfLegs, List<X01MatchPlayer> players) {
        // Get the standings for the set.
        Map<ObjectId, Long> setStandings = getSetStandings(x01Set, players);

        // Get the player(s) that have won the set
        int remainingLegs = calcRemainingLegs(bestOfLegs, x01Set);
        return StandingsUtils.determineWinners(setStandings, remainingLegs);
    }
}
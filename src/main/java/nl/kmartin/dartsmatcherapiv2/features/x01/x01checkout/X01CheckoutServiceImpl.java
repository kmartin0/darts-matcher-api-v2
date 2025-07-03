package nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kmartin.dartsmatcherapiv2.common.MessageKeys;
import nl.kmartin.dartsmatcherapiv2.common.MessageResolver;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.TargetError;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Checkout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class X01CheckoutServiceImpl implements IX01CheckoutService {

    private final MessageResolver messageResolver;
    private final Map<Integer, X01Checkout> checkoutsMap;

    public X01CheckoutServiceImpl(@Value("classpath:data/checkouts.json") Resource checkoutsResourceFile,
                                  MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
        this.checkoutsMap = createCheckoutMap(checkoutsResourceFile);
    }

    /**
     * Loads and parses X01 checkout data from a JSON resource file into an unmodifiable Map.
     *
     * @param checkoutsResourceFile {@link Resource}
     * @return {@code Map<Integer,X01Checkout>} an unmodifiable map where the key is the checkout score and the value is the checkout
     * @throws IllegalStateException if the checkout map is empty or an io error occurred.
     */
    public static Map<Integer, X01Checkout> createCheckoutMap(Resource checkoutsResourceFile) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Read the JSON file to a list of X01Checkout objects
            List<X01Checkout> checkoutsList = mapper.readValue(
                    checkoutsResourceFile.getInputStream(),
                    new TypeReference<>() {
                    }
            );

            if (checkoutsList == null || checkoutsList.isEmpty()) {
                throw new IllegalStateException("Checkouts not found or empty");
            }

            return checkoutsList.stream().collect(Collectors.toUnmodifiableMap(X01Checkout::getCheckout, Function.identity()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize X01CheckoutService due to IO error", e);
        }
    }

    /**
     * Return the checkouts map
     *
     * @return Map<Integer, X01Checkout> map of all available x01 checkouts.
     */
    @Override
    public Map<Integer, X01Checkout> getCheckouts() {
        return checkoutsMap;
    }

    /**
     * Returns the checkouts in a list
     *
     * @return List<X01Checkout> list of all available x01 checkouts.
     */
    @Override
    public List<X01Checkout> getCheckoutsAsList() {
        return checkoutsMap.values().stream().toList();
    }

    /**
     * Retrieves the checkout information based on the remaining score in a x01 match.
     *
     * @param remaining int The value to search for in the checkouts.
     * @return Optional<X01Checkout> containing the matching checkout. if no checkout available an empty Optional.
     */
    @Override
    public Optional<X01Checkout> getCheckout(int remaining) {
        return Optional.ofNullable(checkoutsMap.get(remaining));
    }


    /**
     * Determine if a score could be a checkout
     *
     * @param score int the score that needs to be verified
     * @return boolean whether the score could be a checkout
     */
    @Override
    public boolean isScoreCheckout(int score) {
        Set<Integer> invalidCheckouts = new HashSet<>(Arrays.asList(169, 168, 166, 165, 163, 162, 159));

        return score <= 170 && !invalidCheckouts.contains(score);
    }

    /**
     * Determine if a score could be a checkout, will also check if the minimum darts needed for the checkout have been used
     *
     * @param score     int the score that needs to be verified
     * @param dartsUsed int the darts used to reach the score
     * @return boolean whether the score could be a checkout and if the minimum darts needed were used
     */
    @Override
    public boolean isScoreCheckout(int score, int dartsUsed) {
        if (!isScoreCheckout(score)) return false;
        Optional<X01Checkout> checkout = getCheckout(score);

        if (checkout.isEmpty()) return false;

        if (!isEnoughDartsUsedForCheckout(checkout.get(), dartsUsed)) {
            throw new InvalidArgumentsException(
                    new TargetError(
                            "dartsUsed",
                            messageResolver.getMessage(MessageKeys.MESSAGE_IMPOSSIBLE_CHECKOUT_MIN_DARTS, score, dartsUsed)
                    )
            );
        }

        return true;
    }

    /**
     * Checks if the remaining score is either zero (successful checkout) or a bust (invalid score).
     *
     * @param remaining the remaining score
     * @return true if the remaining score is zero or a bust, false otherwise
     */
    @Override
    public boolean isRemainingZeroOrBust(int remaining) {
        return isRemainingZero(remaining) || isRemainingBust(remaining);
    }

    /**
     * Determines if the remaining score is a bust. A bust occurs when the score is less than 2 but not exactly zero.
     *
     * @param remaining the remaining score
     * @return true if the remaining score is a bust, false otherwise
     */
    @Override
    public boolean isRemainingBust(int remaining) {
        return remaining < 2 && remaining != 0;
    }

    /**
     * Checks if the remaining score is exactly zero. This indicates a successful checkout.
     *
     * @param remaining the remaining score
     * @return true if the remaining score is zero, false otherwise
     */
    @Override
    public boolean isRemainingZero(int remaining) {
        return remaining == 0;
    }

    /**
     * Determines if the checkout is valid.
     * A valid checkout occurs when the remaining score is zero and the last dart lands in a double area.
     *
     * @param remaining the remaining score
     * @param lastDart  the last dart thrown
     * @return true if the checkout is valid, false otherwise
     */
    @Override
    public boolean isValidCheckout(int remaining, Dart lastDart) {
        DartboardSectionArea lastDartArea = lastDart.getArea();

        return isRemainingZero(remaining) && lastDartArea.isDouble();
    }

    /**
     * Checks if the number of darts used meets or exceeds the minimum required for a checkout.
     *
     * @param checkout  the X01 checkout instance
     * @param dartsUsed the number of darts used
     * @return true if the minimum darts requirement is met, false otherwise
     */
    private boolean isEnoughDartsUsedForCheckout(X01Checkout checkout, int dartsUsed) {
        return checkout != null && dartsUsed >= checkout.getMinDarts();
    }
}
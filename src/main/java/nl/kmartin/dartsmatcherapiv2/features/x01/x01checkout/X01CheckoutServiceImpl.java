package nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.TargetError;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Checkout;
import nl.kmartin.dartsmatcherapiv2.utils.MessageKeys;
import nl.kmartin.dartsmatcherapiv2.utils.MessageResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class X01CheckoutServiceImpl implements IX01CheckoutService {

    @Value("classpath:data/checkouts.json")
    private Resource checkoutsResourceFile;

    private final MessageResolver messageResolver;

    public X01CheckoutServiceImpl(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    /**
     * Reads all checkouts from a JSON file and maps it to an ArrayList of type X01Checkout.
     *
     * @return ArrayList<X01Checkout> list of all available x01 checkouts.
     * @throws IOException If there's an issue reading the file.
     */
    @Override
    public ArrayList<X01Checkout> getCheckouts() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Read the JSON file to an ArrayList of X01Checkout objects
        ArrayList<X01Checkout> checkouts = mapper.readValue(
                checkoutsResourceFile.getInputStream(),
                new TypeReference<>() {
                });

        // If for some reason the checkouts is empty throw a RuntimeException as this should not happen.
        if (checkouts == null || checkouts.isEmpty()) {
            throw new RuntimeException("Checkouts not found");
        }

        return checkouts;
    }

    /**
     * Retrieves the checkout information based on the remaining score in a x01 match.
     *
     * @param remaining int The value to search for in the checkouts.
     * @return Optional<X01Checkout> containing the matching checkout. if no checkout available an empty Optional.
     * @throws IOException If there's an issue reading the file.
     */
    @Override
    public Optional<X01Checkout> getCheckout(int remaining) throws IOException {
        return getCheckouts().stream()
                .filter(x01Checkout -> x01Checkout.getCheckout() == remaining)
                .findFirst();
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
     * @throws IOException If there's an issue reading the checkout file.
     */
    @Override
    public boolean isScoreCheckout(int score, int dartsUsed) throws IOException {
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
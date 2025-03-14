package nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.TargetError;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Checkout;
import nl.kmartin.dartsmatcherapiv2.utils.MessageKeys;
import nl.kmartin.dartsmatcherapiv2.utils.MessageResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
     * @throws IOException               If there's an issue reading the file.
     */
    @Override
    public X01Checkout getCheckout(int remaining) throws IOException {
        return getCheckouts().stream()
                .filter(x01Checkout -> x01Checkout.getCheckout() == remaining)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(X01Checkout.class, remaining));
    }

    @Override
    public boolean isValidCheckout(int score) {
        Set<Integer> invalidCheckouts = new HashSet<>(Arrays.asList(169, 168, 166, 165, 163, 162));

        return score <= 170 && !invalidCheckouts.contains(score);
    }

    @Override
    public boolean isValidCheckout(int score, int dartsUsed) throws IOException {
        if (!isValidCheckout(score)) return false;

        try {
            if (!validDartsUsed(getCheckout(score), dartsUsed)) {
                throw new InvalidArgumentsException(
                        new TargetError(
                                "dartsUsed",
                                messageResolver.getMessage(MessageKeys.MESSAGE_IMPOSSIBLE_CHECKOUT_MIN_DARTS, score, dartsUsed)
                        )
                );
            }

            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private boolean validDartsUsed(X01Checkout checkout, int dartsUsed) {
        return checkout != null && dartsUsed >= checkout.getMinDarts();
    }
}

package nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Checkout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;

@Service
public class X01CheckoutServiceImpl implements IX01CheckoutService {

    @Value("classpath:data/checkouts.json")
    private Resource checkoutsResourceFile;

    /**
     * Reads all checkouts from a JSON file and maps it to an ArrayList of type X01Checkout.
     *
     * @return ArrayList<X01Checkout> list of all available x01 checkouts.
     * @throws IOException If there's an issue reading the file.
     */
    @Override
    public ArrayList<X01Checkout> getCheckouts() throws IOException, RuntimeException {
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
     * @throws ResourceNotFoundException If the remaining score doesn't have a checkout.
     */
    @Override
    public X01Checkout getCheckout(int remaining) throws IOException, ResourceNotFoundException {
        return getCheckouts().stream()
                .filter(x01Checkout -> x01Checkout.getCheckout() == remaining)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(X01Checkout.class, remaining));
    }
}

package nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Checkout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public ArrayList<X01Checkout> getCheckouts() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(
                checkoutsResourceFile.getInputStream(),
                new TypeReference<>() {
                }
        );
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
        List<X01Checkout> checkouts = getCheckouts();

        if (checkouts == null || checkouts.isEmpty()) return Optional.empty();

        return getCheckouts().stream()
                .filter(x01Checkout -> x01Checkout.getCheckout() == remaining)
                .findFirst();
    }
}

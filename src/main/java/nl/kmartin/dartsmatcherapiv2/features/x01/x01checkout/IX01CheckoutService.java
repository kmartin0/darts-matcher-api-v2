package nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Checkout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public interface IX01CheckoutService {
    ArrayList<X01Checkout> getCheckouts() throws IOException;

    Optional<X01Checkout> getCheckout(int remaining) throws IOException;
}

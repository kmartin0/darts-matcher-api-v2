package nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Checkout;

import java.io.IOException;
import java.util.ArrayList;

public interface IX01CheckoutService {
    ArrayList<X01Checkout> getCheckouts() throws IOException;

    X01Checkout getCheckout(int remaining) throws IOException;

    boolean isValidCheckout(int score);

    boolean isValidCheckout(int score, int dartsUsed) throws IOException;
}

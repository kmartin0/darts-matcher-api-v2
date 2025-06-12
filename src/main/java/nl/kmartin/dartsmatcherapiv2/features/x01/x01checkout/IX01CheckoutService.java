package nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout;

import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Checkout;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IX01CheckoutService {
    Map<Integer, X01Checkout> getCheckouts();

    List<X01Checkout> getCheckoutsAsList();

    Optional<X01Checkout> getCheckout(int remaining);

    boolean isScoreCheckout(int score);

    boolean isScoreCheckout(int score, int dartsUsed);

    boolean isValidCheckout(int remaining, Dart lastDart);

    boolean isRemainingZeroOrBust(int remaining);

    boolean isRemainingBust(int remaining);

    boolean isRemainingZero(int remaining);
}
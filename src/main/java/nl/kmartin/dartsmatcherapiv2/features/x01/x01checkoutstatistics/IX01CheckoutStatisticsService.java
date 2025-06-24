package nl.kmartin.dartsmatcherapiv2.features.x01.x01checkoutstatistics;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01CheckoutStatistics;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;

public interface IX01CheckoutStatisticsService {
    void updateCheckoutStatistics(X01CheckoutStatistics playerCheckoutStats, X01LegRoundScore playerScore, boolean isCheckout, boolean trackDoubles);
}

package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01CheckoutStatistics {
    private int checkoutHighest;
    private int checkoutTonPlus;
    private int checkoutPercentage;
    private int checkoutsMissed;
    private int checkoutsHit;
}

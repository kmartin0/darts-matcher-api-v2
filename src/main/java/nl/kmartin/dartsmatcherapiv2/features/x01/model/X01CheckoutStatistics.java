package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01CheckoutStatistics {
    @Max(170)
    private int checkoutHighest;

    private int checkoutTonPlus;

    private Integer checkoutPercentage;

    private Integer checkoutsMissed;

    private int checkoutsHit;

    public void incrementCheckoutTonPlus() {
        this.checkoutTonPlus++;
    }

    public void incrementCheckoutsHit() {
        this.checkoutsHit++;
    }

    public void reset() {
        this.checkoutHighest = 0;
        this.checkoutTonPlus = 0;
        this.checkoutPercentage = null;
        this.checkoutsMissed = null;
        this.checkoutsHit = 0;
    }
}
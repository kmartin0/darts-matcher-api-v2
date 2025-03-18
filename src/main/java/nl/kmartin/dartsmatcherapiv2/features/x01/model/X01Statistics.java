package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class X01Statistics {
    @Valid
    private X01AverageStatistics averageStats;

    @Valid
    private X01CheckoutStatistics checkoutStats;

    @Valid
    private X01ScoreStatistics scoreStatistics;

    public X01Statistics() {
        this.setAverageStats(new X01AverageStatistics());
        this.setCheckoutStats(new X01CheckoutStatistics());
        this.setScoreStatistics(new X01ScoreStatistics());
    }

    public void reset() {
        if (this.averageStats == null) this.averageStats = new X01AverageStatistics();
        else this.averageStats.reset();

        if (this.checkoutStats == null) this.checkoutStats = new X01CheckoutStatistics();
        else this.checkoutStats.reset();

        if (this.scoreStatistics == null) this.scoreStatistics = new X01ScoreStatistics();
        else this.scoreStatistics.reset();
    }
}

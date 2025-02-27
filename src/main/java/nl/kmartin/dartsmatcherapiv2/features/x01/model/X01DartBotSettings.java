package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01DartBotSettings {
    public static final int MINIMUM_BOT_AVG = 1;
    private int threeDartAverage;
}

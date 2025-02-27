package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01MatchSettings {
    private int x01;
    private boolean trackDoubles;
    private X01BestOf bestOf;
}

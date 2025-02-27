package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.BaseMatch;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Match extends BaseMatch<X01MatchPlayer> {
    private X01MatchSettings x01MatchSettings;
    private ArrayList<X01Set> sets;
    private ArrayList<X01ScoreLineSnapshot> scoreLineTimeline;
}

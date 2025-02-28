package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.BaseMatch;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class X01Match extends BaseMatch<X01MatchPlayer> {
    @NotNull
    @Valid
    private X01MatchSettings x01MatchSettings;

    @Valid
    private ArrayList<X01Set> sets;

    @Valid
    private ArrayList<X01ScoreLineSnapshot> scoreLineTimeline;
}
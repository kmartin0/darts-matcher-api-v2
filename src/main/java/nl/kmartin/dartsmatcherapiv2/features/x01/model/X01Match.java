package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.BaseMatch;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchStatus;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchType;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "matches")
@TypeAlias("X01Match")
public class X01Match extends BaseMatch<X01MatchPlayer> {
    @NotNull
    @Valid
    private X01MatchSettings matchSettings;

    @Valid
    private ArrayList<X01Set> sets = new ArrayList<>();

    @Valid
    private X01MatchProgress matchProgress;

    public X01Match(ObjectId id, Instant startDate, Instant endDate, MatchStatus matchStatus,
                    ArrayList<X01MatchPlayer> players, MatchType matchType, X01MatchSettings matchSettings,
                    ArrayList<X01Set> sets, X01MatchProgress matchProgress) {
        super(id, startDate, endDate, matchStatus, players, matchType);
        this.matchSettings = matchSettings;
        this.setSets(sets);
        this.matchProgress = matchProgress;
    }

    public void setSets(@Valid ArrayList<X01Set> sets) {
        this.sets = sets != null ? sets : new ArrayList<>();
    }
}
package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class X01Leg {
    private ObjectId winner;

    @NotNull
    private ObjectId throwsFirst;

    @Min(1)
    @Max(3)
    private Integer checkoutDartsUsed;

    @Valid
    private NavigableMap<Integer, X01LegRound> rounds = new TreeMap<>();

    public X01Leg(ObjectId winner, ObjectId throwsFirst, NavigableMap<Integer, X01LegRound> rounds) {
        this.winner = winner;
        this.throwsFirst = throwsFirst;
        this.setRounds(rounds);
    }

    @JsonIgnore
    public @Valid NavigableMap<Integer, X01LegRound> getRounds() {
        return rounds;
    }

    public void setRounds(@Valid NavigableMap<Integer, X01LegRound> rounds) {
        this.rounds = rounds != null ? rounds : new TreeMap<>();
    }

    // Serialize rounds as a list because JSON objects don't guarantee key order.
    @JsonProperty("rounds")
    public List<X01LegRoundEntry> setRoundEntries() {
        return rounds.entrySet().stream()
                .map(X01LegRoundEntry::new)
                .collect(Collectors.toList());
    }

    // Deserialize rounds from a list to a NavigableMap to guarantee key order.
    @JsonProperty("rounds")
    public void setLegEntries(List<X01LegRoundEntry> entries) {
        this.rounds = entries.stream()
                .collect(Collectors.toMap(
                        X01LegRoundEntry::roundNumber,
                        X01LegRoundEntry::round,
                        (oldVal, newVal) -> newVal,
                        TreeMap::new
                ));
    }
}

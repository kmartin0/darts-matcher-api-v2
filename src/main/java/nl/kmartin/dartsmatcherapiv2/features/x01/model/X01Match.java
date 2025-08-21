package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.BaseMatch;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchStatus;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchType;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Document(collection = "matches")
@TypeAlias("X01Match")
public class X01Match extends BaseMatch<X01MatchPlayer> {
    @NotNull
    @Valid
    private X01MatchSettings matchSettings;

    @Valid
    private NavigableMap<Integer, X01Set> sets = new TreeMap<>();

    @Valid
    private X01MatchProgress matchProgress;

    @Valid
    private LinkedHashMap<ObjectId, X01StandingsEntry> standings = new LinkedHashMap<>();

    public X01Match(ObjectId id, Integer version, Integer publishedVersion, Instant startDate, Instant endDate, MatchStatus matchStatus,
                    ArrayList<X01MatchPlayer> players, MatchType matchType, X01MatchSettings matchSettings,
                    NavigableMap<Integer, X01Set> sets, X01MatchProgress matchProgress, LinkedHashMap<ObjectId, X01StandingsEntry> standings) {
        super(id, version, publishedVersion, startDate, endDate, matchStatus, players, matchType);
        this.matchSettings = matchSettings;
        this.setSets(sets);
        this.matchProgress = matchProgress;
        this.setStandings(standings);
    }

    @JsonIgnore
    public @Valid NavigableMap<Integer, X01Set> getSets() {
        return sets;
    }

    public void setSets(@Valid NavigableMap<Integer, X01Set> sets) {
        this.sets = sets != null ? sets : new TreeMap<>();
    }

    public void setStandings(@Valid LinkedHashMap<ObjectId, X01StandingsEntry> standings) {
        this.standings = standings != null ? standings : new LinkedHashMap<>();
    }

    // Serialize sets as a list because JSON objects don't guarantee key order.
    @JsonProperty("sets")
    public List<X01SetEntry> getSetEntries() {
        return sets.entrySet().stream()
                .map(X01SetEntry::new)
                .collect(Collectors.toList());
    }

    // Deserialize sets from a list to a NavigableMap to guarantee key order.
    @JsonProperty("sets")
    public void setSetEntries(List<X01SetEntry> entries) {
        this.sets = entries.stream()
                .collect(Collectors.toMap(
                        X01SetEntry::setNumber,
                        X01SetEntry::set,
                        (oldVal, newVal) -> newVal,
                        TreeMap::new
                ));
    }
}
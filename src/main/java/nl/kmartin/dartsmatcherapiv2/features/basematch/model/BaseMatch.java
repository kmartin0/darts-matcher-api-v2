package nl.kmartin.dartsmatcherapiv2.features.basematch.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapiv2.validators.noduplicatematchplayername.NoDuplicateMatchPlayerName;
import nl.kmartin.dartsmatcherapiv2.validators.validplayercomposition.ValidPlayerComposition;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.ArrayList;

@Data
@NoArgsConstructor
public abstract class BaseMatch<PlayerType extends MatchPlayer> {
    @MongoId
    private ObjectId id;

    @Version
    private Integer version;

    private Integer broadcastVersion = 0;

    private Instant startDate;

    private Instant endDate;

    private MatchStatus matchStatus;

    @Valid
    @NotNull
    @Size(min = 1, max = 4)
    @NoDuplicateMatchPlayerName
    @ValidPlayerComposition
    private ArrayList<PlayerType> players = new ArrayList<>();

    private MatchType matchType;

    public BaseMatch(ObjectId id, Integer version, Integer broadcastVersion, Instant startDate, Instant endDate, MatchStatus matchStatus, ArrayList<PlayerType> players, MatchType matchType) {
        this.id = id;
        this.version = version;
        this.setBroadcastVersion(broadcastVersion);
        this.startDate = startDate;
        this.endDate = endDate;
        this.matchStatus = matchStatus;
        this.setPlayers(players);
        this.matchType = matchType;
    }

    public Integer getBroadcastVersion() {
        return broadcastVersion != null ? broadcastVersion : 0;
    }

    public void setBroadcastVersion(Integer broadcastVersion) {
        this.broadcastVersion = broadcastVersion != null ? broadcastVersion : 0;
    }

    public void setPlayers(@Valid @NotNull @Size(min = 1, max = 4) ArrayList<PlayerType> players) {
        this.players = players != null ? players : new ArrayList<>();
    }
}
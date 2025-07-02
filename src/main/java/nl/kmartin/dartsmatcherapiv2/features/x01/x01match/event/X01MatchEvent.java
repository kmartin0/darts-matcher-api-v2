package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import org.bson.types.ObjectId;

public sealed interface X01MatchEvent {
    @JsonProperty("eventType")
    X01MatchEventType eventType();

    @JsonIgnore
    ObjectId getMatchId();

    record X01ProcessMatchEvent(X01Match payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.PROCESS_MATCH;
        }

        @Override
        public ObjectId getMatchId() {
            return payload.getId();
        }
    }

    record X01AddHumanTurnEvent(X01Match payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.ADD_HUMAN_TURN;
        }

        @Override
        public ObjectId getMatchId() {
            return payload.getId();
        }
    }

    record X01AddBotTurnEvent(X01Match payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.ADD_BOT_TURN;
        }

        @Override
        public ObjectId getMatchId() {
            return payload.getId();
        }
    }

    record X01EditTurnEvent(X01Match payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.EDIT_TURN;
        }

        @Override
        public ObjectId getMatchId() {
            return payload.getId();
        }
    }

    record X01DeleteLastTurnEvent(X01Match payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.DELETE_LAST_TURN;
        }

        @Override
        public ObjectId getMatchId() {
            return payload.getId();
        }
    }

    record X01DeleteMatchEvent(ObjectId payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.DELETE_MATCH;
        }

        @Override
        public ObjectId getMatchId() {
            return payload;
        }
    }

    record X01ResetMatchEvent(X01Match payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.RESET_MATCH;
        }

        @Override
        public ObjectId getMatchId() {
            return payload.getId();
        }
    }
}



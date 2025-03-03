package nl.kmartin.dartsmatcherapiv2.config;


import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import nl.kmartin.dartsmatcherapiv2.serializers.InstantSerializer;
import nl.kmartin.dartsmatcherapiv2.serializers.ObjectIdSerializer;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class JacksonConfig {
    @Bean
    public Module customSerializerModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(ObjectId.class, new ObjectIdSerializer());
        module.addSerializer(Instant.class, new InstantSerializer());
        return module;
    }

    @Bean
    public ObjectMapper objectMapper(Module customSerializerModule) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(customSerializerModule);
        return objectMapper;
    }
}

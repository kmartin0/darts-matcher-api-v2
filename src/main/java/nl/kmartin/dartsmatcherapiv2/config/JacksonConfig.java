package nl.kmartin.dartsmatcherapiv2.config;


import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nl.kmartin.dartsmatcherapiv2.serializers.ObjectIdDeserializer;
import nl.kmartin.dartsmatcherapiv2.serializers.ObjectIdSerializer;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public Module customSerializerModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(ObjectId.class, new ObjectIdSerializer());
        module.addDeserializer(ObjectId.class, new ObjectIdDeserializer());
        return module;
    }

    @Bean
    public ObjectMapper objectMapper(Module customSerializerModule) {
        return new ObjectMapper()
                .registerModule(customSerializerModule)
                .registerModule(new JavaTimeModule());
    }
}

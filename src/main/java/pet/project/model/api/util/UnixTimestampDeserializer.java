package pet.project.model.api.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class UnixTimestampDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        long timestampInSeconds = jsonParser.getValueAsLong();

        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestampInSeconds),
                ZoneId.systemDefault()
        );
    }
}

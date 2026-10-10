package br.com.delta.delta_api_postgres.modules.device.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.IOException;

public record ValidateDeviceRequest(
        @JsonProperty("device_id") @NotBlank @Size(max = 100)
        @JsonDeserialize(using = StrictString.class) String deviceId
) {

    public static class StrictString extends StdDeserializer<String> {
        public StrictString() { super(String.class); }
        @Override public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                context.reportInputMismatch(String.class, "device_id deve ser uma string.");
            }
            return parser.getText();
        }
    }
}

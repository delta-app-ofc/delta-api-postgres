package br.com.delta.delta_api_postgres.modules.device.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.IOException;

public record ValidateDeviceCredentialRequest(
        @JsonProperty("api_key") @NotNull @Size(max = 256)
        @JsonDeserialize(using = StrictString.class) String apiKey
) {
    @Override public String toString() { return "ValidateDeviceCredentialRequest[redacted]"; }

    public static class StrictString extends StdDeserializer<String> {
        public StrictString() { super(String.class); }
        @Override public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                context.reportInputMismatch(String.class, "api_key deve ser uma string.");
            }
            return parser.getText();
        }
    }
}

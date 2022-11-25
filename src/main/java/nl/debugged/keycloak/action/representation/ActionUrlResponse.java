package nl.debugged.keycloak.action.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ActionUrlResponse {
    @JsonProperty("link")
    private String link;
}

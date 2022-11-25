package nl.debugged.keycloak.action.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ActionUrlRequest {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("lifespan")
    private Integer expirationSeconds;

    @JsonProperty("actions")
    private List<String> actions;
}

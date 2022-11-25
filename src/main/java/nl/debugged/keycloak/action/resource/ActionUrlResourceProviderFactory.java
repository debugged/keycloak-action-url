package nl.debugged.keycloak.action.resource;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProviderFactory;

@JBossLog
public class ActionUrlResourceProviderFactory implements RealmResourceProviderFactory {

    private static final String ID = "action-url";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void close() {}

    @Override
    public ActionUrlResourceProvider create(KeycloakSession session) {
        return new ActionUrlResourceProvider(session);
    }

    @Override
    public void init(Config.Scope config) {}

    @Override
    public void postInit(KeycloakSessionFactory factory) {}
}

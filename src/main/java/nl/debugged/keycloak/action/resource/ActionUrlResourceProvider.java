package nl.debugged.keycloak.action.resource;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resource.RealmResourceProvider;

public class ActionUrlResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public ActionUrlResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        RealmModel realm = session.getContext().getRealm();
        ActionUrlResource actionUrl = new ActionUrlResource(realm);
        ResteasyProviderFactory.getInstance().injectProperties(actionUrl);
        actionUrl.setup();
        return actionUrl;
    }

    @Override
    public void close() {}
}

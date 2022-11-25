package nl.debugged.keycloak.action.resource;

import lombok.extern.jbosslog.JBossLog;
import nl.debugged.keycloak.action.representation.ActionUrlRequest;
import nl.debugged.keycloak.action.representation.ActionUrlResponse;
import org.keycloak.Config;
import org.keycloak.authentication.actiontoken.execactions.ExecuteActionsActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.LoginActionsService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.concurrent.TimeUnit;

@JBossLog
public class ActionUrlResource extends AbstractAdminResource {
    protected ActionUrlResource(RealmModel realm) {
        super(realm);
    }

    @POST()
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ActionUrlResponse createMagicLink(final ActionUrlRequest rep) {
        String userId = rep.getUserId();
        String redirectUri = rep.getRedirectUri();
        String clientId = rep.getClientId();
        Integer lifespan = rep.getExpirationSeconds();
        List<String> actions = rep.getActions();

        if (!permissions.users().canManage())
            throw new ForbiddenException("action url requires manage-users");

        UserModel user = session.users().getUserById(realm, userId);
        if (user == null) {
            // we do this to make sure somebody can't phish ids
            if (permissions.users().canQuery()) throw new NotFoundException("User not found");
            else throw new ForbiddenException();
        }

        permissions.users().requireManage(user);

        if (user.getEmail() == null) {
            throw new WebApplicationException(
                ErrorResponse.error("User email missing", Response.Status.BAD_REQUEST));
        }

        if (!user.isEnabled()) {
            throw new WebApplicationException(
                ErrorResponse.error("User is disabled", Response.Status.BAD_REQUEST));
        }

        if (redirectUri != null && clientId == null) {
            throw new WebApplicationException(
                ErrorResponse.error("Client id missing", Response.Status.BAD_REQUEST));
        }

        if (clientId == null) {
            clientId = Constants.ACCOUNT_MANAGEMENT_CLIENT_ID;
        }

        ClientModel client = realm.getClientByClientId(clientId);
        if (client == null) {
            log.debugf("Client %s doesn't exist", clientId);
            throw new WebApplicationException(
                ErrorResponse.error("Client doesn't exist", Response.Status.BAD_REQUEST));
        }
        if (!client.isEnabled()) {
            log.debugf("Client %s is not enabled", clientId);
            throw new WebApplicationException(
                ErrorResponse.error("Client is not enabled", Response.Status.BAD_REQUEST));
        }

        String redirect;
        if (redirectUri != null) {
            redirect = RedirectUtils.verifyRedirectUri(session, redirectUri, client);
            if (redirect == null) {
                throw new WebApplicationException(
                    ErrorResponse.error("Invalid redirect uri.", Response.Status.BAD_REQUEST));
            }
        }

        if (lifespan == null) {
            lifespan = realm.getActionTokenGeneratedByAdminLifespan();
        }

        int expiration = Time.currentTime() + lifespan;
        ExecuteActionsActionToken token = new ExecuteActionsActionToken(user.getId(), user.getEmail(), expiration, actions, redirectUri, clientId);

        // This is a workaround for situations where the realm you are using to call this (e.g. master)
        // is different than the one you are generating the action token for. Because the
        // SignatureProvider
        // assumes the value that is set in session.getContext().getRealm() has the keys it should use,
        // we
        // need to temporarily reset it
        RealmModel r = session.getContext().getRealm();
        log.infof("realm %s session.context.realm %s", realm.getName(), r.getName());
        // Because of the risk, throw an exception for master realm
        if (Config.getAdminRealm().equals(realm.getName())) {
            throw new IllegalStateException(
                String.format("Action url not allowed for %s realm", Config.getAdminRealm()));
        }
        session.getContext().setRealm(realm);

        UriBuilder builder = LoginActionsService.actionTokenProcessor(session.getContext().getUri());
        builder.queryParam("key", token.serialize(session, realm, session.getContext().getUri()));

        String link = builder.build(realm.getName()).toString();

        adminEvent.operation(OperationType.ACTION).resourcePath(session.getContext().getUri()).success();

        ActionUrlResponse resp = new ActionUrlResponse();
        resp.setLink(link);
        return resp;
    }
}

package ru.terra.btobd.server.controller;

import com.sun.jersey.api.core.HttpContext;
import org.apache.vysper.storage.jcr.JcrStorage;
import org.apache.vysper.storage.jcr.JcrStorageException;
import org.apache.vysper.xmpp.addressing.EntityFormatException;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.authentication.AccountCreationException;
import org.apache.vysper.xmpp.modules.roster.AskSubscriptionType;
import org.apache.vysper.xmpp.modules.roster.RosterException;
import org.apache.vysper.xmpp.modules.roster.RosterItem;
import org.apache.vysper.xmpp.modules.roster.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.terra.btobd.server.constants.URLConstants;
import ru.terra.btobd.server.engine.UsersEngine;
import ru.terra.btobd.server.engine.XMMPEngine;
import ru.terra.btobd.server.entity.User;
import ru.terra.server.controller.AbstractResource;
import ru.terra.server.dto.LoginDTO;

import javax.jcr.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 17.11.14
 * Time: 13:10
 */
@Path(URLConstants.Login.LOGIN)
public class LoginController extends AbstractResource {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private UsersEngine usersEngine = new UsersEngine();

    @GET
    @Path(URLConstants.Login.LOGIN_DO_LOGIN_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public LoginDTO login(@Context HttpContext hc,
                          @QueryParam(URLConstants.Login.LOGIN_PARAM_USER) String user,
                          @QueryParam(URLConstants.Login.LOGIN_PARAM_PASS) String pass) {
        logger.info("User requests login with user = " + user + " and pass = " + pass);

        LoginDTO ret = new LoginDTO();
        ret.logged = false;
        if (user != null && pass != null) {
            User u = usersEngine.login(user, pass);
            if (u != null) {
                ret.id = u.getId();
                ret.session = sessionsHolder.registerUserSession(u);
                ret.logged = true;
            } else {
                ret.message = "user not found";
            }
        } else {
            ret.message = "invalid parameters";
        }

        return ret;
    }

    @GET
    @Path(URLConstants.Login.LOGIN_DO_REGISTER_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public LoginDTO reg(@Context HttpContext hc,
                        @QueryParam(URLConstants.Login.LOGIN_PARAM_USER) String user,
                        @QueryParam(URLConstants.Login.LOGIN_PARAM_PASS) String pass,
                        @QueryParam(URLConstants.Login.LOGIN_PARAM_EMAIL) String email) {
        logger.info("User requests reg with user = " + user + " and pass = " + pass);

        LoginDTO ret = new LoginDTO();
        if (user != null && usersEngine.findUserByName(user) == null) {
            if (user != null && pass != null) {
                Integer retId = usersEngine.registerUser(user, pass, email);
                ret.logged = true;
                ret.id = retId;
                try {
                    XMMPEngine engine = XMMPEngine.getInstance();
                    engine.getAccountManagement().addUser(EntityImpl.parse(user + "@" + URLConstants.SERVER_DOMAIN), pass);
                    try {
                        Node accountsNode = JcrStorage.getInstance().getRootNode().getNode("accountentity");
                        NodeIterator iter = accountsNode.getNodes();
                        List<String> jids = new ArrayList<>();
                        while (iter.hasNext()) {
                            Node account = iter.nextNode();
                            if (!account.getName().contains(user)) {
                                jids.add(account.getName());
                                logger.info("Adding friend for " + user + " : " + account.getName());
                                engine.getRosterManager().addContact(EntityImpl.parse(account.getName()),
                                        new RosterItem(EntityImpl.parse(user + "@" + URLConstants.SERVER_DOMAIN),
                                                SubscriptionType.BOTH,
                                                AskSubscriptionType.ASK_SUBSCRIBED));
                            }
                        }
                        for (String jid : jids)
                            engine.getRosterManager().addContact(EntityImpl.parse(user + "@" + URLConstants.SERVER_DOMAIN),
                                    new RosterItem(EntityImpl.parse(jid), SubscriptionType.BOTH, AskSubscriptionType.ASK_SUBSCRIBED));
                    } catch (RepositoryException e) {
                        logger.error("", e);
                    } catch (JcrStorageException e) {
                        logger.error("", e);
                    } catch (RosterException e) {
                        logger.error("Unable to add new friend to roster", e);
                    }

                } catch (AccountCreationException e) {
                    logger.error("Unable to create jabber account", e);
                } catch (EntityFormatException e) {
                    logger.error("Invalid entity", e);
                }
            } else {
                ret.logged = false;
            }
        } else {
            ret.logged = false;
            ret.message = "User already exists";
        }
        return ret;
    }


    @GET
    @Path("test")
    public String test() {
        try {
            listNode(JcrStorage.getInstance().getRootNode().getNode("accountentity"));
        } catch (RepositoryException e) {
            logger.error("", e);
        } catch (JcrStorageException e) {
            logger.error("", e);
        }
        return "";
    }

    private void listNode(Node node) throws RepositoryException {
        logger.info("Listing node " + node.getName());
        NodeIterator iter = node.getNodes();
        while (iter.hasNext()) {
            Node nextNode = iter.nextNode();
            PropertyIterator piter = nextNode.getProperties();
            logger.info("Properties: ");
            while (piter.hasNext()) {
                Property p = piter.nextProperty();
                logger.info(p.getName() + " = " + p.getValue().getString());
            }
            listNode(nextNode);
        }
    }
}

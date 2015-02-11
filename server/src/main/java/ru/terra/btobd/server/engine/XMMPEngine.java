package ru.terra.btobd.server.engine;

import org.apache.vysper.mina.C2SEndpoint;
import org.apache.vysper.storage.jcr.JcrStorageProviderRegistry;
import org.apache.vysper.xmpp.addressing.EntityFormatException;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.authentication.AccountCreationException;
import org.apache.vysper.xmpp.authentication.AccountManagement;
import org.apache.vysper.xmpp.extension.xep0124.BoshEndpoint;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.MUCModule;
import org.apache.vysper.xmpp.modules.extension.xep0049_privatedata.PrivateDataModule;
import org.apache.vysper.xmpp.modules.extension.xep0050_adhoc_commands.AdhocCommandsModule;
import org.apache.vysper.xmpp.modules.extension.xep0054_vcardtemp.VcardTempModule;
import org.apache.vysper.xmpp.modules.extension.xep0092_software_version.SoftwareVersionModule;
import org.apache.vysper.xmpp.modules.extension.xep0202_entity_time.EntityTimeModule;
import org.apache.vysper.xmpp.modules.roster.RosterModule;
import org.apache.vysper.xmpp.modules.roster.persistence.RosterManager;
import org.apache.vysper.xmpp.modules.roster.persistence.RosterManagerUtils;
import org.apache.vysper.xmpp.server.XMPPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.terra.btobd.server.constants.URLConstants;
import ru.terra.server.config.Config;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Date: 05.12.14
 * Time: 13:20
 */
public class XMMPEngine {
    private final JcrStorageProviderRegistry providerRegistry;
    private final AccountManagement accountManagement;
    private final XMPPServer server;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static XMMPEngine instance = new XMMPEngine();
    private RosterManager rosterManager;

    public static XMMPEngine getInstance() {
        return instance;
    }

    private XMMPEngine() {
        providerRegistry = new JcrStorageProviderRegistry();
        accountManagement = (AccountManagement) providerRegistry.retrieve(AccountManagement.class);
        try {
            if (!accountManagement.verifyAccountExists(EntityImpl.parse("terranz@" + URLConstants.SERVER_DOMAIN))) {
                accountManagement.addUser(EntityImpl.parse("terranz@" + URLConstants.SERVER_DOMAIN), Config.getConfig().getValue("defaultuser.pass", "passdefault"));
            }
        } catch (EntityFormatException e) {
            logger.error("", e);
        } catch (AccountCreationException e) {
            logger.error("", e);
        }
        server = new XMPPServer(URLConstants.SERVER_DOMAIN);
        server.addEndpoint(new C2SEndpoint());
        BoshEndpoint boshEndpoint = new BoshEndpoint();
        boshEndpoint.setPort(Integer.parseInt(Config.getConfig().getValue("bosh.server.port", "8184")));
        boshEndpoint.setSSLEnabled(false);
        boshEndpoint.setContextPath("/bosh");
        server.addEndpoint(boshEndpoint);
        server.setStorageProviderRegistry(providerRegistry);
        try {
            server.setTLSCertificateInfo(new File("bogus.cert"), "boguspw");
        } catch (FileNotFoundException e) {
            logger.error("", e);
        }
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                } catch (Exception e) {
                    logger.error("", e);
                }
                server.addModule(new MUCModule());
                server.addModule(new SoftwareVersionModule());
                server.addModule(new EntityTimeModule());
                server.addModule(new VcardTempModule()); // depends on Jcr persistence
                server.addModule(new RosterModule());
                server.addModule(new PrivateDataModule());
                server.addModule(new AdhocCommandsModule());
            }
        }).start();
    }

    public JcrStorageProviderRegistry getProviderRegistry() {
        return providerRegistry;
    }

    public AccountManagement getAccountManagement() {
        return accountManagement;
    }

    public XMPPServer getServer() {
        return server;
    }

    public RosterManager getRosterManager() {
        return RosterManagerUtils.getRosterInstance(server.getServerRuntimeContext(), null);
    }
}

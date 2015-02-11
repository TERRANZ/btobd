package ru.terra.btobd.server.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.terra.server.config.Config;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Date: 02.02.15
 * Time: 12:26
 */
public class TroublesEmailReporteEngine {
    protected String server, from, user, pass, to;
    protected Integer port;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void notify(String error) {
        logger.info("sending error");
        try {
            Properties props = new Properties();

            props.put("mail.transport.protocol", "smtps");
            props.put("mail.smtps.auth", "true");

            Session mailSession = Session.getDefaultInstance(props);
            mailSession.setDebug(true);
            Transport transport = mailSession.getTransport();

            MimeMessage message = new MimeMessage(mailSession);
            message.setSubject("Error report from btobd android client");
            message.setContent(error, "text/plain");

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            transport.connect(server, port, user, pass);

            transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
            transport.close();
        } catch (Exception e) {
            logger.error("error while sending email", e);
        }
    }

    public TroublesEmailReporteEngine(String to) {
        this.to = to;
        server = Config.getConfig().getValue("email.server", "");
        try {
            port = Integer.parseInt(Config.getConfig().getValue("email.server.port", "465"));
        } catch (NumberFormatException e) {
            port = 465;
        }
        from = Config.getConfig().getValue("email.from", "");
        user = Config.getConfig().getValue("email.user", "");
        pass = Config.getConfig().getValue("emai.pass", "");
    }
//
//    public Message produceMessage(Session session, String... params) throws MessagingException {
//        Message msg = new MimeMessage(session);
//        InternetAddress addressFrom = new InternetAddress(from);
//        msg.setFrom(addressFrom);
//        for (String a : to.split(",")) {
//            InternetAddress addressTo = new InternetAddress(a);
//            msg.addRecipient(Message.RecipientType.TO, addressTo);
//            logger.info("Recipient: " + addressTo);
//        }
//        msg.setSubject(params[0]);
//        StringBuilder sb = new StringBuilder();
//        sb.append("<html>");
//        sb.append("<head>");
//        sb.append("<title>");
//        sb.append(params[0]);
//        sb.append("</title>");
//        sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"> \n");
//        sb.append("</head>");
//        sb.append("<body>");
//        sb.append(params[0]);
//        sb.append("</body>");
//        sb.append("</html>");
//
//        MimeMultipart part = new MimeMultipart();
//        BodyPart messageBodyPart = new MimeBodyPart();
//        messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(sb.toString().getBytes(), "text/html")));
//        messageBodyPart.removeHeader("Content-Transfer-Encoding");
//        messageBodyPart.addHeader("Content-Transfer-Encoding", "base64");
//        part.addBodyPart(messageBodyPart);
//        msg.setContent(part);
//        return msg;
//    }
//
//
//    public void notify(String... params) {
//        logger.debug("Sending email with notification...");
//        try {
//            Properties props = getProperties();
//
//            Authenticator authenticator = new Authenticator() {
//                public PasswordAuthentication getPasswordAuthentication() {
//                    return new PasswordAuthentication(user, pass);
//                }
//            };
//
//            Session session = Session.getInstance(props, authenticator);
//            session.setDebug(true);
//            Message msg = produceMessage(session, params);
//            Transport.send(msg, msg.getRecipients(Message.RecipientType.TO));
//        } catch (MessagingException mex) {
//            logger.error("Error while sending email notification", mex);
//        }
//    }
//
//    protected Properties getProperties() {
//        Properties props = new Properties();
//        props.put("mail.transport.protocol", "smtps");
//        props.put("mail.smtps.auth", "true");
//        props.put("mail.smtp.host", server);
//        props.put("mail.smtp.socketFactory.port", port);
//        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.port", port);
//        return props;
//    }
}

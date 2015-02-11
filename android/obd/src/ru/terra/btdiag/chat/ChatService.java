package ru.terra.btdiag.chat;

import android.content.*;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.inject.Inject;
import de.duenndns.ssl.MemorizingTrustManager;
import org.acra.ACRA;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import roboguice.service.RoboIntentService;
import ru.terra.btdiag.MainActivity;
import ru.terra.btdiag.R;
import ru.terra.btdiag.activity.ChatActivity;
import ru.terra.btdiag.chat.db.entity.MessageEntity;
import ru.terra.btdiag.core.InfoService;
import ru.terra.btdiag.core.Logger;
import ru.terra.btdiag.core.SettingsService;
import ru.terra.btdiag.core.constants.URLConstants;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Date: 04.01.15
 * Time: 18:23
 */
public class ChatService extends RoboIntentService {
    private final String TAG = ChatService.class.getName();
    public static final String CHAT_MSG_TO = "chat_msg_to";
    public static final String CHAT_MSG_BODY = "chat_msg_body";
    public static final String SEND_MSG_RECEIVER = "ru.terra.btdiag.service.send_msg_action";
    public static final String GET_ROSTER_RECEIVER = "ru.terra.btdiag.service.get_roster";

    private XMPPTCPConnection connection;
    @Inject
    private SettingsService settingsService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private SendMsgReceiver sendMsgReceiver;
    private GetRosterReciever getRosterReceiver;

    public ChatService() {
        super("Obd chat service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        IntentFilter sendMsgFilter = new IntentFilter(SEND_MSG_RECEIVER);
        sendMsgFilter.addCategory(Intent.CATEGORY_DEFAULT);
        sendMsgReceiver = new SendMsgReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(sendMsgReceiver, sendMsgFilter);

        IntentFilter getRosterFilter = new IntentFilter(GET_ROSTER_RECEIVER);
        getRosterFilter.addCategory(Intent.CATEGORY_DEFAULT);
        getRosterReceiver = new GetRosterReciever();
        LocalBroadcastManager.getInstance(this).registerReceiver(getRosterReceiver, getRosterFilter);
        try {
            start();
        } catch (Exception e) {
            Logger.e(TAG, "Unable to start chat service", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sendMsgReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(getRosterReceiver);
    }

    public void start() throws NoSuchAlgorithmException, KeyManagementException {
        ConnectionConfiguration config = new ConnectionConfiguration(URLConstants.SERVER_DOMAIN, 5222);
        SSLContext sc = SSLContext.getInstance("TLS");
        MemorizingTrustManager mtm = new MemorizingTrustManager(getApplicationContext());
        sc.init(null, new X509TrustManager[]{mtm}, new java.security.SecureRandom());
        config.setCustomSSLContext(sc);
        config.setHostnameVerifier(mtm.wrapHostnameVerifier(new org.apache.http.conn.ssl.StrictHostnameVerifier()));
        connection = new XMPPTCPConnection(config);
        sendStatus("Настроено");
        connection.setPacketReplyTimeout(30000);
        try {
            connection.connect();
            sendStatus("Подключено");
            connection.login(settingsService.getSetting(getString(R.string.username), ""), settingsService.getSetting(getString(R.string.password), ""), "btobd");
            sendStatus("Вошли");
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(e);
            Log.d(TAG, e.getMessage(), e);
            sendStatus("Ошибка: " + e.getMessage());
        }
        connection.addPacketListener(new JabberPacketListener(), MessageTypeFilter.CHAT);
        while (true)
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    private class JabberPacketListener implements PacketListener {
        @Override
        public void processPacket(Packet packet) {
            Message message = (Message) packet;
            if (message.getBody() != null) {
                String fromName = message.getFrom();
                String msg = message.getBody();
                ContentValues cv = new ContentValues();
                cv.put(MessageEntity.NICK, fromName);
                cv.put(MessageEntity.MESSAGE, msg);
                cv.put(MessageEntity.DATE, dateFormat.format(new Date()));
                getContentResolver().insert(MessageEntity.CONTENT_URI, cv);
            }
        }
    }

    private class SendMsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String to = intent.getStringExtra(CHAT_MSG_TO);
            String body = intent.getStringExtra(CHAT_MSG_BODY);
            Message msg = new Message();
            msg.setTo(to);
            msg.setBody(body);
            msg.setType(Message.Type.chat);
            try {
                connection.sendPacket(msg);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetRosterReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Roster roster = connection.getRoster();
            String[] usersArr = new String[roster.getEntries().size()];
            List<String> users = new ArrayList<String>();
            for (RosterEntry rosterEntry : roster.getEntries())
                users.add(rosterEntry.getUser());
            for (int i = 0; i < users.size(); i++)
                usersArr[i] = users.get(i);
            LocalBroadcastManager.getInstance(ChatService.this).sendBroadcast(new Intent(ChatActivity.ROSTER_RECEIVER).addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES).putExtra(ChatActivity.ROSTER_ITEMS, usersArr));
        }
    }

    private void sendStatus(String text) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(MainActivity.STATUS_RECEIVER).putExtra("type", 1).putExtra("text", text));
    }
}

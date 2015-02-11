package ru.terra.btdiag.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import ru.terra.btdiag.R;
import ru.terra.btdiag.activity.parts.ChatAdapter;
import ru.terra.btdiag.core.SettingsService;
import ru.terra.btdiag.chat.db.entity.MessageEntity;
import ru.terra.btdiag.chat.ChatService;
import ru.terra.btdiag.viewpagerindicator.TitlePageIndicator;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 13.12.14
 * Time: 13:31
 */
@ContentView(R.layout.a_chat)
public class ChatActivity extends RoboActivity {
    public static final String ROSTER_RECEIVER = "ru.terra.btdiag.activity.roster_update";
    public static final String ROSTER_ITEMS = "roster_items";

    private List<View> pages = new ArrayList<View>();
    private String[] titles = new String[]{"Пользователи", "Чаты", "Чат"};
    @InjectView(R.id.vp_chat_activity)
    private ViewPager vp;
    @InjectView(R.id.tpi_chat_activity)
    private TitlePageIndicator titleIndicator;
    private ListView lvChats, lvUsers, lvChat;
    private EditText edt_chat_msg;
    private RosterReceiver receiver;
    private TextView tvChatTo;
    @Inject
    private SettingsService settingsService;

    private class MainAcvitiyAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return pages.size();
        }

        @Override
        public Object instantiateItem(View collection, int position) {
            View v = pages.get(position);
            ((ViewPager) collection).addView(v, 0);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (View) arg1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View users, chats, chat;
        users = inflater.inflate(R.layout.f_users, null);
        chats = inflater.inflate(R.layout.f_chats, null);
        chat = inflater.inflate(R.layout.f_chat, null);
        pages.add(users);
        pages.add(chats);
        pages.add(chat);

        MainAcvitiyAdapter adapter = new MainAcvitiyAdapter();
        vp.setAdapter(adapter);

        titleIndicator.setViewPager(vp);

        lvUsers = (ListView) users.findViewById(R.id.lv_chat_users);
        lvChats = (ListView) chats.findViewById(R.id.lv_chats);
        lvChat = (ListView) chat.findViewById(R.id.lv_chat_messages);
        tvChatTo = (TextView) chat.findViewById(R.id.tvChatTo);

        lvChat.setAdapter(new ChatAdapter(this, getContentResolver().query(MessageEntity.CONTENT_URI, null, null, null, null)));
        edt_chat_msg = (EditText) chat.findViewById(R.id.edt_chat_msg);

        IntentFilter filter = new IntentFilter(ROSTER_RECEIVER);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new RosterReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tvChatTo.setText(lvUsers.getAdapter().getItem(position).toString());
                vp.setCurrentItem(2, true);
            }
        });
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ChatService.GET_ROSTER_RECEIVER));


        if (settingsService.getSettingBoolean(getString(R.string.auto_connect), false))
            startService(new Intent(this, ChatService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    public void sendMessage(View view) {
        String to = tvChatTo.getText().toString();
        String body = edt_chat_msg.getText().toString();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent().setAction(ChatService.SEND_MSG_RECEIVER).putExtra(ChatService.CHAT_MSG_TO, to).putExtra(ChatService.CHAT_MSG_BODY, body));
        edt_chat_msg.getText().clear();
    }

    private class RosterReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            lvUsers.setAdapter(new ArrayAdapter<String>(ChatActivity.this, android.R.layout.simple_list_item_1, intent.getStringArrayExtra(ROSTER_ITEMS)));
        }
    }
}


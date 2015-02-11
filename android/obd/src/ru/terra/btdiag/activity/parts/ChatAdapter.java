package ru.terra.btdiag.activity.parts;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import ru.terra.btdiag.R;
import ru.terra.btdiag.chat.db.entity.MessageEntity;

/**
 * Date: 13.12.14
 * Time: 14:37
 */
public class ChatAdapter extends SimpleCursorAdapter {

    private Context context;

    public ChatAdapter(Context context, Cursor c) {
        super(context, R.layout.i_chat_line, c, new String[]{MessageEntity.MESSAGE, MessageEntity.DATE}, new int[]{R.id.tv_chat_line_msg, R.id.tv_chat_line_date});
        this.context = context;
    }

    public static class ChatHolder {
        public TextView tvMessage, tvDate;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        View v = convertView;
//        if (v == null) {
//            v = LayoutInflater.from(context).inflate(R.layout.i_chat_line, null);
//            ChatHolder ch = new ChatHolder();
//            ch.tvDate = (TextView) v.findViewById(R.id.tv_chat_line_date);
//            ch.tvMessage = (TextView) v.findViewById(R.id.tv_chat_line_msg);
//            v.setTag(ch);
//        }
//        ChatMessage chatMessage = getItem(position);
//        TextView tvMessage = ((ChatHolder) v.getTag()).tvMessage;
//        tvMessage.setText(chatMessage.getMessage());
//        TextView tvDate = ((ChatHolder) v.getTag()).tvDate;
//        tvDate.setText(chatMessage.getDate());
//        if (chatMessage.isMy())
//            tvMessage.setGravity(Gravity.LEFT);
//        else
//            tvMessage.setGravity(Gravity.RIGHT);
//
//        return v;
//    }
}

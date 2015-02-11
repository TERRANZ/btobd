package ru.terra.btdiag.db.entity;

import android.net.Uri;
import android.provider.BaseColumns;
import ru.terra.btdiag.constants.Constants;

/**
 * Date: 13.12.14
 * Time: 19:35
 */
public interface MessageEntity extends BaseColumns {
    String CONTENT_DIRECTORY = "msg";
    Uri CONTENT_URI = Uri.parse("content://" + Constants.AUTHORITY + "/" + CONTENT_DIRECTORY);
    String CONTENT_TYPE = "entity.cursor.dir/msg";
    String CONTENT_ITEM_TYPE = "entity.cursor.item/msg";

    String NICK = "nick";
    String DATE = "msgdate";
    String MESSAGE = "message";
}



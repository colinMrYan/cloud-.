package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.common.LogUtils;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;

import java.util.ArrayList;

/**
 * Created by libaochao on 2019/3/21.
 */

public class MessageLongClickUtils {

    public static boolean MessageLongClick(final Context context,final UIMessage uiMessage ){
        Message message = uiMessage.getMessage();
        String type = message.getType();
        switch (type) {
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
                textMessageLongClick(context,uiMessage);
                LogUtils.LbcDebug("MESSAGE_TYPE_TEXT_PLAIN");
                return true;
            case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                LogUtils.LbcDebug("TYPE_TEXT_MARKDOWN");
                break;
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                LogUtils.LbcDebug("MESSAGE_TYPE_FILE_REGULAR_FILE");
                break;
            case Message.MESSAGE_TYPE_EXTENDED_CONTACT_CARD:
                LogUtils.LbcDebug("MESSAGE_TYPE_EXTENDED_CONTACT_CARD");
                break;
            case Message.MESSAGE_TYPE_EXTENDED_ACTIONS:
                LogUtils.LbcDebug("MESSAGE_TYPE_EXTENDED_ACTIONS");
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                LogUtils.LbcDebug("MESSAGE_TYPE_MEDIA_IMAGE");
                imageMessageLongClick(context, uiMessage);
                return true;
            case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                LogUtils.LbcDebug("MESSAGE_TYPE_COMMENT_TEXT_PLAIN");
                break;
            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                LogUtils.LbcDebug("MESSAGE_TYPE_EXTENDED_LINKS");
                break;
            case Message.MESSAGE_TYPE_MEDIA_VOICE:
                LogUtils.LbcDebug("MESSAGE_TYPE_MEDIA_VOICE");
                break;
            default:
                LogUtils.LbcDebug("DEFAULT");
                break;
        }
        return false;
    }

    public static void textMessageLongClick(final Context context,final UIMessage uiMessage) {
        final String[] items = new String[]{"复制", "转发", "日程"};
        LongClickDialog(items, context,uiMessage);

    }

    public static void imageMessageLongClick(final Context context,final UIMessage uiMessage) {
        final String[] items = new String[]{"转发", "日程"};
        LongClickDialog(items, context,uiMessage);
    }



    private static void LongClickDialog(final String[] items, final Context context,final UIMessage uiMessage) {
        new QMUIDialog.MenuDialogBuilder(context)
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (items[which]){
                            case "复制":
                                Toast.makeText(context, "你选择了 " + items[which], Toast.LENGTH_SHORT).show();
                                break;
                            case "转发":
                                shareMessageToFrinds(context,uiMessage);
                                Toast.makeText(context, "你选择了 " + items[which], Toast.LENGTH_SHORT).show();
                                break;
                            case "日程":
                                Toast.makeText(context, "你选择了 " + items[which], Toast.LENGTH_SHORT).show();
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .create(R.style.QMUI_Dialog).show();
    }



    /**
     * 给朋友分享新闻
     */
    private static void shareMessageToFrinds(Context context,UIMessage uiMessage) {
        Intent intent = new Intent();
        intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 0);
        intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, false);
        ArrayList<String> uidList = new ArrayList<>();
        uidList.add(MyApplication.getInstance().getUid());
        intent.putStringArrayListExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, uidList);
        intent.putExtra(ContactSearchFragment.EXTRA_TITLE,context.getString(R.string.news_share));
        intent.setClass(context,
                ContactSearchActivity.class);
        context.startActivity(intent);
    }
}

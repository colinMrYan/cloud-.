package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.View;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.dialog.ShareDialog;
import com.inspur.emmcloud.bean.chat.MessageForwardMultiBean;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.ui.ShareFilesActivity;
import com.inspur.emmcloud.ui.chat.MultiMessageTransmitUtil;
import com.inspur.emmcloud.ui.chat.mvp.view.ConversationSearchActivity;
import com.inspur.emmcloud.ui.chat.mvp.view.ConversationSendMultiActivity;

import java.io.Serializable;
import java.util.List;

import static com.inspur.emmcloud.ui.chat.MultiMessageTransmitUtil.EXTRA_MULTI_MESSAGE_TYPE;

public class ShareUtil {
    public static void share(Context context, final SearchModel searchModel, String shareContent) {
        share(context, searchModel, shareContent, false, MultiMessageTransmitUtil.TYPE_SINGLE);
    }

    public static void share(Context context, final SearchModel searchModel, String shareContent, final boolean isWebShare, final int multiMessageType) {
        final BaseActivity activity = (BaseActivity) context;
        int defaultIcon = CommunicationUtils.getDefaultHeadUrl(searchModel);
        String headUrl = CommunicationUtils.getHeadUrl(searchModel);
        //分享到
        ShareDialog.Builder builder = new ShareDialog.Builder(context);
        builder.setUserName(searchModel.getName());
        builder.setContent(shareContent);
        builder.setDefaultResId(defaultIcon);
        builder.setHeadUrl(headUrl);
        final ShareDialog dialog = builder.build();
        dialog.setCallBack(new ShareDialog.CallBack() {
            @Override
            public void onConfirm(View view) {
                if (searchModel.getType().equals(SearchModel.TYPE_USER) && searchModel.getId().equals(BaseApplication.getInstance().getUid())) {
                    ToastUtils.show(R.string.do_not_select_yourself);
                } else {
                    if (isWebShare && context instanceof ConversationSearchActivity) {
                        ((ConversationSearchActivity) activity).handleShareResult();
                    } else if(isWebShare && context instanceof ConversationSendMultiActivity){
                        ((ConversationSendMultiActivity) activity).handleShareResult();
                    }else {
                        Intent intent = new Intent();
                        intent.putExtra("searchModel", searchModel);
                        intent.putExtra(EXTRA_MULTI_MESSAGE_TYPE, multiMessageType);
                        activity.setResult(activity.RESULT_OK, intent);
                        dialog.dismiss();
                        activity.finish();
                    }
                }
            }

            @Override
            public void onCancel() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 分享到云+
     */
    public static void startVolumeShareActivity(Context context, List<String> uriList) {
        Intent intent = new Intent();
        intent.setClass(context, ShareFilesActivity.class);
        intent.putExtra(Constant.SHARE_FILE_URI_LIST, (Serializable) uriList);
        context.startActivity(intent);
    }

    // 消息转发消息时调用
    public static void shareMultiMessage(Context context, final List<MessageForwardMultiBean> searchModelList, String shareContent, int Con) {
        final BaseActivity activity = (BaseActivity) context;
        ShareMultiDialog.Builder builder = new ShareMultiDialog.Builder(context);
        builder.setConversationList(searchModelList);
        builder.setContent(shareContent);
        final ShareMultiDialog dialog = builder.build();
        dialog.setCallBack(new ShareMultiDialog.CallBack() {
            @Override
            public void onConfirm(View view) {
                Intent intent = new Intent();
                intent.putExtra("selectList", (Serializable) searchModelList);
                activity.setResult(Activity.RESULT_OK, intent);
                dialog.dismiss();
                activity.finish();
            }

            @Override
            public void onCancel() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // 消息转发多人时调用
    public static void shareMultiMembers(Context context, final List<MessageForwardMultiBean> searchModelList, String shareContent, final int multiMessageType) {
        final BaseActivity activity = (BaseActivity) context;
        ShareMultiDialog.Builder builder = new ShareMultiDialog.Builder(context);
        builder.setConversationList(searchModelList);
        builder.setContent(shareContent);
        final ShareMultiDialog dialog = builder.build();
        dialog.setCallBack(new ShareMultiDialog.CallBack() {
            @Override
            public void onConfirm(View view) {
                Intent intent = new Intent();
                intent.putExtra("selectList", (Serializable) searchModelList);
                intent.putExtra(EXTRA_MULTI_MESSAGE_TYPE, multiMessageType);
                activity.setResult(Activity.RESULT_OK, intent);
                dialog.dismiss();
                activity.finish();
            }

            @Override
            public void onCancel() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}

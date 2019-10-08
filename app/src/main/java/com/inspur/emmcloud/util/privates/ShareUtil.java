package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.dialog.ShareDialog;

public class ShareUtil {
    public static void share(Context context, final SearchModel searchModel, String shareContent) {
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
                Intent intent = new Intent();
                intent.putExtra("searchModel", searchModel);
                activity.setResult(activity.RESULT_OK, intent);
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

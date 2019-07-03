package com.inspur.emmcloud.web.plugin.share;

import android.content.Intent;
import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONObject;

import java.util.ArrayList;

public class CloudShareService extends ImpPlugin {

    @Override
    public void execute(String action, JSONObject paramsObject) {
        if (action.equals("share")) {
            Intent intent = new Intent();
//            intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 0);
//            intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, false);
            ArrayList<String> uidList = new ArrayList<>();
            uidList.add(BaseApplication.getInstance().getUid());
            intent.putStringArrayListExtra("excludeContactUidList", uidList);
            intent.putExtra("title", "分享到");
//            intent.setClass(BaseApplication.getInstance().getApplicationContext(),
//                    ContactSearchActivity.class);
//            startActivity(intent);
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("excludeContactUidList", uidList);
            bundle.putString("title", "分享到");
            ARouter.getInstance().build(Constant.AROUTER_CLASS_CONTACT_SEARCH)
                    .with(bundle)
                    .navigation();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        return null;
    }

    @Override
    public void onDestroy() {

    }
}

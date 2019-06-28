package com.inspur.emmcloud.basemodule.interf;

import android.content.Context;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.service.DegradeService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;

/**
 * ARouter自定义全局降级策略
 */
@Route(path = "/aRouter/degrade")
public class ARouterDegradeServiceimpl implements DegradeService {
    @Override
    public void onLost(Context context, Postcard postcard) {
        LogUtils.jasonDebug("onLost====================");
        ToastUtils.show("跳转失败");
    }

    @Override
    public void init(Context context) {

    }
}

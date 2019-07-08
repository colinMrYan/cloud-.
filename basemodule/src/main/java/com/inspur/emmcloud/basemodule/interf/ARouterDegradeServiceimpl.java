package com.inspur.emmcloud.basemodule.interf;

import android.content.Context;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.service.DegradeService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.config.Constant;

/**
 * ARouter自定义全局降级策略
 */
@Route(path = Constant.AROUTER_CLASS_AROUTER_DEGRADE)
public class ARouterDegradeServiceimpl implements DegradeService {
    @Override
    public void onLost(Context context, Postcard postcard) {
        LogUtils.jasonDebug("onLost====================");
    }

    @Override
    public void init(Context context) {

    }
}

package com.inspur.emmcloud.application.serviceimpl;


import com.inspur.emmcloud.application.ui.MyAppFragment;
import com.inspur.emmcloud.componentservice.appcenter.ApplicationService;

/**
 * Created by: yufuchang
 * Date: 2019/11/27
 */
public class AppCenterServiceImpl implements ApplicationService {

    @Override
    public Class getMyAppFragment() {
        return MyAppFragment.class;
    }
}

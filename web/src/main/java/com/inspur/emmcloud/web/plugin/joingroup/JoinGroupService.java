package com.inspur.emmcloud.web.plugin.joingroup;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONObject;

/**
 * Created by: yufuchang
 * Date: 2019/10/10
 */
public class JoinGroupService extends ImpPlugin {
    @Override
    public void execute(String action, JSONObject paramsObject) {
        Router router = Router.getInstance();
        if (router.getService(CommunicationService.class) != null) {
            CommunicationService service = router.getService(CommunicationService.class);
            service.openConversationByChannelId(paramsObject);
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

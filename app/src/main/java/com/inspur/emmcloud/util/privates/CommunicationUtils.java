package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.chat.Channel;

/**
 * Created by chenmch on 2018/4/26.
 */

public class CommunicationUtils {
     /* *
     * 获取频道名称
     * @param channel
     * @return
     */
    public static String getChannelDisplayTitle(Channel channel) {

        String title;
        if (channel.getType().equals("DIRECT")) {
            title = DirectChannelUtils.getDirectChannelTitle(MyApplication.getInstance(),
                    channel.getTitle());
        } else if (channel.getType().equals("SERVICE")) {
            title = DirectChannelUtils.getRobotInfo(MyApplication.getInstance(), channel.getTitle()).getName();
        } else {
            title = channel.getTitle();
        }
        return title;
    }



}

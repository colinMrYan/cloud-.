package com.inspur.emmcloud.util.privates.cache;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;

import java.util.List;

/**
 * Created by chenmch on 2018/5/10.
 */

public class ContactUserCacheUtils {
    public static void saveContactUserList(List<ContactUser> contactUserList){
        if (contactUserList == null || contactUserList.size() == 0) {
            return;
        }
        try {

            DbCacheUtils.getDb().saveOrUpdate(contactUserList);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void setLastQueryTime(long lastQueryTime){
        PreferencesByUserAndTanentUtils.putLong(MyApplication.getInstance(), Constant.PREF_CONTACT_USER_LASTQUERYTIME,lastQueryTime);
    }

    public static Long getLastQueryTime(){
        return  PreferencesByUserAndTanentUtils.getLong(MyApplication.getInstance(), Constant.PREF_CONTACT_USER_LASTQUERYTIME,0L);
    }

}

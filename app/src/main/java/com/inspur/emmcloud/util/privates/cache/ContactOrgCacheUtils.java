package com.inspur.emmcloud.util.privates.cache;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;

import java.util.List;

/**
 * Created by chenmch on 2018/5/10.
 */

public class ContactOrgCacheUtils {
    public static void saveContactOrgList(List<ContactOrg> contactOrgList){
        if (contactOrgList == null || contactOrgList.size() == 0) {
            return;
        }
        try {
            DbCacheUtils.getDb().saveOrUpdate(contactOrgList);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void setLastQueryTime(long lastQueryTime){
        PreferencesByUserAndTanentUtils.putLong(MyApplication.getInstance(), Constant.PREF_CONTACT_ORG_LASTQUERYTIME,lastQueryTime);
    }

    public static Long getLastQueryTime(){
        return  PreferencesByUserAndTanentUtils.getLong(MyApplication.getInstance(), Constant.PREF_CONTACT_ORG_LASTQUERYTIME,0L);
    }

}

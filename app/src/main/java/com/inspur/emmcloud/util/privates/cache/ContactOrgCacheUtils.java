package com.inspur.emmcloud.util.privates.cache;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.StringUtils;
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


        /**
     * 存储更新后客户端通讯录显示起始位置
     *
     * @param unitID
     */
    public static void setContactOrgRootId( String rootId) {
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_CONTACT_ORG_ROOT_ID, rootId);
    }

    /**
     * 获取通讯录显示起始级别，如集团，单位，部门
     *
     * @return
     */
    public static String getContactOrgRootId() {
        return PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_CONTACT_ORG_ROOT_ID, "");
    }

    /**
     * 获取ContactOrg
     * @param id
     */
    public static  ContactOrg getContactOrg(String id){
        try {
            ContactOrg contactOrg = DbCacheUtils.getDb().findById(ContactOrg.class,id);
            return contactOrg;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取根组织架构
     * @return
     */
    public static ContactOrg getRootContactOrg() {
        ContactOrg contactOrg = null;
        try {
            String contactOrgRootId = getContactOrgRootId();
            if (!StringUtils.isBlank(contactOrgRootId)) {
                contactOrg = DbCacheUtils.getDb().findById(ContactOrg.class,contactOrgRootId);
            } else {
                contactOrg = DbCacheUtils.getDb().selector(ContactOrg.class).where(
                        "parentId", "=", "root").findFirst();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return contactOrg;
    }
}

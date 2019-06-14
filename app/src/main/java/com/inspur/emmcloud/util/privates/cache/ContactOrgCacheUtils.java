package com.inspur.emmcloud.util.privates.cache;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.componentservice.contact.ContactUser;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/5/10.
 */

public class ContactOrgCacheUtils {
    public static void saveContactOrgList(List<ContactOrg> contactOrgList) {
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

    public static void deleteContactOrgList(List<String> orgIdList) {
        if (orgIdList == null || orgIdList.size() == 0) {
            return;
        }
        try {

            DbCacheUtils.getDb().delete(ContactOrg.class, WhereBuilder.b("id", "in", orgIdList));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Long getLastQueryTime() {
        return PreferencesByUserAndTanentUtils.getLong(MyApplication.getInstance(), Constant.PREF_CONTACT_ORG_LASTQUERYTIME, 0L);
    }

    public static void setLastQueryTime(long lastQueryTime) {
        PreferencesByUserAndTanentUtils.putLong(MyApplication.getInstance(), Constant.PREF_CONTACT_ORG_LASTQUERYTIME, lastQueryTime);
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
     * 存储更新后客户端通讯录显示起始位置
     *
     * @param unitID
     */
    public static void setContactOrgRootId(String rootId) {
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_CONTACT_ORG_ROOT_ID, rootId);
    }

    /**
     * 获取ContactOrg
     *
     * @param id
     */
    public static ContactOrg getContactOrg(String id) {
        try {
            ContactOrg contactOrg = DbCacheUtils.getDb().findById(ContactOrg.class, id);
            return contactOrg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取根组织架构
     *
     * @return
     */
    public static ContactOrg getRootContactOrg() {
        ContactOrg contactOrg = null;
        try {
            String contactOrgRootId = getContactOrgRootId();
            if (!StringUtils.isBlank(contactOrgRootId)) {
                contactOrg = DbCacheUtils.getDb().findById(ContactOrg.class, contactOrgRootId);
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

    /**
     * 获取组织架构下的组织和人员
     *
     * @param contactOrgId
     * @return
     */
    public static List<Contact> getChildContactList(String contactOrgId) {
        List<Contact> contactList = new ArrayList<>();
        try {
            // 组织下的组织架构列表
            List<ContactOrg> contactOrgList = DbCacheUtils.getDb().selector(ContactOrg.class).where("parentId", "=", contactOrgId).orderBy("sortOrder").findAll();
            List<ContactUser> contactUserList = DbCacheUtils.getDb().selector(ContactUser.class).where("parentId", "=", contactOrgId).orderBy("sortOrder").findAll();
            if (contactOrgList != null) {
                contactList.addAll(Contact.contactOrgList2ContactList(contactOrgList));
            }
            if (contactUserList != null) {
                contactList.addAll(Contact.contactUserList2ContactList(contactUserList));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contactList;
    }
}

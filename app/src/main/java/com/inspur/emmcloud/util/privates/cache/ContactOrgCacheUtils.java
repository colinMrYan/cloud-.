package com.inspur.emmcloud.util.privates.cache;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.bean.contact.MultiOrg;
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
     * @param rootId
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

    /**
     * 获取组织架构下的组织和人员
     *
     * @param contactOrgId
     * @return
     */
    public static List<Contact> getChildContactList(String contactOrgId, List<String> excludeUidList) {
        List<Contact> contactList = new ArrayList<>();
        try {
            String excludeUidSql = "()";
            if (excludeUidList != null && excludeUidList.size() > 0) {
                excludeUidSql = excludeUidSql.substring(0, excludeUidSql.length() - 1);
                for (String uid : excludeUidList) {
                    excludeUidSql = excludeUidSql + uid + ",";
                }
                if (excludeUidSql.endsWith(",")) {
                    excludeUidSql = excludeUidSql.substring(0, excludeUidSql.length() - 1);
                }
                excludeUidSql = excludeUidSql + ")";
            }
            // 组织下的组织架构列表
            List<ContactOrg> contactOrgList = DbCacheUtils.getDb().selector(ContactOrg.class).where("parentId", "=", contactOrgId).orderBy("sortOrder").findAll();
            List<ContactUser> contactUserList = DbCacheUtils.getDb().selector(ContactUser.class).where("parentId", "=", contactOrgId).and(WhereBuilder.b().expr("id not in" + excludeUidSql)).orderBy("sortOrder").findAll();
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

    /**
     * 删除MultiOrg
     */
    private static void deleteMultiOrg(){
        try {
            DbCacheUtils.getDb().dropTable(MultiOrg.class);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 保存多组织数据
     * @param multiOrgList
     */
    public static void saveMultiOrg(List<MultiOrg> multiOrgList){
        deleteMultiOrg();
        try {
            DbCacheUtils.getDb().saveOrUpdate(multiOrgList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据人员inspurId获取人员的所有组织
     * @param inspurId
     * @return
     */
    public static List<ContactOrg> getMultiOrgByInspurId(String inspurId){
        List<ContactOrg> contactOrgList = new ArrayList<>();
        try {
            List<MultiOrg> multiOrgList = DbCacheUtils.getDb().selector(MultiOrg.class).where("inspurId", "=", inspurId).findAll();
            for (int i = 0; i < multiOrgList.size(); i++) {
                contactOrgList.add(getContactOrg(multiOrgList.get(i).getOrgId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contactOrgList;
    }
}

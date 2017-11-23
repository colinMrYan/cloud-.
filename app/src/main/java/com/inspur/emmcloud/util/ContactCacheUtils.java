package com.inspur.emmcloud.util;

import android.content.Context;

import com.inspur.emmcloud.bean.Contact;
import com.inspur.emmcloud.bean.PersonDto;
import com.inspur.emmcloud.bean.Robot;

import org.json.JSONArray;
import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ContactCacheUtils {

    /**
     * 存储通讯录列表
     *
     * @param context
     * @param contactList
     */
    public static void saveContactList(Context context,
                                       List<Contact> contactList) {
        if (contactList == null || contactList.size() == 0) {
            return;
        }
        try {

            DbCacheUtils.getDb(context).saveOrUpdate(contactList);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 存储通讯录列表
     *
     * @param context
     */
    public static void saveContact(Context context,
                                   Contact contact) {
        if (contact == null) {
            return;
        }
        try {
            DbCacheUtils.getDb(context).saveOrUpdate(contact);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 删除通讯录
     *
     * @param deleteIdArray 删除通讯录id的json数组
     */
    public static void deleteContact(final Context context,
                                     final JSONArray deleteIdArray) {
        if (deleteIdArray == null || deleteIdArray.length() == 0) {
            return;
        }
        try {

            for (int i = 0; i < deleteIdArray.length(); i++) {
                String id = deleteIdArray.getString(i);
                DbCacheUtils.getDb(context).deleteById(Contact.class, id);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    /**
     * 删除通讯录
     *
     * @param deleteIdList 删除通讯录id的List
     */
    public static void deleteContact(final Context context,
                                     final List<String> deleteIdList) {
        if (deleteIdList == null || deleteIdList.size() == 0) {
            return;
        }
        try {

            for (int i = 0; i < deleteIdList.size(); i++) {
                String id = deleteIdList.get(i);
                DbCacheUtils.getDb(context).deleteById(Contact.class, id);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    /**
     * 保存此租户通讯录客户端最后的更新时间
     *
     * @param time
     */
    public static void saveLastUpdateTime(Context context, String time) {
        PreferencesByUserAndTanentUtils.putString(context, "contactUpdateTime", time);
    }

    /**
     * 获取此租户通讯录客户端最后的更新时间
     *
     * @return
     */
    public static String getLastUpdateTime(Context context) {
        return PreferencesByUserAndTanentUtils.getString(context, "contactUpdateTime", "");
    }

    /**
     * 存储更新后客户端通讯录显示起始位置
     *
     * @param context
     * @param unitID
     */
    public static void saveLastUpdateunitID(Context context, String unitID) {
        PreferencesByUserAndTanentUtils.putString(context, "unitID", unitID);
    }

    /**
     * 获取通讯录显示起始级别，如集团，单位，部门
     *
     * @param context
     * @return
     */
    public static String getLastUpdateunitID(Context context) {
        return PreferencesByUserAndTanentUtils.getString(context, "unitID", "");
    }

    /**
     * 获取根组织架构
     *
     * @param context
     * @return +
     */
    public static Contact getRootContact(Context context) {
        Contact contact = null;
        try {
            String unitID = "";
            if (!StringUtils.isBlank(PreferencesByUserAndTanentUtils.getString(context, "unitID", ""))) {
                unitID = PreferencesByUserAndTanentUtils.getString(context, "unitID", "");
                contact = DbCacheUtils.getDb(context).selector(Contact.class).where(
                        "id", "=", unitID).findFirst();
            } else {
                contact = DbCacheUtils.getDb(context).selector(Contact.class).where(
                        "parentId", "=", "root").findFirst();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return contact;
    }

    /**
     * 获取组织架构下的组织和人员列表
     *
     * @param contactId
     * @return
     */
    public static List<Contact> getChildContactList(Context context,
                                                    String contactId) {
        List<Contact> childContactList = null;
        try {

            // 组织下的组织架构列表
            List<Contact> childStructList = DbCacheUtils.getDb(context).selector
                    (Contact.class).where("parentId", "=", contactId)
                    .and("type", "=", "").orderBy("sortOrder").findAll();
            // 组织下的人员列表
            List<Contact> childUserList = DbCacheUtils.getDb(context).selector
                    (Contact.class).where("parentId", "=", contactId)
                    .and("type", "=", "user")
                    .orderBy("sortOrder").findAll();
            if (childStructList != null && childUserList != null) {
                childStructList.addAll(childUserList);
                childContactList = childStructList;
            } else if (childStructList != null) {
                childContactList = childStructList;
            } else if (childUserList != null) {
                childContactList = childUserList;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (childContactList == null) {
            childContactList = new ArrayList<Contact>();
        }
        return childContactList;
    }


    /**
     * 搜索子目录中符合条件的通讯录
     *
     * @param context
     * @param searchText
     * @return
     */
    public static List<Contact> getSearchContact(Context context,
                                                 String searchText, List<Contact> haveSearchContactList, int limit) {
        //数字大于4位搜索手机号
        if (searchText.length() > 3 && StringUtils.isNumeric(searchText)) {
            return getSearchContactByPhoneNum(context, searchText, haveSearchContactList, limit);
        }
        String searchStr = searchText;
        String noInSql = "()";
        noInSql = getNoInSql(noInSql, haveSearchContactList);
        List<Contact> searchContactList = new ArrayList<>();
        try {
            List<Contact> searchContactList1 = DbCacheUtils.getDb(context).selector
                    (Contact.class)
                    .where("type", "=", "user")
                    .and(WhereBuilder.b("pinyin", "=", searchStr)
                            .or("realName", "=", searchStr)
                            .or("globalName", "=", searchStr)
                            .or("code", "=", searchStr))
                    .and(WhereBuilder.b().expr("id not in" + noInSql))
                    .limit(limit).findAll();
            searchContactList.addAll(searchContactList1);
            noInSql = getNoInSql(noInSql, searchContactList);
            if (limit == -1 || searchContactList.size() < limit) {
                searchStr = searchText + "%";
                List<Contact> searchContactList2 = DbCacheUtils.getDb(context).selector
                        (Contact.class)
                        .where("type", "=", "user")
                        .and(WhereBuilder.b("pinyin", "like", searchStr)
                                .or("realName", "like", searchStr)
                                .or("globalName", "like", searchStr)
                                .or("code", "like", searchStr))
                        .and(WhereBuilder.b().expr("id not in" + noInSql))
                        .limit(limit - searchContactList.size()).findAll();
                searchContactList.addAll(searchContactList.size(), searchContactList2);
                noInSql = getNoInSql(noInSql, searchContactList);
            }

            if (limit == -1 || searchContactList.size() < limit) {
                searchStr = "%" + searchText;
                List<Contact> searchContactList3 = DbCacheUtils.getDb(context).selector
                        (Contact.class)
                        .where("type", "=", "user")
                        .and(WhereBuilder.b("pinyin", "like", searchStr)
                                .or("realName", "like", searchStr)
                                .or("globalName", "like", searchStr)
                                .or("code", "like", searchStr))
                        .and(WhereBuilder.b().expr("id not in" + noInSql))
                        .limit(limit - searchContactList.size()).findAll();
                searchContactList.addAll(searchContactList.size(), searchContactList3);
                noInSql = getNoInSql(noInSql, searchContactList);
            }

            if (limit == -1 || searchContactList.size() < limit) {
                searchStr = "%" + searchText + "%";
                List<Contact> searchContactList4 = DbCacheUtils.getDb(context).selector
                        (Contact.class)
                        .where("type", "=", "user")
                        .and(WhereBuilder.b("pinyin", "like", searchStr)
                                .or("realName", "like", searchStr)
                                .or("globalName", "like", searchStr)
                                .or("code", "like", searchStr))
                        .and(WhereBuilder.b().expr("id not in" + noInSql))
                        .limit(limit - searchContactList.size()).findAll();
                searchContactList.addAll(searchContactList.size(), searchContactList4);
                noInSql = getNoInSql(noInSql, searchContactList);
            }

            if (limit == -1 || searchContactList.size() < limit) {
                searchStr = "";
                for (int i = 0; i < searchText.length(); i++) {
                    if (i < searchText.length() - 1) {
                        searchStr += "%" + searchText.charAt(i);
                    } else {
                        searchStr += "%" + searchText.charAt(i) + "%";
                    }
                }
                List<Contact> searchContactList5 = DbCacheUtils.getDb(context).selector
                        (Contact.class)
                        .where("type", "=", "user")
                        .and(WhereBuilder.b("pinyin", "like", searchStr)
                                .or("realName", "like", searchStr)
                                .or("globalName", "like", searchStr)
                                .or("code", "like", searchStr))
                        .and(WhereBuilder.b().expr("id not in" + noInSql))
                        .limit(limit - searchContactList.size()).findAll();
                searchContactList.addAll(searchContactList.size(), searchContactList5);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (searchContactList == null) {
            searchContactList = new ArrayList<Contact>();
        }
        return searchContactList;
    }

    /**
     * 获取sql中的id数组
     *
     * @param noInSql
     * @param addSearchContactList
     * @return
     */
    private static String getNoInSql(String noInSql, List<Contact> addSearchContactList) {
        if (addSearchContactList != null && addSearchContactList.size() > 0) {
            noInSql = noInSql.substring(0, noInSql.length() - 1);
            if (noInSql.length() > 1) {
                noInSql = noInSql + ",";
            }
            for (int i = 0; i < addSearchContactList.size(); i++) {
                noInSql = noInSql + addSearchContactList.get(i).getId() + ",";
            }
            if (noInSql.endsWith(",")) {
                noInSql = noInSql.substring(0, noInSql.length() - 1);
            }
            noInSql = noInSql + ")";
        }
        return noInSql;
    }

    /**
     * 通过手机号搜索通讯录
     *
     * @param context
     * @param searchText
     * @param haveSearchContactList
     * @param limit
     * @return
     */
    public static List<Contact> getSearchContactByPhoneNum(Context context,
                                                           String searchText, List<Contact> haveSearchContactList, int limit) {
        List<Contact> searchContactList = null;
        searchText = "%" + searchText + "%";
        int offset = haveSearchContactList == null ? 0 : haveSearchContactList.size();
        try {
            searchContactList = DbCacheUtils.getDb(context).selector
                    (Contact.class)
                    .where("type", "=", "user")
                    .and(WhereBuilder.b("mobile", "like", searchText)
                            .or("realName", "like", searchText)
                    )
                    .offset(offset)
                    .limit(limit).findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (searchContactList == null) {
            searchContactList = new ArrayList<Contact>();
        }
        return searchContactList;
    }


    /**
     * 获取用户名
     *
     * @param context
     * @param uid
     * @return
     */
    public static String getUserName(Context context, String uid) {
        String name = "";
        try {
            Contact contact = DbCacheUtils.getDb(context).selector(Contact.class).where(
                    "inspurID", "=", uid).findFirst();
            if (contact != null) {
                name = contact.getRealName();
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return name;
    }


    /**
     * 通过id List获取PersonDto对象的List
     *
     * @param context
     * @param uidList
     * @return
     */
    public static List<PersonDto> getShowMemberList(Context context, List<String> uidList) {
        List<Contact> userList = new ArrayList<Contact>();
        List<Robot> robotList = new ArrayList<>();
        List<PersonDto> unitMemberList = new ArrayList<>();
        try {
            userList = DbCacheUtils.getDb(context).selector(Contact.class).where("inspurID",
                    "in", uidList).findAll();
            robotList = DbCacheUtils.getDb(context).selector(Robot.class).where("id",
                    "in", uidList).findAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        }

        if (userList != null) {
            Iterator<Contact> contactListIterator = userList.iterator();
            while (contactListIterator.hasNext()) {
                Contact contact = contactListIterator.next();
                PersonDto personDto = new PersonDto();
                personDto.setName(contact.getRealName());
                personDto.setUid(contact.getInspurID());
                personDto.setSortLetters(contact.getPinyin().substring(0, 1));
                personDto.setPinyinFull(contact.getPinyin());
                personDto.setSuoxie(PinyinUtils.getPinYinHeadChar(contact
                        .getRealName()));
                personDto.setUtype("contact");
                unitMemberList.add(personDto);
            }
        }

        if (robotList != null) {
            Iterator<Robot> robotListIterator = robotList.iterator();
            while (robotListIterator.hasNext()) {
                Robot robot = robotListIterator.next();
                PersonDto personDto = new PersonDto();
                personDto.setName(robot.getName());
                personDto.setUid(robot.getId());
                personDto.setSortLetters(PinyinUtils.getPingYin(robot.getName()).substring(0, 1));
                personDto.setPinyinFull(PinyinUtils.getPingYin(robot.getName()));
                personDto.setSuoxie(PinyinUtils.getPinYinHeadChar(robot.getName()));
                personDto.setUtype("robot");
                unitMemberList.add(personDto);
            }
        }

        if (unitMemberList == null) {
            unitMemberList = new ArrayList<PersonDto>();
        }
        return unitMemberList;

    }


    /**
     * 按顺序通过id List获取contact对象的List
     *
     * @param context
     * @param uidList
     * @return
     */
    public static List<Contact> getSoreUserList(Context context, List<String> uidList) {
        List<Contact> userList = new ArrayList<Contact>();
        try {

            for (int i = 0; i < uidList.size(); i++) {
                Contact contact = getUserContact(context, uidList.get(i));
                if (contact != null) {
                    userList.add(contact);
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        }
        return userList;

    }


    /**
     * 按顺序通过id List获取contact对象的List
     *
     * @param context
     * @param uidArray
     * @return
     */
    public static List<Contact> getSoreUserList(Context context, JSONArray uidArray) {
        List<Contact> userList = new ArrayList<Contact>();
        try {

            for (int i = 0; i < uidArray.length(); i++) {
                Contact contact = getUserContact(context, uidArray.getString(i));
                if (contact != null) {
                    userList.add(contact);
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        }
        return userList;

    }


    public static List<Map<String, String>> getUserMap(Context context,
                                                       JSONArray uidArray) {
        List<Map<String, String>> userMapList = new ArrayList<Map<String, String>>();
        try {

            for (int i = 0; i < uidArray.length(); i++) {
                String uid = uidArray.getString(i);
                Contact contact = DbCacheUtils.getDb(context).selector(Contact.class)
                        .where("inspurID", "=", uid).findFirst();
                if (contact != null) {
                    String name = contact.getRealName();
                    Map<String, String> userMap = new HashMap<String, String>();
                    userMap.put(uid, name);
                    userMapList.add(userMap);
                }

            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        }
        if (userMapList == null) {
            userMapList = new ArrayList<Map<String, String>>();
        }
        return userMapList;

    }

    /**
     * 获取通讯录
     *
     * @param context
     * @param uid
     * @return
     */
    public static Contact getUserContact(Context context, String uid) {
        Contact contact = null;
        try {
            contact = DbCacheUtils.getDb(context).selector(Contact.class).where("inspurID",
                    "=", uid).findFirst();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return contact;
    }

    public static Contact getStructContact(Context context, String structId) {
        Contact contact = null;
        try {
            contact = DbCacheUtils.getDb(context).selector(Contact.class).where("id",
                    "=", structId).and("type", "!=", "user").findFirst();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return contact;
    }

    /**
     * 根据Email查询联系人的接口
     * ReactNative中周计划使用
     *
     * @param context
     * @param email
     * @return
     */
    public static Contact getContactByEmail(Context context, String email) {
        Contact contact = null;
        try {
            contact = DbCacheUtils.getDb(context).selector(Contact.class).where("email",
                    "=", email).findFirst();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return contact;
    }

}

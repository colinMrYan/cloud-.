package com.inspur.emmcloud.util.privates.cache;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.PinyinUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.bean.chat.PersonDto;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenmch on 2018/5/10.
 */

public class ContactUserCacheUtils {
    public static void saveContactUserList(List<ContactUser> contactUserList) {
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

    public static void saveContactUser(ContactUser contactUser) {
        if (contactUser == null) {
            return;
        }
        try {

            DbCacheUtils.getDb().saveOrUpdate(contactUser);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void deleteContactUserList(List<String> uidList) {
        if (uidList == null || uidList.size() == 0) {
            return;
        }
        try {

            DbCacheUtils.getDb().delete(ContactUser.class, WhereBuilder.b("id", "in", uidList));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getUserMail(String uid) {
        String mail = "";
        try {
            ContactUser contactUser = DbCacheUtils.getDb().findById(ContactUser.class, uid);
            if (contactUser != null) {
                mail = contactUser.getEmail();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mail;
    }

    public static Long getLastQueryTime() {
        return PreferencesByUserAndTanentUtils.getLong(MyApplication.getInstance(), Constant.PREF_CONTACT_USER_LASTQUERYTIME, 0L);
    }

    public static void setLastQueryTime(long lastQueryTime) {
        PreferencesByUserAndTanentUtils.putLong(MyApplication.getInstance(), Constant.PREF_CONTACT_USER_LASTQUERYTIME, lastQueryTime);
    }

    /**
     * 获取用户名（机器人和人）
     *
     * @param uid
     * @return
     */
    public static String getUserName(String uid) {
        String userName = "";
        try {
            if (uid.startsWith("BOT")) {
                Robot robot = RobotCacheUtils
                        .getRobotById(MyApplication.getInstance(), uid);
                if (robot != null) {
                    userName = robot.getName();
                }
            } else {
                ContactUser contactUser = DbCacheUtils.getDb().findById(ContactUser.class, uid);
                if (contactUser != null) {
                    userName = contactUser.getName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userName;
    }


    /**
     * 通过用户名获取Contact
     *
     * @param userName
     * @return
     */
    public static ContactUser getContactUserByUserName(String userName) {
        ContactUser contactUser = null;
        try {
            contactUser = DbCacheUtils.getDb().selector(ContactUser.class).where(
                    "name", "=", userName).findFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contactUser;
    }


    /**
     * 按传入顺序返回ContactUser
     * uidList为需要查询的完整列表
     * limit为返回个数
     * limit*3为每次查询的步长
     *
     * @param uidList
     * @param limit
     * @return
     */
    public static List<ContactUser> getContactUserListByIdListOrderBy(final List<String> uidList, int limit) {
        List<ContactUser> contactUserList = new ArrayList<>();
        List<ContactUser> searchResultContactUserList = new ArrayList<>();
        int listSize = uidList.size();
        int stepSize = limit * 3;
        int toIndex = stepSize;
        //三十个一组查询直到查完列表或者查到多于九个
        for (int i = 0; i < uidList.size(); i += stepSize) {
            if (i + stepSize > listSize) {
                toIndex = listSize - i;
            }
            List newList = uidList.subList(i, i + toIndex);
            List<ContactUser> contactUserInList = ContactUserCacheUtils.getContactUserListById(newList);
            searchResultContactUserList.addAll(contactUserInList);
            if (contactUserInList.size() >= limit) {
                break;
            }
        }
        //按照顺序取出需要显示的头像的ContactUser
        for (int i = 0; i < uidList.size(); i++) {
            ContactUser contactUser = new ContactUser();
            contactUser.setId(uidList.get(i));
            int index = searchResultContactUserList.indexOf(contactUser);
            if (index != -1) {
                contactUserList.add(searchResultContactUserList.get(index));
            }
            if (contactUserList.size() >= limit) {
                break;
            }
        }
        return contactUserList;
    }


    /**
     * 获取通讯录列表
     *
     * @param uidList
     * @return
     */
    public static List<ContactUser> getContactUserListById(final List<String> uidList) {
        List<ContactUser> contactUserList = null;
        try {
            contactUserList = DbCacheUtils.getDb().selector(ContactUser.class).where("id",
                    "in", uidList).findAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        }
        if (contactUserList == null) {
            contactUserList = new ArrayList<>();
        }
        return contactUserList;
    }


    /**
     * 通过id List获取PersonDto对象的List
     *
     * @param uidList
     * @return
     */
    public static List<PersonDto> getShowMemberList(List<String> uidList) {
        List<ContactUser> userList = new ArrayList<>();
        List<Robot> robotList = new ArrayList<>();
        List<PersonDto> unitMemberList = new ArrayList<>();
        try {
            userList = DbCacheUtils.getDb().selector(ContactUser.class).where("id",
                    "in", uidList).findAll();
            robotList = DbCacheUtils.getDb().selector(Robot.class).where("id",
                    "in", uidList).findAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        }

        if (userList != null) {
            Iterator<ContactUser> contactListIterator = userList.iterator();
            while (contactListIterator.hasNext()) {
                ContactUser contactUser = contactListIterator.next();
                PersonDto personDto = new PersonDto();
                personDto.setName(contactUser.getName());
                personDto.setUid(contactUser.getId());
                personDto.setSortLetters(contactUser.getPinyin().substring(0, 1));
                personDto.setPinyinFull(contactUser.getPinyin());
                personDto.setSuoxie(PinyinUtils.getPinYinHeadChar(contactUser
                        .getName()));
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
     * @param uidList
     * @return
     */
    public static List<ContactUser> getSoreUserList(List<String> uidList) {
        List<ContactUser> userList = new ArrayList<>();
        try {
            for (String uid : uidList) {
                ContactUser contactUser = getContactUserByUid(uid);
                if (contactUser != null) {
                    userList.add(contactUser);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        }
        return userList;

    }

    /**
     * 通过id获取ContactUser
     *
     * @param uid
     * @return
     */
    public static ContactUser getContactUserByUid(String uid) {
        try {
            ContactUser contactUser = DbCacheUtils.getDb().findById(ContactUser.class, uid);
            return contactUser;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据Email查询联系人的接口
     * ReactNative中周计划使用
     *
     * @param email
     * @return
     */
    public static ContactUser getContactUserByEmail(String email) {
        ContactUser contactUser = null;
        try {
            contactUser = DbCacheUtils.getDb().selector(ContactUser.class).where("email",
                    "=", email).findFirst();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return contactUser;
    }


    /**
     * 通过手机号搜索通讯录
     *
     * @param searchText
     * @param haveSearchContactList
     * @param limit
     * @return
     */
    public static List<Contact> getSearchContactByPhoneNum(String searchText, String noInSql, int limit) {
        List<Contact> searchContactList = null;
        searchText = "%" + searchText + "%";
        try {
            List<ContactUser> contactUserList = DbCacheUtils.getDb().selector
                    (ContactUser.class)
                    .where(WhereBuilder.b("mobile", "like", searchText)
                            .or("name", "like", searchText)
                    )
                    .and(WhereBuilder.b().expr("id not in" + noInSql))
                    .limit(limit).findAll();
            if (contactUserList != null) {
                searchContactList = Contact.contactUserList2ContactList(contactUserList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (searchContactList == null) {
            searchContactList = new ArrayList<>();
        }
        return searchContactList;
    }


    /**
     * 搜索子目录中符合条件的通讯录
     *
     * @param searchText
     * @param excludeContactList 排除掉某些数据
     * @param limit
     * @return
     */
    public static List<Contact> getSearchContact(String searchText, List<Contact> excludeContactList, int limit) {
        searchText = searchText.trim();
        //解决ios通讯录复制后手机号带特殊字符的问题
        String iosSpecialString1 = (char) (8236) + "";
        String iosSpecialString2 = (char) (8237) + "";
        searchText = searchText.replaceAll(iosSpecialString1, "").replaceAll(iosSpecialString2, "");
        String searchStr = searchText;
        String noInSql = "()";
        noInSql = getNoInSql(noInSql, excludeContactList);
        //数字大于4位搜索手机号
        if (searchText.length() > 3 && StringUtils.isNumeric(searchText)) {
            return getSearchContactByPhoneNum(searchText, noInSql, limit);
        }
        List<Contact> searchContactList = new ArrayList<>();
        try {
            List<ContactUser> searchContactUserList1 = DbCacheUtils.getDb().selector
                    (ContactUser.class)
                    .where(WhereBuilder.b().expr("id not in" + noInSql))
                    .and(WhereBuilder.b("pinyin", "=", searchStr)
                            .or("name", "=", searchStr)
                            .or("nameGlobal", "=", searchStr)
                            .or("code", "=", searchStr)
                    )
                    .limit(limit).findAll();
            if (searchContactUserList1 != null) {
                searchContactList.addAll(Contact.contactUserList2ContactList(searchContactUserList1));
                noInSql = getNoInSql(noInSql, searchContactList);
            }

            if (limit == -1 || searchContactList.size() < limit) {
                searchStr = searchText + "%";
                List<ContactUser> searchContactUserList2 = DbCacheUtils.getDb().selector
                        (ContactUser.class)
                        .where(WhereBuilder.b().expr("id not in" + noInSql))
                        .and(WhereBuilder.b("pinyin", "like", searchStr)
                                .or("name", "like", searchStr)
                                .or("nameGlobal", "like", searchStr)
                                .or("code", "like", searchStr))
                        .limit(limit - searchContactList.size()).findAll();
                if (searchContactUserList2 != null) {
                    searchContactList.addAll(Contact.contactUserList2ContactList(searchContactUserList2));
                    noInSql = getNoInSql(noInSql, searchContactList);
                }
            }

            if (limit == -1 || searchContactList.size() < limit) {
                searchStr = "%" + searchText;
                List<ContactUser> searchContactUserList3 = DbCacheUtils.getDb().selector
                        (ContactUser.class)
                        .where(WhereBuilder.b().expr("id not in" + noInSql))
                        .and(WhereBuilder.b("pinyin", "like", searchStr)
                                .or("name", "like", searchStr)
                                .or("nameGlobal", "like", searchStr))
                        .limit(limit - searchContactList.size()).findAll();
                if (searchContactUserList3 != null) {
                    searchContactList.addAll(Contact.contactUserList2ContactList(searchContactUserList3));
                    noInSql = getNoInSql(noInSql, searchContactList);
                }
            }
            if (limit == -1 || searchContactList.size() < limit) {
                searchStr = "%" + searchText + "%";
                List<ContactUser> searchContactUserList4 = DbCacheUtils.getDb().selector
                        (ContactUser.class)
                        .where(WhereBuilder.b().expr("id not in" + noInSql))
                        .and(WhereBuilder.b("pinyin", "like", searchStr)
                                .or("name", "like", searchStr)
                                .or("nameGlobal", "like", searchStr))
                        .limit(limit - searchContactList.size()).findAll();
                if (searchContactUserList4 != null) {
                    searchContactList.addAll(Contact.contactUserList2ContactList(searchContactUserList4));
                    noInSql = getNoInSql(noInSql, searchContactList);
                }
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
                List<ContactUser> searchContactList5 = DbCacheUtils.getDb().selector
                        (ContactUser.class)
                        .where(WhereBuilder.b().expr("id not in" + noInSql))
                        .and(WhereBuilder.b("pinyin", "like", searchStr)
                                .or("name", "like", searchStr)
                                .or("nameGlobal", "like", searchStr))
                        .limit(limit - searchContactList.size()).findAll();
                if (searchContactList5 != null) {
                    searchContactList.addAll(Contact.contactUserList2ContactList(searchContactList5));
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
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
}

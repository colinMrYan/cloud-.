package com.inspur.emmcloud.util.privates.cache;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.chat.PersonDto;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.PinyinUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;

import java.util.ArrayList;
import java.util.Iterator;
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

    public static void saveContactUser(ContactUser contactUser){
        if (contactUser == null ) {
            return;
        }
        try {

            DbCacheUtils.getDb().saveOrUpdate(contactUser);
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

    /**
     * 获取用户名（机器人和人）
     * @param uid
     * @return
     */
    public static String getUserName(String uid){
        String userName = "";
        try {
            if (uid.startsWith("BOT")){
                Robot robot =  RobotCacheUtils
                        .getRobotById(MyApplication.getInstance(), uid);
                if (robot != null){
                    userName = robot.getName();
                }
            }else {
                ContactUser contactUser = DbCacheUtils.getDb().findById(ContactUser.class, uid);
                if (contactUser != null) {
                    userName = contactUser.getName();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return userName;
    }


        /**
     * 通过用户名获取Contact
     * @param userName
     * @return
     */
    public static ContactUser getContactUserByUserName(String userName){
        ContactUser contactUser = null;
        try {
            contactUser = DbCacheUtils.getDb().selector(ContactUser.class).where(
                    "name", "=", userName).findFirst();
        }catch (Exception e){
            e.printStackTrace();
        }
        return contactUser;
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
     * @param context
     * @param uidList
     * @return
     */
    public static List<PersonDto> getShowMemberList( List<String> uidList) {
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
            for (String uid:uidList) {
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
     * @param uid
     * @return
     */
    public static ContactUser getContactUserByUid(String uid){
        try {
            ContactUser contactUser = DbCacheUtils.getDb().findById(ContactUser.class,uid);
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
     * @param context
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

}

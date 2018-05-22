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

    public static String getUserName(String uid){
        String userName = "";
        try {
            ContactUser contactUser = DbCacheUtils.getDb().findById(ContactUser.class,uid);
            if (contactUser != null){
                userName = contactUser.getName();
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
    public static ContactUser getContactUser(String userName){
        ContactUser contactUser = null;
        try {
            contactUser = DbCacheUtils.getDb().selector(ContactUser.class).where(
                    "name", "=", userName).findFirst();
        }catch (Exception e){
            e.printStackTrace();
        }
        return contactUser;
    }


    //    /**
//     * 通过id List获取PersonDto对象的List
//     *
//     * @param context
//     * @param uidList
//     * @return
//     */
//    public static List<PersonDto> getShowMemberList(Context context, List<String> uidList) {
//        List<Contact> userList = new ArrayList<Contact>();
//        List<Robot> robotList = new ArrayList<>();
//        List<PersonDto> unitMemberList = new ArrayList<>();
//        try {
//            userList = DbCacheUtils.getDb(context).selector(Contact.class).where("inspurID",
//                    "in", uidList).findAll();
//            robotList = DbCacheUtils.getDb(context).selector(Robot.class).where("id",
//                    "in", uidList).findAll();
//        } catch (Exception e) {
//            // TODO: handle exception
//            e.printStackTrace();
//
//        }
//
//        if (userList != null) {
//            Iterator<Contact> contactListIterator = userList.iterator();
//            while (contactListIterator.hasNext()) {
//                Contact contact = contactListIterator.next();
//                PersonDto personDto = new PersonDto();
//                personDto.setName(contact.getRealName());
//                personDto.setUid(contact.getInspurID());
//                personDto.setSortLetters(contact.getPinyin().substring(0, 1));
//                personDto.setPinyinFull(contact.getPinyin());
//                personDto.setSuoxie(PinyinUtils.getPinYinHeadChar(contact
//                        .getRealName()));
//                personDto.setUtype("contact");
//                unitMemberList.add(personDto);
//            }
//        }
//
//        if (robotList != null) {
//            Iterator<Robot> robotListIterator = robotList.iterator();
//            while (robotListIterator.hasNext()) {
//                Robot robot = robotListIterator.next();
//                PersonDto personDto = new PersonDto();
//                personDto.setName(robot.getName());
//                personDto.setUid(robot.getId());
//                personDto.setSortLetters(PinyinUtils.getPingYin(robot.getName()).substring(0, 1));
//                personDto.setPinyinFull(PinyinUtils.getPingYin(robot.getName()));
//                personDto.setSuoxie(PinyinUtils.getPinYinHeadChar(robot.getName()));
//                personDto.setUtype("robot");
//                unitMemberList.add(personDto);
//            }
//        }
//
//        if (unitMemberList == null) {
//            unitMemberList = new ArrayList<PersonDto>();
//        }
//        return unitMemberList;
//
//    }
}

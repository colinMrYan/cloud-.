package com.inspur.emmcloud.componentservice.contact;

import com.inspur.emmcloud.componentservice.CoreService;

import java.util.List;

/**
 * Created by chenmch on 2019/6/13.
 */

public interface ContactService extends CoreService {
    String getUserPhotoUrlOutOfApp(String uid);

    List<ContactUser> getSortUserList(List<String> uidList);

    ContactUser getContactUserByUid(String uid);

    ContactUser getContactUserByMail(String mail);
}

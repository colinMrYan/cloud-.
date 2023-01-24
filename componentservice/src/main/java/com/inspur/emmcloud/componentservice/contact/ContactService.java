package com.inspur.emmcloud.componentservice.contact;

import androidx.annotation.Nullable;

import com.inspur.emmcloud.componentservice.CoreService;

import java.util.List;

/**
 * Created by chenmch on 2019/6/13.
 */

public interface ContactService extends CoreService {
    String getUserPhotoUrlOutOfApp(String uid);

    List<ContactUser> getSortUserList(List<String> uidList);

    @Nullable
    ContactUser getContactUserByUid(String uid);

    @Nullable
    ContactUser getContactUserByMail(String mail);

    void saveContactUser(ContactUser contactUser);
    String getUserName(String uid);

    String getOrganizeName(String userId);

    ContactUser getContactUserByPhoneNumber(String mobile);
}

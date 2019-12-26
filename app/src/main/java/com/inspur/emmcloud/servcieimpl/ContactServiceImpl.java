package com.inspur.emmcloud.servcieimpl;

import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.List;

/**
 * Created by chenmch on 2019/6/13.
 */

public class ContactServiceImpl implements ContactService {
    @Override
    public String getUserPhotoUrlOutOfApp(String uid) {
        return APIUri.getUserPhotoUrlOutOfApp(uid);
    }

    @Override
    public List<ContactUser> getSortUserList(List<String> uidList) {
        return ContactUserCacheUtils.getSoreUserList(uidList);
    }

    @Override
    public ContactUser getContactUserByUid(String uid) {
        return ContactUserCacheUtils.getContactUserByUid(uid);
    }

    @Override
    public ContactUser getContactUserByMail(String mail) {
        return ContactUserCacheUtils.getContactUserByEmail(mail);
    }

    @Override
    public Class getContactSearchActivity() {
        return ContactSearchActivity.class;
    }
}

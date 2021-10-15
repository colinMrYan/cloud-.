package com.inspur.emmcloud.servcieimpl;

import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    public String getUserName(String uid) {
        return ContactUserCacheUtils.getUserName(uid);
    }


    @Override
    public String getOrganizeName(String userId) {
        ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(userId);
        if (contactUser != null) {
            String parentId = contactUser.getParentId();
            List<ContactOrg> contactOrgList = new ArrayList<>();
            while (true) {
                ContactOrg parentOrg = ContactOrgCacheUtils.getContactOrg(parentId);
                if (parentOrg == null) {
                    break;
                }
                contactOrgList.add(parentOrg);
                parentId = parentOrg.getParentId();
                if (StringUtils.isBlank(parentId)) {
                    break;
                }
            }
            Collections.reverse(contactOrgList);
            StringBuilder orgNameBuilder = new StringBuilder();
            for (int i = 0; i < contactOrgList.size(); i++) {
                ContactOrg contactOrg = contactOrgList.get(i);
                orgNameBuilder.append(contactOrg.getName());
                if (i != contactOrgList.size() - 1) {
                    orgNameBuilder.append("-");
                }
            }
            return orgNameBuilder.toString();
        }
        return null;
    }

    @Override
    public ContactUser getContactUserByPhoneNumber(String mobile) {
        return ContactUserCacheUtils.getContactUserByMobile(mobile);
    }

    @Override
    public void saveContactUser(ContactUser contactUser) {
        if (contactUser != null) {
            ContactUserCacheUtils.saveContactUser(contactUser);
        }
    }
}

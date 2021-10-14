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
        if (contactUser == null) return null;
        String orgNameOrID = contactUser.getParentId();
        if (StringUtils.isBlank(orgNameOrID)) return null;
        String root = "root";
        List<String> orgNameList = new ArrayList<>();
        while (!root.equals(orgNameOrID)) {
            ContactOrg contactOrgTest = ContactOrgCacheUtils.getContactOrg(orgNameOrID);
            if (contactOrgTest == null) return null;
            orgNameOrID = contactOrgTest.getName();
            orgNameList.add(orgNameOrID);
            orgNameOrID = contactOrgTest.getParentId();
        }
        Collections.reverse(orgNameList);
        if (orgNameList.size() > 1) {
            if (orgNameList.size() == 2) {
                return orgNameList.get(1);
            } else {
                return orgNameList.get(1) + "-" + orgNameList.get(2);
            }
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

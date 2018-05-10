package com.inspur.emmcloud.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ContactAPIService;
import com.inspur.emmcloud.bean.contact.ContactProtoBuf;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/5/10.
 */

public class TestActivity extends Activity {
    private ContactAPIService contactAPIService;
    private long a = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        a = System.currentTimeMillis();
        contactAPIService = new ContactAPIService(this);
        contactAPIService.setAPIInterface(new WebService());
        contactAPIService.getContactUsers();
    }

    public class WebService extends APIInterfaceInstance{
        @Override
        public void returnContactUserSuccess(byte[] bytes) {
            long b = System.currentTimeMillis();
            LogUtils.jasonDebug("get----time000000000="+(b-a)/1000.0);
            try {
                List<ContactProtoBuf.user> userList = ContactProtoBuf.users.parseFrom(bytes).getUsersList();
                long c = System.currentTimeMillis();
                List<ContactUser> contactUserList = ContactUser.protoBufUserList2ContactUserList(userList);
                LogUtils.jasonDebug("xuliehua----time000000000="+(c-b)/1000.0);
                long d = System.currentTimeMillis();
                LogUtils.jasonDebug("contactUserList="+contactUserList.size());
                ContactUserCacheUtils.saveContactUserList(contactUserList);
                LogUtils.jasonDebug("存储时间----time000000000="+(System.currentTimeMillis()-d)/1000.0);
                LogUtils.jasonDebug("总时间----time000000000="+(System.currentTimeMillis()-a)/1000.0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void returnContactUserFail(String error, int errorCode) {
            super.returnContactUserFail(error, errorCode);
        }

        @Override
        public void returnContactUserSuccess(String result) {
            long b = System.currentTimeMillis();
            LogUtils.jasonDebug("get----time11111111111111="+(b-a)/1000.0);
            try {
                List<ContactUser> contactUserList = new ArrayList<>();
                JSONArray array = JSONUtils.getJSONArray(result,"users",new JSONArray());
                int size = array.length();
                for (int i=0;i<size;i++){
                    ContactUser contactUser = new ContactUser(array.getJSONObject(i));
                    contactUserList.add(contactUser);
                }
                long c = System.currentTimeMillis();
                LogUtils.jasonDebug("xuliehua----time11111111="+(c-b)/1000.0);
                long d = System.currentTimeMillis();
                ContactUserCacheUtils.saveContactUserList(contactUserList);
                LogUtils.jasonDebug("contactUserList="+contactUserList.size());
                LogUtils.jasonDebug("存储时间----time111111111="+(System.currentTimeMillis()-d)/1000.0);
                LogUtils.jasonDebug("总时间----time111111111="+(System.currentTimeMillis()-a)/1000.0);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}

package com.inspur.emmcloud.ui.contact;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ImagePagerV0Activity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;

@ContentView(R.layout.activity_user_info)
public class UserInfoActivity extends BaseActivity {

    @ViewInject(R.id.department_layout)
    private LinearLayout departmentLayout;
    @ViewInject(R.id.department_text)
    private TextView departmentText;

    @ViewInject(R.id.mail_layout)
    private LinearLayout mailLayout;
    @ViewInject(R.id.mail_text)
    private TextView mailText;
    @ViewInject(R.id.contact_layout)
    private RelativeLayout contactLayout;
    @ViewInject(R.id.phone_num_text)
    private TextView phoneNumText;
    @ViewInject(R.id.photo_img)
    private ImageView photoImg;
    @ViewInject(R.id.name_text)
    private TextView nameText;
    @ViewInject(R.id.start_chat_img)
    private ImageView startChatImg;
    private ContactUser contactUser;
    private final static int MY_PERMISSIONS_PHONECALL = 0;
    private final static int MY_PERMISSIONS_SMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        init();
    }

    private void init(){
        String uid = null;
        String scheme = getIntent().getScheme();
        if (scheme != null) {
            String uri = getIntent().getDataString();
            uid = uri.split("//")[1];
        } else if (getIntent().hasExtra("uid")) {
            uid = getIntent().getExtras().getString("uid");
        }
        if (!StringUtils.isBlank(uid)){
            contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
        }
        if (contactUser == null) {
            ToastUtils.show(UserInfoActivity.this, R.string.cannot_view_info);
            finish();
            return;
        }
        initView();
    }

    private void initView() {
        String id = contactUser.getId();
        String mail = contactUser.getEmail();
        String phoneNum = contactUser.getMobile();
        String name = contactUser.getName();
        String headUrl = APIUri.getUserIconUrl(UserInfoActivity.this, contactUser.getId());
        String organize= null;
        ContactOrg contactOrg = ContactOrgCacheUtils.getContactOrg(contactUser.getParentId());
        if (contactOrg != null){
            organize = contactOrg.getName();
        }
        if (!StringUtils.isEmpty(organize)) {
            departmentLayout.setVisibility(View.VISIBLE);
            departmentText.setText(organize);
        }

        if (!StringUtils.isEmpty(mail)) {
            mailLayout.setVisibility(View.VISIBLE);
            mailText.setText(mail);
        }

        if (!StringUtils.isEmpty(phoneNum)) {
            contactLayout.setVisibility(View.VISIBLE);
            phoneNumText.setText(phoneNum);
        }
        nameText.setText(StringUtils.isEmpty(name)?getString(R.string.not_set):name);
        ImageDisplayUtils.getInstance().displayImage(photoImg, headUrl, R.drawable.icon_person_default);
        if (StringUtils.isBlank(id) || id.equals(MyApplication.getInstance().getUid())) {
            startChatImg.setVisibility(View.GONE);
        }

    }


    public void onClick(View v) {
        String phoneNum = phoneNumText.getText().toString();
        switch (v.getId()) {
            case R.id.mail_img:
                String mail = mailText.getText().toString();
                AppUtils.sendMail(UserInfoActivity.this,mail,1);
                break;
            case R.id.phone_img:
                AppUtils.call(UserInfoActivity.this,phoneNum,1);
                // 取消申请权限
                // if (isMobileSet) {
                // if (ContextCompat.checkSelfPermission(UserInfoActivity.this,
                // Manifest.permission.CALL_PHONE) !=
                // PackageManager.PERMISSION_GRANTED) {
                //
                // ActivityCompat.requestPermissions(UserInfoActivity.this,
                // new String[]{Manifest.permission.CALL_PHONE},
                // MY_PERMISSIONS_PHONECALL);
                // } else {
                // }
                // }
                break;
            case R.id.short_msg_img:
                AppUtils.sendSMS(UserInfoActivity.this,phoneNum,1);
                // if (isMobileSet) {
                // if (ContextCompat.checkSelfPermission(UserInfoActivity.this,
                // Manifest.permission.SEND_SMS) !=
                // PackageManager.PERMISSION_GRANTED) {
                //
                // ActivityCompat.requestPermissions(UserInfoActivity.this,
                // new String[]{Manifest.permission.SEND_SMS},
                // MY_PERMISSIONS_SMS);
                // } else {
                // }
                // }
                break;
            case R.id.back_layout:
                finish();
                break;
            case R.id.photo_img:
                Intent intent = new Intent(UserInfoActivity.this,
                        ImagePagerV0Activity.class);
                ArrayList<String> urls = new ArrayList<>();
                urls.add(APIUri.getChannelImgUrl(UserInfoActivity.this, contactUser.getId()));
                intent.putExtra("image_index", 0);
                intent.putStringArrayListExtra("image_urls", urls);
                startActivity(intent);
                break;
            case R.id.start_chat_img:
                createDireactChannel();
                break;
            default:
                break;
        }
    }

    /**
     * 创建单聊
     */
    private void createDireactChannel() {
        // TODO Auto-generated method stub
        new ChatCreateUtils().createDirectChannel(UserInfoActivity.this, contactUser.getId(),
                new ChatCreateUtils.OnCreateDirectChannelListener() {

                    @Override
                    public void createDirectChannelSuccess(
                            GetCreateSingleChannelResult getCreateSingleChannelResult) {
                        // TODO Auto-generated method stub
                        Bundle bundle = new Bundle();
                        bundle.putString("cid",
                                getCreateSingleChannelResult.getCid());
                        bundle.putString("channelType",
                                getCreateSingleChannelResult.getType());
                        bundle.putString("title", getCreateSingleChannelResult
                                .getName(getApplicationContext()));
                        LogUtils.jasonDebug("title=" + getCreateSingleChannelResult
                                .getName(getApplicationContext()));
                        IntentUtils.startActivity(UserInfoActivity.this,MyApplication.getInstance().isV0VersionChat()?
                                ChannelV0Activity.class:ChannelActivity.class, bundle);
                    }

                    @Override
                    public void createDirectChannelFail() {
                        // TODO Auto-generated method stub

                    }
                });
    }


    // 取消申请权限
    // /**
    // * 授权回调方法
    // */
    // @Override
    // public void onRequestPermissionsResult(int requestCode,
    // String permissions[], int[] grantResults) {
    // String phoneNum = phoneNumText.getText().toString();
    // switch (requestCode) {
    // case MY_PERMISSIONS_PHONECALL:
    // // If request is cancelled, the result arrays are empty.
    // if (grantResults.length > 0
    // && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
    // call(phoneNum);
    // } else {
    // Toast.makeText(UserInfoActivity.this, "未授权拨打电话",
    // Toast.LENGTH_SHORT).show();
    // }
    // break;
    // case MY_PERMISSIONS_SMS:
    // if (grantResults.length > 0
    // && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
    // sendSMS(phoneNum);
    // } else {
    // Toast.makeText(UserInfoActivity.this, "未授权发送短信",
    // Toast.LENGTH_SHORT).show();
    // }
    // break;
    //
    // }
    // }



}

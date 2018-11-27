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
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
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

    @ViewInject(R.id.telephone_ll)
    private LinearLayout telLayout;
    @ViewInject(R.id.telephone_tv)
    private TextView telText;

    @ViewInject(R.id.mail_layout)
    private LinearLayout mailLayout;
    @ViewInject(R.id.tv_mail)
    private TextView mailText;
    @ViewInject(R.id.contact_layout)
    private RelativeLayout contactLayout;
    @ViewInject(R.id.phone_num_text)
    private TextView phoneNumText;
    @ViewInject(R.id.img_photo)
    private ImageView photoImg;
    @ViewInject(R.id.tv_name)
    private TextView nameText;
    @ViewInject(R.id.duty_tv)
    private TextView dutyText;

    @ViewInject(R.id.start_chat_img)
    private ImageView startChatImg;

    private ContactUser contactUser;
    private final static int MY_PERMISSIONS_PHONECALL = 0;
    private final static int MY_PERMISSIONS_SMS = 1;
    private  String  parentUid;
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

    private void init() {
        String uid = null;
        String scheme = getIntent().getScheme();
        if (scheme != null) {
            String uri = getIntent().getDataString();
            uid = uri.split("//")[1];
        } else if (getIntent().hasExtra("uid")) {
            uid = getIntent().getExtras().getString("uid");

        }
        if (!StringUtils.isBlank(uid)){
            parentUid =uid;
            contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
        }
        if (contactUser == null) {
            ToastUtils.show(MyApplication.getInstance(), R.string.cannot_view_info);
            finish();
            return;
        }
        initView();
    }

    private void initView() {
        String mail = contactUser.getEmail();
        String phoneNum = contactUser.getMobile();
        String name = contactUser.getName();
        String telStr= contactUser.getTel();
        String officeStr= contactUser.getOffice();

        String headUrl = APIUri.getUserIconUrl(UserInfoActivity.this, contactUser.getId());
        ContactOrg contactOrg = ContactOrgCacheUtils.getContactOrg(contactUser.getParentId());

        if (contactOrg != null){
            String organize = contactOrg.getName();
            if (!StringUtils.isBlank(organize)) {
                departmentLayout.setVisibility(View.VISIBLE);
                departmentText.setText(organize);
            }else {
                departmentLayout.setVisibility(View.GONE);
            }
        }

        if (!StringUtils.isBlank(mail)) {
            mailLayout.setVisibility(View.VISIBLE);
            mailText.setText(mail);
        }else {
            mailLayout.setVisibility(View.GONE);
        }

        if (!StringUtils.isBlank(phoneNum)) {
            contactLayout.setVisibility(View.VISIBLE);
            phoneNumText.setText(phoneNum);
        }else {
            contactLayout.setVisibility(View.GONE);
        }
        //添加固话
        if (!StringUtils.isBlank(telStr)) {
            telLayout.setVisibility(View.VISIBLE);
            telText.setText(telStr);
        }else {
            telLayout.setVisibility(View.GONE);
        }
        nameText.setText(StringUtils.isBlank(name)?getString(R.string.not_set):name);

        if(!StringUtils.isBlank(officeStr)){
            dutyText.setVisibility(View.VISIBLE);
            dutyText.setText(officeStr);  //lbc
        }else {
            dutyText.setVisibility(View.GONE);
        }
        ImageDisplayUtils.getInstance().displayImage(photoImg, headUrl, R.drawable.icon_person_default);
        if (contactUser.getId().equals(MyApplication.getInstance().getUid())) {
            startChatImg.setVisibility(View.GONE);
        }else {
            startChatImg.setVisibility(View.VISIBLE);
        }

    }


    public void onClick(View v) {
        String phoneNum = phoneNumText.getText().toString();
        String TelephoneNum=telText.getText().toString();

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
            case R.id.img_photo:
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
            case R.id.depart_btn_img:
                Bundle bundle22 = new Bundle();
                bundle22.putString("uid", parentUid);
                IntentUtils.startActivity(UserInfoActivity.this, ContactOrgStructureActivity.class, bundle22);
                break;
            case R.id.telephone_iv:
                AppUtils.call(UserInfoActivity.this,TelephoneNum,1);
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
                                ChannelV0Activity.class:ConversationActivity.class, bundle);
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

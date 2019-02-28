package com.inspur.emmcloud.ui.contact;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Conversation;
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
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.dialogs.ActionSheetDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;

/**
 * 从群聊，单聊，通讯录等位置进入的个人信息页面
 * 规范化改造代码
 */
@ContentView(R.layout.activity_user_info)
public class UserInfoActivity extends BaseActivity {

    private static final String USER_UID = "uid";
    private static final int USER_INFO_ACTIVITY_REQUEST_CODE = 1;

    @ViewInject(R.id.ll_user_department)
    private LinearLayout departmentLayout;
    @ViewInject(R.id.tv_user_department)
    private TextView departmentText;
    @ViewInject(R.id.ll_user_telephone)
    private LinearLayout telLayout;
    @ViewInject(R.id.tv_user_telephone)
    private TextView telText;
    @ViewInject(R.id.ll_user_mail)
    private LinearLayout mailLayout;
    @ViewInject(R.id.tv_user_mail)
    private TextView mailText;
    @ViewInject(R.id.rl_user_contact)
    private RelativeLayout contactLayout;
    @ViewInject(R.id.tv_user_phone_num)
    private TextView phoneNumText;
    @ViewInject(R.id.iv_user_photo)
    private ImageView photoImg;
    @ViewInject(R.id.tv_user_name)
    private TextView nameText;
    @ViewInject(R.id.tv_user_duty)
    private TextView dutyText;
    @ViewInject(R.id.iv_start_chat)
    private TextView startChatImg;

    @ViewInject(R.id.ll_mobile_contact_info)
    private LinearLayout mobileContactInfoLayout;
    @ViewInject(R.id.ll_mobile_phone)
    private LinearLayout mobilePhoneLayout;
    @ViewInject(R.id.ll_mobile_sms)
    private LinearLayout mobileSMSLayout;
    @ViewInject(R.id.ll_mobile_email)
    private LinearLayout mobileEmailLayout;
    @ViewInject(R.id.rl_start_chat)
    private RelativeLayout mobileStartChatLayout;
    @ViewInject(R.id.tv_user_position)
    private TextView positionText;
    @ViewInject(R.id.ll_user_position)
    private LinearLayout mobilePositionLayout;
    @ViewInject(R.id.rl_contact_way)
    private RelativeLayout userContactWayLayout;

    private ContactUser contactUser;
    private String parentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersionBar.with(this).statusBarColor(android.R.color.white).statusBarDarkFont(true).init();
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
        } else if (getIntent().hasExtra(USER_UID)) {
            uid = getIntent().getExtras().getString(USER_UID);
        }
        if (!StringUtils.isBlank(uid)) {
            parentUid = uid;
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
        String telStr = contactUser.getTel();
        String officeStr = contactUser.getOffice();

        String headUrl = APIUri.getUserIconUrl(UserInfoActivity.this, contactUser.getId());
        ContactOrg contactOrg = ContactOrgCacheUtils.getContactOrg(contactUser.getParentId());

        if (contactOrg != null) {
            String organize = contactOrg.getName();
            if (!StringUtils.isBlank(organize)) {
                departmentLayout.setVisibility(View.VISIBLE);
                departmentText.setText(organize);
            } else {
                departmentLayout.setVisibility(View.GONE);
            }
        }

        if (!StringUtils.isBlank(mail)) {
            mailLayout.setVisibility(View.VISIBLE);
            mailText.setText(mail);
        } else {
            mailLayout.setVisibility(View.GONE);
        }

        if (!StringUtils.isBlank(phoneNum)) {
            contactLayout.setVisibility(View.VISIBLE);
            phoneNumText.setText(phoneNum);
        } else {
            contactLayout.setVisibility(View.GONE);
        }
        //添加固话
        if (!StringUtils.isBlank(telStr)) {
            telLayout.setVisibility(View.VISIBLE);
            telText.setText(telStr);
        } else {
            telLayout.setVisibility(View.GONE);
        }
        nameText.setText(StringUtils.isBlank(name) ? getString(R.string.not_set) : name);

        if (!StringUtils.isBlank(officeStr)) {
            dutyText.setVisibility(View.VISIBLE);
            dutyText.setText(officeStr);  //lbc
            positionText.setText(officeStr);
        } else {
            dutyText.setVisibility(View.GONE);
        }
        ImageDisplayUtils.getInstance().displayImage(photoImg, headUrl, R.drawable.icon_person_default);
        startChatImg.setVisibility(contactUser.getId().equals(MyApplication.getInstance().getUid())?View.GONE:View.VISIBLE);
        mobilePositionLayout.setVisibility(StringUtils.isBlank(officeStr)?View.GONE:View.VISIBLE);
        mobilePhoneLayout.setVisibility((StringUtils.isBlank(phoneNum) && StringUtils.isBlank(telStr))?View.GONE:View.VISIBLE);
        mobileSMSLayout.setVisibility(StringUtils.isBlank(phoneNum)?View.GONE:View.VISIBLE);
        mobileEmailLayout.setVisibility(StringUtils.isBlank(mail)?View.GONE:View.VISIBLE);
        boolean isNoContactWay = StringUtils.isBlank(phoneNum) && StringUtils.isBlank(telStr) && StringUtils.isBlank(mail);
//        isNoContactWay = true;
        mobileContactInfoLayout.setVisibility(isNoContactWay?View.GONE:View.VISIBLE);
        mobileStartChatLayout.setVisibility(isNoContactWay?View.VISIBLE:View.GONE);
        userContactWayLayout.setVisibility(contactUser.getId().equals(MyApplication.getInstance().getUid())?View.GONE:View.VISIBLE);
    }

    public void onClick(View v) {
        final String phoneNum = contactUser.getMobile();
        switch (v.getId()) {
            case R.id.ll_mobile_email:
                String mail = mailText.getText().toString();
                AppUtils.sendMail(UserInfoActivity.this, mail, USER_INFO_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.ll_mobile_phone:
                showCallPhoneDialog();
                break;
            case R.id.ll_mobile_sms:
                AppUtils.sendSMS(UserInfoActivity.this, phoneNum, USER_INFO_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.ibt_back:
                finish();
                break;
            case R.id.iv_user_photo:
                Intent intent = new Intent(UserInfoActivity.this,
                        ImagePagerV0Activity.class);
                ArrayList<String> urls = new ArrayList<>();
                urls.add(APIUri.getChannelImgUrl(UserInfoActivity.this, contactUser.getId()));
                intent.putExtra(ImagePagerV0Activity.EXTRA_IMAGE_INDEX, 0);
                intent.putStringArrayListExtra(ImagePagerV0Activity.EXTRA_IMAGE_URLS, urls);
                startActivity(intent);
                break;
            case R.id.ll_mobile_chat:
            case R.id.iv_start_chat:
                createDirectChannel();
                break;
            case R.id.iv_user_depart_detail:
                Bundle bundle = new Bundle();
                bundle.putString(USER_UID, parentUid);
                IntentUtils.startActivity(UserInfoActivity.this, ContactOrgStructureActivity.class, bundle);
                break;
            default:
                break;
        }
    }

    private void showCallPhoneDialog() {
        final String phoneNum = contactUser.getMobile();
        final String officePhoneNum = contactUser.getTel();
        new ActionSheetDialog.ActionListSheetBuilder(UserInfoActivity.this)
                .setTitle(getString(R.string.user_call)+contactUser.getName())
                .addItem(getString(R.string.user_info_phone_number)+":"+phoneNum)
                .addItem(getString(R.string.user_office_phone)+":"+officePhoneNum)
                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                        dialog.dismiss();
                        switch (position){
                            case 0:
                                AppUtils.call(UserInfoActivity.this, phoneNum, USER_INFO_ACTIVITY_REQUEST_CODE);
                                break;
                            case 1:
                                AppUtils.call(UserInfoActivity.this, officePhoneNum, USER_INFO_ACTIVITY_REQUEST_CODE);
                                break;
                        }
                    }
                })
                .build()
                .show();
    }

    private void createDirectChannel() {
        if (MyApplication.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(UserInfoActivity.this, contactUser.getId(),
                    new ConversationCreateUtils.OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(ConversationActivity.EXTRA_CONVERSATION, conversation);
                            IntentUtils.startActivity(UserInfoActivity.this, ConversationActivity.class, bundle);
                        }

                        @Override
                        public void createDirectConversationFail() {
                        }
                    });
        } else {
            new ChatCreateUtils().createDirectChannel(UserInfoActivity.this, contactUser.getId(),
                    new ChatCreateUtils.OnCreateDirectChannelListener() {
                        @Override
                        public void createDirectChannelSuccess(
                                GetCreateSingleChannelResult getCreateSingleChannelResult) {
                            Bundle bundle = new Bundle();
                            bundle.putString("cid",
                                    getCreateSingleChannelResult.getCid());
                            bundle.putString("channelType",
                                    getCreateSingleChannelResult.getType());
                            bundle.putString("title", getCreateSingleChannelResult
                                    .getName(getApplicationContext()));
                            LogUtils.jasonDebug("title=" + getCreateSingleChannelResult
                                    .getName(getApplicationContext()));
                            IntentUtils.startActivity(UserInfoActivity.this, ChannelV0Activity.class, bundle);
                        }

                        @Override
                        public void createDirectChannelFail() {
                        }
                    });
        }

    }
}

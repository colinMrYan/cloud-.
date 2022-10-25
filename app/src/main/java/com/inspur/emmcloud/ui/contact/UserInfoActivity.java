package com.inspur.emmcloud.ui.contact;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inspur.emmcloud.R;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.adapter.DepartMentAdpater;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.ScrollViewWithListView;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.ui.DarkUtil;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.GetCreateSingleChannelResult;
import com.inspur.emmcloud.componentservice.communication.OnCreateDirectConversationListener;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.chat.ImagePagerNewActivity;
import com.inspur.emmcloud.ui.chat.ImagePagerV0Activity;
import com.inspur.emmcloud.ui.chat.messagemenu.MessageMenuItem;
import com.inspur.emmcloud.ui.chat.messagemenu.MessageMenuPopupWindow;
import com.inspur.emmcloud.ui.chat.pop.PopupWindowList;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 从群聊，单聊，通讯录等位置进入的个人信息页面
 * 规范化改造代码
 */

@Route(path = Constant.AROUTER_CLASS_CONTACT_USERINFO)
public class UserInfoActivity extends BaseActivity implements View.OnLongClickListener {

    private static final String USER_UID = "uid";
    public static final String ORG_ID = "org_id";
    private static final int USER_INFO_ACTIVITY_REQUEST_CODE = 1;

    @BindView(R.id.tv_add_system_contact)
    TextView addSysContactTv;
    @BindView(R.id.ll_user_department)
    LinearLayout departmentLayout;
    @BindView(R.id.tv_user_department)
    TextView departmentText;
    @BindView(R.id.ll_user_telephone)
    LinearLayout telLayout;
    @BindView(R.id.tv_user_telephone)
    TextView telText;
    @BindView(R.id.ll_user_mail)
    LinearLayout mailLayout;
    @BindView(R.id.tv_user_mail)
    TextView mailText;
    @BindView(R.id.rl_user_contact)
    RelativeLayout contactLayout;
    @BindView(R.id.tv_user_phone_num)
    TextView phoneNumText;
    @BindView(R.id.iv_user_photo)
    ImageView photoImg;
    @BindView(R.id.tv_user_name)
    TextView nameText;
    @BindView(R.id.tv_user_duty)
    TextView dutyText;
    @BindView(R.id.iv_start_chat)
    TextView startChatImg;

    @BindView(R.id.ll_mobile_contact_info)
    LinearLayout mobileContactInfoLayout;
    @BindView(R.id.ll_mobile_phone)
    LinearLayout mobilePhoneLayout;
    @BindView(R.id.ll_mobile_sms)
    LinearLayout mobileSMSLayout;
    @BindView(R.id.ll_mobile_email)
    LinearLayout mobileEmailLayout;
    @BindView(R.id.rl_start_chat)
    RelativeLayout mobileStartChatLayout;
    //    @BindView(R.id.tv_user_position)
//    private TextView positionText;
//    @BindView(R.id.ll_user_position)
//    private LinearLayout mobilePositionLayout;
    @BindView(R.id.rl_contact_way)
    RelativeLayout userContactWayLayout;
    @BindView(R.id.lv_department)
    ScrollViewWithListView departMentListView;

    private ContactUser contactUser;
    private String parentUid;
    private MessageMenuPopupWindow mPopupWindowList;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_user_info;
    }

//    protected int getStatusType() {
//        return STATUS_WHITE_DARK_FONT;
//    }


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
        if (uid.equals("EVERYBODY")) {
            finish();
            return;
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
        String globalName = contactUser.getNameGlobal();
        String telStr = contactUser.getTel();
        String officeStr = contactUser.getOffice();
        String headUrl = APIUri.getUserIconUrl(UserInfoActivity.this, contactUser.getId());

        //从多组织表里获取组织
        final List<ContactOrg> contactOrgList = ContactOrgCacheUtils.getMultiOrgByInspurId(contactUser.getId());
        //如果不是多组织的则走原来逻辑
        if (contactOrgList.size() == 0) {
            contactOrgList.add(ContactOrgCacheUtils.getContactOrg(contactUser.getParentId()));
        }
        DepartMentAdpater departMentAdpater = new DepartMentAdpater(this, contactOrgList);
        departMentAdpater.setDepartMentDetailListener(new DepartMentAdpater.DepartMentDetailListener() {
            @Override
            public void onDepartMentDetailClickListener(int postion) {
                Bundle bundle = new Bundle();
                bundle.putString(ORG_ID, contactOrgList.get(postion).getId());
                IntentUtils.startActivity(UserInfoActivity.this, ContactOrgStructureActivity.class, bundle);
            }
        });
        departMentListView.setAdapter(departMentAdpater);

//        if (contactOrg != null) {
//            String organize = contactOrg.getName();
//            if (!StringUtils.isBlank(organize)) {
//                departmentLayout.setVisibility(View.VISIBLE);
//                departmentText.setText(organize);
//            } else {
//                departmentLayout.setVisibility(View.GONE);
//            }
//        }

        if (!StringUtils.isBlank(mail)) {
            mailLayout.setVisibility(View.VISIBLE);
            mailText.setText(mail);
            mailLayout.setOnLongClickListener(this);
        } else {
            mailLayout.setVisibility(View.GONE);
        }

        if (!StringUtils.isBlank(phoneNum)) {
            contactLayout.setVisibility(View.VISIBLE);
            phoneNumText.setText(phoneNum);
            contactLayout.setOnLongClickListener(this);
        } else {
            contactLayout.setVisibility(View.GONE);
        }
        //添加固话
        if (!StringUtils.isBlank(telStr)) {
            telLayout.setVisibility(View.VISIBLE);
            telText.setText(telStr);
            telLayout.setOnLongClickListener(this);
        } else {
            telLayout.setVisibility(View.GONE);
        }

        if (StringUtils.isBlank(name)) {
            nameText.setText(getString(R.string.not_set));
        } else {
            if (StringUtils.isBlank(globalName) || name.equals(globalName)) {
                nameText.setText(name);
            } else {
                nameText.setText(name + " |  " + globalName);
            }
            if (isOverSingleLine()) {
                nameText.setText(name);
            }
        }

        if (!StringUtils.isBlank(officeStr)) {
            dutyText.setVisibility(View.VISIBLE);
            dutyText.setText(officeStr);  //lbc
//            positionText.setText(officeStr);
        } else {
            dutyText.setVisibility(View.GONE);
        }
        ImageDisplayUtils.getInstance().displayImage(photoImg, headUrl, R.drawable.icon_person_default);
        startChatImg.setVisibility(contactUser.getId().equals(MyApplication.getInstance().getUid()) ? View.GONE : View.VISIBLE);
//        mobilePositionLayout.setVisibility(StringUtils.isBlank(officeStr)?View.GONE:View.VISIBLE);
        mobilePhoneLayout.setVisibility((StringUtils.isBlank(phoneNum) && StringUtils.isBlank(telStr)) ? View.GONE : View.VISIBLE);
        mobileSMSLayout.setVisibility(StringUtils.isBlank(phoneNum) ? View.GONE : View.VISIBLE);
        mobileEmailLayout.setVisibility(StringUtils.isBlank(mail) ? View.GONE : View.VISIBLE);
        boolean isNoContactWay = StringUtils.isBlank(phoneNum) && StringUtils.isBlank(telStr) && StringUtils.isBlank(mail);
        mobileContactInfoLayout.setVisibility(isNoContactWay ? View.GONE : View.VISIBLE);
        mobileStartChatLayout.setVisibility(isNoContactWay ? View.VISIBLE : View.GONE);
        addSysContactTv.setVisibility(contactUser.getId().equals(MyApplication.getInstance().getUid()) ? View.GONE : View.VISIBLE);
        userContactWayLayout.setVisibility(contactUser.getId().equals(MyApplication.getInstance().getUid()) ? View.GONE : View.VISIBLE);
    }

    private boolean isOverSingleLine() {
        String value = nameText.getText().toString();
        if (StringUtils.isBlank(value)) return false;
        nameText.measure(0, 0);
        int width = nameText.getMeasuredWidth();
        return width >= nameText.getMaxWidth();
    }

    public void onClick(View v) {
        final String phoneNum = contactUser.getMobile();
        String mail = mailText.getText().toString();
        switch (v.getId()) {
            case R.id.ll_mobile_email:
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
            case R.id.tv_add_system_contact:
                addSystemContact();
                break;
            case R.id.iv_user_photo:
                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                    Intent intent = new Intent(UserInfoActivity.this,
                            ImagePagerV0Activity.class);
                    ArrayList<String> urls = new ArrayList<>();
                    urls.add(APIUri.getChannelImgUrl(UserInfoActivity.this, contactUser.getId()));
                    intent.putExtra(ImagePagerV0Activity.EXTRA_IMAGE_INDEX, 0);
                    intent.putStringArrayListExtra(ImagePagerV0Activity.EXTRA_IMAGE_URLS, urls);
                    startActivity(intent);
                } else {
                    // 新版图片查看器
                    Intent intent = new Intent(UserInfoActivity.this,
                            ImagePagerNewActivity.class);
                    ArrayList<String> urls = new ArrayList<>();
                    urls.add(APIUri.getChannelImgUrl(UserInfoActivity.this, contactUser.getId()));
                    intent.putExtra(ImagePagerNewActivity.EXTRA_IMAGE_INDEX, 0);
                    intent.putStringArrayListExtra(ImagePagerNewActivity.EXTRA_IMAGE_URLS, urls);
                    startActivity(intent);
                }

                break;
            case R.id.ll_mobile_chat:
            case R.id.iv_start_chat:
                createDirectChannel();
                break;
//            case R.id.iv_user_depart_detail:
//                Bundle bundle = new Bundle();
//                bundle.putString(USER_UID, parentUid);
//                IntentUtils.startActivity(UserInfoActivity.this, ContactOrgStructureActivity.class, bundle);
//                break;
            case R.id.rl_user_contact:
                showCallUserDialog(contactUser.getMobile());
                break;
            case R.id.ll_user_telephone:
                showCallUserDialog(contactUser.getTel());
                break;
            case R.id.ll_user_mail:
                if (!contactUser.getId().equals(MyApplication.getInstance().getUid())) {
                    AppUtils.sendMail(UserInfoActivity.this, mail, USER_INFO_ACTIVITY_REQUEST_CODE);
                }
                break;
            default:
                break;
        }
    }

    private void showCallUserDialog(final String mobile) {
        if (!contactUser.getId().equals(MyApplication.getInstance().getUid())) {
            new CustomDialog.MessageDialogBuilder(UserInfoActivity.this)
                    .setMessage(mobile)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(R.string.user_call, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            AppUtils.call(UserInfoActivity.this, mobile, USER_INFO_ACTIVITY_REQUEST_CODE);
                        }
                    })
                    .show();
        }
    }

    private void showCallPhoneDialog() {
        final String phoneNum = contactUser.getMobile();
        final String officePhoneNum = contactUser.getTel();
        ActionSheetDialog.ActionListSheetBuilder builder = new ActionSheetDialog.ActionListSheetBuilder(UserInfoActivity.this)
                .setTitle(getString(R.string.user_call))
                .setTitleColor(Color.parseColor("#888888"))
                .setItemColor(Color.parseColor("#36A5F6"))
//                .setCancelColor(ResourceUtils.getResValueOfAttr(this, R.attr.text_color));
                .setCancelColor(DarkUtil.getTextColor());
        if (!StringUtils.isBlank(phoneNum)) {
            builder.addItem(getString(R.string.user_info_phone_number) + ":" + phoneNum);
        }
        if (!StringUtils.isBlank(officePhoneNum)) {
            builder.addItem(getString(R.string.user_office_phone) + ":" + officePhoneNum);
        }
        if (!StringUtils.isBlank(phoneNum) && !StringUtils.isBlank(officePhoneNum)) {
            builder.setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                @Override
                public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                    dialog.dismiss();
                    switch (position) {
                        case 0:
                            AppUtils.call(UserInfoActivity.this, phoneNum, USER_INFO_ACTIVITY_REQUEST_CODE);
                            break;
                        case 1:
                            AppUtils.call(UserInfoActivity.this, officePhoneNum, USER_INFO_ACTIVITY_REQUEST_CODE);
                            break;
                    }
                }
            }).build().show();
        } else if (!StringUtils.isBlank(phoneNum) || !StringUtils.isBlank(officePhoneNum)) {
            builder.setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                @Override
                public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                    dialog.dismiss();
                    String mobileNum = StringUtils.isBlank(phoneNum) ? officePhoneNum : phoneNum;
                    AppUtils.call(UserInfoActivity.this, mobileNum, USER_INFO_ACTIVITY_REQUEST_CODE);
                }
            }).build().show();
        }
    }

    /**
     * 添加到系统联系人
     */
    @SuppressLint("IntentReset")
    private void addSystemContact() {
        Intent intent = new Intent(Intent.ACTION_INSERT,
                Uri.withAppendedPath(Uri.parse("content://com.android.contacts"), "contacts"));
        intent.setType("vnd.android.cursor.dir/person");
        intent.setType("vnd.android.cursor.dir/contact");
        intent.setType("vnd.android.cursor.dir/raw_contact");
        intent.putExtra(ContactsContract.Intents.Insert.NAME, contactUser.getName());
        intent.putExtra(ContactsContract.Intents.Insert.PHONE_ISPRIMARY, true);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, contactUser.getTel());
        intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, contactUser.getMobile());
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, contactUser.getEmail());
        startActivity(intent);
    }

    private void createDirectChannel() {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(UserInfoActivity.this, contactUser.getId(),
                    new OnCreateDirectConversationListener() {
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

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.ll_user_telephone:
                showCopyDialog(telText, contactUser.getTel());
                break;
            case R.id.rl_user_contact:
                showCopyDialog(phoneNumText, contactUser.getMobile());
                break;
            case R.id.ll_user_mail:
                showCopyDialog(mailText, contactUser.getEmail());
                break;
        }
        return true;
    }

    /**
     * 长按复制popup
     *
     * @param v
     * @param content
     */
    private void showCopyDialog(View v, final String content) {
        if (mPopupWindowList == null) {
            mPopupWindowList = new MessageMenuPopupWindow(this);
        }
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.chat_long_click_copy));
        mPopupWindowList.setAnchorView(v);
        mPopupWindowList.setItemData(list);
        mPopupWindowList.setModal(true);
        mPopupWindowList.show();
        mPopupWindowList.setOnItemClickListener(new MessageMenuPopupWindow.PopItemClickListener() {
            @Override
            public void onPopItemClick(MessageMenuItem item) {
                AppUtils.copyContentToPasteBoard(UserInfoActivity.this, content);
                mPopupWindowList.hide();
            }
        });
//        mPopupWindowList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                AppUtils.copyContentToPasteBoard(UserInfoActivity.this, content);
//                mPopupWindowList.hide();
//            }
//        });
    }
}

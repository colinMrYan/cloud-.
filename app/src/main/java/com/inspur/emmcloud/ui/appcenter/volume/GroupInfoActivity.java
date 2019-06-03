package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeInfoMemberAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.bean.appcenter.volume.Group;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.ui.chat.ChannelMembersDelActivity;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.NetUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.NoScrollGridView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 共享网盘详情页面
 */
public class GroupInfoActivity extends BaseActivity {

    private static final int ADD_MEMBER = 1;
    private static final int DEL_MEMBER = 2;
    private static final int UPDATE_GROUP_NAME = 3;
    @BindView(R.id.gv_member)
    NoScrollGridView memberGrid;
    @BindView(R.id.volume_member_text)
    TextView groupMemberText;
    @BindView(R.id.volume_name_text)
    TextView groupNameText;
    @BindView(R.id.volume_name_title)
    TextView groupNameTitle;
    @BindView(R.id.header_text)
    TextView headerText;
    private Volume volume;
    private MyAppAPIService apiService;
    private LoadingDialog loadingDlg;
    private Group group;
    private VolumeInfoMemberAdapter memberAdapter;
    private ArrayList<String> volumeMemList;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        volume = (Volume) getIntent().getSerializableExtra("volume");
        loadingDlg = new LoadingDialog(this);
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        headerText.setText(R.string.clouddriver_volume_group_info);
        group = (Group) getIntent().getSerializableExtra("group");
        volumeMemList = getIntent().getStringArrayListExtra("volumeMemList");
        groupNameTitle.setText(R.string.clouddriver_volume_group_name);
        showGroupDetail();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_share_volumel_info;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                onBackPressed();
                break;
            case R.id.volume_member_layout:
                Bundle bundle = new Bundle();
                bundle.putString("title", getString(R.string.clouddriver_volume_group_member));
                bundle.putInt(MembersActivity.MEMBER_PAGE_STATE, MembersActivity.CHECK_STATE);
                bundle.putStringArrayList("uidList", group.getMemberUidList());
                IntentUtils.startActivity(GroupInfoActivity.this,
                        MembersActivity.class, bundle);
                break;
            case R.id.volume_name_layout:
                Intent intent = new Intent(GroupInfoActivity.this, ShareVolumeNameModifyActivity.class);
                intent.putExtra("group", group);
                startActivityForResult(intent, UPDATE_GROUP_NAME);
                break;
            default:
                break;
        }
    }

    private void showGroupDetail() {
        memberAdapter = new VolumeInfoMemberAdapter(getApplicationContext(), group.getMemberUidList(), true);
        memberGrid.setAdapter(memberAdapter);
        updateGroupMemNum();
        groupNameText.setText(group.getName());
        memberGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                if (position == memberGrid.getCount() - 2) {
                    ArrayList groupAddMemList = new ArrayList();
                    groupAddMemList.addAll(volumeMemList);
                    groupAddMemList.removeAll(group.getMemberUidList());
                    intent.putExtra("memberUidList", groupAddMemList);
                    intent.putExtra("isRemoveMyself", false);
                    intent.setClass(getApplicationContext(),
                            ChannelMembersDelActivity.class);
                    intent.putExtra("title", getString(R.string.clouddriver_add_volume_group_member));
                    startActivityForResult(intent, ADD_MEMBER);
                } else if (position == memberGrid.getCount() - 1) {
                    intent.putExtra("memberUidList", group.getMemberUidList());
                    intent.setClass(getApplicationContext(),
                            ChannelMembersDelActivity.class);
                    intent.putExtra("title", getString(R.string.clouddriver_del_volume_group_member));
                    startActivityForResult(intent, DEL_MEMBER);

                } else {
                    String uid = group.getMemberUidList().get(position);
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", uid);
                    IntentUtils.startActivity(GroupInfoActivity.this, UserInfoActivity.class, bundle);
                }
            }
        });

    }

    private void updateGroupMemNum() {
        groupMemberText.setText(getString(R.string.clouddriver_all_group_member_size, group.getMemberUidList().size()));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ADD_MEMBER:
                    List<String> memAddUidList = (List<String>) data.getSerializableExtra("selectMemList");
                    groupMemAdd(memAddUidList);
                    break;
                case DEL_MEMBER:
                    List<String> memDelUidList = (List<String>) data.getSerializableExtra("selectMemList");
                    groupMemDel(memDelUidList);
                    break;

                case UPDATE_GROUP_NAME:
                    String groupName = data.getStringExtra("groupName");
                    group.setName(groupName);
                    groupNameText.setText(groupName);
                    ShareVolumeInfoActivity.notifyVolumeInfoUpdate(getApplicationContext());
                    break;

                default:
                    break;
            }
        }
    }


    /**
     * 添加组成员
     *
     * @param uidList
     */
    private void groupMemAdd(List<String> uidList) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiService.groupMemAdd(group.getId(), uidList);
        }

    }

    /**
     * 删除组成员
     *
     * @param uidList
     */
    private void groupMemDel(List<String> uidList) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiService.groupMemDel(group.getId(), uidList);
        }

    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnGroupMemAddSuccess(List<String> uidList) {
            LoadingDialog.dimissDlg(loadingDlg);
            group.getMemberUidList().addAll(uidList);
            memberAdapter.notifyDataSetChanged();
            updateGroupMemNum();
            ShareVolumeInfoActivity.notifyVolumeInfoUpdate(getApplicationContext());
        }

        @Override
        public void returnGroupMemAddFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnGroupMemDelSuccess(List<String> uidList) {
            LoadingDialog.dimissDlg(loadingDlg);
            group.getMemberUidList().removeAll(uidList);
            memberAdapter.notifyDataSetChanged();
            updateGroupMemNum();
            ShareVolumeInfoActivity.notifyVolumeInfoUpdate(getApplicationContext());
        }

        @Override
        public void returnGroupMemDelFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}

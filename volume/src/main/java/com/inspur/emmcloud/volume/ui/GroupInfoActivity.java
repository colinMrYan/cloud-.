package com.inspur.emmcloud.volume.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.NoScrollGridView;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppTabUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.componentservice.volume.Volume;
import com.inspur.emmcloud.volume.R;
import com.inspur.emmcloud.volume.R2;
import com.inspur.emmcloud.volume.adapter.VolumeInfoMemberAdapter;
import com.inspur.emmcloud.volume.api.VolumeAPIInterfaceInstance;
import com.inspur.emmcloud.volume.api.VolumeAPIService;
import com.inspur.emmcloud.volume.bean.Group;

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
    public static final String MEMBER_PAGE_STATE = "member_page_state";
    public static final int CHECK_STATE = 3;//查看人员
    @BindView(R2.id.gv_member)
    NoScrollGridView memberGrid;
    @BindView(R2.id.volume_member_text)
    TextView groupMemberText;
    @BindView(R2.id.volume_name_text)
    TextView groupNameText;
    @BindView(R2.id.volume_name_title)
    TextView groupNameTitle;
    @BindView(R2.id.header_text)
    TextView headerText;
    private Volume volume;
    private VolumeAPIService apiService;
    private LoadingDialog loadingDlg;
    private Group group;
    private VolumeInfoMemberAdapter memberAdapter;
    private ArrayList<String> volumeMemList;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        volume = (Volume) getIntent().getSerializableExtra("volume");
        loadingDlg = new LoadingDialog(this);
        apiService = new VolumeAPIService(this);
        apiService.setAPIInterface(new WebService());
        headerText.setText(R.string.volume_clouddriver_volume_group_info);
        group = (Group) getIntent().getSerializableExtra("group");
        volumeMemList = getIntent().getStringArrayListExtra("volumeMemList");
        groupNameTitle.setText(R.string.volume_clouddriver_volume_group_name);
        showGroupDetail();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.volume_activity_share_volumel_info;
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibt_back) {
            onBackPressed();
        } else if (id == R.id.volume_member_layout) {
            Bundle bundle = new Bundle();
            bundle.putString("title", getString(R.string.volume_clouddriver_volume_group_member));
            bundle.putInt(MEMBER_PAGE_STATE, CHECK_STATE);
            bundle.putStringArrayList("uidList", group.getMemberUidList());
            ARouter.getInstance().build(Constant.AROUTER_CLASS_COMMUNICATION_MEMBER).with(bundle).navigation(this);
        } else if (id == R.id.volume_name_layout) {
            Intent intent = new Intent(GroupInfoActivity.this, ShareVolumeNameModifyActivity.class);
            intent.putExtra("group", group);
            startActivityForResult(intent, UPDATE_GROUP_NAME);
        }
    }

    private void showGroupDetail() {
        memberAdapter = new VolumeInfoMemberAdapter(this, group.getMemberUidList(), true);
        memberGrid.setAdapter(memberAdapter);
        updateGroupMemNum();
        groupNameText.setText(group.getName());
        memberGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                if (position == memberGrid.getCount() - 2 && AppTabUtils.hasContactPermission(GroupInfoActivity.this)) {
                    bundle.putInt("select_content", 2);
                    bundle.putBoolean("isMulti_select", true);
                    bundle.putString("title", getString(R.string.volume_clouddriver_add_volume_member));
                    bundle.putStringArrayList("excludeContactUidList", group.getMemberUidList());
                    ARouter.getInstance().build(Constant.AROUTER_CLASS_CONTACT_SEARCH).with(bundle)
                            .navigation(GroupInfoActivity.this, ADD_MEMBER);

                } else if (position == memberGrid.getCount() - 1) {
                    bundle.putStringArrayList("memberUidList", group.getMemberUidList());
                    bundle.putString("title", getString(R.string.volume_clouddriver_del_volume_group_member));
                    ARouter.getInstance().build(Constant.AROUTER_CLASS_COMMUNICATION_MEMBER_DEL).with(bundle)
                            .navigation(GroupInfoActivity.this, DEL_MEMBER);
                } else {
                    String uid = group.getMemberUidList().get(position);
                    bundle.putString("uid", uid);
                    ARouter.getInstance().build(Constant.AROUTER_CLASS_CONTACT_USERINFO).with(bundle)
                            .navigation(GroupInfoActivity.this);
                }
            }
        });

    }

    private void updateGroupMemNum() {
        groupMemberText.setText(getString(R.string.volume_clouddriver_all_group_member_size, group.getMemberUidList().size()));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ADD_MEMBER:
                    List<SearchModel> searchModelList = (List<SearchModel>) data.getSerializableExtra("selectMemList");
                    if (searchModelList.size() > 0) {
                        List<String> memberAddUidList = new ArrayList<>();
                        for (int i = 0; i < searchModelList.size(); i++) {
                            memberAddUidList.add(searchModelList.get(i).getId());
                        }
                        volumeMemAdd(memberAddUidList);
                    }
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
     * 添加网盘成员
     *
     * @param uidList
     */
    private void volumeMemAdd(List<String> uidList) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiService.volumeMemAdd(volume.getId(), uidList);
        }

    }


    /**
     * 添加组成员
     *
     * @param uidList
     */
    private void groupMemAdd(List<String> uidList) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
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

    private class WebService extends VolumeAPIInterfaceInstance {

        @Override
        public void returnVolumeMemAddSuccess(List<String> uidList) {
            groupMemAdd(uidList);
        }

        @Override
        public void returnVolumeMemAddFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }


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

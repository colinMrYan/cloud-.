package com.inspur.emmcloud.volume.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.NoScrollGridView;
import com.inspur.emmcloud.baselib.widget.ScrollViewWithListView;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.volume.R;
import com.inspur.emmcloud.volume.R2;
import com.inspur.emmcloud.volume.adapter.VolumeInfoGroupAdapter;
import com.inspur.emmcloud.volume.adapter.VolumeInfoMemberAdapter;
import com.inspur.emmcloud.volume.api.VolumeAPIInterfaceInstance;
import com.inspur.emmcloud.volume.api.VolumeAPIService;
import com.inspur.emmcloud.volume.bean.GetVolumeGroupResult;
import com.inspur.emmcloud.volume.bean.Group;
import com.inspur.emmcloud.volume.bean.Volume;
import com.inspur.emmcloud.volume.bean.VolumeDetail;
import com.inspur.emmcloud.volume.bean.VolumeGroupContainMe;
import com.inspur.emmcloud.volume.util.VolumeGroupContainMeCacheUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 共享网盘详情页面
 */

public class ShareVolumeInfoActivity extends BaseActivity {

    private static final int ADD_MEMBER = 1;
    private static final int DEL_MEMBER = 2;
    private static final int UPDATE_VOLUME_NAME = 3;
    private static final int VOLUME_HAS_UPLOAD_AND_WATCH_PERMISSION = 0;
    private static final int VOLUME_HAS_WATCH_PERMISSION = 1;
    public static final String MEMBER_PAGE_STATE = "member_page_state";
    public static final int CHECK_STATE = 3;//查看人员
    @BindView(R2.id.gv_member)
    NoScrollGridView memberGrid;
    @BindView(R2.id.volume_member_text)
    TextView volumeMemberText;
    @BindView(R2.id.volume_name_text)
    TextView volumeNameText;
    @BindView(R2.id.slv_write_group)
    ScrollViewWithListView groupWriteListView;
    @BindView(R2.id.slv_read_group)
    ScrollViewWithListView groupReadListView;
    @BindView(R2.id.img_volume_name_arrow)
    ImageView volumeNameArrowImg;
    @BindView(R2.id.ll_write_group)
    LinearLayout groupWriteLayout;
    @BindView(R2.id.ll_group_watch)
    LinearLayout groupReadLayout;
    @BindView(R2.id.rl_member)
    LinearLayout memberLayout;
    private Volume volume;
    private VolumeAPIService apiService;
    private LoadingDialog loadingDlg;
    private VolumeDetail volumeDetail;
    private VolumeInfoMemberAdapter memberAdapter;
    private VolumeInfoGroupAdapter groupWriteAdapter;
    private VolumeInfoGroupAdapter groupReadAdapter;
    private boolean isOwner;
    private boolean isVolumeNameUpdate = false;
    private BroadcastReceiver receiver;

    public static void notifyVolumeInfoUpdate(Context context) {
        Intent intent = new Intent(Constant.ACTION_VOLUME_INFO_UPDATE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        volume = (Volume) getIntent().getSerializableExtra("volume");
        loadingDlg = new LoadingDialog(this);
        apiService = new VolumeAPIService(this);
        apiService.setAPIInterface(new WebService());
        isOwner = BaseApplication.getInstance().getUid().equals(volume.getOwner());
        volumeNameArrowImg.setVisibility(isOwner ? View.VISIBLE : View.INVISIBLE);
        registerReceiver();
        getVolumeInfo();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.volume_activity_share_volumel_info;
    }

    private void registerReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getVolumeInfo();
            }
        };
        IntentFilter intentFilter = new IntentFilter(Constant.ACTION_VOLUME_INFO_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibt_back) {
            onBackPressed();
        } else if (id == R.id.volume_member_layout) {
            Bundle bundle = new Bundle();
            bundle.putString("title", getString(R.string.volume_clouddriver_volume_member));
            bundle.putInt(MEMBER_PAGE_STATE, CHECK_STATE);
            bundle.putStringArrayList("uidList", volumeDetail.getMemberUidList());
            ARouter.getInstance().build(Constant.AROUTER_CLASS_COMMUNICATION_MEMBER).with(bundle).navigation(this);
        } else if (id == R.id.volume_name_layout) {
            if (isOwner) {
                Intent intent = new Intent(ShareVolumeInfoActivity.this, ShareVolumeNameModifyActivity.class);
                intent.putExtra("volume", volume);
                startActivityForResult(intent, UPDATE_VOLUME_NAME);
            }
        }
    }

    private void showVolumeDetail() {
        if (isOwner) {
            if (volumeDetail.getGroupWriteList().size() > 0) {
                groupWriteLayout.setVisibility(View.VISIBLE);
                groupWriteAdapter = new VolumeInfoGroupAdapter(getApplicationContext(), volumeDetail.getGroupWriteList());
                groupWriteListView.setAdapter(groupWriteAdapter);
                groupWriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        openMemberDetail(position, VOLUME_HAS_UPLOAD_AND_WATCH_PERMISSION);
                    }
                });
            }
            if (volumeDetail.getGroupReadList().size() > 0) {
                groupReadLayout.setVisibility(View.VISIBLE);
                groupReadAdapter = new VolumeInfoGroupAdapter(getApplicationContext(), volumeDetail.getGroupReadList());
                groupReadListView.setAdapter(groupReadAdapter);
                groupReadListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        openMemberDetail(position, VOLUME_HAS_WATCH_PERMISSION);
                    }
                });
            }
        }
        memberLayout.setVisibility(View.GONE);
        memberAdapter = new VolumeInfoMemberAdapter(getApplicationContext(), volumeDetail.getMemberUidList(), isOwner);
        memberGrid.setAdapter(memberAdapter);
        updateVolumeMemNum();
        volumeNameText.setText(volumeDetail.getName());
        memberGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if ((position == memberGrid.getCount() - 2) && isOwner) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("select_content", 2);
                    bundle.putBoolean("isMulti_select", true);
                    bundle.putString("title", getString(R.string.volume_clouddriver_add_volume_member));
                    bundle.putStringArrayList("excludeContactUidList", volumeDetail.getMemberUidList());
                    ARouter.getInstance().build(Constant.AROUTER_CLASS_CONTACT_SEARCH).with(bundle)
                            .navigation(ShareVolumeInfoActivity.this, ADD_MEMBER);
                } else if ((position == memberGrid.getCount() - 1) && isOwner) {
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("memberUidList", volumeDetail.getMemberUidList());
                    bundle.putString("title", getString(R.string.volume_clouddriver_del_member));
                    ARouter.getInstance().build(Constant.AROUTER_CLASS_COMMUNICATION_MEMBER_DEL).with(bundle)
                            .navigation(ShareVolumeInfoActivity.this, DEL_MEMBER);

                } else {
                    String uid = volumeDetail.getMemberUidList().get(position);
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", uid);
                    ARouter.getInstance().build(Constant.AROUTER_CLASS_CONTACT_USERINFO).with(bundle)
                            .navigation(ShareVolumeInfoActivity.this);
                }
            }
        });

    }

    /**
     * 获取网盘下包含自己的组
     */
    private void getVolumeGroupContainMe() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            apiService.getVolumeGroupContainMe(volume.getId());
        } else {
            LoadingDialog.dimissDlg(loadingDlg);
        }
    }

    /**
     * 打开详细成员列表
     */
    private void openMemberDetail(int position, int type) {
        Group group = null;
        if (type == VOLUME_HAS_UPLOAD_AND_WATCH_PERMISSION) {
            group = volumeDetail.getGroupWriteList().get(position);
        } else {
            group = volumeDetail.getGroupReadList().get(position);
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable("group", group);
        bundle.putSerializable("volumeMemList", volumeDetail.getMemberUidList());
        bundle.putSerializable("volume", volume);
        IntentUtils.startActivity(ShareVolumeInfoActivity.this, GroupInfoActivity.class, bundle);
    }

    private void updateVolumeMemNum() {
        volumeMemberText.setText(getString(R.string.volume_clouddriver_all_volume_member_size, volumeDetail.getMemberUidList().size()));
    }

    @Override
    public void onBackPressed() {
        if (isVolumeNameUpdate) {
            Intent intent = new Intent();
            intent.putExtra("volume", volume);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ADD_MEMBER:
                    List<SearchModel> searchModelList = (List<SearchModel>) data.getSerializableExtra("selectMemList");
                    List<String> memberAddUidList = new ArrayList<>();
                    for (int i = 0; i < searchModelList.size(); i++) {
                        memberAddUidList.add(searchModelList.get(i).getId());
                    }
                    volumeMemAdd(memberAddUidList);
                    break;
                case DEL_MEMBER:
                    List<String> memDelUidList = (List<String>) data.getSerializableExtra("selectMemList");
                    volumeMemDel(memDelUidList);
                    break;

                case UPDATE_VOLUME_NAME:
                    isVolumeNameUpdate = true;
                    String volumeName = data.getStringExtra("volumeName");
                    volume.setName(volumeName);
                    volumeDetail.setName(volumeName);
                    volumeNameText.setText(volumeDetail.getName());
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 获取云盘信息
     */
    private void getVolumeInfo() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            if (!loadingDlg.isShowing()) {
                loadingDlg.show();
            }
            apiService.getVolumeInfo(volume.getId());
            getVolumeGroupContainMe();
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
     * 删除网盘成员
     *
     * @param uidList
     */
    private void volumeMemDel(List<String> uidList) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiService.volumeMemDel(volume.getId(), uidList);
        }

    }

    private class WebService extends VolumeAPIInterfaceInstance {
        @Override
        public void returnVolumeDetailSuccess(VolumeDetail volumeDetail) {
            LoadingDialog.dimissDlg(loadingDlg);
            ShareVolumeInfoActivity.this.volumeDetail = volumeDetail;
            showVolumeDetail();

        }

        @Override
        public void returnVolumeDetailFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }


        @Override
        public void returnVolumeMemAddSuccess(List<String> uidList) {
            getVolumeInfo();
            //           LoadingDialog.dimissDlg(loadingDlg);
//            volumeDetail.getMemberUidList().addAll(uidList);
//            memberAdapter.notifyDataSetChanged();
//            updateVolumeMemNum();
        }

        @Override
        public void returnVolumeMemAddFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
            finish();
        }

        @Override
        public void returnVolumeMemDelSuccess(List<String> uidList) {
            getVolumeInfo();
            //           LoadingDialog.dimissDlg(loadingDlg);
//            volumeDetail.getMemberUidList().removeAll(uidList);
//            memberAdapter.notifyDataSetChanged();
//            updateVolumeMemNum();
        }

        @Override
        public void returnVolumeMemDelFail(String error, int errorCode) {
            super.returnVolumeMemDelFail(error, errorCode);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnVolumeGroupContainMeSuccess(GetVolumeGroupResult getVolumeGroupResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            List<String> groupIdList = getVolumeGroupResult.getGroupIdList();
            VolumeGroupContainMe volumeGroupContainMe = new VolumeGroupContainMe(volume.getId(), groupIdList);
            VolumeGroupContainMeCacheUtils.saveVolumeGroupContainMe(getApplicationContext(), volumeGroupContainMe);
        }

        @Override
        public void returnVolumeGroupContainMeFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}

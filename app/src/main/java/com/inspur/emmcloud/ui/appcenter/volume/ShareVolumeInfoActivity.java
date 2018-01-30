package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeInfoGroupAdapter;
import com.inspur.emmcloud.adapter.VolumeInfoMemberAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.appcenter.volume.Group;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeDetail;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.chat.ChannelMembersDelActivity;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.NoScrollGridView;
import com.inspur.emmcloud.widget.ScrollViewWithListView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;


/**
 * 共享网盘详情页面
 */

@ContentView(R.layout.activity_share_volumel_info)
public class ShareVolumeInfoActivity extends BaseActivity {

    private static final int ADD_MEMBER = 1;
    private static final int DEL_MEMBER = 2;
    private static final int UPDATE_VOLUME_NAME = 3;
    private Volume volume;
    private MyAppAPIService apiService;
    private LoadingDialog loadingDlg;
    private VolumeDetail volumeDetail;

    @ViewInject(R.id.member_grid)
    private NoScrollGridView memberGrid;

    @ViewInject(R.id.volume_member_text)
    private TextView volumeMemberText;

    @ViewInject(R.id.volume_name_text)
    private TextView volumeNameText;

    @ViewInject(R.id.group_list)
    private ScrollViewWithListView groupListView;

    @ViewInject(R.id.volume_name_arrow)
    private ImageView volumeNameArrowImg;

    @ViewInject(R.id.group_layout)
    private LinearLayout groupLayout;

    private VolumeInfoMemberAdapter memberAdapter;
    private VolumeInfoGroupAdapter groupAdapter;
    private boolean isOwner;
    private boolean isVolumeNameUpdate = false;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        volume = (Volume) getIntent().getSerializableExtra("volume");
        loadingDlg = new LoadingDialog(this);
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        isOwner = MyApplication.getInstance().getUid().equals(volume.getOwner());
        volumeNameArrowImg.setVisibility(isOwner ? View.VISIBLE : View.INVISIBLE);
        registerReceiver();
        getVolumeInfo();
    }

    private void registerReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getVolumeInfo();
            }
        };
        IntentFilter intentFilter = new IntentFilter(Constant.ACTION_VOLUME_INFO_UPDATE);
        registerReceiver(receiver, intentFilter);
    }

    public static void notifyVolumeInfoUpdate(Context context){
        Intent intent = new Intent(Constant.ACTION_VOLUME_INFO_UPDATE);
        context.sendBroadcast(intent);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                onBackPressed();
                break;
            case R.id.volume_member_layout:
                Bundle bundle = new Bundle();
                bundle.putString("title", getString(R.string.volume_member));
                bundle.putString("search", "1");
                bundle.putStringArrayList("uidList", volumeDetail.getMemberUidList());
                IntentUtils.startActivity(ShareVolumeInfoActivity.this,
                        MembersActivity.class, bundle);
                break;
            case R.id.volume_name_layout:
                if (isOwner) {
                    Intent intent = new Intent(ShareVolumeInfoActivity.this, ShareVolumeNameModifyActivity.class);
                    intent.putExtra("volume", volume);
                    startActivityForResult(intent, UPDATE_VOLUME_NAME);
                }
                break;
            default:
                break;
        }
    }

    private void showVolumeDetail() {
        if (isOwner) {
            groupLayout.setVisibility(View.VISIBLE);
            groupAdapter = new VolumeInfoGroupAdapter(getApplicationContext(), volumeDetail.getGroupList());
            groupListView.setAdapter(groupAdapter);
            groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Group group = volumeDetail.getGroupList().get(position);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("group",group);
                    bundle.putSerializable("volumeMemList",volumeDetail.getMemberUidList());
                    IntentUtils.startActivity(ShareVolumeInfoActivity.this,GroupInfoActivity.class,bundle);
                }
            });
        }
        memberAdapter = new VolumeInfoMemberAdapter(getApplicationContext(), volumeDetail.getMemberUidList(), isOwner);
        memberGrid.setAdapter(memberAdapter);
        updateVolumeMemNum();
        volumeNameText.setText(volumeDetail.getName());
        memberGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                if ((position == memberGrid.getCount() - 2) && isOwner) {
                    intent.putExtra("select_content", 2);
                    intent.putExtra("isMulti_select", true);
                    intent.putExtra("title", getString(R.string.add_volume_member));
                    intent.putExtra("excludeContactUidList", volumeDetail.getMemberUidList());
                    intent.setClass(getApplicationContext(),
                            ContactSearchActivity.class);
                    startActivityForResult(intent, ADD_MEMBER);
                } else if ((position == memberGrid.getCount() - 1) && isOwner) {
                    intent.putExtra("memberUidList", volumeDetail.getMemberUidList());
                    intent.setClass(getApplicationContext(),
                            ChannelMembersDelActivity.class);
                    intent.putExtra("title", getString(R.string.del_volume_member));
                    startActivityForResult(intent, DEL_MEMBER);

                } else {
                    String uid = volumeDetail.getMemberUidList().get(position);
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", uid);
                    IntentUtils.startActivity(ShareVolumeInfoActivity.this, UserInfoActivity.class, bundle);
                }
            }
        });

    }

    private void updateVolumeMemNum() {
        volumeMemberText.setText(getString(R.string.all_volume_member_size,volumeDetail.getMemberUidList().size()));
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
        if (receiver != null){
            unregisterReceiver(receiver);
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

    private class WebService extends APIInterfaceInstance {
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
    }
}

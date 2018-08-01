package com.inspur.emmcloud.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.system.MainTabProperty;
import com.inspur.emmcloud.bean.system.MineLayoutItemGroup;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.mine.card.CardPackageActivity;
import com.inspur.emmcloud.ui.mine.feedback.FeedBackActivity;
import com.inspur.emmcloud.ui.mine.myinfo.MyInfoActivity;
import com.inspur.emmcloud.ui.mine.setting.AboutActivity;
import com.inspur.emmcloud.ui.mine.setting.SettingActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppTabUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * 更多页面
 */
public class MoreFragment extends Fragment {

    private static final int REQUEST_CODE_UPDATE_USER_PHOTO = 3;
    private View rootView;
    private List<MineLayoutItemGroup> mineLayoutItemGroupList = new ArrayList<>();
    private BaseExpandableListAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_mine, null);
        initData();
        initViews();
    }

    private void initData(){
        MainTabProperty mainTabProperty = AppTabUtils.getMainTabProperty(MyApplication.getInstance(),getClass().getSimpleName());
        if (mainTabProperty != null){
            mineLayoutItemGroupList = mainTabProperty.getMineLayoutItemGroupList();
        }
        if (mineLayoutItemGroupList.size() == 0){
            MineLayoutItemGroup mineLayoutItemGroupPersonnalInfo = new MineLayoutItemGroup();
            mineLayoutItemGroupPersonnalInfo.getMineLayoutItemList().add("my_personalInfo_function");
            MineLayoutItemGroup mineLayoutItemGroupSetting = new MineLayoutItemGroup();
            mineLayoutItemGroupSetting.getMineLayoutItemList().add("my_setting_function");
            MineLayoutItemGroup mineLayoutItemGroupCardbox = new MineLayoutItemGroup();
            mineLayoutItemGroupCardbox.getMineLayoutItemList().add("my_cardbox_function");
            MineLayoutItemGroup mineLayoutItemGroupAboutUs = new MineLayoutItemGroup();
            mineLayoutItemGroupAboutUs.getMineLayoutItemList().add("my_aboutUs_function");
            mineLayoutItemGroupList.add(mineLayoutItemGroupPersonnalInfo);
            mineLayoutItemGroupList.add(mineLayoutItemGroupSetting);
            mineLayoutItemGroupList.add(mineLayoutItemGroupCardbox);
            mineLayoutItemGroupList.add(mineLayoutItemGroupAboutUs);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_mine, container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    /**
     * 初始化views
     */
    private void initViews() {
        String appTabs = PreferencesByUserAndTanentUtils.getString(getActivity(), Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
        if (!StringUtils.isBlank(appTabs)) {
            ((TextView) rootView.findViewById(R.id.header_text)).setText(AppTabUtils.getTabTitle(getActivity(), getClass().getSimpleName()));
        }
        ExpandableListView expandListView = (ExpandableListView) rootView.findViewById(R.id.expandable_list);
        expandListView.setGroupIndicator(null);
        expandListView.setVerticalScrollBarEnabled(false);
        expandListView.setHeaderDividersEnabled(false);
        expandListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String layoutItem = mineLayoutItemGroupList.get(groupPosition).getMineLayoutItemList().get(childPosition);
                switch (layoutItem){
                    case "my_personalInfo_function":
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), MyInfoActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_UPDATE_USER_PHOTO);
                        recordUserClick("profile");
                        break;
                    case "my_setting_function":
                        IntentUtils.startActivity(getActivity(), SettingActivity.class);
                        recordUserClick("setting");
                        break;
                    case "my_cardbox_function":
                        recordUserClick("wallet");
                        break;
                    case "my_aboutUs_function":
                        IntentUtils.startActivity(getActivity(),
                                AboutActivity.class);
                        recordUserClick("about");
                        break;
                    case "my_feedback_function":
                        IntentUtils.startActivity(getActivity(), FeedBackActivity.class);
                        recordUserClick("feedback");
                        break;
                    case "my_customerService_function":
                        Channel customerChannel = ChannelCacheUtils.getCustomerChannel(getActivity());
                        if (customerChannel != null){
                            Bundle bundle = new Bundle();
                            bundle.putString("cid", customerChannel.getCid());
                            //为区分来自云+客服添加一个from值，在ChannelActivity里使用
                            bundle.putString("from", "customer");
                            IntentUtils.startActivity(getActivity(), MyApplication.getInstance().isV0VersionChat()?
                                    ChannelV0Activity.class:ChannelActivity.class, bundle);
                        }
                        recordUserClick("customservice");
                        break;
                    default:
                        break;
                }


                return false;
            }
        });
        adapter = new MyAdapter();
        expandListView.setAdapter(adapter);
    }

    /**
     * expandableListView适配器
     */
    public class MyAdapter extends BaseExpandableListAdapter {
        //private List<List<String>> child;

        @Override
        public int getGroupCount() {
            return mineLayoutItemGroupList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mineLayoutItemGroupList.get(groupPosition).getMineLayoutItemList().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mineLayoutItemGroupList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mineLayoutItemGroupList.get(groupPosition).getMineLayoutItemList().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            ExpandableListView expandableListView = (ExpandableListView) parent;
            expandableListView.expandGroup(groupPosition);
            View view = new View(getActivity());
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(MyApplication.getInstance(),10)));
            return view;
        }

        @Override
        public View getChildView(final int groupPosition,
                                 final int childPosition, boolean isLastChild, View convertView,
                                 ViewGroup parent) {
            String layoutItem = (String) getChild(groupPosition, childPosition);
            if (layoutItem.equals("my_personalInfo_function")){
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_mine_myinfo_item_view,null);
                ImageView photoImg = (ImageView)convertView.findViewById(R.id.iv_photo);
                TextView nameText = (TextView)convertView.findViewById(R.id.tv_name);
                TextView enterpriseText = (TextView)convertView.findViewById(R.id.tv_enterprise);
                String photoUri = APIUri.getUserIconUrl(getActivity(), MyApplication.getInstance().getUid());
                ImageDisplayUtils.getInstance().displayImage(photoImg, photoUri, R.drawable.icon_photo_default);
                String userName = PreferencesUtils.getString(getActivity(), "userRealName", getString(R.string.not_set));
                nameText.setText(userName);
                enterpriseText.setText(MyApplication.getInstance().getCurrentEnterprise().getName());
            }else {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_mine_common_item_view,null);
                setViewByLayoutItem(convertView,layoutItem);
            }
            return convertView;
        }

        private void setViewByLayoutItem(View convertView,String layoutItem){
            ImageView iconImg = (ImageView)convertView.findViewById(R.id.iv_icon);
            TextView titleText = (TextView)convertView.findViewById(R.id.tv_title);
            switch (layoutItem){
                case "my_setting_function":
                    iconImg.setImageResource(R.drawable.icon_set);
                    titleText.setText(R.string.settings);
                    break;
                case "my_cardbox_function":
                    iconImg.setImageResource(R.drawable.ic_wallet);
                    titleText.setText(R.string.wallet);
                    break;
                case "my_aboutUs_function":
                    iconImg.setImageResource(R.drawable.icon_circle_logo);
                    titleText.setText(R.string.about_text);
                    break;
                case "my_feedback_function":
                    iconImg.setImageResource(R.drawable.icon_help);
                    titleText.setText(R.string.more_feedback);
                    break;
                case "my_customerService_function":
                    iconImg.setImageResource(R.drawable.ic_customer);
                    titleText.setText(R.string.app_customer);
                    break;
                default:
                    break;
            }
        }


        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_UPDATE_USER_PHOTO) {
           adapter.notifyDataSetChanged();
        }
    }


    /**
     * 记录用户点击的functionId
     *
     * @param functionId
     */
    private void recordUserClick(String functionId) {
        PVCollectModel pvCollectModel = new PVCollectModel(functionId, "mine");
        PVCollectModelCacheUtils.saveCollectModel(getActivity(), pvCollectModel);
    }
}

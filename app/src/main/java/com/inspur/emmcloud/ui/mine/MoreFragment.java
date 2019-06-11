package com.inspur.emmcloud.ui.mine;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.mine.GetUserCardMenusResult;
import com.inspur.emmcloud.bean.system.MineLayoutItem;
import com.inspur.emmcloud.bean.system.MineLayoutItemGroup;
import com.inspur.emmcloud.ui.mine.myinfo.MyInfoActivity;
import com.inspur.emmcloud.ui.mine.setting.AboutActivity;
import com.inspur.emmcloud.ui.mine.setting.EnterpriseSwitchActivity;
import com.inspur.emmcloud.ui.mine.setting.SettingActivity;
import com.inspur.emmcloud.util.privates.UriUtils;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * 更多页面
 */
public class MoreFragment extends BaseFragment {

    private static final int REQUEST_CODE_UPDATE_USER_PHOTO = 3;
    private View rootView;
    private List<MineLayoutItemGroup> mineLayoutItemGroupList = new ArrayList<>();
    private BaseExpandableListAdapter adapter;
    private MyClickListener myClickListener = new MyClickListener();
    private GetMyInfoResult getMyInfoResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater inflater =
                (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_mine, null);
        initData();
        initViews();
    }

    private void initData() {
        String myInfo = PreferencesUtils.getString(MyApplication.getInstance(), "myInfo", "");
        getMyInfoResult = new GetMyInfoResult(myInfo);
        MineLayoutItemGroup mineLayoutItemGroupPersonnalInfo = new MineLayoutItemGroup();
        mineLayoutItemGroupPersonnalInfo.getMineLayoutItemList()
                .add(new MineLayoutItem("my_personalInfo_function", "", "", ""));
        MineLayoutItemGroup mineLayoutItemGroupSetting = new MineLayoutItemGroup();
        mineLayoutItemGroupSetting.getMineLayoutItemList().add(new MineLayoutItem("my_setting_function",
                "personcenter_setting", "", getString(R.string.settings)));
        MineLayoutItemGroup mineLayoutItemGroupAboutUs = new MineLayoutItemGroup();
        mineLayoutItemGroupAboutUs.getMineLayoutItemList().add(new MineLayoutItem("my_aboutUs_function",
                "personcenter_aboutus", "", getString(R.string.about_text)));
        mineLayoutItemGroupList.add(mineLayoutItemGroupPersonnalInfo);
        mineLayoutItemGroupList.add(mineLayoutItemGroupSetting);
        mineLayoutItemGroupList.add(mineLayoutItemGroupAboutUs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setFragmentStatusBarWhite();
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_mine, container, false);
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
        ExpandableListView expandListView = rootView.findViewById(R.id.expandable_list);
        expandListView.setGroupIndicator(null);
        expandListView.setVerticalScrollBarEnabled(false);
        expandListView.setHeaderDividersEnabled(false);
        expandListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition,
                                        long id) {
                MineLayoutItem layoutItem =
                        mineLayoutItemGroupList.get(groupPosition).getMineLayoutItemList().get(childPosition);
                openMineLayoutItem(layoutItem);
                return false;
            }
        });
        adapter = new MyAdapter();
        expandListView.setAdapter(adapter);
    }

    private void openMineLayoutItem(MineLayoutItem layoutItem) {
        String uri = layoutItem.getUri();
        if (StringUtils.isBlank(uri)) {
            switch (layoutItem.getId()) {
                case "my_setting_function":
                    IntentUtils.startActivity(getActivity(), SettingActivity.class);
                    recordUserClick("setting");
                    break;
                case "my_aboutUs_function":
                    IntentUtils.startActivity(getActivity(), AboutActivity.class);
                    recordUserClick("about");
                    break;

                default:
                    break;
            }
        } else {
            if (uri.startsWith("http")) {
                UriUtils.openUrl(getActivity(), uri);
            } else {
                try {
                    Intent intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME);
                    intent.setComponent(null);
                    getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
        PVCollectModelCacheUtils.saveCollectModel(functionId, "mine");
    }


    /**
     * expandableListView适配器
     */
    public class MyAdapter extends BaseExpandableListAdapter {
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
        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ExpandableListView expandableListView = (ExpandableListView) parent;
            expandableListView.expandGroup(groupPosition);
            View view = new View(getActivity());
            int height = groupPosition > 1 ? DensityUtil.dip2px(MyApplication.getInstance(), 10) : 0;
            view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, height));
            view.setBackgroundColor(ContextCompat.getColor(MyApplication.getInstance(), R.color.content_bg));
            return view;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            MineLayoutItem layoutItem = (MineLayoutItem) getChild(groupPosition, childPosition);
            if (layoutItem.getId().equals("my_personalInfo_function")) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_mine_my_info_card, null);
                ImageView photoImg = convertView.findViewById(R.id.iv_photo);
                TextView nameText = convertView.findViewById(R.id.tv_name);
                TextView enterpriseText = convertView.findViewById(R.id.tv_enterprise);
                String photoUri = APIUri.getUserIconUrl(getActivity(), MyApplication.getInstance().getUid());
                ImageDisplayUtils.getInstance().displayImage(photoImg, photoUri, R.drawable.icon_photo_default);
                String userName =
                        PreferencesUtils.getString(getActivity(), "userRealName", getString(R.string.not_set));
                nameText.setText(userName);
                enterpriseText.setText(MyApplication.getInstance().getCurrentEnterprise().getName());
                String UserCardMenus = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_MINE_USER_MENUS, "");
                GetUserCardMenusResult getUserCardMenusResult = new GetUserCardMenusResult(UserCardMenus);
                final List<MineLayoutItem> mineLayoutItemList = getUserCardMenusResult.getMineLayoutItemList();
                LinearLayout userCardMenuLayout = convertView.findViewById(R.id.ll_user_card_menu);
                setUserCardMenuLayout(userCardMenuLayout, mineLayoutItemList);
                convertView.findViewById(R.id.ll_my_info).setOnClickListener(myClickListener);
                Drawable drawable = null;
                if (getMyInfoResult.getEnterpriseList().size() > 1) {
                    enterpriseText.setOnClickListener(myClickListener);
                    int drawableId = ResourceUtils.getResValueOfAttr(getActivity(),R.attr.mine_my_info_switch_enterprise);
                    drawable = ContextCompat.getDrawable(MyApplication.getInstance(),drawableId);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                }
                enterpriseText.setCompoundDrawables(null, null, drawable, null);
            } else {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_mine_common_item_view, null);
                View lineView = convertView.findViewById(R.id.line);
//                lineView.setVisibility(
//                        (childPosition == getChildrenCount(groupPosition) - 1) ? View.INVISIBLE : View.VISIBLE);
                setViewByLayoutItem(convertView, layoutItem);
            }
            return convertView;
        }

        private void setViewByLayoutItem(View convertView, MineLayoutItem layoutItem) {
            CircleTextImageView iconImg = convertView.findViewById(R.id.iv_icon);
            TextView titleText = convertView.findViewById(R.id.tv_name_tips);
            titleText.setText(layoutItem.getTitle());
            String iconUrl = getIconUrl(layoutItem.getIco());
            ImageDisplayUtils.getInstance().displayImage(iconImg, iconUrl, R.drawable.ic_mine_item_default);
        }

        private void setUserCardMenuLayout(LinearLayout userCardMenuLayout, List<MineLayoutItem> mineLayoutItemList) {
            for (final MineLayoutItem mineLayoutItem : mineLayoutItemList) {
                ImageButton menuImgBtn = new ImageButton(getActivity());
                menuImgBtn.setBackground(null);
                int height = DensityUtil.dip2px(MyApplication.getInstance(), 42);
                int width = DensityUtil.dip2px(MyApplication.getInstance(), 48);
                int paddingLeft = DensityUtil.dip2px(MyApplication.getInstance(), 8);
                int paddingTop = DensityUtil.dip2px(MyApplication.getInstance(), 10);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
                menuImgBtn.setScaleType(ImageView.ScaleType.FIT_XY);
                menuImgBtn.setLayoutParams(layoutParams);
                menuImgBtn.setPadding(paddingLeft, paddingTop, paddingLeft, 0);
                int tintColor= ResourceUtils.getResValueOfAttr(getActivity(), R.attr.mine_my_info_menu_tint_color);
                menuImgBtn.setColorFilter(getContext().getResources().getColor(tintColor));
                menuImgBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openMineLayoutItem(mineLayoutItem);
                    }
                });
                String iconUrl = getIconUrl(mineLayoutItem.getIco());
                if (iconUrl.startsWith("drawable")){
                    ImageDisplayUtils.getInstance().displayImageNoCache(menuImgBtn,iconUrl);
                }else {
                    ImageDisplayUtils.getInstance().displayImage(menuImgBtn, iconUrl);
                }
                userCardMenuLayout.addView(menuImgBtn);
            }


        }

        private String getIconUrl(String icon) {
            if (!icon.startsWith("http")) {
                switch (icon.toLowerCase()) {
                    case "personcenter_setting":
                        icon = "drawable://" + R.drawable.ic_mine_setting;
                        break;
                    case "personcenter_cardbox":
                        icon = "drawable://" + R.drawable.ic_mine_wallet;
                        break;
                    case "personcenter_feedback":
                        icon = "drawable://" + R.drawable.ic_mine_feedback;
                        break;
                    case "personcenter_customerservice":
                        icon = "drawable://" + R.drawable.ic_mine_customer;
                        break;
                    case "personcenter_aboutus":
                        icon = "drawable://" + R.drawable.ic_mine_about;
                        // icon="drawable://"+AppUtils.getAppIconRes(MyApplication.getInstance());
                        break;
                }
            }
            return icon;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private class MyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tv_enterprise:
                    IntentUtils.startActivity(getActivity(), EnterpriseSwitchActivity.class);
                    break;
                case R.id.ll_my_info:
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MyInfoActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_UPDATE_USER_PHOTO);
                    recordUserClick("profile");
                    break;
            }
        }
    }

}

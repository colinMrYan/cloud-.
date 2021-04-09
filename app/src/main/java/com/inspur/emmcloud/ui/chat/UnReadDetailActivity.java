package com.inspur.emmcloud.ui.chat;

import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiUri;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.ChannelMessageStates;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class UnReadDetailActivity extends BaseActivity {

    public static final String UI_MESSAGE = "uiMessage";
    public static final String CONVERSATION_ALL_MEMBER = "conversationAllMember";

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private List<String> readList;
    private List<String> unReadList;

    @Override
    public void onCreate() {
        initListData();
        initView();
    }

    private void initListData() {
        UIMessage uiMessage = (UIMessage) (getIntent().getSerializableExtra(UI_MESSAGE));
        List<String> memberList = getIntent().getStringArrayListExtra(CONVERSATION_ALL_MEMBER);
        Map<String, Set<String>> statesMap = uiMessage.getStatesMap();
        if (memberList == null || statesMap == null) {
            ToastUtils.show("数据异常");
            finish();
            return;
        }
        readList = new ArrayList<>();
        if (statesMap.get(ChannelMessageStates.READ) != null) {
            readList.addAll(statesMap.get(ChannelMessageStates.READ));
        }
        unReadList = new ArrayList<>();
        if (statesMap.get(ChannelMessageStates.SENT) != null) {
            for (String id : statesMap.get(ChannelMessageStates.SENT)) {
//                if (memberList.contains(id)) {
                    unReadList.add(id);
//                }
            }
        }
        if (statesMap.get(ChannelMessageStates.DELIVERED) != null) {
            for (String id : statesMap.get(ChannelMessageStates.DELIVERED)) {
//                if (memberList.contains(id)) {
                    unReadList.add(id);
//                }
            }
        }
//        Set<String> tempReadList = statesMap.get(ChannelMessageStates.READ);
//        if (tempReadList != null) {
//            for(String readId : tempReadList){
//                if(memberList.contains(readId)){
//                    readList.add(readId);
//                }
//            }
//        }
//        for (String uid : memberList) {
//            if (!readList.contains(uid)) {
//                unReadList.add(uid);
//            }
//        }
    }

    private void initView() {
        TextView tv_top_title = (TextView) findViewById(R.id.header_text);
        tv_top_title.setVisibility(View.VISIBLE);
        tv_top_title.setText(getResources().getString(R.string.unread_detail_title));
        View ll_top_back = findViewById(R.id.ibt_back);
        ll_top_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        mTabLayout = findViewById(R.id.unread_detail_tabLayout);
        mTabLayout.getTabAt(0).setText(getResources().getString(R.string.unread) + "·" + unReadList.size());
        mTabLayout.getTabAt(1).setText(getResources().getString(R.string.read) + (readList.size() > 0 ? ("·" + readList.size()) : ""));
        mViewPager = findViewById(R.id.unread_detail_viewpager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        initViewPager();
    }

    private void initViewPager() {
        List<View> views = new ArrayList<>();
        views.add(getRecyclerView(unReadList));
        views.add(getRecyclerView(readList));
        mViewPager.setAdapter(new AdapterViewpager(views));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                mTabLayout.getTabAt(i).select();
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    private RecyclerView getRecyclerView(List<String> list) {
        RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(this).inflate(R.layout.unread_detail_recyclerview, mViewPager, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置Adapter
        recyclerView.setAdapter(new RecyclerViewAdapter(list));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);

        dividerItemDecoration.setDrawable(getResources().getDrawable(ResourceUtils.getResValueOfAttr(this, R.attr.drawable_list_divider)));
        recyclerView.addItemDecoration(dividerItemDecoration);
        return recyclerView;
    }


    private static class RecyclerViewAdapter extends RecyclerView.Adapter {
        List<String> mList;

        public RecyclerViewAdapter(List<String> list) {
            mList = list;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View item = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.unread_detail_item, viewGroup, false);
            RecyclerView.ViewHolder holder = new UnReadListViewHolder(item);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            UnReadListViewHolder holder = (UnReadListViewHolder) viewHolder;
            String id = mList.get(i);
            String photoUriItem = BaseModuleApiUri.getUserPhoto(BaseApplication.getInstance(), id);
            ImageDisplayUtils.getInstance().displayImage(holder.headerImage, photoUriItem, R.drawable.icon_photo_default);
            ContactService service = Router.getInstance().getService(ContactService.class);
            if (service != null) {
                ContactUser contactUser = service.getContactUserByUid(id);
                if (contactUser != null) {
                    holder.nameText.setText(contactUser.getName());
                } else {
                    holder.nameText.setText(id);
                    holder.nameText.append("已注销");
                }
            }
        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }
    }


    static class UnReadListViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        CircleTextImageView headerImage;
        TextView nameText;

        public UnReadListViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            headerImage = itemView.findViewById(R.id.unread_detail_header);
            nameText = itemView.findViewById(R.id.unread_detail_name);
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_unread_detail;
    }


    private static class AdapterViewpager extends PagerAdapter {
        private List<View> mViewList;

        public AdapterViewpager(List<View> mViewList) {
            this.mViewList = mViewList;
        }

        @Override
        public int getCount() {//必须实现
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {//必须实现
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {//必须实现，实例化
            container.addView(mViewList.get(position));
            return mViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {//必须实现，销毁
            container.removeView(mViewList.get(position));
        }
    }
}

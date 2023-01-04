package com.inspur.emmcloud.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager.widget.ViewPager;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.Design3MsgInputAddItemAdapter;
import com.inspur.emmcloud.adapter.MyViewPagerAdapter;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.widget.NoScrollGridView;
import com.inspur.emmcloud.bean.chat.InputTypeBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 聊天输入菜单
 */

public class Design3ECMChatInputMenuViewpageLayout extends LinearLayout {

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.page_num_layout)
    LinearLayout pageNumLayout;


    private ImageView[] pageNumImgs;
    private List<InputTypeBean> inputTypeBeanList = new ArrayList<>();
    private AdapterView.OnItemClickListener onItemClickListener;

    public Design3ECMChatInputMenuViewpageLayout(Context context) {
        super(context);
        init(context);
    }

    public Design3ECMChatInputMenuViewpageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Design3ECMChatInputMenuViewpageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.communication_widget_chat_input_menu_container, this, true);
        ButterKnife.bind(this, view);
    }

    public void setInputTypeBeanList(List<InputTypeBean> inputTypeBeanList) {
        this.inputTypeBeanList = inputTypeBeanList;
        initViews();
    }

    public void setOnGridItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private void initViews() {
        List<View> views = new ArrayList<>();
        int gridChildSize = (int) Math.ceil((double) inputTypeBeanList.size() / (double) 8);
        for (int i = 1; i <= gridChildSize; i++) {
            views.add(getGridChildView(i));
        }
        pageNumImgs = new ImageView[views.size()];
        if (views.size() > 1) {
            int pageNumSize = DensityUtil.dip2px(getContext(), 6);
            int margin = DensityUtil.dip2px(getContext(), 6);
            for (int i = 0; i < views.size(); i++) {
                LayoutParams params = new LayoutParams(pageNumSize, pageNumSize);
                params.setMargins(margin, 0, 0, 0);
                ImageView pageNumImg = new ImageView(getContext());
                pageNumImg.setLayoutParams(new ViewGroup.LayoutParams(pageNumSize, pageNumSize));
                pageNumImgs[i] = pageNumImg;
                if (i == 0) {
                    pageNumImgs[i].setBackgroundResource(R.drawable.icon_indicator_sel);
                } else {
                    pageNumImgs[i].setBackgroundResource(R.drawable.icon_indicator_nor);
                }
                pageNumLayout.addView(pageNumImgs[i], params);
            }
            viewPager.addOnPageChangeListener(new GuidePageChangeListener());
        }
        viewPager.setAdapter(new MyViewPagerAdapter(views, null));
    }

    private View getGridChildView(int i) {
        View view = View.inflate(getContext(), R.layout.design3_communication_widget_chat_input_menu_grid, null);
        NoScrollGridView gridView = (NoScrollGridView) view.findViewById(R.id.grid);
        final List<InputTypeBean> pageInputTypeBeanList = new ArrayList<>();
        int startInd = (i - 1) * 8;
        if ((startInd + 8) >= inputTypeBeanList.size()) {
            pageInputTypeBeanList.addAll(inputTypeBeanList.subList(startInd, startInd + (inputTypeBeanList.size() - startInd)));
        } else {
            pageInputTypeBeanList.addAll(inputTypeBeanList.subList(startInd, startInd + 8));
        }
        Design3MsgInputAddItemAdapter adapter = new Design3MsgInputAddItemAdapter(getContext(), pageInputTypeBeanList);
        Configuration configuration = getResources().getConfiguration();
        // 适配横屏显示
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridView.setNumColumns(6);

        } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridView.setNumColumns(4);

        }
        gridView.setAdapter(adapter);
        if (onItemClickListener != null) {
            gridView.setOnItemClickListener(onItemClickListener);
        }
        return view;
    }

    class GuidePageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int arg0) {

            for (int i = 0; i < pageNumImgs.length; i++) {
                pageNumImgs[arg0].setBackgroundResource(R.drawable.icon_indicator_sel);

                if (arg0 != i) {
                    pageNumImgs[i].setBackgroundResource(R.drawable.icon_indicator_nor);
                }
            }
        }
    }
}

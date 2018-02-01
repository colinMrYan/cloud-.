package com.inspur.emmcloud.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MsgInputAddItemAdapter;
import com.inspur.emmcloud.adapter.MyViewPagerAdapter;
import com.inspur.emmcloud.bean.chat.InputTypeBean;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.AppUtils;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天输入菜单
 */

public class ECMChatInputMenuViewpageLayout extends LinearLayout {

    private static final int GELLARY_RESULT = 2;
    private static final int CAMERA_RESULT = 3;
    private static final int CHOOSE_FILE = 4;
    private static final int MENTIONS_RESULT = 5;
    @ViewInject(R.id.viewpager)
    private ViewPager viewPager;

    @ViewInject(R.id.page_num_layout)
    private LinearLayout pageNumLayout;


    private ImageView[] pageNumImgs;
    private List<InputTypeBean> inputTypeBeanList = new ArrayList<>();
    private String cid;

    public ECMChatInputMenuViewpageLayout(Context context) {
        super(context);
        init(context);
    }

    public ECMChatInputMenuViewpageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ECMChatInputMenuViewpageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.ecm_widget_chat_input_menu_container, this, true);
        x.view().inject(view);
    }

    public void setInputTypeBeanList(List<InputTypeBean> inputTypeBeanList,String cid) {
        this.inputTypeBeanList = inputTypeBeanList;
        this.cid = cid;
        initViews();
    }

    private void initViews() {
        List<View> views = new ArrayList<>();
        int gridChildSize = (int) Math.ceil((double) inputTypeBeanList.size() / (double) 8);
        for (int i = 1; i <= gridChildSize; i++) {
            views.add(getGridChildView(i));
        }
        pageNumImgs = new ImageView[views.size()];
        if (views.size()>1){
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
        View view = View.inflate(getContext(), R.layout.ecm_widget_chat_input_menu_grid, null);
        NoScrollGridView gridView = (NoScrollGridView) view.findViewById(R.id.grid);
        final List<InputTypeBean> pageInputTypeBeanList = new ArrayList<>();
        int startInd = (i - 1) * 8;
        if ((startInd + 8) >= inputTypeBeanList.size()) {
            pageInputTypeBeanList.addAll(inputTypeBeanList.subList(startInd, startInd + (inputTypeBeanList.size() - startInd)));
        } else {
            pageInputTypeBeanList.addAll(inputTypeBeanList.subList(startInd, startInd + 8));
        }
        MsgInputAddItemAdapter adapter = new MsgInputAddItemAdapter(getContext(), pageInputTypeBeanList);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputTypeBean inputTypeBean = pageInputTypeBeanList.get(position);
                switch (inputTypeBean.getAction()) {
                    case "gallery":
                        AppUtils.openGallery((Activity) getContext(), 5, GELLARY_RESULT);
                        break;
                    case "camera":
                        String fileName = System.currentTimeMillis() + ".jpg";
                        PreferencesUtils.putString(getContext(), "capturekey", fileName);
                        AppUtils.openCamera((Activity) getContext(), fileName, CAMERA_RESULT);
                        break;
                    case "file":
                        AppUtils.openFileSystem((Activity) getContext(), CHOOSE_FILE);
                        break;
                    case "mention":
                        openMention(false);
                        break;
                    default:
                        break;
                }
            }
        });
        return view;
    }

    /**
     * 是否是输入了关键字@字符打开mention页
     *
     * @param isInputKeyWord
     */
    private void openMention(boolean isInputKeyWord) {
        Intent intent = new Intent();
        intent.setClass(getContext(), MembersActivity.class);
        intent.putExtra("title", getContext().getString(R.string.friend_list));
        intent.putExtra("cid", cid);
        intent.putExtra("isInputKeyWord", isInputKeyWord);
        ((Activity) getContext()).overridePendingTransition(
                R.anim.activity_open, 0);

        ((Activity) getContext()).startActivityForResult(intent,
                MENTIONS_RESULT);

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

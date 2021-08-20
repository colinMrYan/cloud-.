package com.inspur.emmcloud.web.ui;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.componentservice.application.maintab.MainTabMenu;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.R2;

import java.util.List;

import butterknife.BindView;

/**
 * Created by chenmch on 2018/11/27.
 */

public class ImpBaseFragment extends BaseFragment {
    protected static final String JAVASCRIPT_PREFIX = "javascript:";
    protected List<MainTabMenu> optionMenuList;
    @BindView(R2.id.function_layout)
    RelativeLayout functionLayout;
    @BindView(R2.id.ll_web_function)
    LinearLayout webFunctionLayout;
    @BindView(R2.id.header_text)
    TextView headerText;
    @BindView(R2.id.header_image)
    ImageView headerImage;
    private int functionLayoutWidth = -1;
    private int webFunctionLayoutWidth = -1;

    protected void initHeaderOptionMenu() {
        if (optionMenuList != null && optionMenuList.size() != 0) {
            webFunctionLayout.removeAllViews();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            if (optionMenuList.size() < 3) {
                for (final MainTabMenu mainTabMenu : optionMenuList) {
                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runJavaScript(JAVASCRIPT_PREFIX + mainTabMenu.getAction());
                        }
                    };
                    String icon = mainTabMenu.getIco();
                    if (!StringUtils.isBlank(icon)) {
                        ImageView imageView = new ImageView(getContext());
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setAdjustViewBounds(true);
                        int paddingLeft = DensityUtil.dip2px(getContext(), 16);
                        int paddingTop = DensityUtil.dip2px(getContext(), 17);
                        imageView.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop);
                        imageView.setOnClickListener(onClickListener);
                        imageView.setLayoutParams(params);
                        imageView.setColorFilter(getContext().getResources().getColor(ResourceUtils.getResValueOfAttr(getActivity(), R.attr.header_text_color_e1)));
                        ImageDisplayUtils.getInstance().displayImage(imageView, mainTabMenu.getIco());
                        webFunctionLayout.addView(imageView);
                    } else {
                        TextView textView = new TextView(getContext());
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.header_function_textsize));
                        int paddingLeft = DensityUtil.dip2px(getContext(), 16);
                        textView.setMinWidth(DensityUtil.dip2px(BaseApplication.getInstance(), 48));
                        textView.setPadding(paddingLeft, 0, paddingLeft, 0);
                        textView.setText(mainTabMenu.getText());
                        textView.setOnClickListener(onClickListener);
                        textView.setLayoutParams(params);
                        int textColor = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.header_text_color_e1);
                        textView.setTextColor(getContext().getResources().getColor(textColor));
                        textView.setGravity(Gravity.CENTER_VERTICAL);
                        webFunctionLayout.addView(textView);
                    }
                }
            } else {
                final ImageView imageView = new ImageView(getContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setAdjustViewBounds(true);
                int paddingLeft = DensityUtil.dip2px(getContext(), 16);
                int paddingTop = DensityUtil.dip2px(getContext(), 17);
                imageView.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showOptionMenu(imageView);
                    }
                });
                imageView.setLayoutParams(params);
                int drawableResId = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.ic_header_option);
                imageView.setImageResource(drawableResId);
                //ImageDisplayUtils.getInstance().displayImage(imageView, "drawable://" + drawableResId);
                webFunctionLayout.addView(imageView);
            }
            setHeaderTextWidth();
        }
    }


    /**
     * 动态监控布局变化
     */
    protected void setHeaderTextWidth() {
        webFunctionLayout.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    functionLayoutWidth = functionLayout.getWidth();
                    webFunctionLayoutWidth = webFunctionLayout.getWidth();
                    headerText.setMaxWidth(ResolutionUtils.getWidth(getActivity()) - getMaxWidth() * 2);
                }
            }
        });
    }

    /**
     * 取两个宽度的最大值
     *
     * @return
     */
    private int getMaxWidth() {
        if (functionLayoutWidth > webFunctionLayoutWidth) {
            return functionLayoutWidth;
        }
        return webFunctionLayoutWidth;
    }


    private void showOptionMenu(View view) {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.web_pop_header_option_menu, null);
        int width = StringUtils.isBlank(optionMenuList.get(0).getIco()) ? DensityUtil.dip2px(BaseApplication.getInstance(), 130) : DensityUtil.dip2px(BaseApplication.getInstance(), 160);
        final PopupWindow optionMenuPop = new PopupWindow(contentView, width, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        ListView menuListView = (ListView) contentView.findViewById(R.id.lv_menu);
        menuListView.setAdapter(new MenuAdapter());
        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                optionMenuPop.dismiss();
                runJavaScript(JAVASCRIPT_PREFIX + optionMenuList.get(position).getAction());
            }
        });
        optionMenuPop.setTouchable(true);
        optionMenuPop.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        optionMenuPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                AppUtils.setWindowBackgroundAlpha(getActivity(), 1.0f);
            }
        });
        optionMenuPop.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_window_view_tran));
        AppUtils.setWindowBackgroundAlpha(getActivity(), 0.8f);
        // 设置好参数之后再show
        optionMenuPop.showAsDropDown(view);
    }

    /**
     * 执行JS脚本
     *
     * @param script
     */
    protected void runJavaScript(String script) {

    }

    private class MenuAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return optionMenuList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MainTabMenu optionMenu = optionMenuList.get(position);
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.web_pop_header_option_menu_item_view, null);
            ImageView iconImg = (ImageView) convertView.findViewById(R.id.iv_icon);
            TextView textView = (TextView) convertView.findViewById(R.id.tv_text);
            if (StringUtils.isBlank(optionMenu.getIco())) {
                iconImg.setVisibility(View.GONE);
            } else {
                iconImg.setVisibility(View.VISIBLE);
                iconImg.setColorFilter(getContext().getResources().getColor(R.color.header_text_black));
                ImageDisplayUtils.getInstance().displayImage(iconImg, optionMenu.getIco());
            }
            textView.setText(optionMenu.getText());
            return convertView;
        }
    }

}

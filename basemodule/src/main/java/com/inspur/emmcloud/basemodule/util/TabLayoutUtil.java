package com.inspur.emmcloud.basemodule.util;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.ResolutionUtils;

import java.lang.reflect.Field;

public class TabLayoutUtil {
    /**
     * 设置layout的宽度
     */
    public static void setTabLayoutWidth(Context context, TabLayout tabLayout) {
        try {
            //拿到tabLayout的mTabStrip属性
            Field mTabStripField = tabLayout.getClass().getDeclaredField("mTabStrip");
            mTabStripField.setAccessible(true);
            LinearLayout mTabStrip = (LinearLayout) mTabStripField.get(tabLayout);
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                View tabView = mTabStrip.getChildAt(i);
                //拿到tabView的mTextView属性
                Field mTextViewField = tabView.getClass().getDeclaredField("mTextView");
                mTextViewField.setAccessible(true);
                TextView mTextView = (TextView) mTextViewField.get(tabView);
                tabView.setPadding(0, 0, 0, 0);
                //因为我想要的效果是   字多宽线就多宽，所以测量mTextView的宽度
                int width = 0;
                width = mTextView.getWidth();
                if (width == 0) {
                    mTextView.measure(0, 0);
                    width = mTextView.getMeasuredWidth();
                }
                //设置tab左右间距为10dp  注意这里不能使用Padding 因为源码中线的宽度是根据 tabView的宽度来设置的
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                params.width = width;
                params.leftMargin = getTabWith(context, tabLayout.getTabCount(), width);
                params.rightMargin = getTabWith(context, tabLayout.getTabCount(), width);
                tabView.setLayoutParams(params);
                tabView.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据当前tab的宽度，计算tab两侧应该加的间距
     *
     * @param width
     * @return
     */
    private static int getTabWith(Context context, int tabCount, int width) {
        return (ResolutionUtils.getWidth(context) - width * tabCount) / (2 * tabCount);
    }
}

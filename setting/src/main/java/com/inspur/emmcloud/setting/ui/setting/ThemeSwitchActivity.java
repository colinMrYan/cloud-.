package com.inspur.emmcloud.setting.ui.setting;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2019/2/26.
 */
public class ThemeSwitchActivity extends BaseActivity {
    private static final int[] THEME_FLAG = {R.drawable.ic_mine_theme_white, R.drawable.ic_mine_theme_grey, R.drawable.ic_mine_theme_blue};
    private static final int[] THEME_NAME = {R.string.setting_mine_theme_white, R.string.setting_mine_theme_grey, R.string.setting_mine_theme_blue};
    @BindView(R2.id.tv_header)
    TextView headerText;
    @BindView(R2.id.lv)
    ListView listView;

    public static String getThemeName() {
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        return BaseApplication.getInstance().getString(THEME_NAME[currentThemeNo]);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        headerText.setText(R.string.setting_mine_theme_switch);
        listView.setAdapter(new Adapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
                if (currentThemeNo != i) {
                    PreferencesUtils.putInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, i);
                    setTheme();
//                    Intent intent = new Intent(ThemeSwitchActivity.this,
//                            IndexActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
                    ARouter.getInstance().build(Constant.AROUTER_CLASS_APP_INDEX).withFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK).navigation(ThemeSwitchActivity.this);
                }

            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_mine_language_switch_activity;
    }

    public void onClick(View v) {
        finish();
    }

    private class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            return THEME_FLAG.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(ThemeSwitchActivity.this).inflate(R.layout.setting_mine_setting_theme_list_item, null);
            ImageView themeFlagImg = view.findViewById(R.id.iv_theme_flag);
            TextView themeNameText = view.findViewById(R.id.tv_theme_name);
            ImageView selectImg = view.findViewById(R.id.iv_select);
            themeFlagImg.setImageResource(THEME_FLAG[i]);
            themeNameText.setText(THEME_NAME[i]);
            int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
            selectImg.setVisibility((currentThemeNo == i) ? View.VISIBLE : View.INVISIBLE);
            return view;
        }
    }
}

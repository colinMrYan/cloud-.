package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.common.PreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2019/2/26.
 */
public class ThemeSwitchActivity extends BaseActivity {
    private static final int[] THEME_FLAG = {R.drawable.ic_mine_theme_white, R.drawable.ic_mine_theme_grey, R.drawable.ic_mine_theme_blue};
    private static final int[] THEME_NAME = {R.string.mine_theme_white, R.string.mine_theme_grey, R.string.mine_theme_blue};
    @BindView(R.id.tv_header)
    TextView headerText;
    @BindView(R.id.lv)
    ListView listView;

    public static String getThemeName() {
        int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        return MyApplication.getInstance().getString(THEME_NAME[currentThemeNo]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        headerText.setText(R.string.mine_theme_switch);
        listView.setAdapter(new Adapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
                if (currentThemeNo != i) {
                    PreferencesUtils.putInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, i);
                    setTheme();
                    Intent intent = new Intent(ThemeSwitchActivity.this,
                            IndexActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_mine_language_switch;
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
            view = LayoutInflater.from(ThemeSwitchActivity.this).inflate(R.layout.mine_setting_theme_list_item, null);
            ImageView themeFlagImg = view.findViewById(R.id.iv_theme_flag);
            TextView themeNameText = view.findViewById(R.id.tv_theme_name);
            ImageView selectImg = view.findViewById(R.id.iv_select);
            themeFlagImg.setImageResource(THEME_FLAG[i]);
            themeNameText.setText(THEME_NAME[i]);
            int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
            selectImg.setVisibility((currentThemeNo == i) ? View.VISIBLE : View.INVISIBLE);
            return view;
        }
    }
}

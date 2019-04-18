package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.content.res.Configuration;
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
import com.inspur.emmcloud.bean.system.navibar.NaviBarModel;
import com.inspur.emmcloud.bean.system.navibar.NaviBarScheme;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.List;

/**
 * Created by yufuchang on 2019/4/12.
 */

@ContentView(R.layout.activity_mine_tab_layout_switch)
public class TabLayoutSwitchActivity extends BaseActivity {
    @ViewInject(R.id.tv_header)
    private TextView headerText;
    @ViewInject(R.id.lv)
    private ListView listView;
    private NaviBarModel naviBarModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        naviBarModel = new NaviBarModel(PreferencesByUserAndTanentUtils.getString(this,Constant.APP_TAB_LAYOUT_DATA,""));
        headerText.setText(R.string.mine_tab_layout);
        listView.setAdapter(new Adapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String currentTabLayoutName = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(),Constant.APP_TAB_LAYOUT_NAME,"");
                String selectedTabLayoutName = naviBarModel.getNaviBarPayload().getNaviBarSchemeList().get(i).getName();
                if(!currentTabLayoutName.equals(selectedTabLayoutName)){
                    PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(),Constant.APP_TAB_LAYOUT_NAME,selectedTabLayoutName);
                    Intent intent = new Intent(TabLayoutSwitchActivity.this, IndexActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    public void onClick(View v) {
        finish();
    }

    private class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            return naviBarModel.getNaviBarPayload().getNaviBarSchemeList().size();
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
            view = LayoutInflater.from(TabLayoutSwitchActivity.this).inflate(R.layout.mine_setting_tab_layout_list_item, null);
            TextView themeNameText = view.findViewById(R.id.tv_tab_layout_name);
            ImageView selectImg = view.findViewById(R.id.iv_select);
            Configuration config = getResources().getConfiguration();
            String environmentLanguage = config.locale.getLanguage();
            switch (environmentLanguage.toLowerCase()) {
                case "zh-hant":
                    themeNameText.setText(naviBarModel.getNaviBarPayload().getNaviBarSchemeList().get(i).getNaviBarTitleResult().getZhHans());
                    break;
                case "en":
                case "en-us":
                    themeNameText.setText(naviBarModel.getNaviBarPayload().getNaviBarSchemeList().get(i).getNaviBarTitleResult().getEnUS());
                    break;
                default:
                    themeNameText.setText(naviBarModel.getNaviBarPayload().getNaviBarSchemeList().get(i).getNaviBarTitleResult().getZhHans());
                    break;
            }
            selectImg.setVisibility(getSelectedShow(i));
            return view;
        }
    }

    /**
     * 根据listView的位置设置选中状态的标识是否显示
     * @param index
     * @return
     */
    private int getSelectedShow(int index) {
        List<NaviBarScheme> naviBarSchemeList = naviBarModel.getNaviBarPayload().getNaviBarSchemeList();
        String currentTabLayoutName = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(),Constant.APP_TAB_LAYOUT_NAME,"");
        if(StringUtils.isBlank(currentTabLayoutName)){
            currentTabLayoutName = naviBarModel.getNaviBarPayload().getDefaultScheme();
        }
        for(int i = 0; i < naviBarSchemeList.size(); i++){
            if(naviBarSchemeList.get(i).getName().equals(currentTabLayoutName) && index == i){
                return View.VISIBLE;
            }
        }
        return View.GONE;
    }
}

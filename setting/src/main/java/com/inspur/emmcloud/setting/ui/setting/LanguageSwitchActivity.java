package com.inspur.emmcloud.setting.ui.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.bean.Language;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ClientConfigUpdateUtils;
import com.inspur.emmcloud.basemodule.util.LanguageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class LanguageSwitchActivity extends BaseActivity implements LanguageManager.GetServerLanguageListener {

    public static final String LANGUAGE_CHANGE = "change_language";
    private static final int GET_LANGUAGE_SUCCESS = 3;
    private ListView listView;
    private ListViewAdapter adapter;
    private List<Language> commonLanguageList = new ArrayList<Language>();
    private LoadingDialog loadingDlg;

    @Override
    public void onCreate() {
        listView = (ListView) findViewById(R.id.lv);
        loadingDlg = new LoadingDialog(this);
        getLanguageList();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_mine_language_switch;
    }

    /**
     * 获取语言列表
     */
    private void getLanguageList() {
        // TODO Auto-generated method stub
        loadingDlg.show();
        LanguageManager.getInstance().getServerSupportLanguage(this);
    }

    private void initData() {
        // TODO Auto-generated method stub
        String appDefaultLanguage = Locale.getDefault().getCountry();
        Language language = LanguageManager.getInstance().getContainedLanguage(
                commonLanguageList, appDefaultLanguage);
        if (language == null) {
            language = commonLanguageList.get(0);
        }
        commonLanguageList.add(0, language);
        adapter = new ListViewAdapter();
        listView.setAdapter(adapter);
        listView.setVerticalScrollBarEnabled(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                String currentLanguageName = LanguageManager.getInstance().getCurrentLanguageName();
                Language language = commonLanguageList.get(position);
                String languageName = "";
                if (position == 0) {
                    languageName = "followSys";
                } else {
                    languageName = language.getIso();
                }
                if (!currentLanguageName.equals(languageName)) {
                    showChangeLanguageDlg(position);
                }
            }
        });
    }

    /**
     * 弹出改变语言提示框
     *
     * @param position
     */
    private void showChangeLanguageDlg(final int position) {
        // TODO Auto-generated method stub

        new CustomDialog.MessageDialogBuilder(LanguageSwitchActivity.this)
                .setMessage(getString(R.string.confirm_modify_language))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //清空我的应用统一更新版本信息防止切换语言不刷新列表
                        ClientConfigUpdateUtils.getInstance().clearDbDataConfigWithMyApp();
                        Language language = commonLanguageList.get(position);
                        String languageName = "";
                        if (position == 0) {
                            languageName = "followSys";
                        } else {
                            languageName = language.getIso();
                        }
                        LanguageManager.getInstance().setCurrentLanguageName(languageName);
                        LanguageManager.getInstance().setCurrentLanguageJson(language.toString());
                        LanguageManager.getInstance().setLanguageLocal();
//                        Intent intentLog = new Intent(LanguageSwitchActivity.this,
//                                IndexActivity.class);
//                        intentLog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        intentLog.putExtra(LANGUAGE_CHANGE, true);
//                        startActivity(intentLog);
                        Bundle build = new Bundle();
                        build.putBoolean(LANGUAGE_CHANGE, true);
                        ARouter.getInstance().build(Constant.AROUTER_CLASS_APP_INDEX).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK).with(build).navigation(LanguageSwitchActivity.this);
                    }
                })
                .show();
    }

    public void onClick(View v) {
        finish();
    }

    @Override
    public void complete() {
        LoadingDialog.dimissDlg(loadingDlg);
        commonLanguageList = LanguageManager.getInstance().getCommonLanguageList(null);
        initData();
    }

    public class ListViewAdapter extends BaseAdapter {

        // 用于记录每个RadioButton的状态，并保证只可选一个
        HashMap<String, Boolean> states = new HashMap<String, Boolean>();

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return commonLanguageList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return commonLanguageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            // TODO Auto-generated method stub
            // 页面
            ViewHolder holder;
            Language language = commonLanguageList.get(position);
            LayoutInflater inflater = LayoutInflater
                    .from(getApplicationContext());
            if (convertView == null) {
                convertView = inflater.inflate(
                        R.layout.mine_setting_language_list_item, null);
                holder = new ViewHolder();
                holder.nameText = convertView
                        .findViewById(R.id.tv_language_name);
                holder.selectImg = convertView
                        .findViewById(R.id.iv_select);
                holder.flagImg = convertView
                        .findViewById(R.id.iv_language_flag);
                holder.space = convertView
                        .findViewById(R.id.space);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String languageName = LanguageManager.getInstance().getCurrentLanguageName();
            if (position == 0) {
                holder.flagImg.setVisibility(View.VISIBLE);
                holder.flagImg.setImageResource(R.drawable.ic_mine_language_follow_system);
                holder.nameText.setText(getString(R.string.follow_system));
                if (languageName.equals("followSys")) {
                    holder.selectImg.setVisibility(View.VISIBLE);
                } else {
                    holder.selectImg.setVisibility(View.INVISIBLE);
                }
                holder.space.setVisibility(View.VISIBLE);
            } else {
                String iso = commonLanguageList.get(position).getIso();
                iso = iso.replace("-", "_");
                iso = iso.toLowerCase();
                Integer id = getResources().getIdentifier(iso, "drawable",
                        getApplicationContext().getPackageName());
                if (id == null) {
                    id = R.drawable.zh_cn;
                }
                holder.flagImg.setVisibility(View.VISIBLE);
                holder.flagImg.setImageResource(id);
                holder.nameText.setText(language.getLabel());
                if (languageName.equals(language.getIso())) {
                    holder.selectImg.setVisibility(View.VISIBLE);
                } else {
                    holder.selectImg.setVisibility(View.INVISIBLE);
                }
                holder.space.setVisibility(View.GONE);
            }

            if (position == 0) {
                if (languageName.equals("followSys")) {
                    holder.selectImg.setVisibility(View.VISIBLE);
                } else {
                    holder.selectImg.setVisibility(View.INVISIBLE);
                }
            } else if (languageName.equals(language.getIso())) {
                holder.selectImg.setVisibility(View.VISIBLE);
            } else {
                holder.selectImg.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        class ViewHolder {
            Space space;
            TextView nameText;
            ImageView selectImg;
            ImageView flagImg;
        }
    }
}

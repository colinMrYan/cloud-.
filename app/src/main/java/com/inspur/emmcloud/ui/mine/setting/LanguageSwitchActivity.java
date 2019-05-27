package com.inspur.emmcloud.ui.mine.setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.mine.Language;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.ClientConfigUpdateUtils;
import com.inspur.emmcloud.util.privates.LanguageUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class LanguageSwitchActivity extends BaseActivity {

    public static final String LANGUAGE_CHANGE = "change_language";
    private static final int GET_LANGUAGE_SUCCESS = 3;
    private ListView listView;
    private ListViewAdapter adapter;
    private LanguageUtils languageUtils;
    private Handler handler;
    private List<Language> commonLanguageList = new ArrayList<Language>();
    private LoadingDialog loadingDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        listView = (ListView) findViewById(R.id.lv);
        loadingDlg = new LoadingDialog(this);
        handMessage();
        getLanguageList();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_mine_language_switch;
    }

    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                if (loadingDlg != null && loadingDlg.isShowing()) {
                    loadingDlg.dismiss();
                }
                switch (msg.what) {
                    case GET_LANGUAGE_SUCCESS:
                        commonLanguageList = languageUtils.getCommonLanguageList();
                        initData();
                        break;

                    default:
                        break;
                }
            }

        };
    }

    /**
     * 获取语言列表
     */
    private void getLanguageList() {
        // TODO Auto-generated method stub
        loadingDlg.show();
        languageUtils = new LanguageUtils(LanguageSwitchActivity.this, handler);
        languageUtils.getServerSupportLanguage();
    }

    private void initData() {
        // TODO Auto-generated method stub
        String appDefaultLanguage = Locale.getDefault().getCountry();
        Language language = languageUtils.getContainedLanguage(
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
                String currentLanguageName = PreferencesUtils.getString(
                        getApplicationContext(), MyApplication.getInstance().getTanent() + "language",
                        "");
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

        new MyQMUIDialog.MessageDialogBuilder(LanguageSwitchActivity.this)
                .setMessage(getString(R.string.confirm_modify_language))
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
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
                        PreferencesUtils.putString(getApplicationContext(),
                                MyApplication.getInstance().getTanent() + "language", languageName);
                        PreferencesUtils.putString(getApplicationContext(),
                                MyApplication.getInstance().getTanent() + "appLanguageObj",
                                language.toString());
                        ((MyApplication) getApplicationContext())
                                .setAppLanguageAndFontScale();
                        Intent intentLog = new Intent(LanguageSwitchActivity.this,
                                IndexActivity.class);
                        intentLog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intentLog.putExtra(LANGUAGE_CHANGE, true);
                        startActivity(intentLog);
                    }
                })
                .show();
    }

    public void onClick(View v) {
        finish();
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

            String languageName = PreferencesUtils.getString(
                    getApplicationContext(), MyApplication.getInstance().getTanent() + "language", "");
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

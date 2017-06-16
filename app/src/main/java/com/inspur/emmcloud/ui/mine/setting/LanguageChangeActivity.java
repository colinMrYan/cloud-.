package com.inspur.emmcloud.ui.mine.setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.DialogInterface;
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
import android.widget.RadioButton;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Language;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.LanguageUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;

public class LanguageChangeActivity extends BaseActivity {

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
		((MyApplication) getApplicationContext()).addActivity(this);
		setContentView(R.layout.activity_language_change);
		listView = (ListView) findViewById(R.id.language_change_list);
		loadingDlg = new LoadingDialog(this);
		handMessage();
		getLanguageList();
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
		languageUtils = new LanguageUtils(LanguageChangeActivity.this, handler);
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
						getApplicationContext(), UriUtils.tanent + "language",
						"");
				Language language = commonLanguageList.get(position);
				String languageName = "";
				if (position == 0) {
					languageName = "followSys";
				} else {
					languageName = language.getIso();
				}
                LogUtils.jasonDebug("00000000000000000000000000");
				if (!currentLanguageName.equals(languageName)) {
					showChangeLanguageDlg(position);
                    LogUtils.jasonDebug("1111111111");
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
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				if (which == -1) {
					Language language = commonLanguageList.get(position);
					String languageName = "";
					if (position == 0) {
						languageName = "followSys";
					} else {
						languageName = language.getIso();
					}
					PreferencesUtils.putString(getApplicationContext(),
							UriUtils.tanent + "language", languageName);
					PreferencesUtils.putString(getApplicationContext(),
							UriUtils.tanent + "appLanguageObj",
							language.toString());

//					Configuration config = getResources().getConfiguration();
					((MyApplication) getApplicationContext())
							.setAppLanguage();
					Intent intentLog = new Intent(LanguageChangeActivity.this,
							IndexActivity.class);
					intentLog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intentLog);

				}

			}

		};
        LogUtils.jasonDebug("333333333333");
		EasyDialog.showDialog(LanguageChangeActivity.this,
				getString(R.string.prompt),
				getString(R.string.confirm_modify_language),
				getString(R.string.ok), getString(R.string.cancel), listener,
				true);
        LogUtils.jasonDebug("444444444444444");
	}

	public void onClick(View v) {
		finish();
	}

	public class ListViewAdapter extends BaseAdapter {

		// 用于记录每个RadioButton的状态，并保证只可选一个
		HashMap<String, Boolean> states = new HashMap<String, Boolean>();

		class ViewHolder {

			TextView tvName;
			RadioButton languageRadioButton;
			ImageView imageView;
			ImageView flagImg;
		}

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
						R.layout.languagechange_list_item, null);
				holder = new ViewHolder();
				holder.tvName = (TextView) convertView
						.findViewById(R.id.language_name_text);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.language_set_img);
				holder.flagImg = (ImageView) convertView
						.findViewById(R.id.flag_img);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String languageName = PreferencesUtils.getString(
					getApplicationContext(), UriUtils.tanent + "language", "");
			if (position == 0) {
				holder.flagImg.setVisibility(View.INVISIBLE);
				holder.tvName.setText(getString(R.string.follow_system));
				if (languageName.equals("followSys")) {
					holder.imageView.setVisibility(View.VISIBLE);
				} else {
					holder.imageView.setVisibility(View.INVISIBLE);
				}
			} else {
				String iso = commonLanguageList.get(position).getIso();
				iso = iso.replace("-", "_");
				iso = iso.toLowerCase();
				int id = getResources().getIdentifier(iso, "drawable",
						getApplicationContext().getPackageName());
				holder.flagImg.setVisibility(View.VISIBLE);
				holder.flagImg.setImageResource(id);
				holder.tvName.setText(language.getLabel());
				if (languageName.equals(language.getIso())) {
					holder.imageView.setVisibility(View.VISIBLE);
				} else {
					holder.imageView.setVisibility(View.INVISIBLE);
				}
			}

			if (position == 0) {
				if (languageName.equals("followSys")) {
					holder.imageView.setVisibility(View.VISIBLE);
				} else {
					holder.imageView.setVisibility(View.INVISIBLE);
				}
			} else if (languageName.equals(language.getIso())) {
				holder.imageView.setVisibility(View.VISIBLE);
			} else {
				holder.imageView.setVisibility(View.INVISIBLE);
			}

			return convertView;
		}
	}
}

package com.inspur.imp.plugin.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.inspur.imp.api.Res;

import java.util.ArrayList;

/**
 * 这个是显示一个文件夹里面的所有图片时的界面
 * 
 * @author @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class ShowAllPhoto extends Activity {
	private GridView gridView;
	private ProgressBar progressBar;
	private AlbumGridViewAdapter gridImageAdapter;
	// 完成按钮
	private Button okButton;
	// 预览按钮
	private Button preview;
	// 返回按钮
	private Button back;
	// 取消按钮
	private Button cancel;
	// 标题
	private TextView headTitle;
	private Intent intent;
	public static ArrayList<ImageItem> dataList = new ArrayList<ImageItem>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(Res.getLayoutID("plugin_camera_show_all_photo"));
		PublicWay.activityList.add(this);
		back = (Button) findViewById(Res.getWidgetID("showallphoto_back"));
		cancel = (Button) findViewById(Res.getWidgetID("showallphoto_cancel"));
		preview = (Button) findViewById(Res.getWidgetID("showallphoto_preview"));
		okButton = (Button) findViewById(Res.getWidgetID("showallphoto_ok_button"));
		headTitle = (TextView) findViewById(Res.getWidgetID("showallphoto_headtitle"));
		this.intent = getIntent();
		String folderName = intent.getStringExtra("folderName");
		if (folderName.length() > 8) {
			folderName = folderName.substring(0, 9) + "...";
		}
		headTitle.setText(folderName);
		cancel.setOnClickListener(new CancelListener());
		back.setOnClickListener(new BackListener(intent));
		preview.setOnClickListener(new PreviewListener());
		init();
		initListener();
		isShowOkBt();
	}

//	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // TODO Auto-generated method stub
//        	gridImageAdapter.notifyDataSetChanged();
//        }
//    };

	private class PreviewListener implements OnClickListener {
		public void onClick(View v) {
			if (Bimp.selectBitmap.size() > 0) {
				intent.putExtra("position", "2");
				intent.setClass(ShowAllPhoto.this, GalleryActivity.class);
				startActivity(intent);
			}
		}

	}

	private class BackListener implements OnClickListener {// 返回按钮监听
		Intent intent;

		public BackListener(Intent intent) {
			this.intent = intent;
		}

		public void onClick(View v) {
			intent.setClass(ShowAllPhoto.this, ImageFile.class);
			startActivity(intent);
		}

	}

	private class CancelListener implements OnClickListener {// 取消按钮的监听
		public void onClick(View v) {
			//清空选择的图片
			Bimp.selectBitmap.clear();
			FolderAdapter.contentList.clear();
			for(Activity activity: PublicWay.activityList){
				if (null != activity) {
					activity.finish();
				}
			}
		}
	}

	private void init() {
//		IntentFilter filter = new IntentFilter("data.broadcast.action");
//		registerReceiver(broadcastReceiver, filter);
		progressBar = (ProgressBar) findViewById(Res.getWidgetID("showallphoto_progressbar"));
		progressBar.setVisibility(View.GONE);
		gridView = (GridView) findViewById(Res.getWidgetID("showallphoto_myGrid"));
		gridImageAdapter = new AlbumGridViewAdapter(this,dataList,
				Bimp.selectBitmap);
		gridView.setAdapter(gridImageAdapter);
		okButton = (Button) findViewById(Res.getWidgetID("showallphoto_ok_button"));
	}

	private void initListener() {

		gridImageAdapter
				.setOnItemClickListener(new AlbumGridViewAdapter.OnItemClickListener() {
					public void onItemClick(final ToggleButton toggleButton,
							int position, boolean isChecked,
							Button button) {
						if (Bimp.selectBitmap.size() >= CameraService.num&&isChecked) {
							button.setVisibility(View.GONE);
							toggleButton.setChecked(false);
							Toast.makeText(ShowAllPhoto.this, Res.getString("only_choose_num"), 200)
									.show();
							return;
						}

						if (isChecked) {
							button.setVisibility(View.VISIBLE);
							Bimp.selectBitmap.add(dataList.get(position));
							okButton.setText(Res.getString("finish")+"(" + Bimp.selectBitmap.size()
									+ "/"+ CameraService.num+")");
						} else {
							button.setVisibility(View.GONE);
							Bimp.selectBitmap.remove(dataList.get(position));
							okButton.setText(Res.getString("finish")+"(" + Bimp.selectBitmap.size() + "/"+ CameraService.num+")");
						}
						isShowOkBt();
					}
				});

		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				okButton.setClickable(false);
				if (PublicWay.photoService != null) {
					PublicWay.selectedDataList.addAll(Bimp.selectBitmap);
					Bimp.selectBitmap.clear();
					FolderAdapter.contentList.clear();
					for(Activity activity: PublicWay.activityList){
						if (null != activity) {
							activity.finish();
						}
					}
					finish();
					PublicWay.photoService.onActivityResult(0, -2,
							intent);
				}

				// Intent intent = new Intent();
				// Bundle bundle = new Bundle();
				// bundle.putStringArrayList("selectedDataList",
				// selectedDataList);
				// intent.putExtras(bundle);
				// intent.setClass(ShowAllPhoto.this, UploadPhoto.class);
				// startActivity(intent);

			}
		});

	}

	public void isShowOkBt() {
		if (Bimp.selectBitmap.size() > 0) {
			okButton.setText(Res.getString("finish")+"(" + Bimp.selectBitmap.size() + "/"+ CameraService.num+")");
			preview.setPressed(true);
			okButton.setPressed(true);
			preview.setClickable(true);
			okButton.setClickable(true);
			okButton.setTextColor(Color.WHITE);
			preview.setTextColor(Color.WHITE);
		} else {
			preview.setPressed(false);
			preview.setClickable(false);
			okButton.setPressed(false);
			okButton.setClickable(false);
			okButton.setTextColor(Color.parseColor("#E1E0DE"));
			preview.setTextColor(Color.parseColor("#E1E0DE"));
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			intent.setClass(ShowAllPhoto.this, ImageFile.class);
			startActivity(intent);
		}

		return false;

	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		isShowOkBt();
		gridImageAdapter.notifyDataSetChanged();
		super.onRestart();
	}

}

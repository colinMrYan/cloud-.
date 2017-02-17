package com.inspur.imp.plugin.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.inspur.imp.api.ImpBaseActivity;
import com.inspur.imp.api.Res;

import java.util.ArrayList;
import java.util.List;

/**
 * 这个是进入相册显示所有图片的界面
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class AlbumActivity extends ImpBaseActivity {
	private GridView gridView;
	private TextView tv;
	private ProgressBar progressBar;
	private AlbumGridViewAdapter gridImageAdapter;
	private Button okButton;
	// 返回按钮
	private Button back;
	// 取消按钮
	private Button cancel;
	private Intent intent;
	// 预览按钮
	private Button preview;
	
	private ArrayList<ImageItem> dataList;
	private AlbumHelper helper;
	public  List<ImageBucket> contentList;
	public static Bitmap bitmap;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(Res.getLayoutID("plugin_camera_album"));
//		IntentFilter filter = new IntentFilter("data.broadcast.action");  
//		registerReceiver(broadcastReceiver, filter);  
        bitmap = BitmapFactory.decodeResource(getResources(),Res.getDrawableID("plugin_camera_no_pictures"));
        init();
		PublicWay.activityList.add(this);
		initListener();
		isShowOkBt();
		
	}
	
//	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {  
//		  
//        @Override  
//        public void onReceive(Context context, Intent intent) {  
//        	//mContext.unregisterReceiver(this);
//            // TODO Auto-generated method stub  
//        	gridImageAdapter.notifyDataSetChanged();
//        }  
//    };  

	// 预览按钮的监听
	private class PreviewListener implements OnClickListener {
		public void onClick(View v) {
			if (Bimp.selectBitmap.size() > 0) {
				intent.putExtra("position", "1");
				intent.setClass(AlbumActivity.this, GalleryActivity.class);
				startActivity(intent);
			}
		}

	}

	// 完成按钮的监听
	private class AlbumSendListener implements OnClickListener {
		public void onClick(View v) {
			if(PublicWay.photoService!=null){
				PublicWay.selectedDataList.addAll(Bimp.selectBitmap);
				Bimp.selectBitmap.clear();
				FolderAdapter.contentList.clear();
				PublicWay.photoService.onActivityResult(0, -2, intent);
				finish();
				okButton.setClickable(false);
			}
		}

	}

	// 返回按钮监听
	private class BackListener implements OnClickListener {
		public void onClick(View v) {
			intent.setClass(AlbumActivity.this, ImageFile.class);
			startActivity(intent);
		}

	}

	// 取消按钮的监听
	private class CancelListener implements OnClickListener {
		public void onClick(View v) {
			Bimp.selectBitmap.clear();
			FolderAdapter.contentList.clear();
			for(Activity activity:PublicWay.activityList){
				if (null != activity) {
					activity.finish();
				}
			}
		}
	}



	// 初始化，给一些对象赋值
	private void init() {
		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());

		contentList = helper.getImagesBucketList(false);
		FolderAdapter.contentList.addAll(contentList);
		dataList = new ArrayList<ImageItem>();
		for(int i = 0; i<contentList.size(); i++){
			dataList.addAll( contentList.get(i).imageList );
		}

		back = (Button) findViewById(Res.getWidgetID("back"));
		cancel = (Button) findViewById(Res.getWidgetID("cancel"));
		cancel.setOnClickListener(new CancelListener());
		back.setOnClickListener(new BackListener());
		preview = (Button) findViewById(Res.getWidgetID("preview"));
		preview.setOnClickListener(new PreviewListener());
		intent = getIntent();
		Bundle bundle = intent.getExtras();
		progressBar = (ProgressBar) findViewById(Res.getWidgetID("progressbar"));
		progressBar.setVisibility(View.GONE);
		gridView = (GridView) findViewById(Res.getWidgetID("myGrid"));
		gridImageAdapter = new AlbumGridViewAdapter(this,dataList,
				Bimp.selectBitmap);
		gridView.setAdapter(gridImageAdapter);
		tv = (TextView) findViewById(Res.getWidgetID("myText"));
		gridView.setEmptyView(tv);
		okButton = (Button) findViewById(Res.getWidgetID("ok_button"));
	}

	private void initListener() {

		gridImageAdapter
				.setOnItemClickListener(new AlbumGridViewAdapter.OnItemClickListener() {

					@Override
					public void onItemClick(final ToggleButton toggleButton,
							int position, boolean isChecked,Button chooseBt) {
						if (Bimp.selectBitmap.size() >= CameraService.num) {
							toggleButton.setChecked(false);
							chooseBt.setVisibility(View.GONE);
							if (!removeOneData(dataList.get(position))) {
								Toast.makeText(AlbumActivity.this, Res.getString("only_choose_num"), Toast.LENGTH_SHORT).show();
							}
							return;
						}
						if (isChecked) {
							chooseBt.setVisibility(View.VISIBLE);
							Bimp.selectBitmap.add(dataList.get(position));
							okButton.setText(Res.getString("finish")+"(" + Bimp.selectBitmap.size()
									+ "/"+CameraService.num+")");
						} else {
							Bimp.selectBitmap.remove(dataList.get(position));
							chooseBt.setVisibility(View.GONE);
							okButton.setText(Res.getString("finish")+"(" + Bimp.selectBitmap.size() + "/"+CameraService.num+")");
						}
						isShowOkBt();
					}
				});

		okButton.setOnClickListener(new AlbumSendListener());

	}

	private boolean removeOneData(ImageItem imageItem) {
			if (Bimp.selectBitmap.contains(imageItem)) {
				Bimp.selectBitmap.remove(imageItem);
				okButton.setText(Res.getString("finish")+"(" + Bimp.selectBitmap.size() + "/"+CameraService.num+")");
				return true;
			}
		return false;
	}

	public void isShowOkBt() {
		if (Bimp.selectBitmap.size() > 0) {
			okButton.setText(Res.getString("finish")+"(" + Bimp.selectBitmap.size() + "/"+CameraService.num+")");
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
			intent.setClass(AlbumActivity.this, ImageFile.class);
			startActivity(intent);
		}
		return true;

	}
@Override
protected void onRestart() {
	// TODO Auto-generated method stub
	isShowOkBt();
	gridImageAdapter.notifyDataSetChanged();
	super.onRestart();
}
}

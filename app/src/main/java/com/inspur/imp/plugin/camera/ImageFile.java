package com.inspur.imp.plugin.camera;


import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.inspur.imp.api.Res;


/**
 * 这个类主要是用来进行显示包含图片的文件夹
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class ImageFile extends Activity {

	private FolderAdapter folderAdapter;
	private Button bt_cancel;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(Res.getLayoutID("plugin_camera_image_file"));
		PublicWay.activityList.add(this);
		bt_cancel = (Button) findViewById(Res.getWidgetID("cancel"));
		bt_cancel.setOnClickListener(new CancelListener());
		GridView gridView = (GridView) findViewById(Res.getWidgetID("fileGridView"));
		TextView textView = (TextView) findViewById(Res.getWidgetID("headerTitle"));
		textView.setText(Res.getString("photo"));
		folderAdapter = new FolderAdapter(this);
		gridView.setAdapter(folderAdapter);
	}

	private class CancelListener implements OnClickListener {// 取消按钮的监听
		public void onClick(View v) {
			//清空选择的图片
			Bimp.selectBitmap.clear();
			FolderAdapter.contentList.clear();
			for(Activity activity:PublicWay.activityList){
				if (null != activity) {
					activity.finish();
				}
			}
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Bimp.selectBitmap.clear();
			FolderAdapter.contentList.clear();
			for(Activity activity:PublicWay.activityList){
				if (null != activity) {
					activity.finish();
				}
			}
		}
		
		return false;
	}

}

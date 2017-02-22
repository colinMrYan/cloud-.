package com.im4j.picturebeautify.editimage.fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.im4j.picturebeautify.editimage.EditImageActivity;
import com.im4j.picturebeautify.editimage.utils.BitmapUtils;
import com.im4j.picturebeautify.editimage.view.RotateImageView;
import com.im4j.picturebeautify.editimage.view.imagezoom.ImageViewTouchBase;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.LogUtils;


/**
 * 图片旋转Fragment
 * 
 * @author 潘易
 * 
 */
public class RotateFragment extends Fragment {
	public static final String TAG = RotateFragment.class.getName();
	private View mainView;
	private EditImageActivity activity;
	private View backToMenu;// 返回主菜单
	public SeekBar mSeekBar;// 角度设定
	private RotateImageView mRotatePanel;// 旋转效果展示控件

	public static RotateFragment newInstance(EditImageActivity activity) {
		RotateFragment fragment = new RotateFragment();
		fragment.activity = activity;
		fragment.mRotatePanel = activity.mRotatePanel;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainView = inflater.inflate(R.layout.plugin_fragment_edit_image_rotate, null);
		backToMenu = mainView.findViewById(R.id.back_to_main);
		mSeekBar = (SeekBar) mainView.findViewById(R.id.rotate_bar);
		mSeekBar.setProgress(0);
		return mainView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		backToMenu.setOnClickListener(new BackToMenuClick());// 返回主菜单

		mSeekBar.setOnSeekBarChangeListener(new RotateAngleChange());
	}

	/**
	 * 角度改变监听
	 * 
	 * @author panyi
	 * 
	 */
	private final class RotateAngleChange implements OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int angle,
				boolean fromUser) {
			LogUtils.JasonDebug("angle="+angle);
			// System.out.println("progress--->" + progress);
			mRotatePanel.rotateImage(angle*90);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}
	}// end inner class

	/**
	 * 返回按钮逻辑
	 * 
	 * @author panyi
	 * 
	 */
	private final class BackToMenuClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			backToMain();
		}
	}// end class

	/**
	 * 返回主菜单
	 */
	public void backToMain() {
		activity.mode = EditImageActivity.MODE_NONE;
		activity.bottomGallery.setCurrentItem(0);
		activity.mainImage.setVisibility(View.VISIBLE);
		this.mRotatePanel.setVisibility(View.GONE);
		activity.bannerFlipper.showPrevious();
	}

	/**
	 * 保存旋转图片
	 */
	public void saveRotateImage() {
		// System.out.println("保存旋转图片");
		if (mSeekBar.getProgress() == 0 || mSeekBar.getProgress() == 3) {// 没有做旋转
			backToMain();
			return;
		} else {// 保存图片
			SaveRotateImageTask task = new SaveRotateImageTask();
			task.execute(activity.mainBitmap);
		}// end if
	}

	/**
	 * 保存图片线程
	 * 
	 * @author panyi
	 * 
	 */
	private final class SaveRotateImageTask extends
			AsyncTask<Bitmap, Void, Bitmap> {
		private Dialog dialog;

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dialog.dismiss();
		}

		@Override
		protected void onCancelled(Bitmap result) {
			super.onCancelled(result);
			dialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = EditImageActivity.getLoadingDialog(getActivity(), "图片保存中...",
					false);
			dialog.show();
		}

		@Override
		protected Bitmap doInBackground(Bitmap... params) {
//			RectF imageRect = mRotatePanel.getImageNewRect();
			Bitmap originBit = params[0];
//			Bitmap result = Bitmap.createBitmap((int) imageRect.width(),
//					(int) imageRect.height(), Bitmap.Config.ARGB_4444);
//			LogUtils.JasonDebug("imageRect.width()="+imageRect.width());
//			LogUtils.JasonDebug("imageRect.height()="+imageRect.height());
//			LogUtils.JasonDebug("mRotatePanel.getScale()="+mRotatePanel.getScale());
//			Canvas canvas = new Canvas(result);
//			int w = originBit.getWidth() >> 1;
//			int h = originBit.getHeight() >> 1;
//			LogUtils.JasonDebug("w="+w);
//			LogUtils.JasonDebug("h="+h);
//			float centerX = imageRect.width() / 2;
//			float centerY = imageRect.height() / 2;
//
//			float left = centerX - w;
//			float top = centerY - h;
//			LogUtils.JasonDebug("left"+left);
//			LogUtils.JasonDebug("top"+top);
//			RectF dst = new RectF(left, top, left + originBit.getWidth(), top
//					+ originBit.getHeight());
//			canvas.save();
//			canvas.scale(mRotatePanel.getScale(), mRotatePanel.getScale(),
//					imageRect.width() / 2, imageRect.height() / 2);
//			canvas.rotate(mRotatePanel.getRotateAngle(), imageRect.width() / 2,
//					imageRect.height() / 2);
//
//			canvas.drawBitmap(originBit, new Rect(0, 0, originBit.getWidth(),
//					originBit.getHeight()), dst, null);
//			canvas.restore();
			Matrix matrix = new Matrix();
		       matrix.postRotate(mRotatePanel.getRotateAngle());
			Bitmap result = Bitmap.createBitmap(originBit, 0, 0, originBit.getWidth(), originBit.getHeight(), matrix, true);  
			BitmapUtils.saveBitmap(result, activity.saveFilePath);// 保存图片
			activity.currentFilePath = activity.saveFilePath;
			return result;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			dialog.dismiss();
			if (result == null)
				return;

			// 切换新底图
			if (activity.mainBitmap != null
					&& !activity.mainBitmap.isRecycled()) {
				activity.mainBitmap.recycle();
			}
			activity.mainBitmap = result;
			activity.mainImage.setImageBitmap(activity.mainBitmap);
			activity.mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
			backToMain();
		}
	}// end inner class
}// end class

package com.inspur.emmcloud.widget;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * 单张图片显示Fragment
 */
public class ImageDetailFragment extends Fragment {
	private String mImageUrl;
	private ImageView mImageView;
	private ProgressBar progressBar;
	private PhotoViewAttacher mAttacher;
	private Button saveImageBtn;

	public static ImageDetailFragment newInstance(String imageUrl) {
		final ImageDetailFragment f = new ImageDetailFragment();

		final Bundle args = new Bundle();
		args.putString("url", imageUrl);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageUrl = getArguments() != null ? getArguments().getString("url")
				: null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.image_pager_detail_fragment,
				container, false);
		mImageView = (ImageView) v.findViewById(R.id.image);
		mAttacher = new PhotoViewAttacher(mImageView);

		saveImageBtn = (Button) v.findViewById(R.id.save_image_btn);
		saveImageBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mImageView.buildDrawingCache(true);
				mImageView.buildDrawingCache();
				Bitmap bitmap = mImageView.getDrawingCache();
				saveBitmapFile(bitmap);
				mImageView.setDrawingCacheEnabled(false);

				LogUtils.debug("yfcLog", "imageUrl:" + mImageUrl);
			}
		});
		mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {

			@Override
			public void onPhotoTap(View arg0, float arg1, float arg2) {
				getActivity().finish();
			}

			@Override
			public void onOutsidePhotoTap() {

			}
		});
		progressBar = (ProgressBar) v.findViewById(R.id.loading);
		return v;
	}

	/**
	 * 保存图片
	 * 
	 * @param bitmap
	 */
	public void saveBitmapFile(Bitmap bitmap) {
		File temp = new File("/sdcard/IMP-Cloud/cache/chat/");// 要保存文件先创建文件夹
		if (!temp.exists()) {
			temp.mkdir();
		}
		// 重复保存时，覆盖原同名图片
		// 将要保存图片的路径和图片名称
		File file = new File("/sdcard/IMP-Cloud/cache/chat/"
				+ FileUtils.getFileName(mImageUrl));
		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			ToastUtils.show(getActivity(), getString(R.string.save_success));
		} catch (IOException e) {
			ToastUtils.show(getActivity(), getString(R.string.save_fail));
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(R.drawable.icon_photo_default)
		.showImageOnFail(R.drawable.icon_photo_default)
		.showImageOnLoading(R.drawable.icon_photo_default)
		// 设置图片的解码类型  
        .bitmapConfig(Bitmap.Config.RGB_565)  
        .cacheInMemory(true)
        .cacheOnDisk(true)
		.build();
		ImageLoader.getInstance().displayImage(mImageUrl, mImageView,options,
				new SimpleImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
						progressBar.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						String message = null;
						switch (failReason.getType()) {
						case IO_ERROR:
							message = getString(R.string.download_fail);
							break;
						case DECODING_ERROR:
							message = getString(R.string.picture_cannot_show);
							break;
						case NETWORK_DENIED:
							message = getString(R.string.cannot_download_for_network_exception);
							break;
						case OUT_OF_MEMORY:
							message = getString(R.string.cannot_show_for_too_big);
							break;
						case UNKNOWN:
							message = getString(R.string.unknown_error);
							break;
						default:
							message = getString(R.string.download_fail);
							break;
						}
						Toast.makeText(getActivity(), message,
								Toast.LENGTH_SHORT).show();
						progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						progressBar.setVisibility(View.GONE);
						mAttacher.update();
					}
				});
	}

	// String
	// suffixes="avi|mpeg|3gp|mp3|mp4|wav|jpeg|gif|jpg|png|apk|exe|txt|html|zip|java|doc";
	// Pattern pat=Pattern.compile("[\\w]+[\\.]("+suffixes+")");//正则判断
	// Matcher mc=pat.matcher(url);//条件匹配
	// while(mc.find()){
	// String substring = mc.group();//截取文件名后缀名
	// }
}

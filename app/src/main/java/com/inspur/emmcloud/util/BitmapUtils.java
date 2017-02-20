/**
 * 
 * BitmapUtils.java
 * classes : com.inspur.emmcloud.util.BitmapUtils
 * V 1.0.0
 * Create at 2016年9月22日 下午4:24:01
 */
package com.inspur.emmcloud.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.util.Base64;
import android.util.TypedValue;

/**
 * com.inspur.emmcloud.util.BitmapUtils
 * create at 2016年9月22日 下午4:24:01
 */
public class BitmapUtils {
	
	/** 图片之间的距离 */
	private static int PADDING = 2;

	private static int rangetWidth;

	/**
	 * bitmap转为base64
	 * @param bitmap
	 * @return
	 */
	public static String bitmapToBase64(Bitmap bitmap) {

		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

				baos.flush();
				baos.close();

				byte[] bitmapBytes = baos.toByteArray();
				result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * base64转为bitmap
	 * @param base64Data
	 * @return
	 */
	public static Bitmap base64ToBitmap(String base64Data) {
		byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}
	
	
	/**
	 * 将Bitmap转化为二进制数组
	 * 
	 * @param bitmap
	 * @return
	 */
	public static byte[] getBitmapByte(Bitmap bitmap) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			// LogConfig.exceptionDebug(TAG, e.toString());
			e.printStackTrace();
		}
		return out.toByteArray();
	}
	
	
	public static Bitmap createGroupFace(Context context,
			List<Bitmap> bitmapList) {
		if (bitmapList == null || bitmapList.size() == 0) {
			return null;
		}
		rangetWidth = dip2px(context, 100);
		if (bitmapList.size() == 1) {
			return createOneBit(bitmapList, context);
		} else if (bitmapList.size() == 2) {
			return createTwoBit(bitmapList, context);
		} else if (bitmapList.size() == 3) {

			return createThreeBit(bitmapList, context);
		} else {
			return createFourBit(bitmapList, context);
		}
	}
	
	/**
	 * 当出现群组中只有一个人的情况
	 * @param paramList
	 * @param context
	 * @return
	 */
	private static Bitmap createOneBit(List<Bitmap> paramList,
			final Context context) {
		Bitmap bit1 = paramList.get(0);
		bit1 = zoomImage(bit1, rangetWidth, rangetWidth);
		return bit1;

	}

	/**
	 * 拼接两个成员的头像
	 * 
	 * @param paramList
	 * @param context
	 * @return
	 */
	private static Bitmap createTwoBit(List<Bitmap> paramList,
			final Context context) {

		// 创建一个空格的bitmap
		Bitmap canvasBitmap = Bitmap.createBitmap(rangetWidth, rangetWidth,
				Config.ARGB_8888);
		Canvas localCanvas = new Canvas(canvasBitmap);
		localCanvas.drawColor(Color.WHITE);
		// 按照最终压缩比例压缩
		Bitmap bit1 = cutBitmap(paramList.get(0));
		bit1 = zoomImage(bit1, rangetWidth / 2, rangetWidth);
		localCanvas.drawBitmap(bit1, 0, 0, null);
		bit1.recycle();

		Bitmap bit2 = cutBitmap(paramList.get(1));
		bit2 = zoomImage(bit2, rangetWidth / 2, rangetWidth);
		localCanvas.drawBitmap(bit2, rangetWidth / 2 + PADDING, 0, null);
		bit1.recycle();
		// 重置padding
		localCanvas.save(Canvas.ALL_SAVE_FLAG);
		localCanvas.restore();
		return canvasBitmap;
	}

	/**
	 * 拼接三个成员的头像
	 * 
	 * @param paramList
	 * @param context
	 * @return
	 */
	private static Bitmap createThreeBit(List<Bitmap> paramList,
			final Context context) {

		// 创建一个空格的bitmap
		Bitmap canvasBitmap = Bitmap.createBitmap(rangetWidth, rangetWidth,
				Config.ARGB_8888);
		Canvas localCanvas = new Canvas(canvasBitmap);
		localCanvas.drawColor(Color.WHITE);
		// 按照最终压缩比例压缩
		Bitmap bit1 = cutBitmap(paramList.get(0));
		bit1 = zoomImage(bit1, rangetWidth / 2, rangetWidth);
		localCanvas.drawBitmap(bit1, 0, 0, null);
		bit1.recycle();

		Bitmap bit2 = zoomImage(paramList.get(1), rangetWidth / 2,
				rangetWidth / 2);
		localCanvas.drawBitmap(bit2, rangetWidth / 2 + PADDING, 0, null);
		bit2.recycle();

		Bitmap bit3 = zoomImage(paramList.get(2), rangetWidth / 2,
				rangetWidth / 2);
		localCanvas.drawBitmap(bit3, rangetWidth / 2 + PADDING, rangetWidth / 2
				+ PADDING, null);
		bit3.recycle();

		localCanvas.save(Canvas.ALL_SAVE_FLAG);
		localCanvas.restore();
		return canvasBitmap;
	}

	/**
	 * 拼接四个成员的头像
	 * 
	 * @param paramList
	 * @param context
	 * @return
	 */
	private static Bitmap createFourBit(List<Bitmap> paramList,
			final Context context) {

		// 创建一个空格的bitmap
		Bitmap canvasBitmap = Bitmap.createBitmap(rangetWidth, rangetWidth,
				Config.RGB_565);
		Canvas localCanvas = new Canvas(canvasBitmap);
		localCanvas.drawColor(Color.WHITE);
		// 按照最终压缩比例压缩
		Bitmap bit1 = zoomImage(paramList.get(0), rangetWidth / 2,
				rangetWidth / 2);
		localCanvas.drawBitmap(bit1, 0, 0, null);
		bit1.recycle();

		Bitmap bit2 = zoomImage(paramList.get(1), rangetWidth / 2,
				rangetWidth / 2);
		localCanvas.drawBitmap(bit2, rangetWidth / 2 + PADDING, 0, null);
		bit2.recycle();

		Bitmap bit3 = zoomImage(paramList.get(2), rangetWidth / 2,
				rangetWidth / 2);
		localCanvas.drawBitmap(bit3, 0, rangetWidth / 2 + PADDING, null);
		bit3.recycle();

		Bitmap bit4 = zoomImage(paramList.get(3), rangetWidth / 2,
				rangetWidth / 2);
		localCanvas.drawBitmap(bit4, rangetWidth / 2 + PADDING, rangetWidth / 2
				+ PADDING, null);
		bit4.recycle();

		localCanvas.save(Canvas.ALL_SAVE_FLAG);
		localCanvas.restore();
		return canvasBitmap;
	}

	private static Bitmap cutBitmap(Bitmap bitmap) {
		// TODO Auto-generated method stub
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		bitmap = Bitmap.createBitmap(bitmap, (int) (width / 4), 0,
				(int) (width / 2), height);
		return bitmap;
	}

	private static int dip2px(Context context, float value) {
		return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				value, context.getResources().getDisplayMetrics()) + 0.5f);
	}

	/***
	 * 图片的缩放方法
	 * 
	 * @param bgimage
	 *            ：源图片资源
	 * @param newWidth
	 *            ：缩放后宽度
	 * @param newHeight
	 *            ：缩放后高度
	 * @return
	 */
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
			double newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
				(int) height, matrix, true);
		return bitmap;
	}
	
	/**
	 * 固定取样率
	 * @param image
	 * @return
	 */
	public static Bitmap btimapCompressBy1(Bitmap image) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		image.compress(Bitmap.CompressFormat.PNG, 20, out);
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		int be = 2;
		newOpts.inSampleSize = be;
		ByteArrayInputStream isBm = new ByteArrayInputStream(out.toByteArray());
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
		return bitmap;

	}
	
	/**
	 * 设置计算出来的取样率来实现压缩
	 * @param srcPath
	 * @return
	 */
	public static Bitmap compressImageFromFile(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;//只读边,不读内容
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		float hh = 800f;//
		float ww = 480f;//
		int be = 1;
		if (w > h && w > ww) {
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置采样率
		
		newOpts.inPreferredConfig = Config.ARGB_8888;//该模式是默认的,可不设
		newOpts.inPurgeable = true;// 同时设置才会有效
		newOpts.inInputShareable = true;//。当系统内存不够时候图片自动被回收
		
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
//		return compressBmpFromBmp(bitmap);//原来的方法调用了这个方法企图进行二次压缩
									//其实是无效的,大家尽管尝试
		return bitmap;
	}

}

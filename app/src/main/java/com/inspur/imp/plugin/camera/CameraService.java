package com.inspur.imp.plugin.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.imp.api.Res;
import com.inspur.imp.api.iLog;
import com.inspur.imp.plugin.ImpPlugin;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;

/**
 * 进入相册选择图片进行上传
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class CameraService extends ImpPlugin {
	private Bitmap photo;
	private String saveDir = Environment.getExternalStorageDirectory()
			.getPath() + "/DCIM";
	private static final String LOG_TAG = "PhotoService";
	private static final int PHOTOLIBRARY = 0; // Choose image from picture
												// library (same as
												// SAVEDPHOTOALBUM for Android)
	private static final int CAMERA = 1; // Take picture from camera
	private static final int SAVEDPHOTOALBUM = 2; // Choose image from picture
													// library (same as
													// PHOTOLIBRARY for Android)
	private static final int DATA_URL = 0; // Return base64 encoded string
	private static final int FILE_URI = 1; // Return file uri
											// (content://media/external/images/media/2
											// for Android)
	private static final int NATIVE_URI = 2; // On Android, this is the same as
												// FILE_URI

	private static final int JPEG = 0; // Take a picture of type JPEG
	private static final int PNG = 1; // Take a picture of type PNG
	public static final int REQUEST_GELLEY_IMG_SELECT = 100;

	private static int destType;
	private static int mQuality; // Compression quality hint (0-100: 0=low
									// quality &
	// high compression, 100=compress of max quality)
	private static int targetWidth; // desired width of the image
	private static int targetHeight; // desired height of the image
	private static Uri imageUri; // Uri of captured image
	private static int encodingType; // Type of encoding to use
	private static boolean saveToPhotoAlbum = false; // Should the picture be
														// saved to the
	// device's photo album

	private int numPics;

	private Uri scanMe; // Uri of image to be added to content store

	private ArrayList<String> dataList = new ArrayList<String>();

	private String successCb, failCb;

	public static int num = 8;// 可以选择的图片数目

	@Override
	public void execute(String action, JSONObject paramsObject) {
		if ("open".equals(action)) {
			open(paramsObject);
		}
		if ("getPicture".equals(action)) {
			getPicture(paramsObject);
		}
	}

	/**
	 * 选择相机拍照进行上传
	 * 
	 */
	private void open(JSONObject jsonObject) {
		try {
			if (!jsonObject.isNull("options")) {
				this.destType = jsonObject.getJSONObject("options").getInt(
						"destinationType");
				this.mQuality = jsonObject.getJSONObject("options").getInt(
						"quality");
				this.targetWidth = jsonObject.getJSONObject("options").getInt(
						"targetWidth");
				this.targetHeight = jsonObject.getJSONObject("options").getInt(
						"targetHeight");
				this.encodingType = jsonObject.getJSONObject("options").getInt(
						"encodingType");
			}
			if (!jsonObject.isNull("success"))
				successCb = jsonObject.getString("success");
			if (!jsonObject.isNull("fail"))
				failCb = jsonObject.getString("fail");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (this.targetWidth < 1) {
			this.targetWidth = -1;
		}
		if (this.targetHeight < 1) {
			this.targetHeight = -1;
		}
		destoryImage();
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			PublicWay.file = createCaptureFile(encodingType);
			PublicWay.photoService = this;
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			this.imageUri = Uri.fromFile(PublicWay.file);
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(PublicWay.file));
			((Activity)context).startActivityForResult(intent, CAMERA);

		} else {
			Toast.makeText(this.context, Res.getString("invalidSD"),
					Toast.LENGTH_SHORT).show();
		}
		File savePath = new File(saveDir);
		if (!savePath.exists()) {
			savePath.mkdirs();
		}
	}

	/**
	 * 进入相册文件夹
	 * 
	 */
	private void getPicture(JSONObject jsonObject) {
		try {
			if (!jsonObject.isNull("options")) {
				this.destType = jsonObject.getJSONObject("options").getInt(
						"destinationType");
				this.mQuality = jsonObject.getJSONObject("options").getInt(
						"quality");
				this.targetWidth = jsonObject.getJSONObject("options").getInt(
						"targetWidth");
				this.targetHeight = jsonObject.getJSONObject("options").getInt(
						"targetHeight");
				this.encodingType = jsonObject.getJSONObject("options").getInt(
						"encodingType");
				if (!jsonObject.getJSONObject("options").isNull("num")) {
					this.num = jsonObject.getJSONObject("options")
							.getInt("num");
				}
				if (this.num < 0 || this.num > 15) {
					this.num = 8;
				}
			}
			if (!jsonObject.isNull("success"))
				successCb = jsonObject.getString("success");
			if (!jsonObject.isNull("fail"))
				failCb = jsonObject.getString("fail");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (this.targetWidth < 1) {
			this.targetWidth = -1;
		}
		if (this.targetHeight < 1) {
			this.targetHeight = -1;
		}
//		Intent intent = new Intent();
//		Bundle bundle = new Bundle();
		PublicWay.photoService = this;
//		bundle.putStringArrayList("dataList", dataList);
//		intent.putExtras(bundle);
//		intent.setClass(this.context, AlbumActivity.class);
//		this.context.startActivity(intent);
		initImagePicker();
		Intent intent = new Intent(this.context,
				ImageGridActivity.class);
		((Activity)this.context).startActivityForResult(intent, REQUEST_GELLEY_IMG_SELECT);
	}
	
	/**
	 * 初始化图片选择控件
	 */
	private void initImagePicker() {
		ImagePicker imagePicker = ImagePicker.getInstance();
		imagePicker.setImageLoader(new ImageDisplayUtils()); // 设置图片加载器
		imagePicker.setShowCamera(false); // 显示拍照按钮
		imagePicker.setCrop(false); // 允许裁剪（单选才有效）
		imagePicker.setSaveRectangle(true); // 是否按矩形区域保存
		imagePicker.setSelectLimit(num); // 选中数量限制
		imagePicker.setStyle(CropImageView.Style.RECTANGLE); // 裁剪框的形状
		imagePicker.setFocusWidth(1000); // 裁剪框的宽度。单位像素（圆形自动取宽高最小值）
		imagePicker.setFocusHeight(1000); // 裁剪框的高度。单位像素（圆形自动取宽高最小值）
		imagePicker.setOutPutX(1000); // 保存文件的宽度。单位像素
		imagePicker.setOutPutY(1000); // 保存文件的高度。单位像素
	}


	private String getTempDirectoryPath() {
		File cache = null;

		// SD Card Mounted
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			cache = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/DCIM");
		}
		// Use internal storage
		else {
			cache = this.context.getCacheDir();
		}

		// Create the cache directory if it doesn't exist
		if (!cache.isDirectory()) {
			cache.mkdirs();
		}
		return cache.getAbsolutePath();
	}

	/**
	 * 根据相应的encodingType创建相应的格式文件
	 * 
	 * @param encodingType
	 *            图片格式
	 * @return 返回对应的图片
	 */
	private File createCaptureFile(int encodingType) {
		File photo = null;
		if (encodingType == JPEG) {
			photo = new File(getTempDirectoryPath(), System.currentTimeMillis()
					+ ".Pic.jpg");
		} else if (encodingType == PNG) {
			photo = new File(getTempDirectoryPath(), System.currentTimeMillis()
					+ ".Pic.png");
		} else {
			throw new IllegalArgumentException("Invalid Encoding Type: "
					+ encodingType);
		}
		return photo;
	}

	/**
	 * IMP代码修改处
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		PublicWay.photoService = null;
		// 照相取得图片
		if (requestCode == CAMERA) {
			LogUtils.debug("jason", "onActivityResult----");
			if (resultCode == -2) {
				if (PublicWay.file != null && PublicWay.file.exists()) {
					try {
						// Create an ExifHelper to save the exif data that is
						// lost during compression
						ExifHelper exif = new ExifHelper();
						try {
							if (this.encodingType == JPEG) {
								exif.createInFile(getTempDirectoryPath()
										+ "/.Pic.jpg");
								exif.readExifData();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}

						Bitmap bitmap = null;
						Uri uri = null;
						// If sending filename back
						if (this.saveToPhotoAlbum) {
							Uri inputUri = getUriFromMediaStore();
							// Just because we have a media URI doesn't mean
							// we have a real file, we need to make it
							uri = Uri.fromFile(new File(FileHelper.getRealPath(
									inputUri, this)));

						} else {
							uri = Uri.fromFile(new File(getTempDirectoryPath(),
									System.currentTimeMillis() + ".jpg"));
						}
						Uri saveUri = Uri.fromFile(new File(FileHelper
								.getRealPath(getUriFromMediaStore(), this)));
						if (uri == null) {
							this.failPicture(Res.getString("no_storage"));
						}
						
						rotateImg();

						// If all this is true we shouldn't compress the
						// image.
						if (this.targetHeight == -1 && this.targetWidth == -1
								&& this.mQuality == 100) {
							writeUncompressedImage(uri);
						} else {
							bitmap = getScaledBitmap(FileHelper
									.stripFileProtocol(this.imageUri.toString()));

							// Add compressed version of captured image to
							// returned media store Uri
							OutputStream os = this.context.getContentResolver()
									.openOutputStream(uri);
							bitmap.compress(Bitmap.CompressFormat.JPEG,
									this.mQuality, os);
							os.close();
							writeUncompressedImage(saveUri);
							// Restore exif data to file
							if (this.encodingType == JPEG) {
								String exifPath;
								if (this.saveToPhotoAlbum) {
									exifPath = FileHelper.getRealPath(saveUri,
											this);
								} else {
									exifPath = saveUri.getPath();
								}
								exif.createOutFile(exifPath);
								exif.writeExifData();
							}
						}
						Bitmap originalBitmap = Bimp.revitionImageSize(saveUri
								.getPath());
						;
						Bitmap overviewBitmap =  Bimp.revitionImageSize(uri.getPath()
								.toString());
						
						
						// Send Uri back to JavaScript for viewing image
						// 将高清图片地址和小图地址传回前端
						callbackData(originalBitmap, overviewBitmap, saveUri,
								uri);

						this.cleanup(FILE_URI, this.imageUri, uri, bitmap);

						bitmap = null;

					} catch (Exception e) {
						e.printStackTrace();
						this.failPicture(Res.getString("capture_error"));
					}
				}
			}// If cancelled
			else if (resultCode == Activity.RESULT_CANCELED) {
				this.failPicture(Res.getString("cancel_camera"));
			}

			// If something else
			else {
				this.failPicture(Res.getString("camera_error"));
			}
		}
		// 从相册取图片
		else if (requestCode  ==REQUEST_GELLEY_IMG_SELECT ) {
			LogUtils.JasonDebug("resultCode="+resultCode);
			if (resultCode == -2) {
				if (intent == null) {
					//解决HCM放弃选择图片时弹出error的问题
//					LogUtils.JasonDebug("00000000");
//					this.failPicture(Res.getString("cancel_select"));
				}else {
					ArrayList<ImageItem> selectedList = (ArrayList<ImageItem>) intent
							.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
					String uris[] = new String[selectedList.size()];
					for (int i = 0; i < selectedList.size(); i++) {
						uris[i] = selectedList.get(i).path;
					}
					Bitmap bitmaps[] = new Bitmap[uris.length];

					Bitmap originalBitmaps[] = new Bitmap[selectedList
							.size()];


					// Do we need to scale the returned file
					if (targetHeight != -1 || targetWidth != -1
							|| mQuality != 100) {

						for (int i = 0; i < uris.length; i++) {
							try {
								bitmaps[i] = getScaledBitmap(uris[i]);
							} catch (IOException e) {
								e.printStackTrace();
							}
							// Get the path to the image. Makes loading so much
							// easier.
							String mimeType = FileHelper.getMimeType(uris[i], this);
							// If we don't have a valid image so quit.
							if (!("image/jpeg".equalsIgnoreCase(mimeType)
									|| !("image/jpg".equalsIgnoreCase(mimeType))
									|| "image/png".equalsIgnoreCase(mimeType)
									|| "image/bmp".equalsIgnoreCase(mimeType) || "image/x-ms-bmp"
									.equalsIgnoreCase(mimeType)
									| "image/gif".equalsIgnoreCase(mimeType))) {
								iLog.d(LOG_TAG,
										"I either have a null image path or bitmap");
								this.failPicture(Res
										.getString("retrive_path_error"));
								return;
							}
						}


						String filePaths[] = new String[uris.length];

						try {
							for (int i = 0; i < uris.length; i++) {
								// Create an ExifHelper to save the exif
								// data that is lost during compression
								Date d = new Date();
								String resizePath = getTempDirectoryPath() + "/"
										+ d.getTime() + i + ".jpg";
								// Some content: URIs do not map to file
								// paths (e.g. picasa).
								String realPath = FileHelper.getRealPath(uris[i],
										this);
								ExifHelper exif = new ExifHelper();
								OutputStream os = new FileOutputStream(resizePath);
								try {
									bitmaps[i].compress(Bitmap.CompressFormat.JPEG,
											this.mQuality, os);
								} catch (Exception e) {
									// TODO: handle exception
									e.printStackTrace();
								}
								
								filePaths[i] = "file://" + resizePath;
								os.close();
								// Restore exif data to file
								if (realPath != null && this.encodingType == JPEG) {
									exif.createOutFile(resizePath);
									exif.writeExifData();
								}
							}

							// The resized image is cached by the app in
							// order to get around this and not have to
							// delete you
							// application cache I'm adding the current
							// system time to the end of the file url.
							// 将选中的大图和小图地址传回前端
							for (int i = 0; i < filePaths.length; i++) {
								originalBitmaps[i] = Bimp.revitionImageSize(selectedList
										.get(i).path);
								bitmaps[i] =  Bimp.revitionImageSize(filePaths[i].split("file://")[1]);
							}
							callbackDatas(originalBitmaps, bitmaps,
									 selectedList, filePaths);
						} catch (Exception e) {
							e.printStackTrace();
							this.failPicture(Res.getString("retrive_image_error"));
						}
					} else {
						// 将选中的大图和小图地址传回前端
						JSONObject jsonObject = new JSONObject();
						JSONArray suolueJson = new JSONArray();
						JSONArray bigJson = new JSONArray();
						try {
							for (int i = 0; i < uris.length; i++) {
								suolueJson.put(i, uris[i]);
								bigJson.put(i,
										selectedList.get(i).path);
//								originalBitmaps[i] = getDiskBitmap(PublicWay.selectedDataList
//										.get(i).imagePath);
								
								originalBitmaps[i] = Bimp.revitionImageSize(selectedList
										.get(i).path);
								bitmaps[i] = Bimp.revitionImageSize(uris[i]);
							}
							jsonObject.put("overviewUrl", suolueJson);
							jsonObject.put("url", bigJson);
						} catch (Exception e) {
							e.printStackTrace();
						}
						callbackDatas(originalBitmaps, bitmaps,
								selectedList, uris);
					}

					for (int i = 0; i < bitmaps.length; i++) {
						if (bitmaps[i] != null) {
							bitmaps[i].recycle();
							bitmaps[i] = null;
						}
						if (originalBitmaps[i] != null) {
							originalBitmaps[i].recycle();
							originalBitmaps[i] = null;
						}
					}
					System.gc();

					
				}
				
				
				
			} else if (resultCode == Activity.RESULT_CANCELED) {
				this.failPicture(Res.getString("cancel_select"));
			} else {
				this.failPicture(Res.getString("select_error"));
			}
		}
		PublicWay.activityList.clear();
	}

	/**
	 * 旋转图片，解决三星手机拍照图片被旋转的问题
	 */
	private void rotateImg() {
		// TODO Auto-generated method stub
		int degree = readPictureDegree(this.imageUri.getPath());
		if(degree == 0){
	        return;
	    }
	    FileInputStream inputStream = null;
	    try {
	    	
	        Bitmap bitmap = Bimp.revitionImageSize(this.imageUri.getPath());
	        Bitmap destBitmap = rotaingImageView(degree,bitmap);
	        bitmap.recycle();
	        //save to file
	        saveBitmapToSDCard(destBitmap, this.imageUri.getPath());
	        destBitmap.recycle();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        if (inputStream != null) {
	            try {
	                inputStream.close();
	            } catch (IOException e) {
	            }
	        }
	    }
		
	}
	
	/**
	 * 保存bitmap
	 *
	 * @param bitmap
	 */
	public void saveBitmapToSDCard(Bitmap bitmap,String path) {
		File orignFile = new File(path);
		File file = new File(path+"---");
		try {
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			if (orignFile.exists()) {
				orignFile.delete();
				file.renameTo(new File(path));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (file.exists()) {
				file.delete();
			}
		}
		

	}
	
	

	/**
	 * Create entry in media store for image
	 * 
	 * @return uri
	 */
	private Uri getUriFromMediaStore() {
		ContentValues values = new ContentValues();
		values.put(android.provider.MediaStore.Images.Media.MIME_TYPE,
				"image/jpeg");
		Uri uri;
		try {
			uri = this.context
					.getContentResolver()
					.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							values);
		} catch (UnsupportedOperationException e) {
			iLog.i(LOG_TAG, "Can't write to external media storage.");
			try {
				uri = this.context
						.getContentResolver()
						.insert(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI,
								values);
			} catch (UnsupportedOperationException ex) {
				iLog.i(LOG_TAG, "Can't write to internal media storage.");
				return null;
			}
		}
		return uri;
	}

	/**
	 * Cleans up after picture taking. Checking for duplicates and that kind of
	 * stuff.
	 * 
	 * @param newImage
	 */
	private void cleanup(int imageType, Uri oldImage, Uri newImage,
			Bitmap bitmap) {
		if (bitmap != null) {
			bitmap.recycle();
		}

		// Clean up initial camera-written image file.
		(new File(FileHelper.stripFileProtocol(oldImage.toString()))).delete();

		checkForDuplicateImage(imageType);
		System.gc();
	}

	/**
	 * In the special case where the default width, height and quality are
	 * unchanged we just write the file out to disk saving the expensive
	 * Bitmap.compress function.
	 * 
	 * @param uri
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void writeUncompressedImage(Uri uri) throws FileNotFoundException,
			IOException {
		FileInputStream fis = new FileInputStream(
				FileHelper.stripFileProtocol(this.imageUri.toString()));
		
		
		OutputStream os = this.context.getContentResolver().openOutputStream(
				uri);
		byte[] buffer = new byte[4096];
		int len;
		while ((len = fis.read(buffer)) != -1) {
			os.write(buffer, 0, len);
		}
		os.flush();
		os.close();
		fis.close();
	}
	
	

	/**
	 * IMP代码修改处
	 * 
	 * @param pathString
	 *            图片本地路径
	 * @return bitmap对象
	 */
	private Bitmap getDiskBitmap(String pathString) {
		Bitmap bitmap = null;
		try {
			File file = new File(pathString);
			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(pathString);
			}
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}

		return bitmap;
	}

	/**
	 * Return a scaled bitmap based on the target width and height
	 * @param imageUrl
	 * @return
	 * @throws IOException
     */
	private Bitmap getScaledBitmap(String imageUrl) throws IOException {
		// If no new width or height were specified return the original bitmap
		if (this.targetWidth <= 0 && this.targetHeight <= 0) {
			// return
			// BitmapFactory.decodeStream(FileHelper.getInputStreamFromUriString(imageUrl,
			// this));
			return Bimp.revitionImageSize(imageUrl);
		}

		// figure out the original width and height of the image
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(
				FileHelper.getInputStreamFromUriString(imageUrl, this), null,
				options);

		// CB-2292: WTF? Why is the width null?
		if (options.outWidth == 0 || options.outHeight == 0) {
			return null;
		}
		// determine the correct aspect ratio
		int[] widthHeight = calculateAspectRatio(options.outWidth,
				options.outHeight);

		// Load in the smallest bitmap possible that is closest to the size we
		// want
		options.inJustDecodeBounds = false;
		options.inSampleSize = calculateSampleSize(options.outWidth,
				options.outHeight, this.targetWidth, this.targetHeight);
		Bitmap unscaledBitmap = BitmapFactory.decodeStream(
				FileHelper.getInputStreamFromUriString(imageUrl, this), null,
				options);
		if (unscaledBitmap == null) {
			return null;
		}
		int angle = readPictureDegree(imageUrl);
		Bitmap bm = Bitmap.createScaledBitmap(unscaledBitmap, widthHeight[0],
				widthHeight[1], true);
		return RoundImage.createFramedPhoto(this.targetWidth,
				this.targetHeight, rotaingImageView(angle, bm), 0);
		// return Bitmap.createScaledBitmap(unscaledBitmap, widthHeight[0],
		// widthHeight[1], true);
	}

	/**
	 * Figure out what ratio we can load our image into memory at while still
	 * being bigger than our desired width and height
	 * 
	 * @param srcWidth
	 * @param srcHeight
	 * @param dstWidth
	 * @param dstHeight
	 * @return
	 */
	public static int calculateSampleSize(int srcWidth, int srcHeight,
			int dstWidth, int dstHeight) {
		final float srcAspect = (float) srcWidth / (float) srcHeight;
		final float dstAspect = (float) dstWidth / (float) dstHeight;

		if (srcAspect > dstAspect) {
			return srcWidth / dstWidth;
		} else {
			return srcHeight / dstHeight;
		}
	}

	/**
	 * Maintain the aspect ratio so the resulting image does not look smooshed
	 * 
	 * @param origWidth
	 * @param origHeight
	 * @return
	 */
	public int[] calculateAspectRatio(int origWidth, int origHeight) {
		int newWidth = this.targetWidth;
		int newHeight = this.targetHeight;

		// If no new width or height were specified return the original bitmap
		if (newWidth <= 0 && newHeight <= 0) {
			newWidth = origWidth;
			newHeight = origHeight;
		}
		// Only the width was specified
		else if (newWidth > 0 && newHeight <= 0) {
			newHeight = (newWidth * origHeight) / origWidth;
		}
		// only the height was specified
		else if (newWidth <= 0 && newHeight > 0) {
			newWidth = (newHeight * origWidth) / origHeight;
		} else {
			double newRatio = newWidth / (double) newHeight;
			double origRatio = origWidth / (double) origHeight;

			if (origRatio > newRatio) {
				newHeight = (newWidth * origHeight) / origWidth;
			} else if (origRatio < newRatio) {
				newWidth = (newHeight * origWidth) / origHeight;
			}
		}

		int[] retval = new int[2];
		// IMP代码修改处，有些图片得出结果为0
		if (newWidth == 0) {
			newWidth = 1;
		}
		if (newHeight == 0) {
			newHeight = 1;
		}
		retval[0] = newWidth;
		retval[1] = newHeight;
		return retval;
	}

	/**
	 * Determine if we are storing the images in internal or external storage
	 * 
	 * @return Uri
	 */
	private Uri whichContentStore() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		} else {
			return android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;
		}
	}

	/**
	 * IMP代码修改处
	 * 
	 * @param originalBitmap
	 *            原图Bitmap
	 * @param overviewBitmap
	 *            缩略图Bitmap
	 * @param saveUri
	 *            原图URI
	 * @param uri
	 *            缩略图URI
	 */
	private void callbackData(Bitmap originalBitmap, Bitmap overviewBitmap,
			Uri saveUri, Uri uri) {
		// TODO Auto-generated method stub
		ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
		ByteArrayOutputStream originalJpeg_data = new ByteArrayOutputStream();
		// 将选中的大图和小图地址传回前端
		JSONObject jsonObject = new JSONObject();
		try {
			if (overviewBitmap.compress(CompressFormat.JPEG, 100, jpeg_data)) {
				byte[] code = jpeg_data.toByteArray();
				byte[] output = Base64.encode(code, Base64.NO_WRAP);
				String js_out = new String(output);
				jsonObject.put("thumbnailUrl", uri.getPath().toString());
				jsonObject.put("thumbnailData", js_out.toString());
				js_out = null;
				output = null;
				code = null;
			}
			if (originalBitmap.compress(CompressFormat.JPEG, 100,
					originalJpeg_data)) {
				byte[] code = originalJpeg_data.toByteArray();
				byte[] output = Base64.encode(code, Base64.NO_WRAP);
				String js_out = new String(output);
				jsonObject.put("originalUrl", saveUri.getPath().toString());
				jsonObject.put("originalData", js_out.toString());
				js_out = null;
				output = null;
				code = null;
			}
			this.jsCallback(successCb, jsonObject.toString());
		} catch (Exception e) {
			this.failPicture(Res.getString("compress_error"));
		}
		jpeg_data = null;
		originalJpeg_data = null;

	}

	/**
	 * IMP代码修改处
	 * 
	 * @param originalBitmaps
	 *            原图Bitmap数组
	 * @param bitmaps
	 *            缩略图Bitmap数组
	 * @param selectedDataList
	 *            原图路径List
	 * @param filePaths
	 *            缩略图路径List
	 */
	private void callbackDatas(Bitmap[] originalBitmaps, Bitmap[] bitmaps,
			ArrayList<ImageItem> selectedDataList, String[] filePaths) {
		// TODO Auto-generated method stub
		String js_outs[] = new String[bitmaps.length];
		String originalJs_outs[] = new String[bitmaps.length];
		try {
			// 将缩略图转换为base64
			for (int i = 0; i < bitmaps.length; i++) {
				ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
				if (bitmaps[i].compress(CompressFormat.JPEG, 100, jpeg_data)) {
					byte[] code = jpeg_data.toByteArray();
					byte[] output = Base64.encode(code, Base64.NO_WRAP);
					String js_out = new String(output);
					js_outs[i] = js_out;
					js_out = null;
					output = null;
					code = null;
				}
				jpeg_data = null;
			}
			// 将原图的bitmap转化为base64
			for (int i = 0; i < originalBitmaps.length; i++) {
				ByteArrayOutputStream originalJpeg_data = new ByteArrayOutputStream();
				if (originalBitmaps[i].compress(CompressFormat.JPEG, 100,
						originalJpeg_data)) {
					byte[] code = originalJpeg_data.toByteArray();
					byte[] output = Base64.encode(code, Base64.NO_WRAP);
					String js_out = new String(output);
					originalJs_outs[i] = js_out;
					js_out = null;
					output = null;
					code = null;
				}
				originalJpeg_data = null;
			}
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < js_outs.length; i++) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("thumbnailUrl", filePaths[i]);
				jsonObject.put("thumbnailData", js_outs[i]);
				jsonObject.put("originalUrl", selectedDataList.get(i).path);
				jsonObject.put("originalData", originalJs_outs[i]);
				jsonArray.put(i, jsonObject);
			}
			
			// 将选中的大图和小图地址传回前端
			this.jsCallback(successCb, jsonArray.toString());
		} catch (Exception e) {
			this.failPicture(Res.getString("image_error"));
		}
	}

	// IMP代码修改处
	// /**
	// * Compress bitmap using jpeg, convert to Base64 encoded string, and
	// return
	// * to JavaScript.
	// *
	// * @param bitmap
	// */
	// public void processPicture(Bitmap bitmap, Bitmap originalBitmap) {
	// ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
	// ByteArrayOutputStream originalJpeg_data = new ByteArrayOutputStream();
	// // 将选中的大图和小图地址传回前端
	// JSONObject jsonObject = new JSONObject();
	// try {
	// if (bitmap.compress(CompressFormat.JPEG, mQuality, jpeg_data)) {
	// byte[] code = jpeg_data.toByteArray();
	// byte[] output = Base64.encode(code, Base64.NO_WRAP);
	// String js_out = new String(output);
	// jsonObject.put("overviewUrl", js_out.toString());
	// js_out = null;
	// output = null;
	// code = null;
	// }
	// if (originalBitmap.compress(CompressFormat.JPEG, mQuality,
	// originalJpeg_data)) {
	// byte[] code = originalJpeg_data.toByteArray();
	// byte[] output = Base64.encode(code, Base64.NO_WRAP);
	// String js_out = new String(output);
	// jsonObject.put("url", js_out.toString());
	// js_out = null;
	// output = null;
	// code = null;
	// }
	// this.jsCallback(successCb, jsonObject.toString());
	// } catch (Exception e) {
	// this.failPicture(Res.getString("compress_error"));
	// }
	// jpeg_data = null;
	// originalJpeg_data = null;
	// }

	/**
	 * Compress bitmap using jpeg, convert to Base64 encoded string, and return
	 * to JavaScript.
	 * 
	 * @param bitmap
	 */
	public void processPictures(Bitmap bitmaps[], Bitmap originalBitmaps[]) {

		String js_outs[] = new String[bitmaps.length];
		String originalJs_outs[] = new String[bitmaps.length];
		try {
			// 将缩略图转换为base64
			for (int i = 0; i < bitmaps.length; i++) {
				ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
				if (bitmaps[i].compress(CompressFormat.JPEG, mQuality,
						jpeg_data)) {
					byte[] code = jpeg_data.toByteArray();
					byte[] output = Base64.encode(code, Base64.NO_WRAP);
					String js_out = new String(output);
					js_outs[i] = js_out;
					js_out = null;
					output = null;
					code = null;
				}
				jpeg_data = null;
			}
			// 将原图的bitmap转化为base64
			for (int i = 0; i < originalBitmaps.length; i++) {
				ByteArrayOutputStream originalJpeg_data = new ByteArrayOutputStream();
				if (originalBitmaps[i].compress(CompressFormat.JPEG, mQuality,
						originalJpeg_data)) {
					byte[] code = originalJpeg_data.toByteArray();
					byte[] output = Base64.encode(code, Base64.NO_WRAP);
					String js_out = new String(output);
					originalJs_outs[i] = js_out;
					js_out = null;
					output = null;
					code = null;
				}
				originalJpeg_data = null;
			}

			// 将选中的大图和小图地址传回前端
			JSONObject jsonObject = new JSONObject();
			JSONArray suolueJson = new JSONArray();
			JSONArray bigJson = new JSONArray();
			try {
				for (int i = 0; i < js_outs.length; i++) {
					suolueJson.put(i, js_outs[i]);
					bigJson.put(i, originalJs_outs[i]);
				}
				jsonObject.put("overviewUrl", suolueJson);
				jsonObject.put("url", bigJson);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			this.jsCallback(successCb, jsonObject.toString());
		} catch (Exception e) {
			this.failPicture(Res.getString("image_error"));
		}

	}

	/**
	 * Used to find out if we are in a situation where the Camera Intent adds to
	 * images to the content store. If we are using a FILE_URI and the number of
	 * images in the DB increases by 2 we have a duplicate, when using a
	 * DATA_URL the number is 1.
	 * 
	 * @param type
	 *            FILE_URI or DATA_URL
	 */
	private void checkForDuplicateImage(int type) {
		int diff = 1;
		Uri contentStore = whichContentStore();
		Cursor cursor = queryImgDB(contentStore);
		int currentNumOfImages = cursor.getCount();

		if (type == FILE_URI && this.saveToPhotoAlbum) {
			diff = 2;
		}

		// delete the duplicate file if the difference is 2 for file URI or 1
		// for Data URL
		if ((currentNumOfImages - numPics) == diff) {
			cursor.moveToLast();
			int id = Integer.valueOf(cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media._ID)));
			if (diff == 2) {
				id--;
			}
			Uri uri = Uri.parse(contentStore + "/" + id);
			this.context.getContentResolver().delete(uri, null, null);
			cursor.close();
		}
	}

	/**
	 * Creates a cursor that can be used to determine how many images we have.
	 * 
	 * @return a cursor
	 */
	private Cursor queryImgDB(Uri contentStore) {
		return this.context.getContentResolver().query(contentStore,
				new String[] { MediaStore.Images.Media._ID }, null, null, null);
	}

	/**
	 * 读取图片属性：旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/*
	 * 旋转图片
	 * 
	 * @param angle
	 * 
	 * @param bitmap
	 * 
	 * @return Bitmap
	 */
	public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		;
		matrix.postRotate(angle);
		System.out.println("angle2=" + angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

	/**
	 * Send error message to JavaScript.
	 * 
	 * @param err
	 */
	public void failPicture(String err) {
		this.jsCallback(failCb, err);
	}

	@Override
	public void onDestroy() {

	}

	private void destoryImage() {
		if (photo != null) {
			photo.recycle();
			photo = null;
		}
	}

}

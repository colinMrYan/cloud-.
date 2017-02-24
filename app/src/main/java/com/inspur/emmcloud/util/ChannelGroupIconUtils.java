package com.inspur.emmcloud.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.bean.GetSearchChannelGroupResult;
import com.inspur.emmcloud.config.MyAppConfig;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChannelGroupIconUtils {
	private static final int RERESH_GROUP_ICON = 2;
	private List<Channel> channelList;
	private Context context;
	private Handler handler;

	public ChannelGroupIconUtils(Context context, List<Channel> channelList,
			Handler handler) {
		this.channelList = channelList;
		this.context = context;
		this.handler = handler;
	}

	public void creat() {
		// TODO Auto-generated method stub
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return;
		}
		List<ChannelGroup> currentChannelGroupList = new ArrayList<ChannelGroup>();
		for (int i = 0; i < channelList.size(); i++) {
			Channel channel = channelList.get(i);
			if (channel.getType().equals("GROUP")) {
				ChannelGroup channelGroup = new ChannelGroup(channel);
				currentChannelGroupList.add(channelGroup);
			}
		}
		List<ChannelGroup> cacheChannelGroupList = ChannelGroupCacheUtils
				.getAllChannelGroupList(context);
		currentChannelGroupList.removeAll(cacheChannelGroupList);
		if (currentChannelGroupList.size() > 0) {
			getChannelGroups(currentChannelGroupList);
		} else {
			creatIcon();
		}

	}

	private void getChannelGroups(List<ChannelGroup> currentChannelGroupList) {
		// TODO Auto-generated method stub
		String[] cidArray = new String[currentChannelGroupList.size()];
		for (int i = 0; i < currentChannelGroupList.size(); i++) {
			cidArray[i] = currentChannelGroupList.get(i).getCid();
		}
		ChatAPIService apiService = new ChatAPIService(context);
		apiService.setAPIInterface(new WebService());
		apiService.getChannelGroupList(cidArray);
	}

	private void creatIcon() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				File dir = new File(MyAppConfig.LOCAL_CACHE_PATH);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				boolean isNeedCreatIcon = false;
				DisplayImageOptions	options = new DisplayImageOptions.Builder()
				// 设置图片的解码类型  
                .bitmapConfig(Bitmap.Config.RGB_565)  
                .cacheInMemory(true)
                .cacheOnDisk(true)
				.build();
				for (int i = 0; i < channelList.size(); i++) {
					Channel channel = channelList.get(i);
					
					if (channel.getType().equals("GROUP")
							&& !isIconInSDcard(channel.getCid())) {
						isNeedCreatIcon = true;
						List<String> memberUidList = ChannelGroupCacheUtils.getMemberUidList(context,channel.getCid(),4);
						List<Bitmap> bitmapList = new ArrayList<Bitmap>();
						for (int j = 0; j < memberUidList.size(); j++) {
							String pid = memberUidList.get(j);
							Bitmap bitmap = null;
							if (!StringUtils.isBlank(pid) && !pid.equals("null")) {
								bitmap = ImageLoader.getInstance().loadImageSync(UriUtils.getChannelImgUri(pid),options);
							}
							if (bitmap == null) {
								bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_person_default);
							}
							bitmapList.add(bitmap);
						}
						Bitmap combineBitmap = BitmapUtils
								.createGroupFace(context, bitmapList);
						
						if (combineBitmap != null) {
							saveBitmap(channel.getCid(), combineBitmap);
						}
					}
				}
				if (handler != null && isNeedCreatIcon) {
					handler.sendEmptyMessage(RERESH_GROUP_ICON);
				}
			}
		};
		new Thread(runnable).start();
	}

	private boolean isIconInSDcard(String cid) {
		File file = new File(MyAppConfig.LOCAL_CACHE_PATH, UriUtils.tanent+cid + "_100.png1");
		if (file.exists()
				) {
			return true;
		}
		return false;

	}

	public Bitmap returnBitMap(String url) {
		URL myFileUrl = null;
		Bitmap bitmap = null;
		try {
			myFileUrl = new URL(url);
			HttpURLConnection conn;
			conn = (HttpURLConnection) myFileUrl.openConnection();
			String token =  ((MyApplication)context.getApplicationContext()).getToken();
			conn.setRequestProperty("Authorization", token);
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bitmap;
	}

	/** 保存方法 */
	public void saveBitmap(String cid, Bitmap bitmap) {
		File dir = new File(MyAppConfig.LOCAL_CACHE_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(MyAppConfig.LOCAL_CACHE_PATH, UriUtils.tanent+cid + "_100.png1");
		if (file.exists()) {
			file.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (file.exists()) {
				file.delete();
			}
		}

	}

	private class WebService extends APIInterfaceInstance{

		@Override
		public void returnSearchChannelGroupSuccess(
				GetSearchChannelGroupResult getSearchChannelGroupResult) {
			// TODO Auto-generated method stub
			List<ChannelGroup> channelGroupList = getSearchChannelGroupResult.getSearchChannelGroupList();
			ChannelGroupCacheUtils.saveChannelGroupList(context, channelGroupList);
			creatIcon();
		}

		@Override
		public void returnSearchChannelGroupFail(String error) {
			// TODO Auto-generated method stub
			creatIcon();
		}
		
	}
}

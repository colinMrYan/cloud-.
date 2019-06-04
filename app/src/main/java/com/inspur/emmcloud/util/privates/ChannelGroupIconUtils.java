package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/7/6.
 */

public class ChannelGroupIconUtils {
    private static final int RERESH_GROUP_ICON = 2;
    private static int PADDING = 2;
    private static int rangetWidth;
    private Context context;
    private Handler handler;
    private List<Channel> channelTypeGroupList = new ArrayList<>();

    public static Bitmap createGroupFace(Context context,
                                         List<Bitmap> bitmapList) {
        if (bitmapList == null || bitmapList.size() == 0) {
            return null;
        }
        rangetWidth = dip2px(context, 40);
        PADDING = 1;
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
     *
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
                Bitmap.Config.RGB_565);
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
        localCanvas.save();
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
                Bitmap.Config.ARGB_8888);
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

        localCanvas.save();
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
                Bitmap.Config.RGB_565);
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

        localCanvas.save();
        localCanvas.restore();
        return canvasBitmap;
    }

    private static Bitmap cutBitmap(Bitmap bitmap) {
        // TODO Auto-generated method stub
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        bitmap = Bitmap.createBitmap(bitmap, width / 4, 0,
                width / 2, height);
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

    public boolean create(Context context, List<Channel> channelList,
                          Handler handler) {
        // TODO Auto-generated method stub
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED) || !NetUtils.isNetworkConnected(context, false)) {
            return false;
        }
        if (channelList == null) {
            channelTypeGroupList = ChannelCacheUtils.getChannelList(context, "GROUP");
        } else {
            channelTypeGroupList.clear();
            for (Channel channel : channelList) {
                if (channel.getType().equals("GROUP")) {
                    channelTypeGroupList.add(channel);
                }
            }

        }
        if (channelTypeGroupList.size() == 0) {
            return false;
        }
        this.context = context;
        this.handler = handler;
        List<ChannelGroup> currentChannelGroupList = new ArrayList<>();
        for (int i = 0; i < channelTypeGroupList.size(); i++) {
            Channel channel = channelTypeGroupList.get(i);
            ChannelGroup channelGroup = new ChannelGroup(channel);
            currentChannelGroupList.add(channelGroup);
        }
        List<ChannelGroup> cacheChannelGroupList = ChannelGroupCacheUtils
                .getAllChannelGroupList(context);
        currentChannelGroupList.removeAll(cacheChannelGroupList);
        if (currentChannelGroupList.size() > 0) {
            getChannelGroups(currentChannelGroupList);
        } else {
            new ChannelGroupIconCreateTask().execute();
        }
        return true;
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

    /**
     * 保存方法
     */
    public String saveBitmap(String cid, Bitmap bitmap) {
        File dir = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH, MyApplication.getInstance().getTanent() + cid + "_100.png1");
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
            out.flush();
            out.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            if (file.exists()) {
                file.delete();
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        return null;

    }

    class ChannelGroupIconCreateTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean isCreateNewGroupIcon = false;
            synchronized (this) {
                File dir = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        // 设置图片的解码类型
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .build();
                isCreateNewGroupIcon = (channelTypeGroupList.size() > 0);
                for (int i = 0; i < channelTypeGroupList.size(); i++) {
                    Channel channel = channelTypeGroupList.get(i);
                    List<String> memberUidList = ChannelGroupCacheUtils.getExistMemberUidList(context, channel.getCid(), 4);
                    List<Bitmap> bitmapList = new ArrayList<Bitmap>();
                    for (int j = 0; j < memberUidList.size(); j++) {
                        String pid = memberUidList.get(j);
                        Bitmap bitmap = null;
                        if (!StringUtils.isBlank(pid) && !pid.equals("null")) {
                            bitmap = ImageLoader.getInstance().loadImageSync(APIUri.getChannelImgUrl(context, pid), options);
                        }
                        if (bitmap == null) {
                            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_person_default);
                        }
                        bitmapList.add(bitmap);
                    }
                    Bitmap combineBitmap = createGroupFace(context, bitmapList);
                    if (combineBitmap != null) {
                        saveBitmap(channel.getCid(), combineBitmap);
                    }
                }

            }
            return isCreateNewGroupIcon;
        }

        @Override
        protected void onPostExecute(Boolean isCreateNewGroupIcon) {
            if (handler != null) {
                Message msg = new Message();
                msg.what = RERESH_GROUP_ICON;
                msg.obj = isCreateNewGroupIcon;
                handler.sendMessage(msg);
            }
        }
    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnSearchChannelGroupSuccess(
                GetSearchChannelGroupResult getSearchChannelGroupResult) {
            // TODO Auto-generated method stub
            List<ChannelGroup> channelGroupList = getSearchChannelGroupResult.getSearchChannelGroupList();
            ChannelGroupCacheUtils.saveChannelGroupList(context, channelGroupList);
            new ChannelGroupIconCreateTask().execute();
        }

        @Override
        public void returnSearchChannelGroupFail(String error, int errCode) {
            // TODO Auto-generated method stub
            new ChannelGroupIconCreateTask().execute();
        }

    }

}

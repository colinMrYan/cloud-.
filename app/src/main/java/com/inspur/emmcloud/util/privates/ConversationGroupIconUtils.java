package com.inspur.emmcloud.util.privates;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Environment;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/10/2.
 */

public class ConversationGroupIconUtils {
    private static ConversationGroupIconUtils instance;
    private int rangetWidth;
    private int padding;

    private ConversationGroupIconUtils() {
        rangetWidth = DensityUtil.dip2px(MyApplication.getInstance(), 45);
        padding = DensityUtil.dip2px(MyApplication.getInstance(), 1);
    }

    public static ConversationGroupIconUtils getInstance() {
        if (instance == null) {
            synchronized (ConversationGroupIconUtils.class) {
                if (instance == null) {
                    instance = new ConversationGroupIconUtils();
                }
            }
        }
        return instance;
    }

    public void create(List<Conversation> conversationList) {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED) || !NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            return;
        }
        new CreateIconTask(conversationList).execute();
    }

    private Bitmap createGroupIcon(List<Bitmap> bitmapList) {
        if (bitmapList == null || bitmapList.size() == 0) {
            return null;
        }
        if (bitmapList.size() == 1) {
            return createOneBit(bitmapList);
        } else if (bitmapList.size() == 2) {
            return createTwoBit(bitmapList);
        } else if (bitmapList.size() == 3) {
            return createThreeBit(bitmapList);
        } else {
            return createFourBit(bitmapList);
        }
    }

    /**
     * 当出现群组中只有一个人的情况
     *
     * @param paramList
     * @param context
     * @return
     */
    private Bitmap createOneBit(List<Bitmap> paramList) {
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
    private Bitmap createTwoBit(List<Bitmap> paramList) {

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
        localCanvas.drawBitmap(bit2, rangetWidth / 2 + padding, 0, null);
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
    private Bitmap createThreeBit(List<Bitmap> paramList) {

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
        localCanvas.drawBitmap(bit2, rangetWidth / 2 + padding, 0, null);
        bit2.recycle();

        Bitmap bit3 = zoomImage(paramList.get(2), rangetWidth / 2,
                rangetWidth / 2);
        localCanvas.drawBitmap(bit3, rangetWidth / 2 + padding, rangetWidth / 2
                + padding, null);
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
    private Bitmap createFourBit(List<Bitmap> paramList) {

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
        localCanvas.drawBitmap(bit2, rangetWidth / 2 + padding, 0, null);
        bit2.recycle();

        Bitmap bit3 = zoomImage(paramList.get(2), rangetWidth / 2,
                rangetWidth / 2);
        localCanvas.drawBitmap(bit3, 0, rangetWidth / 2 + padding, null);
        bit3.recycle();

        Bitmap bit4 = zoomImage(paramList.get(3), rangetWidth / 2,
                rangetWidth / 2);
        localCanvas.drawBitmap(bit4, rangetWidth / 2 + padding, rangetWidth / 2
                + padding, null);
        bit4.recycle();

        localCanvas.save();
        localCanvas.restore();
        return canvasBitmap;
    }

    private Bitmap cutBitmap(Bitmap bitmap) {
        // TODO Auto-generated method stub
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        bitmap = Bitmap.createBitmap(bitmap, width / 4, 0,
                width / 2, height);
        return bitmap;
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
    private Bitmap zoomImage(Bitmap bgimage, double newWidth,
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
     * 保存方法
     */
    private String saveBitmap(String cid, Bitmap bitmap) {
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
            return "file://" + file.getAbsolutePath();
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

    private class CreateIconTask extends AsyncTask<Void, Integer, Boolean> {
        private List<Conversation> conversationList;

        private CreateIconTask(List<Conversation> conversationList) {
            this.conversationList = conversationList;
        }

        @Override
        protected void onPostExecute(Boolean isCreateGroupIcon) {
            if (isCreateGroupIcon) {
                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_REFRESH_CONVERSATION_ADAPTER));
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (conversationList == null) {
                conversationList = ConversationCacheUtils.getConversationList(MyApplication.getInstance(), Conversation.TYPE_GROUP);
            }
            if (conversationList.size() == 0) {
                return false;
            }
            File iconDir = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH);
            if (!iconDir.exists()) {
                iconDir.mkdirs();
            }
            DisplayImageOptions options = ImageDisplayUtils.getInstance().getDefaultOptions(R.drawable.icon_person_default);
            for (Conversation conversation : conversationList) {
                List<Bitmap> bitmapList = new ArrayList<>();
                List<String> memberUidList = conversation.getMemberList();
                for (String uid : memberUidList) {
                    String iconUrl = APIUri.getChannelImgUrl(MyApplication.getInstance(), uid);
                    if (!StringUtils.isBlank(iconUrl)) {
                        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(iconUrl, options);
                        if (bitmap == null) {
                            bitmap = BitmapFactory.decodeResource(MyApplication.getInstance().getResources(), R.drawable.icon_person_default);
                        }
                        bitmapList.add(bitmap);
                    }
                    if (bitmapList.size() == 4) {
                        break;
                    }
                }
                Bitmap groupBitmap = createGroupIcon(bitmapList);
                if (groupBitmap != null) {
                    saveBitmap(conversation.getId(), groupBitmap);
//                    String iconUrl = saveBitmap(conversation.getId(), groupBitmap);
//                    if (iconUrl != null){
//                        //清空原来的缓存
//                        ImageDisplayUtils.getInstance().clearCache(iconUrl);
//                    }
                }
            }
            return true;
        }
    }
}

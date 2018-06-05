package com.inspur.imp.plugin.camera.mycamera;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera.Size;

import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.ResolutionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by chenmch on 2017/6/22.
 */

public class CameraUtils {
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private static CameraUtils myCamPara = null;
    private static float rate = 1.78f;
    private CameraUtils(){

    }
    public static CameraUtils getInstance(Activity context){
        if(myCamPara == null){
            myCamPara = new CameraUtils();
            rate = ResolutionUtils.getResolutionRate(context);
            return myCamPara;
        }
        else{
            return myCamPara;
        }
    }

    /**
     * 获取preview的size
     * @param list
     * @param th  最大尺寸
     * @return
     */
    public  Size getPreviewSize(List<Size> list, int th){
        Collections.sort(list, sizeComparator);
        Collections.reverse(list);
        Size size = null;
        for(Size s:list){
            if((s.width < th) && (s.height < th) && equalRateLevel0(s, rate)){
                size = s;
                break;
            }
        }
        if (size == null){
            for(Size s:list){
                if((s.width < th) && (s.height < th) && equalRateLevel1(s, rate)){
                    size = s;
                    break;
                }
            }
        }

        if (size == null){
            for(Size s:list){
                if((s.width < th) && (s.height < th) && equalRateLevel2(s, rate)){
                    size = s;
                    break;
                }
            }
        }
        if(size == null){
            return  list.get(0);
        }
        return size;
    }

    /**
     * 获取图片的大小
     * @param list
     * @param th  最小尺寸
     * @return
     */
    public Size getPictureSize(List<Size> list, int th){
        Collections.sort(list, sizeComparator);
        Size size = null;
        for(Size s:list){
            if((s.width > th) && (s.height > th) && equalRateLevel0(s, rate)){
                LogUtils.jasonDebug("width="+s.width);
                LogUtils.jasonDebug("height="+s.height);
                size = s;
            }
        }
        if (size == null){
            for(Size s:list) {
                if ((s.width > th) && (s.height > th) && equalRateLevel1(s, rate)) {
                    size = s;
                    break;
                }
            }
        }

        if (size == null){
            for(Size s:list) {
                if ((s.width > th) && (s.height > th) && equalRateLevel2(s, rate)) {
                    size = s;
                    break;
                }
            }
        }
        if(size == null){
            return  list.get(0);
        }
        return size;
    }

    public boolean equalRateLevel0(Size s, float rate){
        float r = (float)(s.width)/(float)(s.height);
        if(Math.abs(r - rate) <= 0.01)
        {
            return true;
        }
        else{
            return false;
        }
    }

    public boolean equalRateLevel1(Size s, float rate){
        float r = (float)(s.width)/(float)(s.height);
        if(Math.abs(r - rate) <= 0.2)
        {
            return true;
        }
        else{
            return false;
        }
    }

    public boolean equalRateLevel2(Size s, float rate){
        float r = (float)(s.width)/(float)(s.height);
        if(Math.abs(r - rate) <= 0.5)
        {
            return true;
        }
        else{
            return false;
        }
    }

    public  class CameraSizeComparator implements Comparator<Size> {
        //按升序排列
        public int compare(Size lhs, Size rhs) {
            // TODO Auto-generated method stub
            if(lhs.width == rhs.width && lhs.height == rhs.height){
                return 0;
            }
            else if(lhs.width > rhs.width || (lhs.width == rhs.width && lhs.height > rhs.height)){
                return 1;
            }
            else{
                return -1;
            }
        }

    }

    /**
     * 打开相机拍照
     * @param context
     * @param photoSaveDirectoryPath
     * @param photoName
     * @param requestCode
     */
    public static void takePhoto(Activity context, String photoSaveDirectoryPath, String photoName, int requestCode){
        Intent intent = new Intent(context,MyCameraActivity.class);
        intent.putExtra(MyCameraActivity.PHOTO_DIRECTORY_PATH,photoSaveDirectoryPath);
        intent.putExtra(MyCameraActivity.PHOTO_NAME,photoName);
        context.startActivityForResult(intent,requestCode);
    }

}

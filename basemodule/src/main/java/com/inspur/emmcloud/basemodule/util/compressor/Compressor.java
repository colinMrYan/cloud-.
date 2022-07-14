package com.inspur.emmcloud.basemodule.util.compressor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created on : June 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class Compressor {
    //max width and height values of the compressed image is taken as 612x816
    private int maxWidth = 612;
    private int maxHeight = 816;
    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    private int quality = 80;
    private String destinationDirectoryPath;
    private int maxArea = -1;

    public Compressor(Context context) {
        destinationDirectoryPath = context.getCacheDir().getPath() + File.separator + "images";
    }

    public Compressor setMaxArea(int maxArea){
        this.maxArea = maxArea;
        return this;
    }

    public Compressor setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public Compressor setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public Compressor setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
        return this;
    }

    public Compressor setQuality(int quality) {
        this.quality = quality;
        return this;
    }

    public Compressor setDestinationDirectoryPath(String destinationDirectoryPath) {
        this.destinationDirectoryPath = destinationDirectoryPath;
        return this;
    }

    public File compressToFile(File imageFile) throws IOException {
        return compressToFile(imageFile, imageFile.getName());
    }

    public File compressToFile(File imageFile, String compressedFileName) throws IOException {
        if (maxArea >0){
            return ImageUtil.compressImage(imageFile, maxArea, compressFormat, quality,
                    destinationDirectoryPath + File.separator + compressedFileName);
        }else {
            return ImageUtil.compressImage(imageFile, maxWidth, maxHeight, compressFormat, quality,
                    destinationDirectoryPath + File.separator + compressedFileName);
        }

    }

    public Bitmap compressToBitmap(File imageFile) throws IOException {
        if (maxArea >0){
            return ImageUtil.decodeSampledBitmapFromFile(imageFile, maxArea);
        }else {
            return ImageUtil.decodeSampledBitmapFromFile(imageFile, maxWidth, maxHeight);
        }

    }

    public ResolutionRatio getResolutionRation(File imageFile) {
        ResolutionRatio resolutionRatio = new ResolutionRatio();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        int height = options.outHeight;
        int width = options.outWidth;
        if (height * width > maxArea) {
            float ratio = (float) height * width / maxArea;
            double ratioDouble = (double) ratio;
            ratioDouble = Math.sqrt(ratioDouble);
            height = (int) (height / ratioDouble);
            width = (int) (width / ratioDouble);
        }
        if (height > 4095 || width > 4095) {
            double heightRadio = (double) height / 4095;
            double widthRadio = (double) width / 4095;
            double reduceRadio = heightRadio > widthRadio ? heightRadio : widthRadio;
            height = (int) (height / reduceRadio);
            width = (int) (width / reduceRadio);
        }
        resolutionRatio.setHigh(height);
        resolutionRatio.setWidth(width);
        return resolutionRatio;
    }

    // 创建已知宽高的ResolutionRatio对象
    public ResolutionRatio getResolutionRation(int width, int height) {
        ResolutionRatio resolutionRatio = new ResolutionRatio();
        resolutionRatio.setHigh(height);
        resolutionRatio.setWidth(width);
        return resolutionRatio;
    }

    public class ResolutionRatio {
        private int high = 0;
        private int width = 0;
        private long size = 0;

        public ResolutionRatio() {
        }

        public ResolutionRatio(int high, int width) {
            this.high = high;
            this.width = width;
        }

        public int getHigh() {
            return high;
        }

        public void setHigh(int high) {
            this.high = high;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public long getSize() {
            return high * width;
        }

    }

}

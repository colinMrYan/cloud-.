package com.inspur.imp.plugin.camera.editimage.utils;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * 图片处理类
 */
public class PhotoUtils {

//    public static final Map<String, Class<? extends GPUImageFilter>> FILTERS = new HashMap<>();
    static {
//        FILTERS.put("原色", null);
//        FILTERS.put("深褐色", GPUImageSepiaFilter.class);
//        FILTERS.put("白平衡", GPUImageWhiteBalanceFilter.class);
//        FILTERS.put("3x3卷积", GPUImage3x3ConvolutionFilter.class);
//        FILTERS.put("3x3的纹理采样", GPUImage3x3TextureSamplingFilter.class);
//        FILTERS.put("添加共混物", GPUImageAddBlendFilter.class);
//        FILTERS.put("Alpha混合", GPUImageAlphaBlendFilter.class);
//        FILTERS.put("双边", GPUImageBilateralFilter.class);
//        FILTERS.put("方框模糊", GPUImageBoxBlurFilter.class);
//        FILTERS.put("亮度", GPUImageBrightnessFilter.class);
//        FILTERS.put("隆起变形", GPUImageBulgeDistortionFilter.class);
//        FILTERS.put("CGA的色彩", GPUImageCGAColorspaceFilter.class);
//        FILTERS.put("色度键混合", GPUImageChromaKeyBlendFilter.class);
//        FILTERS.put("颜色平衡", GPUImageColorBalanceFilter.class);
//        FILTERS.put("颜色混合", GPUImageColorBlendFilter.class);
//        FILTERS.put("色彩调和", GPUImageColorBurnBlendFilter.class);
//        FILTERS.put("颜色减淡混合", GPUImageColorDodgeBlendFilter.class);
//        FILTERS.put("颜色反转", GPUImageColorInvertFilter.class);
//        FILTERS.put("颜色矩阵", GPUImageColorMatrixFilter.class);
//        FILTERS.put("对比度", GPUImageContrastFilter.class);
//        FILTERS.put("交叉线", GPUImageCrosshatchFilter.class);
//        FILTERS.put("变暗混合", GPUImageDarkenBlendFilter.class);
//        FILTERS.put("差分混合", GPUImageDifferenceBlendFilter.class);
//        FILTERS.put("扩张", GPUImageDilationFilter.class);
//        FILTERS.put("方向Sobel边缘检测", GPUImageDirectionalSobelEdgeDetectionFilter.class);
//        FILTERS.put("溶解混合(灰色遮盖)", GPUImageDissolveBlendFilter.class);
//        FILTERS.put("间隔混合", GPUImageDivideBlendFilter.class);
//        FILTERS.put("浮雕效果", GPUImageEmbossFilter.class);
//        FILTERS.put("排除混合", GPUImageExclusionBlendFilter.class);
//        FILTERS.put("曝光", GPUImageExposureFilter.class);
//        FILTERS.put("伪彩色", GPUImageFalseColorFilter.class);
//        FILTERS.put("伽马射线", GPUImageGammaFilter.class);
//        FILTERS.put("高斯模糊", GPUImageGaussianBlurFilter.class);
//        FILTERS.put("玻璃球", GPUImageGlassSphereFilter.class);
//        FILTERS.put("灰度", GPUImageGrayscaleFilter.class);
//        FILTERS.put("硬光混合", GPUImageHardLightBlendFilter.class);
//        FILTERS.put("阴霾", GPUImageHazeFilter.class);
//        FILTERS.put("突出阴影", GPUImageHighlightShadowFilter.class);
//        FILTERS.put("颜色混合", GPUImageHueBlendFilter.class);
//        FILTERS.put("色调", GPUImageHueFilter.class);
//        FILTERS.put("桑原", GPUImageKuwaharaFilter.class);
//        FILTERS.put("拉普拉斯", GPUImageLaplacianFilter.class);
//        FILTERS.put("水平", GPUImageLevelsFilter.class);
//        FILTERS.put("减轻混合", GPUImageLightenBlendFilter.class);
//        FILTERS.put("线性混合", GPUImageLinearBurnBlendFilter.class);
//        FILTERS.put("查找", GPUImageLookupFilter.class);
//        FILTERS.put("亮度混合", GPUImageLuminosityBlendFilter.class);
//        FILTERS.put("混合交融", GPUImageMixBlendFilter.class);
//        FILTERS.put("黑白", GPUImageMonochromeFilter.class);
//        FILTERS.put("叠加的混合", GPUImageMultiplyBlendFilter.class);
//        FILTERS.put("非最大抑制", GPUImageNonMaximumSuppressionFilter.class);
//        FILTERS.put("正常混合", GPUImageNormalBlendFilter.class);
//        FILTERS.put("不透明度", GPUImageOpacityFilter.class);
//        FILTERS.put("叠加混合", GPUImageOverlayBlendFilter.class);
//        FILTERS.put("像素化", GPUImagePixelationFilter.class);
//        FILTERS.put("色调分离", GPUImagePosterizeFilter.class);
//        FILTERS.put("RGB扩张", GPUImageRGBDilationFilter.class);
//        FILTERS.put("RGB", GPUImageRGBFilter.class);
//        FILTERS.put("饱和混合", GPUImageSaturationBlendFilter.class);
//        FILTERS.put("饱和度", GPUImageSaturationFilter.class);
//        FILTERS.put("屏幕混合", GPUImageScreenBlendFilter.class);
//        FILTERS.put("深褐色", GPUImageSepiaFilter.class);
//        FILTERS.put("锐化", GPUImageSharpenFilter.class);
//        FILTERS.put("素描", GPUImageSketchFilter.class);
//        FILTERS.put("卡通", GPUImageToonFilter.class);
//        FILTERS.put("光滑的卡通", GPUImageSmoothToonFilter.class);
//        FILTERS.put("索贝尔阈值", GPUImageSobelThresholdFilter.class);
//        FILTERS.put("柔光", GPUImageSoftLightBlendFilter.class);
//        FILTERS.put("混合源", GPUImageSourceOverBlendFilter.class);
//        FILTERS.put("球面折射", GPUImageSphereRefractionFilter.class);
//        FILTERS.put("减法混合", GPUImageSubtractBlendFilter.class);
//        FILTERS.put("涡流", GPUImageSwirlFilter.class);
//        FILTERS.put("色调曲线", GPUImageToneCurveFilter.class);
//        FILTERS.put("晕影", GPUImageVignetteFilter.class);
//        FILTERS.put("弱像素包含", GPUImageWeakPixelInclusionFilter.class);
//        FILTERS.put("白平衡", GPUImageWhiteBalanceFilter.class);
    }

    public static Bitmap filterPhoto(Context context, Bitmap bitmap, String key) {
//        if (bitmap == null) {
//            return null;
//        }
//
//
//        // 实现图片过滤处理
//        GPUImageFilter filter = null;
//
//        if (!TextUtils.isEmpty(key)) {
//            try {
//                filter = FILTERS.get(key).newInstance();
//            } catch (Exception e) {
//            }
//        }
//
//        if (filter != null) {
//            GPUImage gpuImage = new GPUImage(context);
//            gpuImage.setFilter(filter);
//            bitmap = gpuImage.getBitmapWithFilterApplied(bitmap);
//            gpuImage.deleteImage();
//        }
        return bitmap;
    }

}

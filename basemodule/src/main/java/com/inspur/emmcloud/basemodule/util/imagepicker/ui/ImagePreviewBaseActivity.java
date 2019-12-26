package com.inspur.emmcloud.basemodule.util.imagepicker.ui;

import android.os.Bundle;
import android.view.View;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.util.imagepicker.ImagePicker;
import com.inspur.emmcloud.basemodule.util.imagepicker.adapter.ImagePageAdapter;
import com.inspur.emmcloud.basemodule.util.imagepicker.bean.ImageItem;
import com.inspur.emmcloud.basemodule.util.imagepicker.view.ViewPagerFixed;

import java.util.ArrayList;

public abstract class ImagePreviewBaseActivity extends ImageBaseActivity {

    protected ImagePicker imagePicker;
    protected ArrayList<ImageItem> mImageItems;      //跳转进ImagePreviewFragment的图片文件夹
    protected int mCurrentPosition = 0;              //跳转进ImagePreviewFragment时的序号，第几个图片
    protected ArrayList<ImageItem> selectedImages;   //所有已经选中的图片
    protected View content;
    protected View topBar;
    protected ViewPagerFixed mViewPager;
    protected ImagePageAdapter mAdapter;
    protected boolean isCanDelete = true;//是否显示删除按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        ImmersionBar.with(this).navigationBarColor(R.color.color_image_grid_header).init();
        mCurrentPosition = getIntent().getIntExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0);
        mImageItems = (ArrayList<ImageItem>) getIntent().getSerializableExtra(ImagePicker.EXTRA_IMAGE_ITEMS);
        imagePicker = ImagePicker.getInstance();
        selectedImages = imagePicker.getSelectedImages();
        isCanDelete = getIntent().getBooleanExtra("isCanDelete", true);
        //初始化控件
        content = findViewById(R.id.content);
        topBar = findViewById(R.id.top_bar);
        topBar.findViewById(R.id.tv_ok).setVisibility(View.GONE);
        topBar.findViewById(R.id.ibt_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImmersionBar.with(ImagePreviewBaseActivity.this).statusBarColor(R.color.color_image_grid_header).init();
                finish();
            }
        });


        mViewPager = (ViewPagerFixed) findViewById(R.id.viewpager);
        mAdapter = new ImagePageAdapter(this, mImageItems);
        mAdapter.setPhotoViewClickListener(new ImagePageAdapter.PhotoViewClickListener() {
            @Override
            public void OnPhotoTapListener(View view, float v, float v1) {
                onImageSingleTap();
            }
        });
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mCurrentPosition, false);

    }

    /**
     * 单击时，隐藏头和尾
     */
    public abstract void onImageSingleTap();
}
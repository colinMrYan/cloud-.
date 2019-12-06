package com.inspur.emmcloud.basemodule.util.imagepicker.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.util.imageedit.IMGEditActivity;
import com.inspur.emmcloud.basemodule.util.imagepicker.ImagePicker;
import com.inspur.emmcloud.basemodule.util.imagepicker.bean.ImageItem;
import com.inspur.emmcloud.basemodule.util.imagepicker.view.SuperCheckBox;

public class ImagePreviewActivity extends ImagePreviewBaseActivity implements
        ImagePicker.OnImageSelectedListener, View.OnClickListener {

    public static final String ISORIGIN = "isOrigin";
    public static final String EXTRA_ENCODING_TYPE = "IMAGE_ENCODING_TYPE";
    protected static final int REQ_IMAGE_EDIT = 1;
    private int encodingType = 0;
    private boolean isOrigin; // 是否选中原图
    private SuperCheckBox mCbCheck; // 是否选中当前图片的CheckBox
    private Button editBtn; // 原图
    private TextView OKText; // 确认图片的选择
    private View bottomBar;
    private AppCompatCheckBox originCheck;
    private View statusBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isOrigin = getIntent().getBooleanExtra(ImagePreviewActivity.ISORIGIN,
                false);
        imagePicker.addOnImageSelectedListener(this);
        OKText = (TextView) topBar.findViewById(R.id.tv_ok);
        OKText.setVisibility(View.VISIBLE);
        statusBarView = findViewById(R.id.v_status_bar);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) statusBarView.getLayoutParams();
        params.height = ImmersionBar.getStatusBarHeight(this);
        statusBarView.setLayoutParams(params);
        bottomBar = findViewById(R.id.bottom_bar);
        bottomBar.setVisibility(View.VISIBLE);
        originCheck = findViewById(R.id.cb_origin);
        mCbCheck = (SuperCheckBox) findViewById(R.id.cb_check);
        editBtn = (Button) findViewById(R.id.edit_btn);
        encodingType = getIntent().getIntExtra(EXTRA_ENCODING_TYPE, 0);
        editBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivityForResult(
                        new Intent(ImagePreviewActivity.this, IMGEditActivity.class)
                                .putExtra(IMGEditActivity.EXTRA_IMAGE_PATH, mImageItems.get(mCurrentPosition).path)
                                .putExtra(IMGEditActivity.EXTRA_ENCODING_TYPE, encodingType),
                        REQ_IMAGE_EDIT
                );
            }
        });
        originCheck.setVisibility(imagePicker.isSupportOrigin() ? View.VISIBLE : View.GONE);
        originCheck.setChecked(isOrigin);
        setEditBtnStatus(!isOrigin);
        // 初始化当前页面的状态
        onImageSelected(0, null, true);
        ImageItem item = mImageItems.get(mCurrentPosition);
        boolean isSelected = imagePicker.isSelect(item);
        LogUtils.jasonDebug("isSelected1=" + isSelected);
        mCbCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                LogUtils.jasonDebug("mCbCheck.setOnCheckedChangeListener="
                        + isChecked);
            }
        });
        mCbCheck.setChecked(isSelected);
        // 滑动ViewPager的时候，根据外界的数据改变当前的选中状态和当前的图片的位置描述文本
        mViewPager
                .addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        mCurrentPosition = position;
                        ImageItem item = mImageItems.get(mCurrentPosition);
                        boolean isSelected = imagePicker.isSelect(item);
                        setEditBtnStatus(isSelected);
                        mCbCheck.setChecked(isSelected);
                    }
                });
        // 当点击当前选中按钮的时候，需要根据当前的选中状态添加和移除图片
        mCbCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageItem imageItem = mImageItems.get(mCurrentPosition);
                int selectLimit = imagePicker.getSelectLimit();
                LogUtils.jasonDebug("mCbCheck.isChecked()="
                        + mCbCheck.isChecked());
                if (mCbCheck.isChecked()) {
                    // Toast.makeText(ImagePreviewActivity.this,
                    // ImagePreviewActivity.this.getString(R.string.select_limit,
                    // selectLimit), Toast.LENGTH_SHORT).show();
                    editBtn.setEnabled(true);
                } else {
                    mCbCheck.setChecked(false);
                }
                setEditBtnStatus(mCbCheck.isChecked());
                imagePicker.addSelectedImageItem(mCurrentPosition, imageItem,
                        mCbCheck.isChecked());
            }
        });
        originCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isOrigin = b;
                setEditBtnStatus(!isOrigin);
            }
        });
    }

    private void setEditBtnStatus(boolean isEnable) {
        editBtn.setTextColor(isEnable ? Color.parseColor("#ffffff") : Color.parseColor("#888888"));
        editBtn.setEnabled(isEnable);
    }

    /**
     * 图片添加成功后，修改当前图片的选中数量 当调用 addSelectedImageItem 或 deleteSelectedImageItem
     * 都会触发当前回调
     */
    @Override
    public void onImageSelected(int position, ImageItem item, boolean isAdd) {
        OKText.setText(getString(R.string.send));
        OKText.setEnabled(isAdd);
    }

    @Override
    public void onImageSelectedRelace(int position, ImageItem item) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_ok) {
            Intent intent = new Intent();
            intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS,
                    imagePicker.getSelectedImages());
            intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent);
            finish();
        } else if (id == R.id.ibt_back) {
            Intent intent = new Intent();
            intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
            setResult(ImagePicker.RESULT_CODE_BACK, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
        setResult(ImagePicker.RESULT_CODE_BACK, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        imagePicker.removeOnImageSelectedListener(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        // TODO Auto-generated method stub
        super.onActivityResult(arg0, arg1, arg2);
        if (arg1 == RESULT_OK && arg0 == REQ_IMAGE_EDIT) {
            String imgFilePath = arg2.getStringExtra(IMGEditActivity.OUT_FILE_PATH);
            mImageItems.get(mCurrentPosition).path = imgFilePath;
            imagePicker.getSelectedImages().get(mCurrentPosition).path = imgFilePath;
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 单击时，隐藏头和尾
     */
    @Override
    public void onImageSingleTap() {
        if (topBar.getVisibility() == View.VISIBLE) {
            // topBar.setAnimation(AnimationUtils.loadAnimation(this,
            // R.anim.top_out));
            // bottomBar.setAnimation(AnimationUtils.loadAnimation(this,
            // R.anim.fade_out));
            topBar.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_STATUS_BAR).navigationBarColor(R.color.color_image_grid_header).init();
            // tintManager.setStatusBarTintResource(R.color.transparent);//通知栏所需颜色
            // 给最外层布局加上这个属性表示，Activity全屏显示，且状态栏被隐藏覆盖掉。
            // if (Build.VERSION.SDK_INT >= 16)
            // content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            // topBar.setAnimation(AnimationUtils.loadAnimation(this,
            // R.anim.top_in));
            // bottomBar.setAnimation(AnimationUtils.loadAnimation(this,
            // R.anim.fade_in));
            ImmersionBar.with(this).hideBar(BarHide.FLAG_SHOW_BAR).init();
            topBar.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
            // tintManager.setStatusBarTintResource(R.color.status_bar);//通知栏所需颜色
            // Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住
            // if (Build.VERSION.SDK_INT >= 16)
            // content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

}

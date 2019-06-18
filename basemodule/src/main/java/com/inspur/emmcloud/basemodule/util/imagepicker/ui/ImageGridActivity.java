package com.inspur.emmcloud.basemodule.util.imagepicker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.util.imageedit.IMGEditActivity;
import com.inspur.emmcloud.basemodule.util.imagepicker.ImageDataSource;
import com.inspur.emmcloud.basemodule.util.imagepicker.ImagePicker;
import com.inspur.emmcloud.basemodule.util.imagepicker.adapter.ImageFolderAdapter;
import com.inspur.emmcloud.basemodule.util.imagepicker.adapter.ImageGridAdapter;
import com.inspur.emmcloud.basemodule.util.imagepicker.bean.ImageFolder;
import com.inspur.emmcloud.basemodule.util.imagepicker.bean.ImageItem;
import com.inspur.emmcloud.basemodule.util.imagepicker.view.FolderPopUpWindow;
import com.inspur.emmcloud.basemodule.util.mycamera.MyCameraActivity;

import java.text.DecimalFormat;
import java.util.List;


public class ImageGridActivity extends ImageBaseActivity implements
        ImageDataSource.OnImagesLoadedListener,
        ImageGridAdapter.OnImageItemClickListener,
        ImagePicker.OnImageSelectedListener {

    public static final int REQUEST_PERMISSION_STORAGE = 0x01;
    public static final int REQUEST_PERMISSION_CAMERA = 0x02;
    public static final String EXTRA_ENCODING_TYPE = "IMAGE_ENCODING_TYPE";
    public static final String EXTRA_ORIGINAL_PICTURE = "ORIGINAL_PICTURE";
    protected static final int CUT_IMG_SUCCESS = 1;
    private int encodingType = 0;
    private ImagePicker imagePicker;

    private boolean isOrigin = false; // 是否选中原图
    private GridView mGridView; // 图片展示控件
    private View mFooterBar; // 底部栏
    private TextView OkText; // 确定按钮
    private Button mBtnDir; // 文件夹切换按钮
    private Button mBtnPre; // 预览按钮
    private SuperCheckBox orgPictureCheckBox;
    //private Button mBtnEdit;
    private ImageFolderAdapter mImageFolderAdapter; // 图片文件夹的适配器
    private FolderPopUpWindow mFolderPopupWindow; // ImageSet的PopupWindow
    private List<ImageFolder> mImageFolders; // 所有的图片文件夹
    private ImageGridAdapter mImageGridAdapter; // 图片九宫格展示的适配器
    private ImageDataSource imageDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid);
        // hideBars();
        imagePicker = ImagePicker.getInstance();
        imagePicker.clear();
        imagePicker.addOnImageSelectedListener(this);
        OkText = (TextView) findViewById(R.id.tv_ok);
        mBtnDir = (Button) findViewById(R.id.btn_dir);
        mBtnPre = (Button) findViewById(R.id.btn_preview);
        mGridView = (GridView) findViewById(R.id.gridview);
        orgPictureCheckBox = findViewById(R.id.cb_origin);
        mFooterBar = findViewById(R.id.footer_bar);
        OkText.setVisibility(imagePicker.isMultiMode() ? View.VISIBLE : View.GONE);
        mBtnPre.setVisibility(imagePicker.isMultiMode() ? View.VISIBLE : View.GONE);
        mImageGridAdapter = new ImageGridAdapter(this, null);
        mImageFolderAdapter = new ImageFolderAdapter(this, null);
        onImageSelected(0, null, false);
        imageDataSource = new ImageDataSource(this, null, this);
        encodingType = getIntent().getIntExtra(EXTRA_ENCODING_TYPE, 0);
        setStatus();
    }


    @Override
    protected void onDestroy() {
        imagePicker.removeOnImageSelectedListener(this);
        super.onDestroy();
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_ok) {
            returnDataAndClose();
        } else if (id == R.id.btn_dir) {
            if (mImageFolders == null) {
                Log.i("ImageGridActivity", "您的手机没有图片");
                return;
            }
            // 点击文件夹按钮
            createPopupFolderList();
            mImageFolderAdapter.refreshData(mImageFolders); // 刷新数据
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.showAtLocation(mFooterBar,
                        Gravity.NO_GRAVITY, 0, 0);
                // 默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
                int index = mImageFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.setSelection(index);
            }
        } else if (id == R.id.btn_preview) {
            Intent intent = new Intent(ImageGridActivity.this, ImagePreviewActivity.class);
            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, imagePicker.getSelectedImages());
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);
        } else if (id == R.id.ibt_back) {
            // 点击返回按钮
            finish();
        }
    }

    private void returnDataAndClose() {
        Intent intent = new Intent();
        intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS,
                imagePicker.getSelectedImages());
        intent.putExtra(EXTRA_ORIGINAL_PICTURE, orgPictureCheckBox.isChecked());
        setResult(ImagePicker.RESULT_CODE_ITEMS, intent); // 多选不允许裁剪裁剪，返回数据
        finish();
    }

    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList() {
        mFolderPopupWindow = new FolderPopUpWindow(this, mImageFolderAdapter);
        mFolderPopupWindow
                .setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView,
                                            View view, int position, long l) {
                        mImageFolderAdapter.setSelectIndex(position);
                        imagePicker.setCurrentImageFolderPosition(position);
                        mFolderPopupWindow.dismiss();
                        ImageFolder imageFolder = (ImageFolder) adapterView
                                .getAdapter().getItem(position);
                        if (null != imageFolder) {
                            mImageGridAdapter.refreshData(imageFolder.images);
                            mBtnDir.setText(imageFolder.name);
                        }
                        mGridView.smoothScrollToPosition(0);// 滑动到顶部
                    }
                });
        mFolderPopupWindow.setMargin(mFooterBar.getHeight());
    }

    @Override
    public void onImagesLoaded(List<ImageFolder> imageFolders) {
        this.mImageFolders = imageFolders;
        imagePicker.setImageFolders(imageFolders);
        if (imageFolders.size() == 0)
            mImageGridAdapter.refreshData(null);
        else
            mImageGridAdapter.refreshData(imageFolders.get(0).images);
        mImageGridAdapter.setOnImageItemClickListener(this);
        mGridView.setAdapter(mImageGridAdapter);
        mImageFolderAdapter.refreshData(imageFolders);

    }

    @Override
    public void onImageItemClick(View view, ImageItem imageItem, int position) {
        // 根据是否有相机按钮确定位置
        position = imagePicker.isShowCamera() ? position - 1 : position;
        if (imagePicker.isMultiMode()) {
            int selectLimit = imagePicker.getSelectLimit();
            boolean isCheck = imagePicker.getSelectedImages().contains(imageItem);
            if (!isCheck && imagePicker.getSelectedImages().size() >= selectLimit) {
                ToastUtils.show(getApplicationContext(), getString(R.string.select_limit, selectLimit));
            } else {
                imagePicker.addSelectedImageItem(position, imageItem, !isCheck);
            }
            mImageGridAdapter.notifyDataSetChanged();
        } else {
            imagePicker.clearSelectedImages();
            imagePicker.addSelectedImageItem(position, imagePicker
                    .getCurrentImageFolderItems().get(position), true);
            if (imagePicker.isCrop()) {
                Intent intent = new Intent(ImageGridActivity.this,
                        ImageCropActivity.class);
                startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP); // 单选需要裁剪，进入裁剪界面
            } else {
                startActivityForResult(
                        new Intent(ImageGridActivity.this, IMGEditActivity.class)
                                .putExtra(IMGEditActivity.EXTRA_IMAGE_PATH, imagePicker.getSelectedImages().get(0).path)
                                .putExtra(IMGEditActivity.EXTRA_ENCODING_TYPE, encodingType),
                        ImagePicker.REQUEST_CODE_EDIT
                );

            }
        }
    }

    @Override
    public void onImageSelected(int position, ImageItem item, boolean isAdd) {
        if (imagePicker.getSelectImageCount() > 0) {
            OkText.setText(getString(R.string.select_complete,
                    imagePicker.getSelectImageCount(),
                    imagePicker.getSelectLimit()));
            OkText.setEnabled(true);
        } else {
            OkText.setText(getString(R.string.complete));
            OkText.setEnabled(false);
        }

        if (imagePicker.getSelectImageCount() == 1 && imagePicker.isMultiMode()) {
            mBtnPre.setVisibility(View.VISIBLE);
        } else {
            mBtnPre.setVisibility(View.GONE);
        }
        mImageGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onImageSelectedRelace(int position, ImageItem imageItem) {
        // TODO Auto-generated method stub
        mImageGridAdapter.replaceData(position, imageItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK
                && requestCode == ImagePicker.REQUEST_CODE_TAKE) {
            ImageItem imageItem = new ImageItem();
            imageItem.path = data.getExtras().getString(MyCameraActivity.OUT_FILE_PATH, "");
            // 发送广播通知图片增加了
            ImagePicker.galleryAddPic(this, imagePicker.getTakeImageFile());
            imagePicker.clearSelectedImages();
            imagePicker.addSelectedImageItem(0, imageItem, true);
            if (imagePicker.isCrop()) {
                Intent intent = new Intent(ImageGridActivity.this,
                        ImageCropActivity.class);
                startActivityForResult(intent,
                        ImagePicker.REQUEST_CODE_CROP); // 单选需要裁剪，进入裁剪界面
            } else {
                Intent intent = new Intent();
                intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS,
                        imagePicker.getSelectedImages());
                setResult(ImagePicker.RESULT_CODE_ITEMS, intent); // 单选不需要裁剪，返回数据
                finish();
            }
        } else if (data != null) {
            if (requestCode == ImagePicker.REQUEST_CODE_EDIT) {// 说明是从裁剪页面过来的数据，直接返回就可以
                if (resultCode == RESULT_OK) {
                    String newPath = data.getStringExtra(
                            IMGEditActivity.OUT_FILE_PATH);
                    imagePicker.getSelectedImages().get(0).path = newPath;
                    Intent intent = new Intent();
                    intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS,
                            imagePicker.getSelectedImages());
                    setResult(ImagePicker.RESULT_CODE_ITEMS, intent);
                    finish();
                }
            } else {
                if (resultCode == ImagePicker.RESULT_CODE_BACK) {
                    isOrigin = data.getBooleanExtra(
                            ImagePreviewActivity.ISORIGIN, false);
                } else {
                    // 从拍照界面返回
                    // 点击 X , 没有选择照片
                    if (data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS) == null) {
                        // 什么都不做
                    } else {
                        // 说明是从裁剪页面过来的数据，直接返回就可以
                        setResult(ImagePicker.RESULT_CODE_ITEMS, data);
                        finish();
                    }
                }
            }
        }
    }

}
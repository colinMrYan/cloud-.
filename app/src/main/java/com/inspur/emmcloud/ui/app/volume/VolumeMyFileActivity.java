package com.inspur.emmcloud.ui.app.volume;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.adapter.VolumeFileFilterPopGridAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileListResult;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileUploadSTSTokenResult;
import com.inspur.emmcloud.bean.Volume.Volume;
import com.inspur.emmcloud.bean.Volume.VolumeFile;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.InputMethodUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * 云盘-我的文件
 */

@ContentView(R.layout.activity_volume_my_file)
public class VolumeMyFileActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{

    private static final int REQUEST_OPEN_GALLERY = 3;
    private static final int REQUEST_OPEN_FILE_BROWSER = 4;

    @ViewInject(R.id.header_text)
    private TextView headerText;

    @ViewInject(R.id.operation_sort_text)
    private TextView operationSortText;

    @ViewInject(R.id.file_list)
    private RecyclerView fileRecycleView;

    @ViewInject(R.id.refresh_layout)
    private SwipeRefreshLayout swipeRefreshLayout;

    @ViewInject(R.id.data_blank_layout)
    private LinearLayout dataBlankLayout;

    @ViewInject(R.id.batch_operation_bar_layout)
    private RelativeLayout batchOperationBarLayout;

    @ViewInject(R.id.batch_operation_header_layout)
    private RelativeLayout batchOprationHeaderLayout;


    private PopupWindow sortOperationPop;
    private LoadingDialog loadingDlg;
    private boolean isSortByTime = true;
    private TextView sortByTimeText, sortByNameText;
    private ImageView sortByTimeSelectImg, sortByNameSelectImg;
    private VolumeFileAdapter adapter;
    private List<VolumeFile> volumeFileList = new ArrayList<>();
    private Volume volume;
    private MyAppAPIService apiService;
    private String subPath= "/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        getVolumeFileList();

    }
    private void initView(){
        loadingDlg = new LoadingDialog(this);
        apiService = new MyAppAPIService(VolumeMyFileActivity.this);
        apiService.setAPIInterface(new NetService());
        volume =(Volume)getIntent().getSerializableExtra("volume");
        boolean isMyPrivate =volume.getType().equals("private");
        headerText.setText(isMyPrivate?"我的文件":volume.getName());
        initRecycleView();
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecycleView(){
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg), getResources().getColor(R.color.header_bg));
        swipeRefreshLayout.setOnRefreshListener(this);
        fileRecycleView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VolumeFileAdapter(this,volumeFileList);
        fileRecycleView.setAdapter(adapter);
        adapter.setItemClickListener(new VolumeFileAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        });
        adapter.setItemDropDownImgClickListener(new VolumeFileAdapter.MyItemDropDownImgClickListener() {
            @Override
            public void onItemDropDownImgClick(View view, int position) {
                showFileOperationDlg("办公文件");
            }
        });
    }

    /**
     * 弹出文件操作框
     * @param title
     */
    private void showFileOperationDlg(String title){
        new ActionSheetDialog.ActionListSheetBuilder(VolumeMyFileActivity.this)
                .setTitle("我的文件")
                .addItem("删除")
                .addItem("下载")
                .addItem("重命名")
                .addItem("移动到")
                .addItem("复制")
                .addItem("分享")
               .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener(){
                   @Override
                   public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                       switch (position){
                           case 0:
                               showFileDelWranibgDlg();
                               break;
                           case 2:
                               break;
                           default:
                               break;
                       }
                       dialog.dismiss();
                   }
               })
        .build()
        .show();
    }

    /**
     * 弹出选择上传文件提示框
     */
    private void showUploadFileDlg(){
        new ActionSheetDialog.ActionListSheetBuilder(VolumeMyFileActivity.this)
                .addItem("拍照")
                .addItem("选择照片")
                .addItem("选择文件")
                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener(){
                    @Override
                    public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                        switch (position){
                            case 0:
                                break;
                            case 1:
                                AppUtils.openGallery(VolumeMyFileActivity.this,1,REQUEST_OPEN_GALLERY);
                                break;
                            case 2:
                                AppUtils.openFileSystem(VolumeMyFileActivity.this,REQUEST_OPEN_FILE_BROWSER);
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .build()
                .show();
    }


    /**
     * 弹出文件删除提示框
     */
    private void showFileDelWranibgDlg(){
        new MyQMUIDialog.MessageDialogBuilder(VolumeMyFileActivity.this)
                .setMessage("确定要删除所选文件吗？")
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showNewFolderDlg(){

        final Dialog dialog = new MyDialog(VolumeMyFileActivity.this,
                R.layout.dialog_my_app_approval_password_input,R.style.userhead_dialog_bg);
        dialog.setCancelable(false);
        final EditText inputEdit = (EditText) dialog.findViewById(R.id.edit);
        inputEdit.setHint("请输入文件夹名称");
        inputEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        ((TextView) dialog.findViewById(R.id.app_update_title)).setText("新建文件夹");
        Button okBtn = (Button) dialog.findViewById(R.id.ok_btn);
        okBtn.setText("新建");
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dirName = inputEdit.getText().toString();
                if (StringUtils.isBlank(dirName)) {
                    ToastUtils.show(getApplicationContext(), "请输入文件夹名称");
                }else{
                    dialog.dismiss();
                }
            }
        });

        (dialog.findViewById(R.id.cancel_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        InputMethodUtils.display(VolumeMyFileActivity.this,inputEdit);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.new_forder_img:
                showNewFolderDlg();
                break;
            case R.id.upload_btn:
            case R.id.upload_img:
                showUploadFileDlg();
                break;
            case R.id.operation_sort_text:
                showSortOperationPop();
                break;
            case R.id.operation_multiselect_text:
                setMutiselect(true);
                break;
            case R.id.operation_filter_text:
                showFileFilterPop(v);
                break;
            case R.id.sort_by_time_layout:
                isSortByTime = true;
                sortOperationPop.dismiss();
                break;
            case R.id.sort_by_name_layout:
                isSortByTime = false;
                sortOperationPop.dismiss();
                break;
            case R.id.batch_operation_delete_text:
                break;
            case R.id.batch_operation_copy_text:
                break;
            case R.id.batch_operation_move_text:
                break;
            case R.id.batch_operation_cancel_text:
                setMutiselect(false);
                break;
            case R.id.batch_operation_select_all_text:
                break;

            default:
                break;
        }
    }

    /**
     * 弹出文件排序选择框
     */
    private void showSortOperationPop() {
        View contentView = LayoutInflater.from(VolumeMyFileActivity.this)
                .inflate(R.layout.app_volume_file_sort_operation_pop, null);
        sortOperationPop = new PopupWindow(contentView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        sortByTimeText = (TextView) contentView.findViewById(R.id.sort_by_time_text);
        sortByNameText = (TextView) contentView.findViewById(R.id.sort_by_name_text);
        sortByTimeSelectImg = (ImageView) contentView.findViewById(R.id.sort_by_time_select_img);
        sortByNameSelectImg = (ImageView) contentView.findViewById(R.id.sort_by_name_select_img);
        sortByTimeText.setTextColor(Color.parseColor(isSortByTime?"#2586CD":"#666666"));
        sortByNameText.setTextColor(Color.parseColor(isSortByTime?"#666666":"#2586CD"));
        sortByTimeSelectImg.setVisibility(isSortByTime?View.VISIBLE:View.INVISIBLE);
        sortByNameSelectImg.setVisibility(isSortByTime?View.INVISIBLE:View.VISIBLE);
        sortOperationPop.setTouchable(true);
        sortOperationPop.setBackgroundDrawable(ContextCompat.getDrawable(
                getApplicationContext(), R.drawable.pop_window_view_tran));
        sortOperationPop.setOutsideTouchable(true);
        sortOperationPop.showAsDropDown(operationSortText);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_volume_menu_drop_up);
        drawable.setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(),14), DensityUtil.dip2px(getApplicationContext(),14));
        operationSortText.setCompoundDrawables(null,null,drawable,null);
        sortOperationPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                operationSortText.setText(isSortByTime?"时间排序":"名称排序");
                Drawable drawable1 = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_volume_menu_drop_down);
                drawable1.setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(),14), DensityUtil.dip2px(getApplicationContext(),14));
                operationSortText.setCompoundDrawables(null,null,drawable1,null);
            }
        });

    }

    /**
     * 弹出文件筛选框
     */
    private void showFileFilterPop(View v){
        View contentView = LayoutInflater.from(VolumeMyFileActivity.this)
                .inflate(R.layout.app_volume_file_filter_pop, null);
        final PopupWindow fileFilterPop = new PopupWindow(contentView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        GridView fileFilterGrid = (GridView)contentView.findViewById(R.id.file_filter_type_grid);
        fileFilterGrid.setAdapter(new VolumeFileFilterPopGridAdapter(VolumeMyFileActivity.this));
        fileFilterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fileFilterPop.dismiss();
            }
        });
        fileFilterPop.setTouchable(true);
        fileFilterPop.setBackgroundDrawable(ContextCompat.getDrawable(
                getApplicationContext(), R.drawable.pop_window_view_tran));
        fileFilterPop.setOutsideTouchable(true);
        fileFilterPop.showAsDropDown(v);
    }

    /**
     * 设置是否是多选状态
     * @param isMutiselect
     */
    private void setMutiselect(boolean isMutiselect){
        batchOperationBarLayout.setVisibility(isMutiselect?View.VISIBLE:View.GONE);
        batchOprationHeaderLayout.setVisibility(isMutiselect?View.VISIBLE:View.GONE);
        adapter.setMultiselect(isMutiselect);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_OPEN_FILE_BROWSER){
                Uri uri = data.getData();
                String filePath = GetPathFromUri4kitkat.getPathByUri(getApplicationContext(), uri);
                File file =new File(filePath);
                getVolumeFileUploadSTSToken(file);
            }
        }

    }

    /**
     * 获取云盘上传STS Token
     * @param file
     */
    private void getVolumeFileUploadSTSToken(File file){
        if(NetUtils.isNetworkConnected(getApplicationContext())){
            loadingDlg.show();
            String volumeFilePath = subPath+file.getName();
            apiService.getVolumeFileUploadSTSToken(volume.getId(),file.getName(),volumeFilePath);
        }
    }

    @Override
    public void onRefresh() {

    }

    private void getVolumeFileList(){
        if (NetUtils.isNetworkConnected(getApplicationContext())){
            loadingDlg.show();
            apiService.getVolumeFileList(volume.getId(),subPath);
        }
    }

    private class NetService extends APIInterfaceInstance{
        @Override
        public void returnVolumeFileListSuccess(GetVolumeFileListResult getVolumeFileListResult) {
            if (loadingDlg != null && loadingDlg.isShowing()){
                loadingDlg.dismiss();
            }
            volumeFileList = getVolumeFileListResult.getVolumeFileList();
            dataBlankLayout.setVisibility((volumeFileList.size() == 0)?View.VISIBLE:View.GONE);
        }

        @Override
        public void returnVolumeFileListFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()){
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(),error,errorCode);
//            VolumeFile volumeFile = new VolumeFile();
//            volumeFile.setName("办公然间.jpg");
//            volumeFile.setCreationDate(System.currentTimeMillis());
//            volumeFile.setSize(1024*1024*23);
//            volumeFileList.add(volumeFile);
//
//            VolumeFile volumeFile2 = new VolumeFile();
//            volumeFile2.setName("办公文档.docx");
//            volumeFile2.setCreationDate(System.currentTimeMillis());
//            volumeFile2.setSize(1024*1024*5);
//            volumeFileList.add(volumeFile2);
//            adapter.notifyDataSetChanged();
//            dataBlankLayout.setVisibility((volumeFileList.size() == 0)?View.VISIBLE:View.GONE);
        }

        @Override
        public void returnVolumeFileUploadSTSTokenSuccess(GetVolumeFileUploadSTSTokenResult getVolumeFileUploadSTSTokenResult) {
            super.returnVolumeFileUploadSTSTokenSuccess(getVolumeFileUploadSTSTokenResult);
        }

        @Override
        public void returnVolumeFileUploadSTSTokenFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()){
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(),error,errorCode);
        }
    }
}

package com.inspur.imp.plugin.file;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.basemodule.util.Res;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件浏览类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class FileBrowerAppActivity extends ListActivity {

    private static final String TAG = "FileBrowerDemo";
    //上下文菜单项
    private final int MENU_RENAME = Menu.FIRST;
    private final int MENU_COPY = Menu.FIRST + 1;
    private final int MENU_MOVE = Menu.FIRST + 2;
    private final int MENU_DELETE = Menu.FIRST + 3;
    private final int MENU_INFO = Menu.FIRST + 4;
    //按钮点击监听,关闭当前页面
    OnClickListener canclebutton_click = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            finish();
        }

    };
    private TextView _filePath;
    private List<FileInfo> _files = new ArrayList<FileInfo>();
    private String _rootPath = FileUtil.getSDPath();//默认的为sd卡根目录
    private String _currentPath = _rootPath;//当前工作目录
    private BaseAdapter adapter = null;//适配器
    //返回按钮
    private Button backBtn;
    //传递给页面intent
    private Intent intent;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        setContentView(Res.getLayoutID("plugin_file_brower"));
        this.setTitle("文件浏览器");
        this.setTitleColor(Color.WHITE);
        _filePath = (TextView) findViewById(Res.getWidgetID("file_path"));

        adapter = new FileListViewAdapter(this, _files);
        setListAdapter(adapter);
        backBtn = (Button) findViewById(Res.getWidgetID("back"));
        backBtn.setOnClickListener(canclebutton_click);
        // 获取当前目录的文件列表
        viewFiles(_currentPath);
    }

    /**
     * 获取该目录下所有文件
     **/
    public void viewFiles(String filePath) {
        ArrayList<FileInfo> tmp = FileActivityHelper.getFiles(this, filePath);
        if (tmp != null) {
            // 清空数据
            _files.clear();
            _files.addAll(tmp);
            tmp.clear();

            // 设置当前目录
            _currentPath = filePath;
            _filePath.setText(filePath);

            // 更新UI刷新
            adapter.notifyDataSetChanged();
        }
    }


    /**
     * 覆盖重写返回键事件
     **/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {//back键
            File f = new File(_currentPath);
            String parentPath = f.getParent();
            if (parentPath != null) {
                viewFiles(parentPath);//返回上一级
            } else {
                this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // ListView点击事件即每一行被点击事件处理
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        FileInfo f = _files.get(position);

        if (f.canRead) {
            if (f.IsDirectory) {//判断是否为目录
                Log.i(TAG, "onListItemClick======== f.Path=" + f.Path);
                viewFiles(f.Path);
            } else {
                intent.putExtra("filePath", f.Path + "");
                setResult(4, intent);
                finish();
            }
        } else {
            //不可读
            showFileCanNOTReadMyDialog();
        }

    }

    //显示不可读对话框
    public void showFileCanNOTReadMyDialog() {
        new AlertDialog.Builder(FileBrowerAppActivity.this, AlertDialog.THEME_HOLO_LIGHT)
                .setIcon(Res.getDrawable("plugin_file_dialogtitle_icon"))
                .setMessage(getResources().getString(Res.getStringID("nopermission")))
                .setTitle(getResources().getString(Res.getStringID("dialogtitle")))
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

}
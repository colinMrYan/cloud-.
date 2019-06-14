package com.inspur.emmcloud.web.plugin.file;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.basemodule.util.Res;

import java.util.List;

/**
 * 文件列表适配器类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class FileListViewAdapter extends BaseAdapter {

    private Context con;
    private List<FileInfo> _files;

    public FileListViewAdapter(Context con, List<FileInfo> _files) {
        this.con = con;
        this._files = _files;
    }

    @Override
    public int getCount() {
        return _files.size();
    }

    @Override
    public Object getItem(int arg0) {
        return _files.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(con).inflate(Res.getLayoutID("plugin_file_browser_item"), null);
            viewHolder.imageView = (ImageView) convertView.findViewById(Res.getWidgetID("file_icon"));
            viewHolder.text = (TextView) convertView.findViewById(Res.getWidgetID("file_name"));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //判断文件类型并设置图标
        String name = _files.get(position).Name.toLowerCase().trim();
        //	Log.i("==========name", "name=="+name);
        if (_files.get(position).IsDirectory) {
            viewHolder.imageView.setImageResource(Res.getDrawableID("plugin_file_folder"));
        } else if (name.endsWith(".apk")) {
            viewHolder.imageView.setImageResource(Res.getDrawableID("plugin_file_apkicon"));
        } else if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".3gp")
                || name.endsWith(".rmvb")) {
            viewHolder.imageView.setImageResource(Res.getDrawableID("plugin_file_video"));
        } else if (name.endsWith(".mp3") || name.endsWith(".mid") || name.endsWith(".wav")) {
            viewHolder.imageView.setImageResource(Res.getDrawableID("plugin_file_audio"));
        } else if (name.endsWith(".jpg") || name.endsWith(".gif") || name.endsWith(".png")
                || name.endsWith(".jpeg") || name.endsWith(".bmp")) {
            viewHolder.imageView.setImageResource(Res.getDrawableID("plugin_file_image"));
        } else if (name.endsWith(".txt") || name.endsWith(".log")) {
            viewHolder.imageView.setImageResource(Res.getDrawableID("plugin_file_text"));
        } else {
            viewHolder.imageView.setImageResource(Res.getDrawableID("plugin_file_doc"));
        }

        viewHolder.text.setText(_files.get(position).Name);

        return convertView;
    }

    /**
     * 存放列表项控件句柄
     */
    private class ViewHolder {
        public ImageView imageView;
        public TextView text;
    }


}

package com.inspur.emmcloud.web.plugin.file;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.inspur.emmcloud.web.ui.iLog;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class FileHelper {

    private static final String LOG_TAG = "FileUtils";
    private static final String _DATA = "_data";

    /*
     * @return 返回给定字符串格式的uri的文件真实路径 返回文件的真实路径
     * 如果文件的uri是以content://开头的，那么证明真正的路径是从媒体存储中检索
     *
     * @param uriString 已知的文件uri地址(字符串格式)
     * @param context 调用方法的应用上下文
     */
    @SuppressWarnings("deprecation")
    public static String getRealPath(String uriString, Context context) {
        String realPath = null;
        if (uriString.startsWith("content://")) {
            String[] proj = {_DATA};
            // 调用了managedQuery，将自动对查询的cursor进行管理，不需要特别关闭
            Cursor cursor = ((Activity) context).managedQuery(
                    Uri.parse(uriString), proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(_DATA);
            cursor.moveToFirst();
            realPath = cursor.getString(column_index);
            if (realPath == null) {
                iLog.e(LOG_TAG, "Could get real path for URI string %s",
                        uriString);
            }
        } else if (uriString.startsWith("file://")) {
            // 从file://后面的第7个字符开始提取字符串
            realPath = uriString.substring(7);
            // 如果文件的uri指向一个asset中文件，则挑出，不做处理
            if (realPath.startsWith("/android_asset/")) {
                iLog.e(LOG_TAG,
                        "Cannot get real path for URI string %s because it is a file:///android_asset/ URI.",
                        uriString);
                realPath = null;
            }
        } else {
            realPath = uriString;
        }
        return realPath;
    }

    /*
     * @return 返回一个uri 的真实路径 返回文件的真实路径
     * 如果文件的uri是以content://开头的，那么证明真正的路径是从媒体存储中检索
     *
     * @param uri 已知的文件uri地址
     * @param context 调用方法的应用上下文
     */
    public static String getRealPath(Uri uri, Context context) {
        // 将uri转换为字符串，重新调用getRealPath方法
        return FileHelper.getRealPath(uri.toString(), context);
    }

    /*
     * @return 从已知的uri字符串获得一个输入流
     *
     * @param uriString uri字符串
     * @param context 调用方法的应用上下文
     *
     * @throw IOException
     */
    public static InputStream getInputStreamFromUriString(String uriString,
                                                          Context context) throws IOException {
        if (uriString.startsWith("content")) {
            Uri uri = Uri.parse(uriString);
            return context.getContentResolver().openInputStream(uri);
        } else if (uriString.startsWith("file://")) {
            int question = uriString.indexOf('?');
            if (question > -1) {
                uriString = uriString.substring(0, question);
            }
            if (uriString.startsWith("file:///android_asset/")) {
                Uri uri = Uri.parse(uriString);
                String relativePath = uri.getPath().substring(15);
                return context.getAssets().open(relativePath);
            } else {
                return new FileInputStream(getRealPath(uriString, context));
            }
        } else {
            return new FileInputStream(getRealPath(uriString, context));
        }
    }

    /*
     * @description 如果uri的字符串是以file://开头的
     * 如果字符串没有以file开头，那么久直接返回
     *
     * @param uriString  uri的字符串
     *
     * @return 返回一个没有file://开头的uri字符串
     */
    public static String stripFileProtocol(String uriString) {
        if (uriString.startsWith("file://")) {
            uriString = uriString.substring(7);
        }
        return uriString;
    }
    /*
     * @description 在安卓的目录下有一个MIMEType来管理mimeType可以读取的文件类型，如text/plain即处理文件.txt格式的文件
	 * 
	 * @param path传入可用的路径，解析出相应的扩展名
	 * 
	 * @return得到可扩展的所有MimeType
	 */

    public static String getMimeTypeForExtension(String path) {
        String extension = path;
        int lastDot = extension.lastIndexOf('.');
        if (lastDot != -1) {
            extension = extension.substring(lastDot + 1);
        }
        // 得到可用的文件扩展名信息,修改原先的3ga至3gp媒体格式
        extension = extension.toLowerCase(Locale.getDefault());
        if (extension.equals("3ga")) {
            return "audio/3gpp";
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    /*
     * @description 得到提供的uri的资源的mimeType
     *
     * @param uriString 数据的uri
     * @param context   调用方法对象的上下文
     *
     * @return 获得传入资源的MimeType
     */
    public static String getMimeType(String uriString, Context context) {
        String mimeType = null;

        Uri uri = Uri.parse(uriString);
        if (uriString.startsWith("content://")) {
            mimeType = context.getContentResolver().getType(uri);
        } else {
            mimeType = getMimeTypeForExtension(uri.getPath());
        }

        return mimeType;
    }
}

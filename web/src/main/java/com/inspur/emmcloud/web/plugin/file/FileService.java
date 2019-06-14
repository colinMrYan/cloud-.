package com.inspur.emmcloud.web.plugin.file;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.ui.ImpFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * 文件管理
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
@SuppressLint({"SdCardPath", "NewApi"})
public class FileService extends ImpPlugin {

    public final static int MESSAGE_TYPE_STRING = 0;
    public final static int MESSAGE_TYPE_ARRAYBUFFER = 1;
    public final static int MESSAGE_TYPE_BINARYSTRING = 2;
    public static int TEMPORARY = 0;
    public static int PERSISTENT = 1;
    public static FileService fileService;
    public static String sdpath = Environment.getExternalStorageDirectory()
            + "";
    private static String callback;
    private String fileName;
    private String data;
    private String temp;
    private String filePath;
    private String newParent;
    private String newName;
    private String funct;
    private String fileInfo;
    private String success = "删除成功";
    private String failed = "文件不存在或者不能被删除";
    private int start;
    private int end;
    private int resultType;
    private int type;
    private long size;
    private boolean isBinary;
    private boolean isDirectory;
    private boolean move;

    /**
     * 讲一个文件改成Json格式表示的数据
     *
     * @param file 待转换的文件
     * @return Json对象代表原来的文件
     * @throws JSONException
     */
    public static JSONObject getEntry(File file) throws JSONException {
        JSONObject entry = new JSONObject();

        entry.put("isFile", file.isFile());
        entry.put("isDirectory", file.isDirectory());
        entry.put("name", file.getName());
        entry.put("fullPath", "file://" + file.getAbsolutePath());
        return entry;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        if ("getFileInfo".equals(action)) {
            try {
                fileInfo = getFileInfo(paramsObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            showCallIMPMethodErrorDlg();
        }
        return fileInfo;
    }

    @Override
    public void execute(String action, JSONObject paramsObject) {
        // 读入文件流
        if ("readAsText".equals(action)) {
            try {
                readFileAs(paramsObject);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 写文件
        else if ("write".equals(action)) {
            try {
                write(paramsObject);
            } catch (NoModificationAllowedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 得到一个可以存储应用文件的目录
        else if ("requestFileSystem".equals(action)) {
            try {
                requestFileSystem(paramsObject);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // getMetadata获取文件夹元数据
        else if ("getMetadata".equals(action)) {
            try {
                getMetadata(paramsObject);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 获取文件元数据
        else if ("getFileMetadata".equals(action)) {
            try {
                getFileMetadata(paramsObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 获取文件的上级目录
        else if ("getParent".equals(action)) {
            try {
                getParent(paramsObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 获取或者创建一个文件或者文件夹
        else if ("getDirectoryOrFile".equals(action)) {
            try {
                getFile(paramsObject);
            } catch (FileExistsException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TypeMismatchException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 删除文件
        else if ("remove".equals(action)) {
            try {
                remove(paramsObject);
            } catch (NoModificationAllowedException e) {
                e.printStackTrace();
            } catch (InvalidModificationException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 递归删除一个文件夹以及其子文件
        else if ("removeRecursively".equals(action)) {
            try {
                removeRecursively(paramsObject);
            } catch (FileExistsException e) {
                e.printStackTrace();
            }
        }
        // 拷贝一个文件到目标文件夹下
        else if ("copyTo".equals(action)) {
            try {
                transferTo(paramsObject);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (EncodingException e) {
                e.printStackTrace();
            } catch (InvalidModificationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoModificationAllowedException e) {
                e.printStackTrace();
            } catch (FileExistsException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 获取一个路径下所有文件及文件夹的信息
        else if ("readEntries".equals(action)) {
            try {
                readEntries(paramsObject);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 打开存储卡浏览文件
        else if ("browser".equals(action)) {
            fileService = this;
            browser(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    /**
     * @param paramsObject
     * @return
     * @throws JSONException
     * @throws FileNotFoundException
     * @description 获取一个路径下文件以及文件夹的信息
     */
    private void readEntries(JSONObject paramsObject) throws JSONException,
            FileNotFoundException {
        if (!paramsObject.isNull("filePath")) {
            filePath = Environment.getExternalStorageDirectory() + "/"
                    + paramsObject.getString("filePath");
        }
        if (!paramsObject.isNull("callback")) {
            funct = paramsObject.getString("callback");
        }
        File fp = createFileObject(filePath);
        if (!fp.exists()) {
            throw new FileNotFoundException();
        }
        JSONArray entries = new JSONArray();
        if (fp.isDirectory()) {
            File[] files = fp.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].canRead()) {
                    entries.put(getEntry(files[i]));
                }
            }
        }
        jsCallback(funct, entries.toString().trim());
    }

    /**
     * @param paramsObject
     * @return
     * @throws JSONException
     * @throws EncodingException
     * @throws InvalidModificationException
     * @throws IOException
     * @throws NoModificationAllowedException
     * @throws FileExistsException
     * @description 移动（复制或者剪切）文件至目标目录
     */
    private JSONObject transferTo(JSONObject paramsObject)
            throws JSONException, EncodingException,
            InvalidModificationException, IOException,
            NoModificationAllowedException, FileExistsException {
        // 注意：在这个文件名传递信息时需要使用绝对路径，负责会出现空指针异常
        if (!paramsObject.isNull("fileName")) {
            fileName = paramsObject.getString("fileName");
        }
        if (!paramsObject.isNull("newParent")) {
            newParent = paramsObject.getString("newParent");
        }
        if (!paramsObject.isNull("newName")) {
            newName = paramsObject.getString("newName");
        }
        if (!paramsObject.isNull("move")) {
            move = paramsObject.getBoolean("move");
        }
        if (!paramsObject.isNull("callback")) {
            funct = paramsObject.getString("callback");
        }
        String newFileName = FileHelper.getRealPath(fileName, getFragmentContext());
        newParent = FileHelper.getRealPath(newParent, getFragmentContext());
        // 查看新文件名是否可用
        if (newName != null && newName.contains(":")) {
            JSONObject obj = new JSONObject();
            obj.put("code", 1);
            jsCallback(funct, obj.toString().trim());
        }
        // 查看待移动文件是否存在
        File source = new File(newFileName);
        if (!source.exists()) {
            JSONObject obj = new JSONObject();
            obj.put("code", 2);
            jsCallback(funct, obj.toString().trim());
        }
        // 检测移动文件的目标地点是否存在
        File destinationDir = new File(newParent);
        if (!destinationDir.exists()) {
            JSONObject obj = new JSONObject();
            obj.put("code", 3);
            jsCallback(funct, obj.toString().trim());
        }
        // 得到文件的目标文件目录
        File destination = createDestination(newName, source, destinationDir);
        // 查看新文件目录和另一个文件目录是不是同一个目录
        if (source.getAbsolutePath().equals(destination.getAbsolutePath())) {
            JSONObject obj = new JSONObject();
            obj.put("code", 4);
            jsCallback(funct, obj.toString().trim());
        }

        if (source.isDirectory()) {
            if (move) {
                return moveDirectory(source, destination);
            } else {
                return copyDirectory(source, destination);
            }
        } else {
            if (move) {
                JSONObject newFileEntry = moveFile(source, destination);
                if (fileName.startsWith("content://")) {
                    notifyDelete(fileName);
                }
                return newFileEntry;
            } else {
                return copyFile(source, destination);
            }
        }
    }

    /**
     * @param filePath 待检查的路径
     * @description 检查我们是否需要清理content store
     */
    private void notifyDelete(String filePath) {
        String newFilePath = FileHelper.getRealPath(filePath, getFragmentContext());
        try {
            getFragmentContext().getContentResolver().delete(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.DATA + " = ?",
                    new String[]{newFilePath});
        } catch (UnsupportedOperationException t) {

        }
    }

    /**
     * @param srcFile  待拷贝文件
     * @param destFile 目标目录文件夹
     * @return a FileEntry object
     * @throws IOException
     * @throws InvalidModificationException
     * @throws JSONException
     * @description 拷贝一个文件
     */
    private JSONObject copyFile(File srcFile, File destFile)
            throws IOException, InvalidModificationException, JSONException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new InvalidModificationException(
                    "Can't rename a file to a directory");
        }
        copyAction(srcFile, destFile);
        return getEntry(destFile);
    }

    /**
     * 当文件移动在不同的文件系统之间的时候需要调用的一个方法，被copyTo和moveTo调用
     */
    private void copyAction(File srcFile, File destFile)
            throws IOException {
        FileInputStream istream = new FileInputStream(srcFile);
        FileOutputStream ostream = new FileOutputStream(destFile);
        FileChannel input = istream.getChannel();
        FileChannel output = ostream.getChannel();

        try {
            input.transferTo(0, input.size(), output);
        } finally {
            istream.close();
            ostream.close();
            input.close();
            output.close();
        }
    }

    /**
     * @param srcDir         待拷贝的文件夹
     * @param destinationDir 目标文件夹
     * @return
     * @throws JSONException
     * @throws IOException
     * @throws NoModificationAllowedException
     * @throws InvalidModificationException
     * @description 复制一个文件夹以及子文件，被TransferTo方法调用
     */
    private JSONObject copyDirectory(File srcDir, File destinationDir)
            throws JSONException, IOException, NoModificationAllowedException,
            InvalidModificationException {
        if (destinationDir.exists() && destinationDir.isFile()) {
            throw new InvalidModificationException(
                    "Can't rename a file to a directory");
        }

        if (isCopyOnItself(srcDir.getAbsolutePath(),
                destinationDir.getAbsolutePath())) {
            throw new InvalidModificationException(
                    "Can't copy itself into itself");
        }

        if (!destinationDir.exists()) {
            if (!destinationDir.mkdir()) {
                throw new NoModificationAllowedException(
                        "Couldn't create the destination directory");
            }
        }

        for (File file : srcDir.listFiles()) {
            File destination = new File(destinationDir.getAbsoluteFile()
                    + File.separator + file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, destination);
            } else {
                copyFile(file, destination);
            }
        }

        return getEntry(destinationDir);
    }

    /**
     * @param
     * @param
     * @return
     * @description 检查用户是不是将一个文件移动至本目录下而没有改名字。被copyDirectory调用
     */
    private boolean isCopyOnItself(String src, String dest) {
        return dest.startsWith(src)
                && dest.indexOf(File.separator, src.length() - 1) != -1;

    }

    /**
     * @param srcFile  待移动文件
     * @param destFile 目录文件夹
     * @return 文件条目
     * @throws IOException
     * @throws InvalidModificationException
     * @throws JSONException
     * @description 移动文件方法 被TransferTo调用
     */
    private JSONObject moveFile(File srcFile, File destFile)
            throws IOException, JSONException, InvalidModificationException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new InvalidModificationException(
                    "Can't rename a file to a directory");
        }
        if (!srcFile.renameTo(destFile)) {
            copyAction(srcFile, destFile);
            if (destFile.exists()) {
                srcFile.delete();
            } else {
                throw new IOException("moved failed");
            }
        }

        return getEntry(destFile);
    }

    /**
     * @param srcDir         待移动文件夹
     * @param destinationDir 目标文件夹
     * @return 文件目录对象
     * @throws JSONException
     * @throws IOException
     * @throws InvalidModificationException
     * @throws NoModificationAllowedException
     * @throws FileExistsException
     * @description 复制或者剪切一个文件夹，被transferTo方法调用
     */
    private JSONObject moveDirectory(File srcDir, File destinationDir)
            throws IOException, JSONException, InvalidModificationException,
            NoModificationAllowedException, FileExistsException {
        if (destinationDir.exists() && destinationDir.isFile()) {
            throw new InvalidModificationException(
                    "Can't rename a file to a directory");
        }
        if (isCopyOnItself(srcDir.getAbsolutePath(),
                destinationDir.getAbsolutePath())) {
            throw new InvalidModificationException(
                    "Can't move itself into itself");
        }
        if (destinationDir.exists()) {
            if (destinationDir.list().length > 0) {
                throw new InvalidModificationException("directory is not empty");
            }
        }
        if (!srcDir.renameTo(destinationDir)) {
            copyDirectory(srcDir, destinationDir);
            if (destinationDir.exists()) {
                removeDirRecursively(srcDir);
            } else {
                throw new IOException("moved failed");
            }
        }

        return getEntry(destinationDir);
    }

    /**
     * @param newName
     * @param fp
     * @param destination
     * @return 根据一个路径返回一个文件类型的对象
     * @description 根据即将移动至的目录，文件名以及复制资源创建一个新的资源文件。被函数transferTo调用。
     */
    private File createDestination(String newName, File fp, File destination) {
        File destFile = null;

        if ("null".equals(newName) || "".equals(newName)) {
            newName = null;
        }
        if (newName != null) {
            destFile = new File(destination.getAbsolutePath() + File.separator
                    + newName);
        } else {
            destFile = new File(destination.getAbsolutePath() + File.separator
                    + fp.getName());
        }
        return destFile;
    }

    /**
     * @param paramsObject
     * @return true删除成功
     * @throws FileExistsException
     * @description 递归删除一个文件夹
     */
    private boolean removeRecursively(JSONObject paramsObject)
            throws FileExistsException {
        if (!paramsObject.isNull("filePath")) {
            try {
                filePath = Environment.getExternalStorageDirectory() + "/"
                        + paramsObject.getString("filePath");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (!paramsObject.isNull("callback")) {
            try {
                funct = paramsObject.getString("callback");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        File fp = createFileObject(filePath);
        if (atRootDirectory(filePath)) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("code", 10);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsCallback(funct, obj.toString().trim());
            /*
             * try { throw new NoModificationAllowedException(
			 * "You can't delete the root directory"); } catch
			 * (NoModificationAllowedException e) { e.printStackTrace(); }
			 */
        }
        if (fp.delete()) {
            jsCallback(funct, success);
        } else {
            jsCallback(funct, failed);
        }
        return removeDirRecursively(fp);
    }

    /**
     * 递归删除一个文件夹下的所有子文件及子目录，被removeRecursively调用
     *
     * @param fp
     * @return
     * @throws FileExistsException
     */
    private boolean removeDirRecursively(File fp) throws FileExistsException {
        if (fp.isDirectory()) {
            for (File file : fp.listFiles()) {
                removeDirRecursively(file);
            }
        }
        if (!fp.delete()) {
            throw new FileExistsException("could not delete: " + fp.getName());
        } else {
            return true;
        }
    }

    /**
     * @param paramsObject
     * @return
     * @throws JSONException
     * @throws NoModificationAllowedException
     * @throws InvalidModificationException
     * @description 删除一个文件
     */
    private boolean remove(JSONObject paramsObject) throws JSONException,
            NoModificationAllowedException, InvalidModificationException {
        if (!paramsObject.isNull("filePath")) {
            filePath = Environment.getExternalStorageDirectory() + "/"
                    + paramsObject.getString("filePath");
        }
        if (!paramsObject.isNull("callback")) {
            funct = paramsObject.getString("callback");
        }
        File fp = createFileObject(filePath);
        if (atRootDirectory(filePath)) {
            JSONObject obj = new JSONObject();
            obj.put("code", 10);
            jsCallback(funct, obj.toString().trim());
        }

        // 如果一个文件目录下有子文件，那么不能被删除
        if (fp.isDirectory() && fp.list().length > 0) {
            JSONObject obj = new JSONObject();
            obj.put("code", 7);
            jsCallback(funct, obj.toString().trim());
        }
        if (fp.delete()) {
            jsCallback(funct, success);
        } else {
            jsCallback(funct, failed);
        }
        return fp.delete();
    }

    /**
     * @param paramsObject
     * @return
     * @throws JSONException
     * @throws FileExistsException
     * @throws IOException
     * @throws TypeMismatchException
     * @description 查找一个文件或者创建一个文件
     */
    private void getFile(JSONObject paramsObject) throws JSONException,
            FileExistsException, IOException, TypeMismatchException {
        boolean create = false;
        boolean exclusive = false;
        if (!paramsObject.isNull("filePath")) {
            filePath = Environment.getExternalStorageDirectory() + "/"
                    + paramsObject.getString("filePath");
        }
        if (!paramsObject.isNull("fileName")) {
            fileName = paramsObject.getString("fileName");
        }
        if (!paramsObject.isNull("isDirectory")) {
            isDirectory = paramsObject.getBoolean("isDirectory");
        }
        if (!paramsObject.isNull("callback")) {
            funct = paramsObject.getString("callback");
        }
        if (paramsObject != null) {
            create = paramsObject.optBoolean("create");
            if (create) {
                exclusive = paramsObject.optBoolean("exclusive");
            }
        }
        File fp = createFileObject(filePath, fileName);

        if (create) {
            if (exclusive && fp.exists()) {
                JSONObject obj = new JSONObject();
                obj.put("code", 13);
                jsCallback(funct, obj.toString().trim());
            }
            if (isDirectory) {
                fp.mkdir();
            } else {
                fp.createNewFile();
            }
            if (!fp.exists()) {
                JSONObject obj = new JSONObject();
                obj.put("code", 14);
                jsCallback(funct, obj.toString().trim());
            }
        } else {
            if (!fp.exists()) {
                JSONObject obj = new JSONObject();
                obj.put("code", 15);
                jsCallback(funct, obj.toString().trim());
            }
            if (isDirectory) {
                if (fp.isFile()) {
                    JSONObject obj = new JSONObject();
                    obj.put("code", 2);
                    jsCallback(funct, obj.toString().trim());
                }
            } else {
                if (fp.isDirectory()) {
                    JSONObject obj = new JSONObject();
                    obj.put("code", 15);
                    jsCallback(funct, obj.toString().trim());
                }
            }
        }
        jsCallback(funct, getEntry(fp).toString().trim());
    }

    /**
     * @param dirPath  文件夹目录
     * @param fileName 文件名
     * @return
     * @description 创建一个文件，在指定目录下，被getFile调用
     */
    private File createFileObject(String dirPath, String fileName) {
        File fp = null;
        if (fileName.startsWith("/")) {
            fp = new File(fileName);
        } else {
            dirPath = FileHelper.getRealPath(dirPath, getFragmentContext());
            fp = new File(dirPath + File.separator + fileName);
        }
        return fp;
    }

    /**
     * @param paramsObject
     * @return
     * @throws JSONException
     * @description 获取传入文件路径的上层目录
     */
    private void getParent(JSONObject paramsObject) throws JSONException {
        if (!paramsObject.isNull("filePath")) {
            filePath = Environment.getExternalStorageDirectory() + "/"
                    + paramsObject.getString("filePath");
        }
        if (!paramsObject.isNull("callback")) {
            funct = paramsObject.getString("callback");
        }
        filePath = FileHelper.getRealPath(filePath, getFragmentContext());

        if (atRootDirectory(filePath)) {
            jsCallback(funct, getEntry(filePath).toString());
        }
        jsCallback(funct, getEntry(new File(filePath).getParent()).toString());
    }

    /**
     * @param filePath
     * @return
     * @description 判断当前给出的路径是不是根目录
     */
    private boolean atRootDirectory(String filePath) {
        filePath = FileHelper.getRealPath(filePath, getFragmentContext());

        return filePath.equals(Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + "/Android/data/"
                + getActivity().getPackageName() + "/cache")
                || filePath.equals(Environment.getExternalStorageDirectory()
                .getAbsolutePath())
                || filePath.equals("/data/data/"
                + getActivity().getPackageName());
    }

    /**
     * @param paramsObject
     * @return 封装了文件元数据的JSON对象
     * @throws JSONException
     * @description 获取文件的信息元数据
     */
    private String getFileInfo(JSONObject paramsObject) throws JSONException {
        //double_sd是为了适配两张内存卡的情况。
        String double_sd = sdpath.replace("0", "1");
        if (!paramsObject.isNull("filePath")) {
            if (paramsObject.getString("filePath").startsWith(sdpath)) {
                filePath = paramsObject.getString("filePath");
            } else if (paramsObject.getString("filePath").startsWith(double_sd)) {
                filePath = paramsObject.getString("filePath").replaceFirst(sdpath, double_sd);
            } else {
                filePath = Environment.getExternalStorageDirectory() + "/"
                        + paramsObject.getString("filePath");
            }
        }
        File file = createFileObject(filePath);

        if (file.lastModified() == 0) {
            return new JSONObject().toString().trim();
        } else {
            JSONObject metadata = new JSONObject();
            if (file.length() == 0) {
                metadata.put("size", file.length() / 1024);
            } else {
                DecimalFormat fnum = new DecimalFormat("##0.00");
                metadata.put("size", fnum.format(file.length() / 1024.0));
            }
            metadata.put("type", FileHelper.getMimeType(filePath, getFragmentContext()));
            metadata.put("name", file.getName());
            metadata.put("fullPath", "file://" + file.getAbsolutePath());
            java.util.Date date = new java.util.Date(file.lastModified());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dddd = df.format(date);
            metadata.put("lastModifiedDate", dddd);

            return metadata.toString().trim();
        }
    }

    /**
     * @param paramsObject
     * @return 封装了文件元数据的JSON对象
     * @throws JSONException
     * @description 获取文件的信息元数据
     */
    private void getFileMetadata(JSONObject paramsObject) throws JSONException {
        if (!paramsObject.isNull("filePath")) {
            filePath = Environment.getExternalStorageDirectory() + "/"
                    + paramsObject.getString("filePath");
        }
        if (!paramsObject.isNull("callback")) {
            funct = paramsObject.getString("callback");
        }
        File file = createFileObject(filePath);
        JSONObject metadata = new JSONObject();
        metadata.put("size", file.length());
        metadata.put("type", FileHelper.getMimeType(filePath, getFragmentContext()));
        metadata.put("name", file.getName());
        metadata.put("fullPath", filePath);
        metadata.put("lastModifiedDate", file.lastModified());

        jsCallback(funct, metadata.toString().trim());
    }

    /**
     * @param paramsObject 封装了传入参数的JSON存储对象
     * @return 返回抽象路径的最后修改时间。
     * @description 获取抽象路径的元数据
     */
    private void getMetadata(JSONObject paramsObject) throws JSONException,
            FileNotFoundException {
        if (!paramsObject.isNull("filePath")) {
            filePath = Environment.getExternalStorageDirectory() + "/"
                    + paramsObject.getString("filePath");
        }
        if (!paramsObject.isNull("callback")) {
            funct = paramsObject.getString("callback");
        }
        File file = createFileObject(filePath);
        if (!file.exists()) {
            JSONObject obj = new JSONObject();
            obj.put("code", 16);
            jsCallback(funct, obj.toString().trim());
        }
        jsCallback(funct, Long.toString(file.lastModified()));
    }

    /**
     * @param filePath
     * @return
     * @description 通过传入一个文件路径创建一个文件
     */
    private File createFileObject(String filePath) {
        filePath = FileHelper.getRealPath(filePath, getFragmentContext());
        File file = new File(filePath);
        return file;
    }

    /**
     * @param paramsObject
     * @return
     * @throws JSONException
     * @throws IOException
     * @description 在用户的SD卡上为应用程序申请一个文件夹目录
     */
    private void requestFileSystem(JSONObject paramsObject)
            throws JSONException, IOException {
        if (!paramsObject.isNull("type")) {
            type = paramsObject.getInt("type");
        }
        if (!paramsObject.isNull("size")) {
            size = paramsObject.getLong("size");
        }
        if (!paramsObject.isNull("callback")) {
            funct = paramsObject.getString("callback");
        }
        JSONObject fs = new JSONObject();
        if (size != 0
                && size > (DirectoryManager.getFreeDiskSpace(true) * 1024)) {
            Log.d("requestFileSystem", "超过SD卡容量");
        } else {

            if (type == TEMPORARY) {
                File fp;
                fs.put("type", "temporary");
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    fp = new File(Environment.getExternalStorageDirectory()
                            .getAbsolutePath()
                            + "/Android/data/"
                            + getActivity().getPackageName() + "/cache");
                    fp.mkdirs();
                    fs.put("root", getEntry(Environment
                            .getExternalStorageDirectory().getAbsolutePath()
                            + "/Android/data/"
                            + getActivity().getPackageName()
                            + "/cache/"));
                } else {
                    fp = new File("/data/data/"
                            + getActivity().getPackageName() + "/cache");
                    fp.mkdirs();
                    fs.put("root", getEntry("/data/data/"
                            + getActivity().getPackageName() + "/cache/cache"));
                }
            } else if (type == PERSISTENT) {
                fs.put("type", "persistent");
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    fs.put("root",
                            getEntry(Environment.getExternalStorageDirectory()));
                } else {
                    fs.put("root", getEntry("/data/data/"
                            + getActivity().getPackageName()));
                }
            } else {
                throw new IOException("没有找到需要类型的文件系统");
            }
        }
        jsCallback(funct, fs.toString().trim());
    }

    /**
     * 返回一个Json对象来代表一个路径
     *
     * @param path 一个字符串类型的路径
     * @return
     * @throws JSONException
     */
    private JSONObject getEntry(String path) throws JSONException {
        return getEntry(new File(path));
    }

    /**
     * @param paramsObject
     * @throws JSONException
     * @throws NoModificationAllowedException
     * @throws IOException
     * @description 写文件方法
     */
    private void write(JSONObject paramsObject) throws JSONException,
            NoModificationAllowedException, IOException {
        if (!paramsObject.isNull("fileName")) {
            fileName = Environment.getExternalStorageDirectory() + "/"
                    + paramsObject.getString("fileName");
        }
        if (!paramsObject.isNull("data")) {
            data = paramsObject.getString("data");
        }
        if (!paramsObject.isNull("init")) {
            start = paramsObject.getInt("init");
        }
        if (!paramsObject.isNull("callback")) {
            funct = paramsObject.getString("callback");
        }
        if (!paramsObject.isNull("isBinary")) {
            isBinary = paramsObject.getBoolean("isBinary");
        }
        if (fileName.startsWith("content://")) {
            JSONObject obj = new JSONObject();
            obj.put("code", 8);
            jsCallback(funct, obj.toString().trim());
        }
        fileName = FileHelper.getRealPath(fileName, getFragmentContext());
        boolean append = false;
        if (start > 0) {
            this.truncateFile(fileName, start);
            append = true;
        }

        byte[] rawData;
        if (isBinary) {
            rawData = Base64.decode(data, Base64.DEFAULT);
        } else {
            rawData = data.getBytes();
        }
        ByteArrayInputStream in = new ByteArrayInputStream(rawData);
        try {
            FileOutputStream out = new FileOutputStream(fileName, append);
            byte buff[] = new byte[rawData.length];
            in.read(buff, 0, buff.length);
            out.write(buff, 0, rawData.length);
            out.flush();
            out.close();
        } catch (NullPointerException e) {
            NoModificationAllowedException realException = new NoModificationAllowedException(
                    fileName);
            throw realException;
        }
    }

    /**
     * 把文件改到截短到对应大小
     *
     * @param fileName 文件名
     * @param size     大小
     * @return
     * @throws NoModificationAllowedException
     * @throws IOException
     */
    private long truncateFile(String fileName, long size)
            throws NoModificationAllowedException, IOException {

        if (fileName.startsWith("content://")) {
            throw new NoModificationAllowedException(
                    "Couldn't truncate file given its content URI");
        }

        fileName = FileHelper.getRealPath(fileName, getFragmentContext());

        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        try {
            if (raf.length() >= size) {
                FileChannel channel = raf.getChannel();
                channel.truncate(size);
                return size;
            }
            return raf.length();
        } finally {
            raf.close();
        }
    }

    /**
     * @param
     * @throws IOException
     * @throws JSONException
     * @description 选择读取文件的方式
     */
    private void readFileAs(JSONObject paramsObject) throws IOException,
            JSONException {
        if (!paramsObject.isNull("callback")) {
            callback = paramsObject.getString("callback");
        }
        if (!paramsObject.isNull("fileName")) {
            fileName = Environment.getExternalStorageDirectory() + "/"
                    + paramsObject.getString("fileName");
        }
        if (!paramsObject.isNull("init")) {
            start = paramsObject.getInt("init");
        }
        if (!paramsObject.isNull("end")) {
            end = paramsObject.getInt("end");
        }
        if (!paramsObject.isNull("resultType")) {
            resultType = paramsObject.getInt("resultType");
        }
        byte[] bytes = readAsBinaryHelper(fileName, start, end);

        switch (resultType) {
            case MESSAGE_TYPE_STRING:
                temp = new String(bytes, "utf-8");
                jsCallback(callback, temp);
                break;
            default:// base64
                String contentType = FileHelper.getMimeType(fileName, getFragmentContext());
                byte[] base64 = Base64.encode(bytes, Base64.NO_WRAP);
                temp = "data:" + contentType + ";base64,"
                        + new String(base64, "US-ASCII");
                jsCallback(callback, temp);
        }

    }

    /**
     * @param fileName 文件名.
     * @param start    读取开始的位置.
     * @param end      读取结束的位置.(主要用于计算需要多大的字符数组来存储文件)。
     * @return 讲一个文件解析为二进制字符数组.
     * @throws IOException
     * @description 将一个文件转为二进制方式
     */
    private byte[] readAsBinaryHelper(String fileName, int start, int end)
            throws IOException {
        int numBytesToRead = end - start;
        byte[] bytes = new byte[numBytesToRead];
        InputStream inputStream = FileHelper.getInputStreamFromUriString(
                fileName, getFragmentContext());
        int numBytesRead = 0;

        if (start > 0) {
            inputStream.skip(start);
        }
        while (numBytesToRead > 0
                && (numBytesRead = inputStream.read(bytes, numBytesRead,
                numBytesToRead)) >= 0) {
            numBytesToRead -= numBytesRead;
        }
        return bytes;
    }

    /**
     * 打开存储卡，浏览文件
     */
    private void browser(JSONObject jsonObject) {
        try {
            if (!jsonObject.isNull("callback"))
                callback = jsonObject.getString("callback");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(getFragmentContext(), FileBrowerAppActivity.class);
        if (getImpCallBackInterface() != null) {
            getImpCallBackInterface().onStartActivityForResult(intent, ImpFragment.FILE_SERVICE_REQUEST);
        }
    }

    // 获取到文件的绝对路径
    protected String getAbsolutePath(Uri uri) {

        // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        String[] audioProj = {MediaStore.Audio.Media.DATA};
        String[] vedioProj = {MediaStore.Video.Media.DATA};
        @SuppressWarnings("deprecation")
        Cursor cursor = getActivity().managedQuery(uri, proj, null,
                null, null);
        @SuppressWarnings("deprecation")
        Cursor audioCursor = getActivity().managedQuery(uri,
                audioProj, null, null, null);
        @SuppressWarnings("deprecation")
        Cursor vedioCursor = getActivity().managedQuery(uri,
                vedioProj, null, null, null);

        if (cursor != null) {
            return getPath(cursor, proj[0]);
        }
        // 如果是音频文件
        else if (audioCursor != null) {
            return getPath(audioCursor, audioProj[0]);
        }
        // 如果是视频文件
        else if (vedioCursor != null) {
            return getPath(vedioCursor, vedioProj[0]);
        }
        // 如果是普通文件
        else {
            // 如果游标为空说明获取的已经是绝对路径了
            return uri.getPath();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == ImpFragment.FILE_SERVICE_REQUEST) {
            String filePath = data.getStringExtra("filePath");
            jsCallback(callback, filePath + "");
        }
    }

    /**
     * 查询文件路径
     *
     * @param cursor     游标
     * @param columnname 查询列名
     * @return 文件路径
     */
    private String getPath(Cursor cursor, String columnname) {
        int column_index = cursor.getColumnIndexOrThrow(columnname);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void onDestroy() {

    }

}

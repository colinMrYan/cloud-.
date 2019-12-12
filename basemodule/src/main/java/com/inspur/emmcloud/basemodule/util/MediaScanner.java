package com.inspur.emmcloud.basemodule.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.inspur.emmcloud.baselib.util.StringUtils;

import java.io.File;

/**
 * Created by chenmch on 2019/12/11.
 */

public class MediaScanner {
    private MediaScannerConnection mConn = null;
    private ScannerClient mClient = null;
    private File mFile = null;
    private String mMimeType = null;

    public MediaScanner(Context context) {
        if (mClient == null) {
            mClient = new ScannerClient();
        }
        if (mConn == null) {
            mConn = new MediaScannerConnection(context, mClient);
        }
    }

    public void scanFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        mFile = file;
        mMimeType = FileUtils.getMimeType(file);
        if (StringUtils.isBlank(mMimeType)) {
            mMimeType = "*/*";
        }
        mConn.connect();
    }

    public void scanFile(String filePath) {
        scanFile(new File(filePath));
    }

    class ScannerClient implements MediaScannerConnection.MediaScannerConnectionClient {
        @Override
        public void onMediaScannerConnected() {

            if (mFile == null) {
                return;
            }
            scan(mFile);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            mConn.disconnect();
        }

        private void scan(File file) {
            mConn.scanFile(file.getAbsolutePath(), null);
        }
    }
}

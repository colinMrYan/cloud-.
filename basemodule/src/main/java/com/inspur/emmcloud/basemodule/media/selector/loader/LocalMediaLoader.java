package com.inspur.emmcloud.basemodule.media.selector.loader;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.selector.config.FileSizeUnit;
import com.inspur.emmcloud.basemodule.media.selector.config.PictureConfig;
import com.inspur.emmcloud.basemodule.media.selector.config.PictureMimeType;
import com.inspur.emmcloud.basemodule.media.selector.config.PictureSelectionConfig;
import com.inspur.emmcloud.basemodule.media.selector.config.SelectMimeType;
import com.inspur.emmcloud.basemodule.media.selector.entity.LocalMedia;
import com.inspur.emmcloud.basemodule.media.selector.entity.LocalMediaFolder;
import com.inspur.emmcloud.basemodule.media.selector.interfaces.OnQueryAlbumListener;
import com.inspur.emmcloud.basemodule.media.selector.interfaces.OnQueryAllAlbumListener;
import com.inspur.emmcloud.basemodule.media.selector.interfaces.OnQueryDataResultListener;
import com.inspur.emmcloud.basemodule.media.selector.thread.PictureThreadUtils;
import com.inspur.emmcloud.basemodule.media.selector.utils.MediaUtils;
import com.inspur.emmcloud.basemodule.media.selector.utils.SdkVersionUtils;
import com.inspur.emmcloud.basemodule.media.selector.utils.SortUtils;
import com.inspur.emmcloud.basemodule.media.selector.utils.ValueOf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author：luck
 * @data：2016/12/31 19:12
 * @describe: Local media database query class
 */
public final class LocalMediaLoader extends IBridgeMediaLoader {

    /**
     * Video mode conditions
     *
     * @param durationCondition
     * @param queryMimeCondition
     * @return
     */
    private static String getSelectionArgsForVideoMediaCondition(String durationCondition, String queryMimeCondition) {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + queryMimeCondition + " AND " + durationCondition;
    }

    /**
     * Audio mode conditions
     *
     * @param durationCondition
     * @param queryMimeCondition
     * @return
     */
    private static String getSelectionArgsForAudioMediaCondition(String durationCondition, String queryMimeCondition) {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + queryMimeCondition + " AND " + durationCondition;
    }

    /**
     * Query conditions in all modes
     *
     * @param timeCondition
     * @param sizeCondition
     * @param queryMimeCondition
     * @return
     */
    private static String getSelectionArgsForAllMediaCondition(String timeCondition,
                                                               String sizeCondition,
                                                               String queryMimeCondition) {
        return "(" +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                queryMimeCondition + " OR " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=? AND " +
                timeCondition + ") AND " +
                sizeCondition;
    }

    /**
     * Query conditions in image modes
     *
     * @param fileSizeCondition
     * @param queryMimeCondition
     * @return
     */
    private static String getSelectionArgsForImageMediaCondition(String fileSizeCondition, String queryMimeCondition) {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + queryMimeCondition + " AND " + fileSizeCondition;
    }


    @Override
    public void loadAllAlbum(final OnQueryAllAlbumListener<LocalMediaFolder> query) {
        PictureThreadUtils.executeByIo(new PictureThreadUtils.SimpleTask<List<LocalMediaFolder>>() {

            @Override
            public List<LocalMediaFolder> doInBackground() {
                List<LocalMediaFolder> imageFolders = new ArrayList<>();
                Cursor data = getContext().getContentResolver().query(QUERY_URI, PROJECTION,
                        getSelection(), getSelectionArgs(), getSortOrder());
                try {
                    if (data != null) {
                        LocalMediaFolder allImageFolder = new LocalMediaFolder();
                        ArrayList<LocalMedia> latelyImages = new ArrayList<>();
                        int count = data.getCount();
                        if (count > 0) {
                            int idColumn = data.getColumnIndexOrThrow(PROJECTION[0]);
                            int dataColumn = data.getColumnIndexOrThrow(PROJECTION[1]);
                            int mimeTypeColumn = data.getColumnIndexOrThrow(PROJECTION[2]);
                            int widthColumn = data.getColumnIndexOrThrow(PROJECTION[3]);
                            int heightColumn = data.getColumnIndexOrThrow(PROJECTION[4]);
                            int durationColumn = data.getColumnIndexOrThrow(PROJECTION[5]);
                            int sizeColumn = data.getColumnIndexOrThrow(PROJECTION[6]);
                            int folderNameColumn = data.getColumnIndexOrThrow(PROJECTION[7]);
                            int fileNameColumn = data.getColumnIndexOrThrow(PROJECTION[8]);
                            int bucketIdColumn = data.getColumnIndexOrThrow(PROJECTION[9]);
                            int dateAddedColumn = data.getColumnIndexOrThrow(PROJECTION[10]);
                            int orientationColumn = data.getColumnIndexOrThrow(PROJECTION[11]);

                            data.moveToFirst();
                            do {
                                long id = data.getLong(idColumn);
                                String mimeType = data.getString(mimeTypeColumn);
                                mimeType = TextUtils.isEmpty(mimeType) ? PictureMimeType.ofJPEG() : mimeType;
                                String absolutePath = data.getString(dataColumn);
                                if (PictureSelectionConfig.onQueryFilterListener != null) {
                                    if (PictureSelectionConfig.onQueryFilterListener.onFilter(absolutePath)) {
                                        continue;
                                    }
                                }
                                String url = SdkVersionUtils.isQ() ? MediaUtils.getRealPathUri(id, mimeType) : absolutePath;
                                // Here, it is solved that some models obtain mimeType and return the format of image / *,
                                // which makes it impossible to distinguish the specific type, such as mi 8,9,10 and other models
                                if (mimeType.endsWith("image/*")) {
                                    mimeType = MediaUtils.getMimeTypeFromMediaUrl(absolutePath);
                                    if (!getConfig().isGif) {
                                        if (PictureMimeType.isHasGif(mimeType)) {
                                            continue;
                                        }
                                    }
                                }

                                if (mimeType.endsWith("image/*")) {
                                    continue;
                                }

                                if (!getConfig().isWebp) {
                                    if (mimeType.startsWith(PictureMimeType.ofWEBP())) {
                                        continue;
                                    }
                                }
                                if (!getConfig().isBmp) {
                                    if (PictureMimeType.isHasBmp(mimeType)) {
                                        continue;
                                    }
                                }

                                int width = data.getInt(widthColumn);
                                int height = data.getInt(heightColumn);
                                int orientation = data.getInt(orientationColumn);
                                if (orientation == 90 || orientation == 270) {
                                    width = data.getInt(heightColumn);
                                    height = data.getInt(widthColumn);
                                }
                                long duration = data.getLong(durationColumn);
                                long size = data.getLong(sizeColumn);
                                String folderName = data.getString(folderNameColumn);
                                String fileName = data.getString(fileNameColumn);
                                long bucketId = data.getLong(bucketIdColumn);
                                if (TextUtils.isEmpty(fileName)) {
                                    fileName = PictureMimeType.getUrlToFileName(absolutePath);
                                }
                                if (getConfig().isFilterSizeDuration && size > 0 && size < FileSizeUnit.KB) {
                                    // Filter out files less than 1KB
                                    continue;
                                }
                                if (PictureMimeType.isHasVideo(mimeType) || PictureMimeType.isHasAudio(mimeType)) {
                                    if (getConfig().filterVideoMinSecond > 0 && duration < getConfig().filterVideoMinSecond) {
                                        // If you set the minimum number of seconds of video to display
                                        continue;
                                    }
                                    if (getConfig().filterVideoMaxSecond > 0 && duration > getConfig().filterVideoMaxSecond) {
                                        // If you set the maximum number of seconds of video to display
                                        continue;
                                    }
                                    if (getConfig().isFilterSizeDuration && duration <= 0) {
                                        //If the length is 0, the corrupted video is processed and filtered out
                                        continue;
                                    }
                                }
                                if (PictureMimeType.isHasVideo(mimeType) && width == 0 && height == 0) {
                                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                                    retriever.setDataSource(absolutePath);
                                    String videoOrientation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                                    if (TextUtils.equals("90", videoOrientation) || TextUtils.equals("270", videoOrientation)) {
                                        height = ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                                        width = ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                                    } else {
                                        width = ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                                        height = ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                                    }
                                }
                                LocalMedia media = LocalMedia.parseLocalMedia(id, url, absolutePath, fileName, folderName, duration, getConfig().chooseMode, mimeType, width, height, size, bucketId, data.getLong(dateAddedColumn));
                                LocalMediaFolder folder = getImageFolder(url, mimeType, folderName, imageFolders);
                                folder.setBucketId(media.getBucketId());
                                folder.getData().add(media);
                                folder.setFolderTotalNum(folder.getFolderTotalNum() + 1);
                                folder.setBucketId(media.getBucketId());
                                latelyImages.add(media);
                                int imageNum = allImageFolder.getFolderTotalNum();
                                allImageFolder.setFolderTotalNum(imageNum + 1);

                            } while (data.moveToNext());

                            LocalMediaFolder selfFolder = SandboxFileLoader
                                    .loadInAppSandboxFolderFile(getContext(), getConfig().sandboxDir);
                            if (selfFolder != null) {
                                imageFolders.add(selfFolder);
                                allImageFolder.setFolderTotalNum(allImageFolder.getFolderTotalNum() + selfFolder.getFolderTotalNum());
                                allImageFolder.setData(selfFolder.getData());
                                latelyImages.addAll(0, selfFolder.getData());
                                if (MAX_SORT_SIZE > selfFolder.getFolderTotalNum()) {
                                    if (latelyImages.size() > MAX_SORT_SIZE) {
                                        SortUtils.sortLocalMediaAddedTime(latelyImages.subList(0, MAX_SORT_SIZE));
                                    } else {
                                        SortUtils.sortLocalMediaAddedTime(latelyImages);
                                    }
                                }
                            }

                            if (latelyImages.size() > 0) {
                                SortUtils.sortFolder(imageFolders);
                                imageFolders.add(0, allImageFolder);
                                allImageFolder.setFirstImagePath
                                        (latelyImages.get(0).getPath());
                                allImageFolder.setFirstMimeType(latelyImages.get(0).getMimeType());
                                String folderName;
                                if (TextUtils.isEmpty(getConfig().defaultAlbumName)) {
                                    folderName = getConfig().chooseMode == SelectMimeType.ofAudio()
                                            ? getContext().getString(R.string.ps_all_audio) : getContext().getString(R.string.ps_camera_roll);
                                } else {
                                    folderName = getConfig().defaultAlbumName;
                                }
                                allImageFolder.setFolderName(folderName);
                                allImageFolder.setBucketId(PictureConfig.ALL);
                                allImageFolder.setData(latelyImages);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (data != null && !data.isClosed()) {
                        data.close();
                    }
                }
                return imageFolders;
            }

            @Override
            public void onSuccess(List<LocalMediaFolder> result) {
                PictureThreadUtils.cancel(this);
                if (query != null) {
                    query.onComplete(result);
                }
            }
        });
    }


    @Override
    public void loadOnlyInAppDirAllMedia(final OnQueryAlbumListener<LocalMediaFolder> listener) {
        PictureThreadUtils.executeByIo(new PictureThreadUtils.SimpleTask<LocalMediaFolder>() {

            @Override
            public LocalMediaFolder doInBackground() {
                return SandboxFileLoader.loadInAppSandboxFolderFile(getContext(), getConfig().sandboxDir);
            }

            @Override
            public void onSuccess(LocalMediaFolder result) {
                PictureThreadUtils.cancel(this);
                if (listener != null) {
                    listener.onComplete(result);
                }
            }
        });
    }

    @Override
    public void loadPageMediaData(long bucketId, int page, int pageSize, OnQueryDataResultListener<LocalMedia> query) {

    }

    @Override
    public String getAlbumFirstCover(long bucketId) {
        return null;
    }

    @Override
    protected String getSelection() {
        String durationCondition = getDurationCondition();
        String fileSizeCondition = getFileSizeCondition();
        String queryMimeCondition = getQueryMimeCondition();
        switch (getConfig().chooseMode) {
            case SelectMimeType.TYPE_ALL:
                // Get all, not including audio
                return getSelectionArgsForAllMediaCondition(durationCondition, fileSizeCondition, queryMimeCondition);
            case SelectMimeType.TYPE_IMAGE:
                // Gets the image
                return getSelectionArgsForImageMediaCondition(fileSizeCondition, queryMimeCondition);
            case SelectMimeType.TYPE_VIDEO:
                // Access to video
                return getSelectionArgsForVideoMediaCondition(durationCondition, queryMimeCondition);
            case SelectMimeType.TYPE_AUDIO:
                // Access to the audio
                return getSelectionArgsForAudioMediaCondition(durationCondition, queryMimeCondition);
        }
        return null;
    }

    @Override
    protected String[] getSelectionArgs() {
        switch (getConfig().chooseMode) {
            case SelectMimeType.TYPE_ALL:
                // Get all
                return new String[]{
                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)};
            case SelectMimeType.TYPE_IMAGE:
                // Get photo
                return new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)};
            case SelectMimeType.TYPE_VIDEO:
                // Get video
                return new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)};
            case SelectMimeType.TYPE_AUDIO:
                // Get audio
                return new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO)};
        }
        return null;
    }

    @Override
    protected String getSortOrder() {
        return TextUtils.isEmpty(getConfig().sortOrder) ? ORDER_BY : getConfig().sortOrder;
    }

    /**
     * Create folder
     *
     * @param firstPath
     * @param firstMimeType
     * @param imageFolders
     * @param folderName
     * @return
     */
    private LocalMediaFolder getImageFolder(String firstPath, String firstMimeType, String folderName, List<LocalMediaFolder> imageFolders) {
        for (LocalMediaFolder folder : imageFolders) {
            // Under the same folder, return yourself, otherwise create a new folder
            String name = folder.getFolderName();
            if (TextUtils.isEmpty(name)) {
                continue;
            }
            if (TextUtils.equals(name, folderName)) {
                return folder;
            }
        }
        LocalMediaFolder newFolder = new LocalMediaFolder();
        newFolder.setFolderName(folderName);
        newFolder.setFirstImagePath(firstPath);
        newFolder.setFirstMimeType(firstMimeType);
        imageFolders.add(newFolder);
        return newFolder;
    }
}

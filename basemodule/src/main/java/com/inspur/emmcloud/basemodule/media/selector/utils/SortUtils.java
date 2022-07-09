package com.inspur.emmcloud.basemodule.media.selector.utils;

import com.inspur.emmcloud.basemodule.media.selector.entity.LocalMedia;
import com.inspur.emmcloud.basemodule.media.selector.entity.LocalMediaFolder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author：luck
 * @date：2021/11/11 6:11 下午
 * @describe：排序类
 */
public class SortUtils {
    /**
     * Sort by the number of files
     *
     * @param imageFolders
     */
    public static void sortFolder(List<LocalMediaFolder> imageFolders) {
//        Collections.sort(imageFolders, (lhs, rhs) -> {
//            if (lhs.getData() == null || rhs.getData() == null) {
//                return 0;
//            }
//            int lSize = lhs.getFolderTotalNum();
//            int rSize = rhs.getFolderTotalNum();
//            return Integer.compare(rSize, lSize);
//        });
        Collections.sort(imageFolders, new Comparator<LocalMediaFolder>() {
            @Override
            public int compare(LocalMediaFolder lhs, LocalMediaFolder rhs) {
                if (lhs.getData() == null || rhs.getData() == null) {
                    return 0;
                }
                int lSize = lhs.getFolderTotalNum();
                int rSize = rhs.getFolderTotalNum();
                return Integer.compare(rSize, lSize);
            }
        });
    }


    /**
     * Sort by the add Time of files
     *
     * @param list
     */
    public static void sortLocalMediaAddedTime(List<LocalMedia> list) {
//        Collections.sort(list, (lhs, rhs) -> {
//            long lAddedTime = lhs.getDateAddedTime();
//            long rAddedTime = rhs.getDateAddedTime();
//            return Long.compare(rAddedTime, lAddedTime);
//        });
        Collections.sort(list, new Comparator<LocalMedia>() {
            @Override
            public int compare(LocalMedia lhs, LocalMedia rhs) {
                long lAddedTime = lhs.getDateAddedTime();
                long rAddedTime = rhs.getDateAddedTime();
                return Long.compare(rAddedTime, lAddedTime);
            }
        });
    }
}

package com.inspur.emmcloud.basemodule.media.selector.interfaces;

import java.util.List;

/**
 * @author：luck
 * @date：2021/12/5 9:41 下午
 * @describe：OnExternalQueryAllAlbumListener
 */
public interface OnQueryAllAlbumListener<T> {

    void onComplete(List<T> result);
}

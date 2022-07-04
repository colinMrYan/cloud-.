package com.inspur.emmcloud.basemodule.media.selector.basic;

import com.inspur.emmcloud.basemodule.media.selector.loader.IBridgeMediaLoader;

/**
 * @author：luck
 * @date：2022/6/10 9:37 上午
 * @describe：IBridgeLoaderFactory
 */
public interface IBridgeLoaderFactory {
    /**
     * CreateLoader
     */
    IBridgeMediaLoader onCreateLoader();
}

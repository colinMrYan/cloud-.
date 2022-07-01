package com.inspur.emmcloud.basemodule.media.selector.engine;

import com.inspur.emmcloud.basemodule.media.selector.basic.IBridgeLoaderFactory;
import com.inspur.emmcloud.basemodule.media.selector.entity.LocalMedia;
import com.inspur.emmcloud.basemodule.media.selector.interfaces.OnInjectLayoutResourceListener;
import com.inspur.emmcloud.basemodule.media.selector.interfaces.OnResultCallbackListener;

/**
 * @author：luck
 * @date：2020/4/22 11:36 AM
 * @describe：PictureSelectorEngine
 */
public interface PictureSelectorEngine {

    /**
     * Create ImageLoad Engine
     *
     * @return
     */
    ImageEngine createImageLoaderEngine();

    /**
     * Create compress Engine
     *
     * @return
     */
    CompressEngine createCompressEngine();

    /**
     * Create compress Engine
     *
     * @return
     */
    CompressFileEngine createCompressFileEngine();

    /**
     * Create loader data Engine
     *
     * @return
     */
    ExtendLoaderEngine createLoaderDataEngine();

    /**
     * Create loader data Engine
     *
     * @return
     */
    IBridgeLoaderFactory onCreateLoader();

    /**
     * Create SandboxFileEngine  Engine
     *
     * @return
     */
    SandboxFileEngine createSandboxFileEngine();

    /**
     * Create UriToFileTransformEngine  Engine
     *
     * @return
     */
    UriToFileTransformEngine createUriToFileTransformEngine();

    /**
     * Create LayoutResource  Listener
     *
     * @return
     */
    OnInjectLayoutResourceListener createLayoutResourceListener();

    /**
     * Create Result Listener
     *
     * @return
     */
    OnResultCallbackListener<LocalMedia> getResultCallbackListener();
}

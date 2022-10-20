package com.inspur.emmcloud.basemodule.media.selector.basic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

public interface IPagerAdapterLifecycle {
    /**绑定到界面windows
     * onAttached
     */
    void attach(int currentPosition);

    /**父组件生命周期
     * onResume
     */
    void resume(int currentPosition);

    /**父组件生命周期
     * onStop
     */
    void pause(int currentPosition);

    /**界面windows解绑
     * onDetached
     */
    void detach(int currentPosition);

    /**父组件生命周期
     * destroy
     */
    void destroy();
}

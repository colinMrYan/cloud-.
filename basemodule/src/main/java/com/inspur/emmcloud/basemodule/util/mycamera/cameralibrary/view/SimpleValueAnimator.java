package com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.view;

@SuppressWarnings("unused")
public interface SimpleValueAnimator {
    void startAnimation(long duration);

    void cancelAnimation();

    boolean isAnimationStarted();

    void addAnimatorListener(SimpleValueAnimatorListener animatorListener);
}

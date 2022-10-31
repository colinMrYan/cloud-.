package com.inspur.emmcloud.basemodule.media.selector.animators;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * @author：luck
 * @date：2020-04-18 14:19
 * @describe：SlideInBottomAnimationAdapter
 */
public class SlideInBottomAnimationAdapter extends BaseAnimationAdapter {

    public SlideInBottomAnimationAdapter(RecyclerView.Adapter adapter) {
        super(adapter);
    }

    @Override
    protected Animator[] getAnimators(View view) {
        return new Animator[]{
                ObjectAnimator.ofFloat(view, "translationY", view.getMeasuredHeight(), 0)
        };
    }
}

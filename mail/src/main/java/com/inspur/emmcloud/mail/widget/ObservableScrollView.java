package com.inspur.emmcloud.mail.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ObservableScrollView extends ScrollView {
    private List<OnScrollChangedListener> mOnScrollChangedListeners;

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addOnScrollChangedListener(ObservableScrollView.OnScrollChangedListener onScrollChangedListener) {
        if (this.mOnScrollChangedListeners == null) {
            this.mOnScrollChangedListeners = new ArrayList();
        }

        if (!this.mOnScrollChangedListeners.contains(onScrollChangedListener)) {
            this.mOnScrollChangedListeners.add(onScrollChangedListener);
        }
    }

    public void removeOnScrollChangedListener(ObservableScrollView.OnScrollChangedListener onScrollChangedListener) {
        if (this.mOnScrollChangedListeners != null) {
            this.mOnScrollChangedListeners.remove(onScrollChangedListener);
        }
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (this.mOnScrollChangedListeners != null && !this.mOnScrollChangedListeners.isEmpty()) {
            Iterator var5 = this.mOnScrollChangedListeners.iterator();

            while (var5.hasNext()) {
                ObservableScrollView.OnScrollChangedListener listener = (ObservableScrollView.OnScrollChangedListener) var5.next();
                listener.onScrollChanged(this, l, t, oldl, oldt);
            }
        }

    }

    public interface OnScrollChangedListener {
        void onScrollChanged(ObservableScrollView var1, int var2, int var3, int var4, int var5);
    }
}

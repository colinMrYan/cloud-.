package com.inspur.emmcloud.util.privates.richtext;

import android.support.v4.util.LruCache;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.inspur.emmcloud.util.privates.richtext.ext.MD5;
import com.inspur.emmcloud.util.privates.richtext.parser.CachedSpannedParser;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.WeakHashMap;

/**
 * Created by zhou on 2017/3/25.
 * RichTextPool
 */

@SuppressWarnings("WeakerAccess")
class RichTextPool {

    private static final int MAX_RICH_TEXT_SIZE = 50;

    private final LruCache<String, SoftReference<SpannableStringBuilder>> richCache;
    private final WeakHashMap<Object, HashSet<WeakReference<RichText>>> instances;


    private RichTextPool() {
        richCache = new LruCache<>(MAX_RICH_TEXT_SIZE);
        instances = new WeakHashMap<>();
    }

    public static RichTextPool getPool() {
        return RichTextPoolHolder.RICH_TEXT_POOL;
    }

    void cache(String source, SpannableStringBuilder ssb) {
        ssb = new SpannableStringBuilder(ssb);
        ssb.setSpan(new CachedSpannedParser.Cached(), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        richCache.put(MD5.generate(source), new SoftReference<>(ssb));
    }

    SpannableStringBuilder loadCache(String source) {
        SoftReference<SpannableStringBuilder> cache = richCache.get(MD5.generate(source));
        SpannableStringBuilder ssb = cache == null ? null : cache.get();
        if (ssb != null) {
            return new SpannableStringBuilder(ssb);
        }
        return null;
    }

    void clear(Object tag) {
        HashSet<WeakReference<RichText>> richTexts = instances.get(tag);
        if (richTexts != null) {
            for (WeakReference<RichText> weakReference : richTexts) {
                RichText richText = weakReference.get();
                if (richText != null) {
                    richText.clear();
                }
            }
        }
        instances.remove(tag);
    }

    void bind(Object tag, RichText richText) {
        HashSet<WeakReference<RichText>> richTexts = instances.get(tag);
        if (richTexts == null) {
            richTexts = new HashSet<>();
            instances.put(tag, richTexts);
        }
        richTexts.add(new WeakReference<>(richText));
    }

    public void recycle() {
        richCache.evictAll();
    }

    private static class RichTextPoolHolder {
        private static final RichTextPool RICH_TEXT_POOL = new RichTextPool();
    }

}

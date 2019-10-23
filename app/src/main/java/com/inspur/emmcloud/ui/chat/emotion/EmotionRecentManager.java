package com.inspur.emmcloud.ui.chat.emotion;

import android.content.Context;
import android.content.SharedPreferences;

import com.inspur.emmcloud.basemodule.application.BaseApplication;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class EmotionRecentManager extends ArrayList<String> {
    private static final Object LOCK = new Object();
    private static final String DELIMITER = ",";
    private static final String PREFERENCE_EMOTION_NAME = "emojicon";
    private static final String PREF_RECENTS = "recent_emojis";
    private static final int MAX_SAVE_RECENT_EMOTION_SIZE = 8;
    private static EmotionRecentManager instance;

    public static EmotionRecentManager getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new EmotionRecentManager();
                }
            }
        }
        return instance;
    }

    public ArrayList<String> loadRecent() {
        SharedPreferences sp = getPreferences();
        String str = sp.getString(PREF_RECENTS, "");
        StringTokenizer tokenizer = new StringTokenizer(str, DELIMITER);
        while (tokenizer.hasMoreTokens()) {
            add(tokenizer.nextToken());
        }

        return this;
    }

    private void saveRecent() {
        StringBuilder stringBuilder = new StringBuilder();
        int count = size();
        for (int i = 0; i < size(); i++) {
            stringBuilder.append(get(i));
            if (i < count - 1) {
                stringBuilder.append(DELIMITER);
            }
        }

        SharedPreferences prefs = getPreferences();
        prefs.edit().putString(PREF_RECENTS, stringBuilder.toString()).apply();
    }

    private SharedPreferences getPreferences() {
        return BaseApplication.getInstance().getSharedPreferences(PREFERENCE_EMOTION_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public boolean add(String s) {
        boolean ret = super.add(s);

        while (size() > MAX_SAVE_RECENT_EMOTION_SIZE) {
            super.remove(MAX_SAVE_RECENT_EMOTION_SIZE);
        }

        saveRecent();
        return ret;
    }
}

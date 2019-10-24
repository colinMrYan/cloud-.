package com.inspur.emmcloud.ui.chat.emotion;

import android.content.Context;
import android.content.SharedPreferences;

import com.inspur.emmcloud.basemodule.application.BaseApplication;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class EmotionRecentManager extends ArrayList<String> {
    private static final Object LOCK = new Object();
    private static final String DELIMITER = ",";
    private static final String PREFERENCE_EMOTION_NAME = "emotion_pref";
    private static final String PREF_RECENT = "recent_emotion";
    private static final int MAX_SAVE_RECENT_EMOTION_SIZE = 7;
    private static volatile EmotionRecentManager instance;
    private Context mContext;

    private EmotionRecentManager(Context mContext) {
        this.mContext = mContext;
        clear();
        loadRecent();
    }

    public static EmotionRecentManager getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new EmotionRecentManager(context);
                }
            }
        }
        return instance;
    }

    private void loadRecent() {
        SharedPreferences sp = getPreferences();
        String str = sp.getString(PREF_RECENT, "");
        StringTokenizer tokenizer = new StringTokenizer(str, DELIMITER);
        while (tokenizer.hasMoreTokens()) {
            add(tokenizer.nextToken());
        }
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
        prefs.edit().putString(PREF_RECENT, stringBuilder.toString()).apply();
    }

    private SharedPreferences getPreferences() {
        return BaseApplication.getInstance().getSharedPreferences(PREFERENCE_EMOTION_NAME, Context.MODE_PRIVATE);
    }

    public void addItem(String s) {
        if (contains(s)) {
            super.remove(s);
        }

        add(0, s);
    }

    @Override
    public void add(int index, String element) {
        super.add(index, element);

        if (index == 0) {
            while (size() > MAX_SAVE_RECENT_EMOTION_SIZE) {
                super.remove(MAX_SAVE_RECENT_EMOTION_SIZE);
            }
        } else {
            while (size() > MAX_SAVE_RECENT_EMOTION_SIZE) {
                super.remove(0);
            }
        }
        saveRecent();
    }
}

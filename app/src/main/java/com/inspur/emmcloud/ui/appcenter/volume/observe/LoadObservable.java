package com.inspur.emmcloud.ui.appcenter.volume.observe;

import android.database.Observable;

public class LoadObservable extends Observable<LoadObserver> {
    private static volatile LoadObservable instance;

    public static LoadObservable getInstance() {
        if (instance == null) {
            synchronized (LoadObservable.class) {
                if (instance == null) {
                    instance = new LoadObservable();
                }
            }
        }

        return instance;
    }

    public void notifyDateChange() {
        for (LoadObserver observer : mObservers) {
            observer.onDataChange();
        }
    }

    @Override
    public void registerObserver(LoadObserver observer) {
        super.registerObserver(observer);
    }

    @Override
    public void unregisterObserver(LoadObserver observer) {
        if (mObservers.indexOf(observer) == -1) {
            return;
        }
        super.unregisterObserver(observer);
    }
}

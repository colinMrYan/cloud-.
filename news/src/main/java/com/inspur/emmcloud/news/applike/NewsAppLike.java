package com.inspur.emmcloud.news.applike;

import com.inspur.emmcloud.baselib.applicationlike.IApplicationLike;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.componentservice.news.NewsService;
import com.inspur.emmcloud.news.servcieimpl.NewsServiceImpl;

public class NewsAppLike implements IApplicationLike {
    Router router = Router.getInstance();
    @Override
    public void onCreate() {
        router.addService(NewsService.class, new NewsServiceImpl());
    }

    @Override
    public void onStop() {
        router.removeService(NewsService.class);
    }
}

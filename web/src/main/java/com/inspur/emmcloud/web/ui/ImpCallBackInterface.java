package com.inspur.emmcloud.web.ui;

import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.basemodule.bean.MainTabMenu;
import com.inspur.emmcloud.web.plugin.window.DropItemTitle;
import com.inspur.emmcloud.web.plugin.window.OnKeyDownListener;

import java.util.List;
import java.util.Map;

/**
 * Created by yufuchang on 2018/7/11.
 */

public interface ImpCallBackInterface {
    void onLoadingDlgDimiss();

    void onShowImpDialog();

    Map<String, String> onGetWebViewHeaders(String url);

    void onInitWebViewGoBackOrClose();

    void onSetTitle(String title);

    void onFinishActivity();

    void onLoadingDlgShow(String content);

    void onStartActivityForResult(Intent intent, int requestCode);

    void onStartActivityForResult(String routerPath, Bundle bundle, int requestCode);

    void onSetDropTitles(List<DropItemTitle> dropItemTitleList);

    void onProgressChanged(int newProgress);

    void onSetOptionMenu(List<MainTabMenu> optionMenuList);

    void setOnKeyDownListener(OnKeyDownListener onKeyDownListener);

    boolean isWebFromIndex();

    void showLoadFailLayout(String url, String description);

    void showScreenshotImg(String sceenshotImgPath);
}

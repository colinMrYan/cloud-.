package com.inspur.imp.api;

import android.content.Intent;

import com.inspur.emmcloud.bean.system.MainTabMenu;
import com.inspur.imp.plugin.window.DropItemTitle;
import com.inspur.imp.plugin.window.OnKeyDownListener;

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

    void onSetDropTitles(List<DropItemTitle> dropItemTitleList);

    void onProgressChanged(int newProgress);

    void onSetOptionMenu(List<MainTabMenu> optionMenuList);
    void setOnKeyDownListener(OnKeyDownListener onKeyDownListener);
}

package com.winsun.fruitmix.setting;

import android.content.Context;

import com.winsun.fruitmix.SettingActivity;

/**
 * Created by Administrator on 2017/6/22.
 */

public interface SettingPresenter {

    void onCreate(Context context);

    void clearCache(Context context, SettingActivity.SettingViewModel settingViewModel);

    void onDestroy(Context context);
}
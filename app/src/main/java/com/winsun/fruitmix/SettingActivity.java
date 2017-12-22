package com.winsun.fruitmix;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Bundle;

import com.winsun.fruitmix.databinding.ActivitySettingBinding;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.plugin.view.PluginManageActivity;
import com.winsun.fruitmix.setting.SettingPresenter;
import com.winsun.fruitmix.setting.SettingPresenterImpl;
import com.winsun.fruitmix.plugin.data.InjectPluginManageDataSource;
import com.winsun.fruitmix.setting.SettingView;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.upload.media.CheckMediaIsUploadStrategy;
import com.winsun.fruitmix.upload.media.InjectUploadMediaUseCase;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class SettingActivity extends BaseActivity implements SettingView {

    public static final String TAG = "SettingActivity";

    private SettingPresenter settingPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySettingBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);

        SettingViewModel settingViewModel = new SettingViewModel();

        SystemSettingDataSource systemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(this);

        settingPresenter = new SettingPresenterImpl(this, settingViewModel, systemSettingDataSource,
                InjectMedia.provideMediaDataSourceRepository(this), CheckMediaIsUploadStrategy.getInstance(),
                InjectUploadMediaUseCase.provideUploadMediaUseCase(this), systemSettingDataSource.getCurrentLoginUserUUID(),
                ThreadManagerImpl.getInstance(), InjectPluginManageDataSource.provideInstance(this),
                binding);

        binding.setSettingPresenter(settingPresenter);

        binding.setSetting(settingViewModel);

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getString(R.string.setting));

        toolbarViewModel.setBaseView(this);

        binding.setToolbarViewModel(toolbarViewModel);

        binding.setSettingView(this);

        settingPresenter.onCreate(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        settingPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        settingPresenter.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        settingPresenter.onDestroy(this);

    }

    @Override
    public void gotoPluginManageActivity() {

        Util.startActivity(this, PluginManageActivity.class);

    }

    public class SettingViewModel {

        public final ObservableBoolean autoUploadOrNot = new ObservableBoolean(false);
        public final ObservableBoolean alreadyUploadMediaCountTextViewVisibility = new ObservableBoolean(false);
        public final ObservableBoolean onlyAutoUploadWhenConnectedWithWifi = new ObservableBoolean(true);
        public final ObservableField<String> alreadyUploadMediaCountText = new ObservableField<>();
        public final ObservableField<String> cacheSizeText = new ObservableField<>();
    }

}

package com.winsun.fruitmix;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.ViewDataBinding;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.winsun.fruitmix.databinding.ActivityMaintenanceBinding;
import com.winsun.fruitmix.databinding.NewFirmwareVersionPromptBinding;
import com.winsun.fruitmix.databinding.ToolbarLayoutBinding;
import com.winsun.fruitmix.dialog.DialogFactory;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.firmware.FirmwareActivity;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.logout.InjectLogoutUseCase;
import com.winsun.fruitmix.network.InjectNetworkStateManager;
import com.winsun.fruitmix.network.NetworkReceiver;
import com.winsun.fruitmix.network.NetworkState;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.SnackbarUtil;
import com.winsun.fruitmix.util.ToastUtil;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Administrator on 2017/3/7.
 */

public abstract class BaseActivity extends AppCompatActivity implements BaseView {

    public static final String TAG = BaseActivity.class.getSimpleName();

    protected String action;

    private ProgressDialog mDialog;

    private NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkReceiver = new NetworkReceiver();

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, intentFilter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        MobclickAgent.onPageStart(TAG);
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

//        MobclickAgent.onPageEnd(TAG);
//        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {

        EventBus.getDefault().unregister(this);

        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dismissDialog();

        mDialog = null;

        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        action = operationEvent.getAction();

        switch (action) {
            case Util.TOKEN_INVALID:
                FNAS.handleLogout();
                InjectLogoutUseCase.provideLogoutUseCase(this).logout();

                InjectLoginUseCase.provideLoginUseCase(this).setAlreadyLogin(false);

                showToast("token失效");

                EquipmentSearchActivity.gotoEquipmentActivity(this, true);

                break;
            case Util.NETWORK_CHANGED:

//            checkShowAutoUploadWhenConnectedWithMobileNetwork();

                break;
            case Util.KEY_STOP_CURRENT_ACTIVITY:

                Log.d(TAG, "handleOperationEvent: call onBackPressed in current activity " + this);

                break;

        }

    }

    @Override
    public void finishView() {
        finish();
    }

    @Override
    public Dialog showProgressDialog(String message) {
        mDialog = ProgressDialog.show(this, null, message, true, false);

        return mDialog;
    }

    @Override
    public void dismissDialog() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    public void showToast(String text) {

        ToastUtil.showToast(this,text);

    }

    @Override
    public void showCustomErrorCode(String text) {

        ToastUtil.showToast(this,String.format(getString(R.string.server_exception), text));

    }

    protected ToolbarViewModel initToolBar(ViewDataBinding binding, ToolbarLayoutBinding toolbarLayoutBinding, String title) {

        Toolbar mToolbar = toolbarLayoutBinding.toolbar;

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();
        toolbarViewModel.setBaseView(this);

        toolbarViewModel.titleTextColorResID.set(ContextCompat.getColor(this, R.color.eighty_seven_percent_white));
        toolbarViewModel.titleText.set(title);

        toolbarViewModel.navigationIconResId.set(R.drawable.ic_back);

        binding.setVariable(BR.toolbarViewModel, toolbarViewModel);

        setStatusBarToolbarBgColor(mToolbar, R.color.login_ui_blue);

        return toolbarViewModel;

    }


    private void setStatusBarToolbarBgColor(Toolbar mToolbar, int colorResID) {
        mToolbar.setBackgroundColor(ContextCompat.getColor(this, colorResID));

        Util.setStatusBarColor(this, colorResID);
    }


}

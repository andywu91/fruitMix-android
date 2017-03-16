package com.winsun.fruitmix.presenter;

import android.content.Intent;

import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.business.LoadTokenParam;
import com.winsun.fruitmix.business.callback.LoadDeviceIdOperationCallback;
import com.winsun.fruitmix.business.callback.LoadTokenOperationCallback;
import com.winsun.fruitmix.contract.LoginContract;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/2/9.
 */

public class LoginPresenterImpl implements LoginContract.LoginPresenter {

    private LoginContract.LoginView mView;
    private DataRepository mRepository;

    private String mEquipmentGroupName;
    private String mEquipmentChildName;
    private int mUserDefaultBgColor;
    private String mUserUUid;
    private String mGateway;

    public LoginPresenterImpl(DataRepository repository, String equipmentGroupName, String equipmentChildName, int userDefaultBgColor, String gateway, String userUUID) {
        mRepository = repository;

        mEquipmentGroupName = equipmentGroupName;
        mEquipmentChildName = equipmentChildName;
        mUserDefaultBgColor = userDefaultBgColor;
        mGateway = gateway;
        mUserUUid = userUUID;
    }

    @Override
    public void login(String userPassword) {

        mView.hideSoftInput();

        if (!mView.isNetworkAlive()) {
            mView.showNoNetwork();
            return;
        }

        mView.showDialog();

        LoadTokenParam param = new LoadTokenParam(mGateway, mUserUUid, userPassword);
        mRepository.loadRemoteTokenWhenLoginInThread(param, new LoadTokenOperationCallback.LoadTokenCallback() {
            @Override
            public void onLoadSucceed(OperationResult result, String token) {

                mRepository.loadRemoteDeviceIDInThread(new LoadDeviceIdOperationCallback.LoadDeviceIDCallback() {
                    @Override
                    public void onLoadSucceed(OperationResult result, String deviceID) {

                        if (mView == null) return;

                        loadData();

                        mView.dismissDialog();

                        mView.handleLoginSucceed();
                    }

                    @Override
                    public void onLoadFail(OperationResult result) {

                        if (mView == null) return;

                        mView.dismissDialog();

                        mView.handleLoginFail(result);
                    }
                });

            }

            @Override
            public void onLoadFail(OperationResult result) {

                if (mView == null) return;

                mView.dismissDialog();

                mView.handleLoginFail(result);
            }
        });
    }

    private void loadData() {
        mRepository.loadUsersInThread(null);
        mRepository.loadMediasInThread(null);
        mRepository.loadMediaSharesInThread(null);
    }

    @Override
    public void onFocusChange(boolean hasFocus) {

        if (hasFocus) {
            mView.hidePwdEditHint();
        } else {
            mView.showPwdEditHint();
        }

    }

    @Override
    public void attachView(LoginContract.LoginView view) {
        mView = view;
    }

    @Override
    public void detachView() {

        mView.dismissDialog();

        mView = null;
    }

    @Override
    public void showEquipmentAndUser() {
        mView.setEquipmentGroupNameText(mEquipmentGroupName);
        mView.setEquipmentChildNameText(mEquipmentChildName);

        mView.setUserDefaultPortraitText(Util.getUserNameFirstLetter(mEquipmentChildName));
        mView.setUserDefaultPortraitBgColor(mUserDefaultBgColor);
    }

    @Override
    public void handleBackEvent() {
        mView.finishActivity();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }
}

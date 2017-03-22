package com.winsun.fruitmix.presenter;

import android.content.Context;
import android.content.Intent;

import com.winsun.fruitmix.contract.MediaFragmentContract;
import com.winsun.fruitmix.contract.NewPhotoSelectContract;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/3/2.
 */

public class NewPhotoSelectPresenterImpl implements NewPhotoSelectContract.NewPhotoSelectPresenter {

    private NewPhotoSelectContract.NewPhotoSelectView mView;

    private MediaFragmentContract.MediaFragmentPresenter mediaFragmentPresenter;

    public NewPhotoSelectPresenterImpl(MediaFragmentContract.MediaFragmentPresenter mediaFragmentPresenter) {
        this.mediaFragmentPresenter = mediaFragmentPresenter;
    }

    @Override
    public void attachView(NewPhotoSelectContract.NewPhotoSelectView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void handleBackEvent() {
        mView.finishActivity();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Util.KEY_CREATE_ALBUM_REQUEST_CODE) {
            mView.setResult(resultCode);
            mView.finishActivity();
        }
    }

    @Override
    public void handleSelectFinished() {
        mediaFragmentPresenter.albumBtnOnClick();
    }

    @Override
    public void initView(Context context) {

        mediaFragmentPresenter.enterChooseMode();

        mediaFragmentPresenter.onCreate(context);
    }

    @Override
    public void setTitle(String title) {
        mView.setTitle(title);
    }
}

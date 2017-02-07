package com.winsun.fruitmix.refactor.presenter;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.LoadTokenParam;
import com.winsun.fruitmix.refactor.common.callback.LoadTokenOperationCallback;
import com.winsun.fruitmix.refactor.contract.SplashContract;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.ui.SplashScreenActivity;
import com.winsun.fruitmix.util.FileUtil;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/2/6.
 */

public class SplashPresenterImpl implements SplashContract.SplashPresenter {

    public static final String TAG = SplashPresenterImpl.class.getSimpleName();

    private SplashContract.SplashView mView;

    public static final int WELCOME = 0x0010;

    public static final int DELAY_TIME_MILLISECOND = 3 * 1000;

    private CustomHandler mHandler;

    private DataRepository mRepository;

    public SplashPresenterImpl(DataRepository repository) {

        mRepository = repository;

    }

    private void createDownloadFileStoreFolder() {
        boolean result = FileUtil.createDownloadFileStoreFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create download file store folder failed");
        }
    }

    @Override
    public void attachView(SplashContract.SplashView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void startMission() {

        createDownloadFileStoreFolder();

        final String tokenInDB = mRepository.loadTokenInDB();

        if (tokenInDB.isEmpty()) {
            mView.emptyCacheToken();
        } else {

            mHandler = new CustomHandler(this);
            mHandler.sendEmptyMessageDelayed(WELCOME, DELAY_TIME_MILLISECOND);

            mRepository.loadRemoteToken(mRepository.getLoadTokenParamInDB(), new LoadTokenOperationCallback.LoadTokenCallback() {
                @Override
                public void onLoadSucceed(OperationResult result, String token) {

                    mRepository.loadUsers(token, null);
                    mRepository.loadMedias(token, null);
                    mRepository.loadMediaShares(token, null);

                }

                @Override
                public void onLoadFail(OperationResult result) {
                    mRepository.loadUsers(tokenInDB, null);
                    mRepository.loadMedias(tokenInDB, null);
                    mRepository.loadMediaShares(tokenInDB, null);
                }
            });
        }

    }

    @Override
    public void handleBackEvent() {
        mHandler.removeMessages(WELCOME);
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode) {

    }

    private static class CustomHandler extends Handler {

        WeakReference<SplashPresenterImpl> weakReference = null;

        CustomHandler(SplashPresenterImpl splashPresenter) {
            weakReference = new WeakReference<>(splashPresenter);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WELCOME:
                    weakReference.get().mView.welcome();
                    break;
                default:
            }
        }
    }

}

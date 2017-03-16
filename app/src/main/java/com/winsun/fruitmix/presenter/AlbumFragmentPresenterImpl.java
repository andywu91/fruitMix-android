package com.winsun.fruitmix.presenter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.business.callback.MediaShareOperationCallback;
import com.winsun.fruitmix.contract.AlbumFragmentContract;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2017/2/13.
 */

public class AlbumFragmentPresenterImpl implements AlbumFragmentContract.AlbumFragmentPresenter {

    public static final String TAG = AlbumFragmentPresenterImpl.class.getSimpleName();

    private AlbumFragmentContract.AlbumFragmentView mView;

    private DataRepository mRepository;

    private List<MediaShare> mAlbumList;

    private MediaShareOperationCallback.LoadMediaSharesCallback mCallback;

    public AlbumFragmentPresenterImpl(DataRepository repository) {
        mRepository = repository;

        mAlbumList = new ArrayList<>();
    }

    private boolean preTreatOperateMediaShare(MediaShare mediaShare) {
        if (mView.isNetworkAlive()) {

            if (mRepository.checkPermissionToOperateMediaShare(mediaShare)) {
                return true;
            } else {
                mView.showNoOperatePermission();
                return false;
            }

        } else {
            mView.showNoNetwork();
            return false;
        }
    }

    @Override
    public void modifyMediaShare(MediaShare mediaShare) {

        if (!preTreatOperateMediaShare(mediaShare)) return;

        mView.showDialog();

        mRepository.modifyMediaShare(mediaShare.createToggleShareStateRequestData(mRepository.loadAllUserUUIDInMemory()), mediaShare, new MediaShareOperationCallback.OperateMediaShareCallback() {
            @Override
            public void onOperateSucceed(OperationResult operationResult, MediaShare mediaShare) {

                if (mView == null) return;

                mView.dismissDialog();

                mView.showOperationResultToast(operationResult);

                mRepository.loadMediaSharesInThread(null);
            }

            @Override
            public void onOperateFail(OperationResult operationResult) {

                if (mView == null) return;

                mView.dismissDialog();

                mView.showOperationResultToast(operationResult);
            }
        });
    }

    @Override
    public void deleteMediaShare(MediaShare mediaShare) {

        if (!preTreatOperateMediaShare(mediaShare)) return;

        mView.showDialog();

        mRepository.deleteMediaShare(mediaShare, new MediaShareOperationCallback.OperateMediaShareCallback() {
            @Override
            public void onOperateSucceed(OperationResult operationResult, MediaShare mediaShare) {

                if (mView == null) return;

                mView.dismissDialog();

                mView.showOperationResultToast(operationResult);

                mRepository.loadMediaSharesInThread(null);
            }

            @Override
            public void onOperateFail(OperationResult operationResult) {

                if (mView == null) return;

                mView.dismissDialog();

                mView.showOperationResultToast(operationResult);
            }
        });
    }

    @Override
    public void refreshData() {

        loadMediaShares();
    }

    private void sortMediaShareList(List<MediaShare> mediaShareList) {
        Collections.sort(mediaShareList, new Comparator<MediaShare>() {
            @Override
            public int compare(MediaShare lhs, MediaShare rhs) {

                long time1 = Long.parseLong(lhs.getTime());
                long time2 = Long.parseLong(rhs.getTime());
                if (time1 < time2)
                    return 1;
                else if (time1 > time2)
                    return -1;
                else return 0;
            }
        });
    }

    private void showTips() {
        if (mRepository.getShowAlbumTipsValue()) {
            mRepository.saveShowAlbumTipsValue(false);
            mView.setAlbumBalloonVisibility(View.VISIBLE);
            mView.setAlbumBalloonOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.setAlbumBalloonVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public Media loadMedia(String mediaKey) {
        return mRepository.loadMediaFromMemory(mediaKey);
    }

    @Override
    public User loadUser(String userUUID) {
        return mRepository.loadUserFromMemory(userUUID);
    }

    @Override
    public void loadMediaToView(Context context, Media media, NetworkImageView view) {

        mRepository.loadThumbMediaToNetworkImageView(context, media, view);

    }

    private void loadMediaShares() {

        if (mCallback == null) {

            mCallback = new MediaShareOperationCallback.LoadMediaSharesCallback() {
                @Override
                public void onLoadSucceed(OperationResult operationResult, Collection<MediaShare> mediaShares) {

                    if (mView == null) return;

                    mAlbumList.clear();

                    for (MediaShare mediaShare : mediaShares) {
                        if (mediaShare.isAlbum() && !mediaShare.isArchived()) {
                            mAlbumList.add(mediaShare);
                        }
                    }

                    sortMediaShareList(mAlbumList);

                    if (mView != null) {
                        mView.dismissLoadingUI();
                        mView.setAddAlbumBtnVisibility(View.VISIBLE);
                        showTips();
                        if (mAlbumList.size() == 0) {
                            mView.showNoContentUI();
                            mView.dismissContentUI();
                        } else {
                            mView.dismissNoContentUI();
                            mView.showContentUI();
                            mView.showAlbums(mAlbumList);
                        }
                    }

                }

                @Override
                public void onLoadFail(OperationResult operationResult) {

                }
            };
            mRepository.registerTimeRetrieveMediaShareCallback(mCallback);
        }

        mRepository.loadMediaSharesInThread(mCallback);

    }

    @Override
    public void attachView(AlbumFragmentContract.AlbumFragmentView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;

        mRepository.unregisterTimeRetrieveMediaShareCallback(mCallback);
    }


    @Override
    public void handleBackEvent() {

    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }
}

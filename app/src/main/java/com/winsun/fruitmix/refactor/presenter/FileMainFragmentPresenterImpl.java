package com.winsun.fruitmix.refactor.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.refactor.contract.FileDownloadFragmentContract;
import com.winsun.fruitmix.refactor.contract.FileFragmentContract;
import com.winsun.fruitmix.refactor.contract.FileMainFragmentContract;
import com.winsun.fruitmix.refactor.contract.FileShareFragmentContract;
import com.winsun.fruitmix.refactor.contract.MainPageContract;

/**
 * Created by Administrator on 2017/2/21.
 */

public class FileMainFragmentPresenterImpl implements FileMainFragmentContract.FileMainFragmentPresenter {

    private FileMainFragmentContract.FileMainFragmentView mView;

    private MainPageContract.MainPagePresenter mainPagePresenter;

    private FileFragmentContract.FileFragmentPresenter fileFragmentPresenter;
    private FileShareFragmentContract.FileShareFragmentPresenter fileShareFragmentPresenter;
    private FileDownloadFragmentContract.FileDownloadFragmentPresenter fileDownloadFragmentPresenter;

    private static final int PAGE_FILE_SHARE = 0;
    private static final int PAGE_FILE = 1;
    static final int PAGE_FILE_DOWNLOAD = 2;

    public FileMainFragmentPresenterImpl(MainPageContract.MainPagePresenter mainPagePresenter, FileFragmentContract.FileFragmentPresenter fileFragmentPresenter,
                                         FileShareFragmentContract.FileShareFragmentPresenter fileShareFragmentPresenter,
                                         FileDownloadFragmentContract.FileDownloadFragmentPresenter fileDownloadFragmentPresenter) {
        this.fileFragmentPresenter = fileFragmentPresenter;
        this.fileShareFragmentPresenter = fileShareFragmentPresenter;
        this.fileDownloadFragmentPresenter = fileDownloadFragmentPresenter;

        this.mainPagePresenter = mainPagePresenter;

        this.mainPagePresenter.setFileMainFragmentPresenter(this);
    }

    @Override
    public void setBottomNavigationItemChecked(int position) {
        mView.setBottomNavigationItemChecked(position);
    }

    @Override
    public void fileMainMenuOnClick() {
        if (mView.getCurrentPage() == PAGE_FILE) {
            fileFragmentPresenter.fileMainMenuOnClick();
        } else if (mView.getCurrentPage() == PAGE_FILE_DOWNLOAD) {
            fileDownloadFragmentPresenter.fileMainMenuOnClick();
//            fileDownloadFragment.getBottomSheetDialog(fileDownloadFragment.getMainMenuItem()).show();
        }
    }

    @Override
    public void onNavigationItemSelected(int itemID) {

        switch (itemID) {
            case R.id.share:
                mView.setViewPagerCurrentItem(PAGE_FILE_SHARE);
                break;
            case R.id.file:
                mView.setViewPagerCurrentItem(PAGE_FILE);
                break;
            case R.id.download:
                mView.setViewPagerCurrentItem(PAGE_FILE_DOWNLOAD);
                break;
        }

    }

    @Override
    public void onPageSelected(int position) {

        mView.resetBottomNavigationItemCheckState();
        mView.setBottomNavigationItemChecked(position);

        switch (position) {
            case PAGE_FILE:
                mView.setFileMainMenuVisibility(View.VISIBLE);
                fileFragmentPresenter.handleTitle();
                break;
            case PAGE_FILE_DOWNLOAD:
                mView.setFileMainMenuVisibility(View.VISIBLE);
                fileDownloadFragmentPresenter.handleTitle();
                break;
            case PAGE_FILE_SHARE:
                mView.setFileMainMenuVisibility(View.GONE);
                fileShareFragmentPresenter.handleTitle();
                break;
        }

    }

    @Override
    public boolean handleBackPressedOrNot() {

        switch (mView.getCurrentPage()) {
            case PAGE_FILE:
                return fileFragmentPresenter.handleBackPressedOrNot();
            case PAGE_FILE_DOWNLOAD:
                return fileDownloadFragmentPresenter.handleBackPressedOrNot();
            case PAGE_FILE_SHARE:
                return fileShareFragmentPresenter.handleBackPressedOrNot();
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (mView.getCurrentPage() == PAGE_FILE)
            fileFragmentPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void initView() {

        mView.setTitleText("");
        mView.resetBottomNavigationItemCheckState();

        mView.setViewPagerCurrentItem(PAGE_FILE);
    }

    @Override
    public void setTitleText(String titleText) {
        mView.setTitleText(titleText);
    }

    @Override
    public void setNavigationIcon(int resID) {
        mView.setNavigationIcon(resID);
    }

    @Override
    public void setDefaultNavigationOnClickListener() {
        mView.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainPagePresenter.switchDrawerOpenState();
            }
        });

    }

    @Override
    public void setNavigationOnClickListener(View.OnClickListener listener) {
        mView.setNavigationOnClickListener(listener);
    }

    @Override
    public void attachView(FileMainFragmentContract.FileMainFragmentView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void handleBackEvent() {

        switch (mView.getCurrentPage()) {
            case PAGE_FILE:
                fileFragmentPresenter.handleBackEvent();
                break;
            case PAGE_FILE_DOWNLOAD:
                fileDownloadFragmentPresenter.handleBackEvent();
                break;
            case PAGE_FILE_SHARE:
                fileShareFragmentPresenter.handleBackEvent();
                break;
        }

    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }


}
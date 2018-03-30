package com.winsun.fruitmix.mediaModule;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateCallbackWrapper;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.databinding.NewActivityAlbumPicChooseBinding;
import com.winsun.fruitmix.file.data.FileFragmentViewDataSource;
import com.winsun.fruitmix.file.data.FileListViewDataSource;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.file.view.LocalFileFragment;
import com.winsun.fruitmix.file.view.fragment.FileFragment;
import com.winsun.fruitmix.file.view.interfaces.FileListSelectModeListener;
import com.winsun.fruitmix.file.view.interfaces.HandleFileListOperateCallback;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.mediaModule.fragment.NewPhotoList;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.ToastUtil;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.RevealToolbarViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/9.
 */
public class NewPicChooseActivity extends BaseActivity implements IPhotoListListener, RevealToolbarViewModel.RevealToolbarRightTextOnClickListener, HandleFileListOperateCallback,
        FileListSelectModeListener, ActiveView {

    public static final String TAG = "NewAlbumPicChooseActivity";

    public static final String KEY_SHOW_MEDIA = "key_show_media";

    public static final String KEY_CREATE_COMMENT = "key_create_comment";

    public static final String KEY_PIN_UUID = "key_pin_uuid";

    public static final String ALREADY_SELECT_FILE_NAME = "already_select_file_name";

    Toolbar revealToolbar;

    FrameLayout mMainFrameLayout;

    private NewPhotoList mNewPhotoList;

//    private LocalFileFragment localFileFragment;

    private FileFragment mFileFragment;

    private boolean onResume = false;

    private int mAlreadySelectedImageKeyListSize = 0;

    private RevealToolbarViewModel viewModel;

    private GroupRepository groupDataSource;

    private User currentUser;

    private String groupUUID;

    private String stationID;

    private PrivateGroup mPrivateGroup;

    private boolean showMedia;

    private boolean createComment;

    private boolean isDestroy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NewActivityAlbumPicChooseBinding binding = DataBindingUtil.setContentView(this, R.layout.new_activity_album_pic_choose);

        revealToolbar = binding.revealToolbarLayout.revealToolbar;

        mMainFrameLayout = binding.mainFramelayout;

        viewModel = new RevealToolbarViewModel();

        viewModel.selectCountTitleText.set(getString(R.string.choose_text));

        viewModel.setBaseView(this);

        viewModel.setRevealToolbarRightTextOnClickListener(this);

        viewModel.showRevealToolbar.set(true);

        viewModel.enterSelectModeText.set(getString(R.string.send));

        binding.setRevealToolbarViewModel(viewModel);

        groupDataSource = InjectGroupDataSource.provideGroupRepository(this);

        currentUser = InjectUser.provideRepository(this).getUserByUUID(InjectSystemSettingDataSource.provideSystemSettingDataSource(this)
                .getCurrentLoginUserUUID());

        groupUUID = getIntent().getStringExtra(Util.KEY_GROUP_UUID);

        mPrivateGroup = groupDataSource.getGroupFromMemory(groupUUID);

        stationID = mPrivateGroup.getStationID();

        createComment = getIntent().getBooleanExtra(KEY_CREATE_COMMENT, true);

        showMedia = getIntent().getBooleanExtra(KEY_SHOW_MEDIA, true);

        if (showMedia) {

            initPhotoList();

        } else {

//            setEnterSelectModeVisibility(View.VISIBLE);
//
//            localFileFragment = new LocalFileFragment(this);
//
//            localFileFragment.setSelectMode(true);
//
//            localFileFragment.setAlreadySelectedFileArrayList(getIntent().getStringArrayListExtra(ALREADY_SELECT_FILE_NAME));
//
//            mMainFrameLayout.addView(localFileFragment.getView());

            StationFileRepository stationFileRepository = InjectStationFileRepository.provideStationFileRepository(this);

            FileListViewDataSource fileListViewDataSource = new FileFragmentViewDataSource(this, stationFileRepository);

            mFileFragment = new FileFragment(this, this, this, fileListViewDataSource);

            mFileFragment.setCanEnterFolderWhenSelectMode(true);

            mMainFrameLayout.addView(mFileFragment.getView());


        }

//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Util.setStatusBarColor(this, R.color.fab_bg_color);

    }

    private void initPhotoList() {
        mNewPhotoList = new NewPhotoList(this, this);

        final List<String> alreadySelectedImageKeyArrayList = getIntent().getStringArrayListExtra(Util.KEY_ALREADY_SELECTED_IMAGE_KEY_ARRAYLIST);

        if (alreadySelectedImageKeyArrayList != null)
            mAlreadySelectedImageKeyListSize = alreadySelectedImageKeyArrayList.size();

        mMainFrameLayout.addView(mNewPhotoList.getView());
        mNewPhotoList.setSelectMode(true);
        mNewPhotoList.setAlreadySelectedImageKeysFromChooseActivity(alreadySelectedImageKeyArrayList);

        mNewPhotoList.setSelectForCreateComment(true);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!onResume) {

            if (showMedia)
                mNewPhotoList.refreshView();
            else {

//                localFileFragment.refreshView();

                mFileFragment.refreshView();

                mFileFragment.enterSelectMode();

            }


            onResume = true;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        Util.setStatusBarColor(this, R.color.colorPrimaryDark);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (showMedia)
            mNewPhotoList.onDestroy();
        else {

//            localFileFragment.onDestroy();

            mFileFragment.onDestroy();

        }

        isDestroy = true;

    }

    @Override
    public void onBackPressed() {

        if (showMedia)
            super.onBackPressed();
        else {

//            if (!localFileFragment.onBackPressed())
//                super.onBackPressed();

            if (mFileFragment.notRootFolder())
                mFileFragment.goToUpperLevel();
            else
                super.onBackPressed();

        }
    }

    @Override
    public void onPhotoItemClick(int selectedItemCount) {

        handleSelectItemCountChanged(selectedItemCount);

    }

    private void handleSelectItemCountChanged(int selectedItemCount) {
        if (selectedItemCount > mAlreadySelectedImageKeyListSize) {
            setEnterSelectModeVisibility(View.VISIBLE);
        } else {
            setEnterSelectModeVisibility(View.INVISIBLE);
        }

        setSelectCountText(String.format(getString(R.string.select_count), selectedItemCount));
    }

    private void setSelectCountText(String text) {
        viewModel.selectCountTitleText.set(text);
    }

    private void setEnterSelectModeVisibility(int visibility) {
        viewModel.rightTextVisibility.set(visibility);
    }

    @Override
    public void onPhotoItemLongClick() {
    }

    @Override
    public void onNoPhotoItem(boolean noPhotoItem) {
    }

    @Override
    public void onPhotoListScrollDown() {

        if (revealToolbar.getVisibility() == View.INVISIBLE)
            return;

        ViewCompat.setElevation(revealToolbar, Util.dip2px(this, 6f));

        revealToolbar.setVisibility(View.GONE);
    }

    @Override
    public void onPhotoListScrollUp() {

        if (revealToolbar.getVisibility() == View.VISIBLE)
            return;

        ViewCompat.setElevation(revealToolbar, Util.dip2px(this, 6f));

        revealToolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPhotoListScrollFinished() {

        ViewCompat.setElevation(revealToolbar, Util.dip2px(this, 2f));

    }

    @Override
    public View getToolbar() {
        return revealToolbar;
    }

    @Override
    public void onRightTextClick() {

        if (createComment)
            handleCreateComment();
        else {

            handleAddMediaOrFileToPin();

        }
    }

    private void handleAddMediaOrFileToPin() {
        String pinUUID = getIntent().getStringExtra(KEY_PIN_UUID);

        if (showMedia) {

            groupDataSource.insertMediaToPin(mNewPhotoList.getSelectedMedias(), groupUUID, pinUUID, new BaseOperateDataCallback<Boolean>() {
                @Override
                public void onSucceed(Boolean data, OperationResult result) {
                    NewPicChooseActivity.this.setResult(RESULT_OK);

                    finish();
                }

                @Override
                public void onFail(OperationResult result) {
                    ToastUtil.showToast(NewPicChooseActivity.this, "插入失败");

                    NewPicChooseActivity.this.setResult(RESULT_CANCELED);

                    finish();
                }
            });

        } else {

            List<AbstractRemoteFile> abstractRemoteFiles = mFileFragment.getSelectedFiles();

            List<AbstractFile> files = new ArrayList<>(abstractRemoteFiles.size());

            files.addAll(abstractRemoteFiles);

            groupDataSource.insertFileToPin(files, groupUUID, pinUUID, new BaseOperateDataCallback<Boolean>() {

                @Override
                public void onSucceed(Boolean data, OperationResult result) {
                    NewPicChooseActivity.this.setResult(RESULT_OK);

                    finish();
                }

                @Override
                public void onFail(OperationResult result) {
                    ToastUtil.showToast(NewPicChooseActivity.this, "插入失败");

                    NewPicChooseActivity.this.setResult(RESULT_CANCELED);

                    finish();
                }
            });


        }
    }

    private void handleCreateComment() {
        UserComment userComment;

        if (showMedia) {
            userComment = createMediaComment();
        } else {
            userComment = createFileComment();
        }

        showProgressDialog(getString(R.string.operating_title, getString(R.string.send)));

        GroupRequestParam groupRequestParam = new GroupRequestParam(mPrivateGroup.getUUID(), mPrivateGroup.getStationID());

        groupDataSource.insertUserComment(groupRequestParam, userComment, new BaseOperateCallbackWrapper(
                new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        dismissDialog();

//                        showToast(getString(R.string.success, getString(R.string.send)));

                        NewPicChooseActivity.this.setResult(RESULT_OK);

                        finish();

                    }

                    @Override
                    public void onFail(OperationResult result) {

                        dismissDialog();

                        showToast(result.getResultMessage(NewPicChooseActivity.this));

                        NewPicChooseActivity.this.setResult(RESULT_CANCELED);

                        finish();
                    }
                }, this
        ));
    }

    @NonNull
    private UserComment createFileComment() {
        UserComment userComment;

        List<AbstractRemoteFile> abstractRemoteFiles = mFileFragment.getSelectedFiles();

        List<AbstractFile> files = new ArrayList<>(abstractRemoteFiles.size());

        files.addAll(abstractRemoteFiles);

//        if (files.size() == 1) {
//            userComment = new SingleFileComment(currentUser, System.currentTimeMillis(), selectFiles.get(0));
//        } else {
//            userComment = new MultiFileComment(currentUser, System.currentTimeMillis(), selectFiles);
//        }

        userComment = new FileComment(Util.createLocalUUid(), currentUser, System.currentTimeMillis(), groupUUID, stationID, files);

        return userComment;
    }

    @NonNull
    private UserComment createMediaComment() {
        UserComment userComment;

        List<Media> medias = mNewPhotoList.getSelectedMedias();

//        List<Media> selectMedias;
//        if (medias.size() > 6) {
//            selectMedias = medias.subList(0, 6);
//        } else
//            selectMedias = medias;

//        if (medias.size() == 1) {
//            userComment = new SinglePhotoComment(currentUser, System.currentTimeMillis(), selectMedias.get(0));
//        } else {
//            userComment = new MultiPhotoComment(currentUser, System.currentTimeMillis(), selectMedias);
//        }

        userComment = new MediaComment(Util.createLocalUUid(), currentUser, System.currentTimeMillis(), groupUUID, stationID, medias);

        return userComment;

    }


    @Override
    public void handleFileListOperate(String currentFolderName) {

    }

    @Override
    public void onFileSelectItemClick(int selectItemCount) {
        handleSelectItemCountChanged(selectItemCount);
    }

    @Override
    public void onFileItemLongClick() {

    }

    @Override
    public void onFileSelectOperationUnavailable() {

    }

    @Override
    public void onFileSelectOperationAvailable() {

    }

    @Override
    public boolean isActive() {
        return !isDestroy;
    }
}

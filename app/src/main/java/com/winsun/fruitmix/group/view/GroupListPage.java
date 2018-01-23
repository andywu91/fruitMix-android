package com.winsun.fruitmix.group.view;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.databinding.ActivityGroupListBinding;
import com.winsun.fruitmix.group.data.source.FakeGroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.group.data.viewmodel.GroupListViewModel;
import com.winsun.fruitmix.group.presenter.GroupListPresenter;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.interfaces.Page;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.token.InjectTokenRemoteDataSource;
import com.winsun.fruitmix.token.TokenDataSource;
import com.winsun.fruitmix.torrent.data.TorrentDataRepository;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.List;
import java.util.Map;

public class GroupListPage implements Page, IShowHideFragmentListener, GroupListPageView {

    private View view;

    private RecyclerView recyclerView;

    private GroupListPresenter groupListPresenter;

    private Activity containerActivity;

    public static final int CREATE_GROUP_REQUEST_CODE = 1;

    public GroupListPage(Activity activity) {

        containerActivity = activity;

        ActivityGroupListBinding binding = ActivityGroupListBinding.inflate(LayoutInflater.from(activity), null, false);

        final LoadingViewModel loadingViewModel = new LoadingViewModel();

        binding.setLoadingViewModel(loadingViewModel);

        final NoContentViewModel noContentViewModel = new NoContentViewModel();
        noContentViewModel.setNoContentImgResId(R.drawable.no_file);
        noContentViewModel.setNoContentText("没有内容");

        binding.setNoContentViewModel(noContentViewModel);

        final GroupListViewModel groupListViewModel = new GroupListViewModel();

        binding.setGroupListViewModel(groupListViewModel);

        view = binding.getRoot();

        final GroupRepository groupRepository = InjectGroupDataSource.provideGroupRepository(containerActivity);

        String currentLoginUserUUID = InjectSystemSettingDataSource.provideSystemSettingDataSource(containerActivity).getCurrentLoginUserUUID();

        User currentUser = InjectUser.provideRepository(containerActivity).getUserByUUID(currentLoginUserUUID);

        groupRepository.setCurrentUser(currentUser);

        TokenDataSource tokenDataSource = InjectTokenRemoteDataSource.provideTokenDataSource(containerActivity);

        groupListPresenter = new GroupListPresenter(this,currentUser,tokenDataSource,
                groupRepository, loadingViewModel, noContentViewModel, groupListViewModel);

        recyclerView = binding.groupRecyclerview;

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(groupListPresenter.getGroupListAdapter());

        groupListPresenter.refreshView();

        binding.addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(containerActivity,CreateGroupActivity.class);
                containerActivity.startActivityForResult(intent,CREATE_GROUP_REQUEST_CODE);

            }
        });

    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void refreshView() {

        groupListPresenter.refreshGroups();

    }

    @Override
    public void refreshViewForce() {

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

    }

    @Override
    public void onDestroy() {
        groupListPresenter.onDestroyView();

        containerActivity = null;
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void gotoGroupContentActivity(String groupUUID, String groupName) {

        Intent intent = new Intent(containerActivity, GroupContentActivity.class);
        intent.putExtra(GroupContentActivity.GROUP_UUID, groupUUID);
        intent.putExtra(GroupContentActivity.GROUP_NAME, groupName);

        containerActivity.startActivity(intent);

    }

    @Override
    public boolean canEnterSelectMode() {
        return false;
    }
}

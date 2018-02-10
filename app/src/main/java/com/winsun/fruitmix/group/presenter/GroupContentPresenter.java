package com.winsun.fruitmix.group.presenter;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.databinding.GroupAddPingItemBinding;
import com.winsun.fruitmix.databinding.GroupPingItemBinding;
import com.winsun.fruitmix.eventbus.MqttMessageEvent;
import com.winsun.fruitmix.group.data.model.AudioComment;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.SystemMessageTextComment;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.model.UserCommentShowStrategy;
import com.winsun.fruitmix.group.data.model.UserCommentViewFactory;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.group.data.viewmodel.GroupContentViewModel;
import com.winsun.fruitmix.group.usecase.PlayAudioUseCase;
import com.winsun.fruitmix.group.view.GroupContentView;
import com.winsun.fruitmix.group.view.customview.CustomArrowToggleButton;
import com.winsun.fruitmix.group.view.customview.UserCommentView;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.parser.RemoteGroupParser;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/21.
 */

public class GroupContentPresenter implements CustomArrowToggleButton.PingToggleListener, ActiveView {

    private PlayAudioUseCase playAudioUseCase;

    private ImageLoader imageLoader;

    private GroupRepository groupRepository;

    private GroupContentViewModel groupContentViewModel;

    private GroupContentAdapter groupContentAdapter;

    private PingViewPageAdapter pingViewPageAdapter;

    private User currentLoggedInUser;

    private String groupUUID;

    private String stationID;

//    private PrivateGroup currentPrivateGroup;

    private LoadingViewModel mLoadingViewModel;

    private ToolbarViewModel mToolbarViewModel;

    private GroupContentView groupContentView;

    private List<UserComment> userComments;

    private UserDataRepository mUserDataRepository;

    private PrivateGroup mPrivateGroup;

    public static final int RESULT_GROUP_REFRESH = 0x1001;

    public GroupContentPresenter(GroupContentView groupContentView, String groupUUID,
                                 UserDataRepository userDataRepository, SystemSettingDataSource systemSettingDataSource,
                                 GroupRepository groupRepository, GroupContentViewModel groupContentViewModel,
                                 LoadingViewModel loadingViewModel, ToolbarViewModel toolbarViewModel,
                                 ImageLoader imageLoader, PlayAudioUseCase playAudioUseCase) {

        mLoadingViewModel = loadingViewModel;
        mToolbarViewModel = toolbarViewModel;

        this.playAudioUseCase = playAudioUseCase;
        this.imageLoader = imageLoader;
        this.groupContentView = groupContentView;
        this.groupUUID = groupUUID;
        this.groupRepository = groupRepository;
        this.groupContentViewModel = groupContentViewModel;

        mUserDataRepository = userDataRepository;

        currentLoggedInUser = userDataRepository.getUserByUUID(systemSettingDataSource.getCurrentLoginUserUUID());

//        currentLoggedInUser = new User();
//        currentLoggedInUser.setUuid(FakeGroupDataSource.MYSELF_UUID);

        groupContentAdapter = new GroupContentAdapter();

        pingViewPageAdapter = new PingViewPageAdapter();

        userComments = new ArrayList<>();

    }

    public GroupContentAdapter getGroupContentAdapter() {
        return groupContentAdapter;
    }

    public PingViewPageAdapter getPingViewPageAdapter() {
        return pingViewPageAdapter;
    }

    public void refreshView() {

        refreshTitle();

        refreshGroup();
    }

    public void refreshTitle() {

        mPrivateGroup = groupRepository.getGroupFromMemory(groupUUID);

        stationID = mPrivateGroup.getStationID();

        String groupName = mPrivateGroup.getName();

        if (groupName.isEmpty()) {
            groupName = groupContentView.getString(R.string.group_chat, mPrivateGroup.getUsers().size());
        }

        mToolbarViewModel.titleText.set(groupName);

    }

    private void refreshGroup() {

        GroupRequestParam groupRequestParam = new GroupRequestParam(mPrivateGroup.getUUID(), mPrivateGroup.getStationID());

        groupRepository.getAllUserCommentByGroupUUID(groupRequestParam, new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<UserComment>() {
            @Override
            public void onSucceed(List<UserComment> data, OperationResult operationResult) {

                mLoadingViewModel.showLoading.set(false);

                userComments = data;

                refreshUserComment();

//                refreshPinView();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mLoadingViewModel.showLoading.set(false);

                groupContentView.showToast(operationResult.getResultMessage(groupContentView.getContext()));

            }
        }, this));
    }

    private void refreshUserComment() {

        if (userComments.size() == 0)
            return;

        groupContentAdapter.setUserComments(userComments);
        groupContentAdapter.notifyDataSetChanged();

        smoothToChatListEnd();
    }

    public void handleMqttMessage(MqttMessageEvent mqttMessageEvent) {

        String message = mqttMessageEvent.getMessage();

        try {

            List<PrivateGroup> newGroups = new RemoteGroupParser().parse(message);

            for (PrivateGroup group : newGroups) {

                groupRepository.refreshGroupInMemory(newGroups);

                if (group.getUUID().equals(groupUUID)) {

                    refreshTitle();

                    if (group.getLastComment().getIndex() > mPrivateGroup.getLastComment().getIndex()) {

                        refreshGroup();

                    }

                }

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void refreshPin() {

        refreshGroup();

        refreshPinView();

    }

    private void refreshPinView() {

        List<Pin> pins = new ArrayList<>();

        groupContentViewModel.showPing.set(false);

        List<PinView> pinViews = new ArrayList<>(pins.size() + 1);

        for (Pin pin : pins) {
            PinView pinView = new PinContentView(pin);
            pinViews.add(pinView);
        }
        pinViews.add(new AddPinView());

        pingViewPageAdapter.setPingViews(pinViews);
        pingViewPageAdapter.notifyDataSetChanged();

    }

    public void onDestroy() {

        groupContentView = null;

    }

    @Override
    public void onPingToggleArrowToDown() {

        groupContentViewModel.showPing.set(true);

    }

    @Override
    public void onPingToggleArrowToUp() {

        groupContentViewModel.showPing.set(false);

    }

    @Override
    public boolean isActive() {
        return groupContentView != null;
    }

    private class GroupContentAdapter extends RecyclerView.Adapter<UserCommentViewHolder> {

        private List<UserComment> mUserComments;

        private UserCommentViewFactory factory;

        GroupContentAdapter() {
            mUserComments = new ArrayList<>();

            factory = UserCommentViewFactory.getInstance(imageLoader, playAudioUseCase);
        }

        void setUserComments(List<UserComment> userComments) {

            mUserComments.clear();

            for (UserComment userComment : userComments) {

                if (userComment instanceof SystemMessageTextComment) {

                    SystemMessageTextComment systemMessageTextComment = (SystemMessageTextComment) userComment;

                    try {
                        JSONObject commentJson = new JSONObject(systemMessageTextComment.getText());

                        String op = commentJson.optString("op");

                        if (!op.equals("deleteUser")) {
                            mUserComments.add(userComment);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else
                    mUserComments.add(userComment);

            }

        }

        @Override
        public UserCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            UserCommentView userCommentView = factory.createUserCommentView(viewType);

            ViewDataBinding viewDataBinding = userCommentView.getViewDataBinding(parent.getContext(), parent);

            return new UserCommentViewHolder(viewDataBinding.getRoot(), userCommentView);

        }

        @Override
        public void onBindViewHolder(UserCommentViewHolder holder, int position) {

            UserComment preUserComment;
            UserComment currentUserComment;

            if (position == 0)
                preUserComment = null;
            else
                preUserComment = mUserComments.get(position - 1);

            currentUserComment = mUserComments.get(position);

            UserCommentShowStrategy userCommentShowStrategy = new UserCommentShowStrategy(preUserComment, currentUserComment, currentLoggedInUser.getAssociatedWeChatGUID());

            holder.userCommentView.refreshCommentView(groupContentView.getContext(), groupContentView.getToolbar(), userCommentShowStrategy, currentUserComment);

        }

        @Override
        public int getItemCount() {
            return mUserComments.size();
        }

        @Override
        public int getItemViewType(int position) {

            return factory.getUserCommentViewType(mUserComments.get(position));

        }

    }


    private class UserCommentViewHolder extends RecyclerView.ViewHolder {

        private UserCommentView userCommentView;

        UserCommentViewHolder(View itemView, UserCommentView userCommentView) {
            super(itemView);
            this.userCommentView = userCommentView;
        }

    }


    public void sendTxt(String text) {

        TextComment textComment = new TextComment(Util.createLocalUUid(), currentLoggedInUser, System.currentTimeMillis(), groupUUID, stationID, text);

        insertUserComment(textComment);

    }

    public void sendAudio(String filePath, long audioRecordTime) {

        AudioComment audioComment = new AudioComment(Util.createLocalUUid(), currentLoggedInUser, System.currentTimeMillis(), groupUUID,
                stationID, filePath, audioRecordTime);

        insertUserComment(audioComment);

    }

    private void insertUserComment(final UserComment userComment) {

        GroupRequestParam groupRequestParam = new GroupRequestParam(mPrivateGroup.getUUID(), mPrivateGroup.getStationID());

        groupRepository.insertUserComment(groupRequestParam, userComment, new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                userComments.add(userComment);

                int lastPosition = userComments.size() - 1;

                groupContentAdapter.setUserComments(userComments);

                groupContentAdapter.notifyItemInserted(lastPosition);

                groupContentView.smoothToChatListPosition(lastPosition);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                groupContentView.showToast(operationResult.getResultMessage(groupContentView.getContext()));

            }
        });
    }


    public void smoothToChatListEnd() {

        if (userComments.size() == 0)
            return;

        groupContentView.smoothToChatListPosition(userComments.size() - 1);
    }

    private class PingViewPageAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<PinView> mPinViews;

        PingViewPageAdapter() {
            mPinViews = new ArrayList<>();
        }

        void setPingViews(List<PinView> pinViews) {
            mPinViews.clear();
            mPinViews.addAll(pinViews);
        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ViewDataBinding binding;

            if (viewType == PinView.TYPE_PING) {
                binding = GroupPingItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            } else
                binding = GroupAddPingItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

            return new BindingViewHolder(binding);
        }


        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            mPinViews.get(position).bindView(holder.getViewDataBinding());

        }

        @Override
        public int getItemCount() {
            return mPinViews.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mPinViews.get(position).getViewType();
        }
    }

    public interface PinView {

        int TYPE_PING = 0;
        int TYPE_ADD_PING = 1;

        int getViewType();

        void bindView(ViewDataBinding viewDataBinding);

        void onClick();

    }

    private class PinContentView implements PinView {

        private Pin pin;

        PinContentView(Pin pin) {
            this.pin = pin;
        }

        @Override
        public int getViewType() {
            return TYPE_PING;
        }

        @Override
        public void bindView(ViewDataBinding viewDataBinding) {

            viewDataBinding.setVariable(BR.ping, pin);

            viewDataBinding.setVariable(BR.pingView, this);

            viewDataBinding.executePendingBindings();

        }

        @Override
        public void onClick() {

            groupContentView.showPinContent(groupUUID, pin.getUuid());

        }
    }

    private class AddPinView implements PinView {

        @Override
        public int getViewType() {
            return TYPE_ADD_PING;
        }

        @Override
        public void bindView(ViewDataBinding viewDataBinding) {

            viewDataBinding.setVariable(BR.pingView, this);

            viewDataBinding.executePendingBindings();

        }

        @Override
        public void onClick() {

            groupContentView.showCreatePing();
        }
    }


}

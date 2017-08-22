package com.winsun.fruitmix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.component.AnimatedExpandableListView;
import com.winsun.fruitmix.databinding.ActivityEquipmentSearchBinding;
import com.winsun.fruitmix.equipment.EquipmentPresenter;
import com.winsun.fruitmix.equipment.EquipmentRemoteDataSource;
import com.winsun.fruitmix.equipment.EquipmentSearchView;
import com.winsun.fruitmix.equipment.InjectEquipment;
import com.winsun.fruitmix.equipment.WeChatLoginListener;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.equipment.EquipmentSearchManager;
import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.anim.AnimatorBuilder;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.wxapi.MiniProgram;
import com.winsun.fruitmix.wxapi.WXEntryActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.circleindicator.CircleIndicator;

public class EquipmentSearchActivity extends AppCompatActivity implements View.OnClickListener, EquipmentSearchView, WeChatLoginListener {

    public static final String TAG = "EquipmentSearchActivity";

    private Context mContext;

    private ViewPager equipmentViewPager;
    private RecyclerView equipmentUserRecyclerView;

    private EquipmentPresenter equipmentPresenter;

    public static void gotoEquipmentActivity(Activity activity, boolean shouldStopService) {
        Intent intent = new Intent(activity, EquipmentSearchActivity.class);
        intent.putExtra(Util.KEY_SHOULD_STOP_SERVICE, shouldStopService);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEquipmentSearchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_equipment_search);

        Util.setStatusBarColor(this, R.color.equipment_ui_blue);

        LoadingViewModel loadingViewModel = new LoadingViewModel();

        binding.setLoadingViewModel(loadingViewModel);

        equipmentViewPager = binding.equipmentViewpager;

        equipmentUserRecyclerView = binding.equipmentUserRecyclerview;

        mContext = this;

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        EquipmentSearchManager mEquipmentSearchManager = InjectEquipment.provideEquipmentSearchManager(mContext);

        EquipmentRemoteDataSource mEquipmentRemoteDataSource = InjectEquipment.provideEquipmentRemoteDataSource(mContext);

        LoginUseCase loginUseCase = InjectLoginUseCase.provideLoginUseCase(mContext);

        ThreadManager threadManager = ThreadManager.getInstance();

        equipmentPresenter = new EquipmentPresenter(loadingViewModel, this,
                mEquipmentSearchManager, mEquipmentRemoteDataSource, loginUseCase, threadManager);

        binding.setWechatLoginListener(this);

        equipmentViewPager.setAdapter(equipmentPresenter.getEquipmentViewPagerAdapter());

        CircleIndicator circleIndicator = binding.viewpagerIndicator;

        circleIndicator.setViewPager(equipmentViewPager);

        equipmentViewPager.getAdapter().registerDataSetObserver(circleIndicator.getDataSetObserver());

        equipmentUserRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        equipmentUserRecyclerView.setItemAnimator(new DefaultItemAnimator());
        equipmentUserRecyclerView.setAdapter(equipmentPresenter.getEquipmentUserRecyclerViewAdapter());

        equipmentViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                equipmentPresenter.refreshEquipment(position);
            }
        });

        final TextView equipmentUserTitle = binding.equipmentUserTitle;

        equipmentUserRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    ViewCompat.setElevation(equipmentUserTitle, Util.dip2px(mContext, 0f));
                else
                    ViewCompat.setElevation(equipmentUserTitle, Util.dip2px(mContext, 2f));

            }
        });

//        Equipment equipment = new Equipment("", Collections.singletonList("10.10.9.126"), 3000);
//        equipment.setModel("");
//        equipment.setSerialNumber("");
//        getUserList(equipment);

    }


    @Override
    public void handleLoginWithUserSucceed(boolean autoUpload) {

        if (!autoUpload)
            Toast.makeText(mContext, getString(R.string.photo_auto_upload_already_close), Toast.LENGTH_SHORT).show();

        setResult(RESULT_OK);
        finish();

        startActivity(new Intent(mContext, NavPagerActivity.class));

        Log.d(TAG, "handleLoginWithUserSucceed: start nav pager ");
    }

    @Override
    public void handleLoginWithUserFail(Equipment equipment, User user) {

        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(Util.GATEWAY, Util.HTTP + equipment.getHosts().get(0));
        intent.putExtra(Util.USER_GROUP_NAME, equipment.getEquipmentName());
        intent.putExtra(Util.USER_NAME, user.getUserName());
        intent.putExtra(Util.USER_UUID, user.getUuid());
        intent.putExtra(Util.USER_BG_COLOR, user.getDefaultAvatarBgColor());

        startActivityForResult(intent, Util.KEY_LOGIN_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        equipmentPresenter.onResume();

//        MobclickAgent.onPageStart(TAG);
//        MobclickAgent.onResume(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        equipmentPresenter.onPause();

//        MobclickAgent.onPageEnd(TAG);
//        MobclickAgent.onPause(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

        equipmentPresenter.onDestroy();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Util.KEY_LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        } else if (requestCode == Util.KEY_MANUAL_INPUT_IP_REQUEST_CODE && resultCode == RESULT_OK) {

            String ip = data.getStringExtra(Util.KEY_MANUAL_INPUT_IP);
            equipmentPresenter.handleInputIpbyByUser(ip);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                Intent intent = new Intent(mContext, CreateNewEquipmentActivity.class);
                startActivityForResult(intent, Util.KEY_MANUAL_INPUT_IP_REQUEST_CODE);
                break;
            default:
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (getIntent().getBooleanExtra(Util.KEY_SHOULD_STOP_SERVICE, true))
            ButlerService.stopButlerService(mContext);

    }

    @Override
    public void wechatLogin() {

/*        WXEntryActivity.setWxEntryCallback(new WXEntryActivity.WXEntryCallback() {
            @Override
            public void loginSucceed() {

                Log.d(TAG, "loginSucceed: ");

                finish();
            }

            @Override
            public void loginFail() {

                Log.d(TAG, "loginFail: ");
            }
        });


        IWXAPI iwxapi = MiniProgram.registerToWX(this);

        MiniProgram.sendAuthRequest(iwxapi);*/

    }

}

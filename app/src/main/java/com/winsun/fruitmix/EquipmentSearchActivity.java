package com.winsun.fruitmix;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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

import com.winsun.fruitmix.component.AnimatedExpandableListView;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EquipmentSearchActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = EquipmentSearchActivity.class.getSimpleName();

    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.equipment_expandablelist)
    AnimatedExpandableListView mEquipmentExpandableListView;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;

    private Context mContext;

    private EquipmentExpandableAdapter mAdapter;

    private List<Equipment> mUserLoadedEquipments;

    private List<Equipment> mFoundedEquipments;

    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    private NsdManager mManager;

    private static final String SERVICE_PORT = "_http._tcp";

    private List<List<User>> mUserExpandableLists;

    private CustomHandler mHandler;

    private static final int DATA_CHANGE = 0x0001;

    private static final String SYSTEM_PORT = "3000";
    private static final String IPALIASING = "/system/ipaliasing";

    private boolean mStartDiscovery = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_search);

        ButterKnife.bind(this);

        mContext = this;

        Util.loginType = LoginType.LOGIN;

        mUserExpandableLists = new ArrayList<>();
        mUserLoadedEquipments = new ArrayList<>();

        mFoundedEquipments = new ArrayList<>();

        mHandler = new CustomHandler(this, getMainLooper());

        mAdapter = new EquipmentExpandableAdapter();
        mEquipmentExpandableListView.setAdapter(mAdapter);

        mEquipmentExpandableListView.setGroupIndicator(null);

        mEquipmentExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                User user = mUserExpandableLists.get(groupPosition).get(childPosition);

                Intent intent = new Intent(mContext, LoginActivity.class);
                intent.putExtra(Util.GATEWAY, "http://" + mUserLoadedEquipments.get(groupPosition).getHosts().get(0));
                intent.putExtra(Util.USER_GROUP_NAME, mUserLoadedEquipments.get(groupPosition).getServiceName());
                intent.putExtra(Util.USER_NAME, user.getUserName());
                intent.putExtra(Util.USER_UUID, user.getUuid());
                intent.putExtra(Util.USER_BG_COLOR, user.getDefaultAvatarBgColor());

                startActivityForResult(intent, Util.KEY_LOGIN_REQUEST_CODE);

                return false;
            }
        });

        mEquipmentExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

                int count = mEquipmentExpandableListView.getExpandableListAdapter().getGroupCount();
                for (int i = 0; i < count; i++) {
                    if (i != groupPosition) {

                        if (mEquipmentExpandableListView.isGroupExpanded(i)) {

                            mEquipmentExpandableListView.collapseGroupWithAnimation(i);

                            Animator animator = AnimatorInflater.loadAnimator(mContext, R.animator.ic_back_restore);
                            animateArrow(i, animator);
                        }

                    }
                }
            }
        });

        mEquipmentExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                Animator animator;

                if (mEquipmentExpandableListView.isGroupExpanded(groupPosition)) {
                    mEquipmentExpandableListView.collapseGroupWithAnimation(groupPosition);

                    animator = AnimatorInflater.loadAnimator(mContext, R.animator.ic_back_restore);
                } else {
                    mEquipmentExpandableListView.expandGroupWithAnimation(groupPosition);

                    animator = AnimatorInflater.loadAnimator(mContext, R.animator.ic_back_remote);

                }

                animateArrow(groupPosition, animator);

                return true;
            }
        });

        mBack.setOnClickListener(this);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (mManager == null)
            mManager = (NsdManager) mContext.getApplicationContext().getSystemService(Context.NSD_SERVICE);

    }

    private void animateArrow(int groupPosition, Animator animator) {
        ImageView mArrow = (ImageView) mEquipmentExpandableListView.getChildAt(groupPosition).findViewById(R.id.arrow);
        animator.setTarget(mArrow);
        animator.start();
    }

    @Override
    protected void onStart() {

        initResolveListener();

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        discoverService();
    }

    @Override
    protected void onPause() {

        stopDiscoverServices();

        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Util.KEY_LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            finish();
        } else if (requestCode == Util.KEY_MANUAL_INPUT_IP_REQUEST_CODE && resultCode == RESULT_OK) {

            String ip = data.getStringExtra(Util.KEY_MANUAL_INPUT_IP);

            List<String> hosts = new ArrayList<>();
            hosts.add(ip);

            Equipment equipment = new Equipment("Winsuc Appliction " + ip, hosts, 6666);
            getUserList(equipment);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:

                ButlerService.stopButlerService(mContext);

                finish();
                break;
            case R.id.fab:
                Intent intent = new Intent(mContext, CreateNewEquipmentActivity.class);
                startActivityForResult(intent, Util.KEY_MANUAL_INPUT_IP_REQUEST_CODE);
                break;
            default:
        }
    }

    @Override
    public void onBackPressed() {
        ButlerService.stopButlerService(mContext);

        super.onBackPressed();
    }

    class EquipmentExpandableAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

        List<Equipment> equipmentList;
        List<List<User>> mapList;
        LruCache<Long, View> viewLruCache;

        EquipmentExpandableAdapter() {
            equipmentList = new ArrayList<>();
            mapList = new ArrayList<>();

            viewLruCache = new LruCache<>(5);
        }

        @Override
        public int getGroupCount() {
            return equipmentList.size();
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {

            List<User> list = mapList.get(groupPosition);

            return list == null ? 0 : list.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return equipmentList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mapList.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            GroupViewHolder groupViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.equipment_group_item, parent, false);

                groupViewHolder = new GroupViewHolder(convertView);
                convertView.setTag(groupViewHolder);
            } else {
                groupViewHolder = (GroupViewHolder) convertView.getTag();
            }

            groupViewHolder.refreshView(groupPosition, isExpanded);

            return convertView;
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            ChildViewHolder childViewHolder;
            if (convertView == null) {

                Long key = ((long) groupPosition << 32) + childPosition;
                View view = viewLruCache.get(key);
                if (view != null && view.getTag() != null) {

                    convertView = view;
                    childViewHolder = (ChildViewHolder) convertView.getTag();

                } else {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.equipment_child_item, parent, false);
                    childViewHolder = new ChildViewHolder(convertView);
                    convertView.setTag(childViewHolder);

                    viewLruCache.put(key, convertView);
                }

            } else {
                childViewHolder = (ChildViewHolder) convertView.getTag();
            }

            childViewHolder.refreshView(groupPosition, childPosition);

            return convertView;
        }

    }

    class GroupViewHolder {

        @BindView(R.id.arrow)
        ImageView mArrow;

        @BindView(R.id.equipment_group_name)
        TextView mGroupName;
        @BindView(R.id.equipment_ip_tv)
        TextView mEquipmentIpTV;

        GroupViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void refreshView(int groupPosition, boolean isExpanded) {
            Equipment equipment = mAdapter.equipmentList.get(groupPosition);
            if (equipment == null) {
                return;
            }
            mGroupName.setText(mAdapter.equipmentList.get(groupPosition).getServiceName());

            List<String> hosts = mAdapter.equipmentList.get(groupPosition).getHosts();

            StringBuilder builder = new StringBuilder();
            for (String host : hosts) {
                builder.append(",");
                builder.append(host);
            }

            mEquipmentIpTV.setText(builder.substring(1));

        }
    }


    class ChildViewHolder {

        @BindView(R.id.user_default_portrait)
        TextView mUserDefaultPortrait;
        @BindView(R.id.equipment_child_name)
        TextView mChildName;

        ChildViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void refreshView(int groupPosition, int childPosition) {

            if (mAdapter.mapList.get(groupPosition) == null || mAdapter.mapList.get(groupPosition).size() == 0)
                return;

            User user = mAdapter.mapList.get(groupPosition).get(childPosition);


            String childName = user.getUserName();
            mChildName.setText(childName);

            String firstLetter = Util.getUserNameFirstLetter(childName);
            mUserDefaultPortrait.setText(firstLetter);

            if (user.getDefaultAvatarBgColor() == 0) {
                user.setDefaultAvatarBgColor(new Random().nextInt(3) + 1);
            }

            mUserDefaultPortrait.setBackgroundResource(user.getDefaultAvatarBgColorResourceId());

        }
    }

    private void initDiscoveryListener() {
        if (mDiscoveryListener == null) {

            mDiscoveryListener = new NsdManager.DiscoveryListener() {
                @Override
                public void onStopDiscoveryFailed(String serviceType, int errorCode) {

                    Log.i(TAG, "onStopDiscoveryFailed: errorCode:" + errorCode);

                }

                @Override
                public void onStartDiscoveryFailed(String serviceType, int errorCode) {

                    Log.i(TAG, "onStartDiscoveryFailed: errorCode:" + errorCode);

                    mStartDiscovery = false;
                }

                @Override
                public void onServiceLost(NsdServiceInfo serviceInfo) {

                    Log.i(TAG, "onServiceLost");

                }

                @Override
                public void onServiceFound(NsdServiceInfo serviceInfo) {

                    Log.i(TAG, "Service resolved: " + serviceInfo);

                    if (serviceInfo.getServiceName().toLowerCase().contains("wisnuc")) {
                        resolveService(serviceInfo);
                    }

                }

                @Override
                public void onDiscoveryStopped(String serviceType) {
                    Log.i(TAG, "onDiscoveryStopped");
                }

                @Override
                public void onDiscoveryStarted(String serviceType) {
                    Log.i(TAG, "onDiscoveryStarted");

                    mStartDiscovery = true;
                }
            };
        }
    }

    private void initResolveListener() {

        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {

                resolveService(serviceInfo);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "onServiceResolved Service info:" + serviceInfo);

                String serviceName = serviceInfo.getServiceName();
                String hostAddress = serviceInfo.getHost().getHostAddress();

                for (Equipment equipment : mFoundedEquipments) {
                    if (equipment == null || serviceName.equals(equipment.getServiceName()) || equipment.getHosts().contains(hostAddress)) {
                        return;
                    }
                }

                Equipment equipment = new Equipment();
                equipment.setServiceName(serviceName);
                Log.i(TAG, "host address:" + hostAddress);

                List<String> hosts = new ArrayList<>();
                hosts.add(hostAddress);

                equipment.setHosts(hosts);
                equipment.setPort(serviceInfo.getPort());

                mFoundedEquipments.add(equipment);

                getUserList(equipment);
            }
        };

    }

    private void discoverService() {

        stopDiscoverServices();

        initDiscoveryListener();

        mManager.discoverServices(SERVICE_PORT, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

    }

    private void resolveService(NsdServiceInfo serviceInfo) {

        mResolveListener = null;
        initResolveListener();

        mManager.resolveService(serviceInfo, mResolveListener);
    }

    private void stopDiscoverServices() {

        if (mDiscoveryListener != null && mStartDiscovery) {

            Log.i(TAG, "stopDiscoverServices: stopServiceDiscovery");

            try {
                mManager.stopServiceDiscovery(mDiscoveryListener);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            mDiscoveryListener = null;
        }
    }

    private void getUserList(final Equipment equipment) {

        //get user list;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                User user;
                List<User> itemList;
                JSONObject itemRaw;
                JSONArray json;
                String str;

                try {

                    String url = Util.HTTP + equipment.getHosts().get(0) + ":" + SYSTEM_PORT + IPALIASING;

                    Log.i(TAG, "login retrieve equipment alias:" + url);

                    str = FNAS.GetRemoteCall(url).getResponseData();

                    json = new JSONArray(str);
                    for (int i = 0; i < json.length(); i++) {
                        itemRaw = json.getJSONObject(i);

                        String ip = itemRaw.getString("ipv4");

                        List<String> hosts = equipment.getHosts();
                        if (!hosts.contains(ip)) {
                            hosts.add(ip);
                        }

                    }

                    url = Util.HTTP + equipment.getHosts().get(0) + ":" + FNAS.PORT + Util.LOGIN_PARAMETER;

                    Log.i(TAG, "login url:" + url);

                    str = FNAS.GetRemoteCall(url).getResponseData();

                    json = new JSONArray(str);
                    itemList = new ArrayList<>();
                    for (int i = 0; i < json.length(); i++) {
                        itemRaw = json.getJSONObject(i);
                        user = new User();
                        user.setUserName(itemRaw.getString("username"));
                        user.setUuid(itemRaw.getString("uuid"));
                        user.setAvatar(itemRaw.getString("avatar"));
                        itemList.add(user);
                    }

                    if (itemList.isEmpty())
                        return;

                    for (Equipment equipment1 : mUserLoadedEquipments) {
                        if (equipment1.getHosts().contains(equipment.getHosts().get(0)))
                            return;
                    }

                    mUserLoadedEquipments.add(equipment);
                    mUserExpandableLists.add(itemList);

                    Log.i(TAG, "EquipmentSearch: " + mUserExpandableLists.toString());

                    //update list
                    mHandler.sendEmptyMessage(DATA_CHANGE);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;
        instance.doOneTaskInCachedThread(runnable);

    }

    private class CustomHandler extends Handler {

        WeakReference<EquipmentSearchActivity> weakReference = null;

        CustomHandler(EquipmentSearchActivity activity, Looper looper) {
            super(looper);
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DATA_CHANGE:

                    if (mEquipmentExpandableListView.getVisibility() == View.GONE) {
                        mEquipmentExpandableListView.setVisibility(View.VISIBLE);
                        mLoadingLayout.setVisibility(View.GONE);
                    }

                    EquipmentExpandableAdapter adapter = weakReference.get().mAdapter;

                    adapter.equipmentList.clear();
                    adapter.mapList.clear();
                    adapter.equipmentList.addAll(mUserLoadedEquipments);
                    adapter.mapList.addAll(mUserExpandableLists);
                    adapter.viewLruCache.evictAll();
                    adapter.notifyDataSetChanged();
                    break;
                default:
            }
        }
    }

}

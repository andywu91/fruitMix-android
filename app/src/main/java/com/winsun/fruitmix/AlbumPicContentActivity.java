package com.winsun.fruitmix;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Administrator on 2016/4/28.
 */
public class AlbumPicContentActivity extends AppCompatActivity {

    public static final String TAG = AlbumPicContentActivity.class.getSimpleName();

    GridView mainGridView;
    ImageView ivBack;
    TextView mTitleTextView;

    Toolbar mToolBar;

    ArrayList<Media> mediaList;

    private MenuItem mPrivatePublicMenu;

    private MediaShare mediaShare;

    private Context mContext;

    private boolean mShowMenu;

    private RequestQueue mRequestQueue;

    private ImageLoader mImageLoader;

    private boolean mShowCommentBtn = false;

    private ProgressDialog mDialog;

    private boolean isOperated = false;

    private Bundle reenterState;

    private LocalBroadcastManager localBroadcastManager;
    private CustomReceiver customReceiver;
    private IntentFilter filter;

    private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (reenterState != null) {

                int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
                int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);

                if (initialPhotoPosition != currentPhotoPosition) {

                    names.clear();
                    sharedElements.clear();

                    Media media = mediaList.get(currentPhotoPosition);

                    String sharedElementName = media.getUuid();
                    View newSharedElement = mainGridView.findViewWithTag(sharedElementName);

                    names.add(sharedElementName);
                    sharedElements.put(sharedElementName, newSharedElement);
                }

                reenterState = null;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContext = this;

        setExitSharedElementCallback(sharedElementCallback);

        mRequestQueue = RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        mediaShare = getIntent().getParcelableExtra(Util.KEY_MEDIASHARE);
        mShowMenu = getIntent().getBooleanExtra(Util.NEED_SHOW_MENU, true);
        mShowCommentBtn = getIntent().getBooleanExtra(Util.KEY_SHOW_COMMENT_BTN, false);

        setContentView(R.layout.activity_album_pic_content);

        ivBack = (ImageView) findViewById(R.id.back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mainGridView = (GridView) findViewById(R.id.mainGrid);
        mainGridView.setAdapter(new PicGridViewAdapter(this));

        mTitleTextView = (TextView) findViewById(R.id.title);
        mTitleTextView.setText(mediaShare.getTitle());

        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mediaList = new ArrayList<>();
        fillPicList(mediaShare.getImageDigests());
        ((BaseAdapter) mainGridView.getAdapter()).notifyDataSetChanged();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        customReceiver = new CustomReceiver();
        filter = new IntentFilter(Util.LOCAL_SHARE_DELETED);
        filter.addAction(Util.REMOTE_SHARE_DELETED);

    }

    @Override
    protected void onResume() {
        super.onResume();

        localBroadcastManager.registerReceiver(customReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        localBroadcastManager.unregisterReceiver(customReceiver);
    }

    @Override
    public void onBackPressed() {
        if (isOperated)
            setResult(RESULT_OK);

        super.onBackPressed();
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        reenterState = new Bundle(data.getExtras());
        int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
        int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);

        if (initialPhotoPosition != currentPhotoPosition) {

            mainGridView.smoothScrollToPosition(currentPhotoPosition);

/*            ActivityCompat.postponeEnterTransition(AlbumPicContentActivity.this);
            mainGridView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mainGridView.getViewTreeObserver().removeOnPreDrawListener(this);
                    // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                    mainGridView.requestLayout();


                    return true;
                }
            });*/
            ActivityCompat.startPostponedEnterTransition(AlbumPicContentActivity.this);
        }
    }

    private void fillPicList(List<String> imageDigests) {

        Media picItemRaw;

        mediaList.clear();
        Media media;

        for (String aStArr : imageDigests) {
            media = new Media();
            picItemRaw = LocalCache.RemoteMediaMapKeyIsUUID.get(aStArr);

            Log.i(TAG, "media has it or not:" + (picItemRaw != null ? "true" : "false"));

            if (picItemRaw == null) {
                picItemRaw = LocalCache.LocalMediaMapKeyIsUUID.get(aStArr);

                media.setLocal(true);
                media.setThumb(picItemRaw.getThumb());
            } else {
                media.setLocal(false);
            }

            media.setUuid(picItemRaw.getUuid());
            media.setWidth(picItemRaw.getWidth());
            media.setHeight(picItemRaw.getHeight());
            media.setTime(picItemRaw.getTime());
            media.setSelected(false);

            mediaList.add(media);

        }
    }

    public void showSlider(int position, View sharedElement, String sharedElementName) {
        Intent intent = new Intent();
        intent.putExtra(Util.INITIAL_PHOTO_POSITION, position);
        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, mShowCommentBtn);
        intent.putParcelableArrayListExtra(Util.KEY_MEDIA_LIST, mediaList);
        intent.setClass(this, PhotoSliderActivity.class);

        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, sharedElement, sharedElementName);
        startActivity(intent, optionsCompat.toBundle());
    }

    class PicGridViewAdapter extends BaseAdapter {

        AlbumPicContentActivity activity;

        public PicGridViewAdapter(AlbumPicContentActivity activity_) {
            activity = activity_;
        }

        @Override
        public int getCount() {
            if (activity.mediaList == null) return 0;
            return activity.mediaList.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            final Media currentItem;
            final NetworkImageView ivMain;

            if (convertView == null)
                view = LayoutInflater.from(activity).inflate(R.layout.photo_list_cell_cell, parent, false);
            else view = convertView;

            currentItem = (Media) this.getItem(position);

            ivMain = (NetworkImageView) view.findViewById(R.id.mainPic);

            if (currentItem.isLocal()) {  // local bitmap path

                String url = String.valueOf(currentItem.getThumb());

                mImageLoader.setShouldCache(false);
                ivMain.setTag(url);
                ivMain.setDefaultImageResId(R.drawable.placeholder_photo);
                ivMain.setImageUrl(url, mImageLoader);

            } else {

                int width = Integer.parseInt(currentItem.getWidth());
                int height = Integer.parseInt(currentItem.getHeight());

                int[] result = Util.formatPhotoWidthHeight(width, height);

                String url = String.format(getString(R.string.thumb_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + currentItem.getUuid(), String.valueOf(result[0]), String.valueOf(result[1]));

                mImageLoader.setShouldCache(true);
                ivMain.setTag(url);
                ivMain.setDefaultImageResId(R.drawable.placeholder_photo);
                ivMain.setImageUrl(url, mImageLoader);
            }

            ivMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String sharedElementName = currentItem.getUuid();
                    ViewCompat.setTransitionName(ivMain, sharedElementName);
                    activity.showSlider(position, ivMain, currentItem.getUuid());
                }
            });

            return view;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public Object getItem(int position) {
            return activity.mediaList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (mShowMenu) {

            getMenuInflater().inflate(R.menu.album_menu, menu);

            mPrivatePublicMenu = menu.findItem(R.id.set_private_public);

            if (mediaShare.getViewer().isEmpty()) {
                mPrivatePublicMenu.setTitle(getString(R.string.set_public));
            } else {
                mPrivatePublicMenu.setTitle(getString(R.string.set_private));
            }
        }

        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.i("islock", mediaShare.isLocked() + "");

        if (Util.getNetworkState(mContext)) {
            if (mediaShare.isLocked()) {
                Toast.makeText(mContext, getString(R.string.share_uploading), Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            if (!mediaShare.isLocked()) {
                Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        if (!mediaShare.getMaintainer().contains(FNAS.userUUID)) {
            Toast.makeText(mContext, getString(R.string.no_edit_photo_permission), Toast.LENGTH_SHORT).show();

            return true;
        }

        Intent intent;
        switch (item.getItemId()) {
            case R.id.setting_album:
                intent = new Intent(this, ModifyAlbumActivity.class);
                intent.putExtra(Util.MEDIASHARE_UUID, mediaShare.getUuid());
                startActivityForResult(intent, Util.KEY_MODIFY_ALBUM_REQUEST_CODE);
                break;
            case R.id.edit_photo:
                intent = new Intent(this, EditPhotoActivity.class);
                intent.putExtra(Util.KEY_MEDIASHARE, mediaShare);
                startActivityForResult(intent, Util.KEY_EDIT_PHOTO_REQUEST_CODE);
                break;
            case R.id.set_private_public:
                setPublicPrivate();
                break;
            case R.id.delete_album:
                deleteCurrentAblum();
                break;
            default:
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Util.KEY_EDIT_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {

            MediaShare mediaShare = data.getParcelableExtra(Util.KEY_MEDIASHARE);

            fillPicList(mediaShare.getImageDigests());
            ((BaseAdapter) mainGridView.getAdapter()).notifyDataSetChanged();
        } else if (requestCode == Util.KEY_MODIFY_ALBUM_REQUEST_CODE && resultCode == RESULT_OK) {
            String title = data.getStringExtra(Util.UPDATED_ALBUM_TITLE);
            mediaShare.setTitle(title);
            mTitleTextView.setText(mediaShare.getTitle());
        }

        isOperated = true;
    }

    private void deleteCurrentAblum() {

        mDialog = ProgressDialog.show(mContext, getString(R.string.loading_title), getString(R.string.loading_message), true, false);

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.DELETE.name());
        if (Util.getNetworkState(mContext)) {
            intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIASHARE.name());
        } else {
            intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIASHARE.name());
        }

        intent.putExtra(Util.OPERATION_MEDIASHARE, mediaShare);
        localBroadcastManager.sendBroadcast(intent);

    }

    private void setPublicPrivate() {

        MediaShare cloneMediaShare = mediaShare.cloneMyself();

        if (cloneMediaShare.getViewer().isEmpty()) {
            cloneMediaShare.setViewer(new ArrayList<>(LocalCache.RemoteUserMapKeyIsUUID.keySet()));
        } else {
            cloneMediaShare.setViewer(Collections.<String>emptyList());
        }

        mDialog = ProgressDialog.show(mContext, getString(R.string.loading_title), getString(R.string.loading_message), true, false);

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.MODIFY.name());
        if (Util.getNetworkState(mContext)) {
            intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIASHARE.name());
        } else {
            intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIASHARE.name());
        }

        intent.putExtra(Util.OPERATION_MEDIASHARE, cloneMediaShare);
        localBroadcastManager.sendBroadcast(intent);

    }

    private class CustomReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (mDialog != null && mDialog.isShowing())
                mDialog.dismiss();

            if (intent.getAction().equals(Util.LOCAL_SHARE_DELETED) || intent.getAction().equals(Util.REMOTE_SHARE_DELETED)) {

                String result = intent.getStringExtra(Util.OPERATION_RESULT_NAME);

                OperationResult operationResult = OperationResult.valueOf(result);

                switch (operationResult) {
                    case SUCCEED:
                        ((Activity) mContext).setResult(RESULT_OK);
                        break;
                    case FAIL:
                        Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
                        break;
                }

                finish();

            } else if (intent.getAction().equals(Util.LOCAL_SHARE_MODIFIED) || intent.getAction().equals(Util.REMOTE_SHARE_MODIFIED)) {

                String result = intent.getStringExtra(Util.OPERATION_RESULT_NAME);

                OperationResult operationResult = OperationResult.valueOf(result);

                switch (operationResult) {
                    case SUCCEED:
                        Toast.makeText(mContext, getString(R.string.setting_succeed), Toast.LENGTH_SHORT).show();

                        if (mediaShare.getViewer().isEmpty()) {
                            mPrivatePublicMenu.setTitle(getString(R.string.set_public));
                        } else {
                            mPrivatePublicMenu.setTitle(getString(R.string.set_private));
                        }

                        isOperated = true;

                        break;
                    case FAIL:
                        Toast.makeText(mContext, getString(R.string.setting_fail), Toast.LENGTH_SHORT).show();
                        break;
                }

            }

        }
    }
}

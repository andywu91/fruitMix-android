package com.winsun.fruitmix.mediaModule;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoreMediaActivity extends AppCompatActivity {
    public static final String TAG = MoreMediaActivity.class.getSimpleName();

    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.more_photo_gridview)
    RecyclerView mMorePhotoRecyclerView;

    private int mSpanCount = 3;
    private Context mContext;
    private List<Media> mPhotos;
    private ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_photo);

        ButterKnife.bind(this);

        initImageLoader();

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mContext = this;

        MediaShare mediaShare = getIntent().getParcelableExtra(Util.KEY_MEDIASHARE);

        GridLayoutManager mManager = new GridLayoutManager(mContext, mSpanCount);
        mMorePhotoRecyclerView.setLayoutManager(mManager);
        mMorePhotoRecyclerView.setItemAnimator(new DefaultItemAnimator());

        MorePhotoAdapter mAdapter = new MorePhotoAdapter();
        mMorePhotoRecyclerView.setAdapter(mAdapter);

        mPhotos = new ArrayList<>();
        fillPhotoList(mediaShare.getMediaKeyInMediaShareContents());
        mAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;
    }

    private void initImageLoader() {
        mImageLoader = new ImageLoader(RequestQueueInstance.getInstance(this).getRequestQueue(), ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);
    }

    private void fillPhotoList(List<String> imageKeys) {

        mPhotos.clear();

        Media picItem;
        Media picItemRaw;
        for (String str : imageKeys) {

            picItemRaw = LocalCache.RemoteMediaMapKeyIsUUID.get(str);
            if (picItemRaw == null) {

                picItemRaw = LocalCache.LocalMediaMapKeyIsThumb.get(str);

                if (picItemRaw == null) {
                    continue;
                } else {

                    picItem = picItemRaw.cloneSelf();
                    picItem.setLocal(true);
                }

            } else {

                picItem = picItemRaw.cloneSelf();
                picItem.setLocal(false);

            }

            picItem.setSelected(false);

            mPhotos.add(picItem);
        }

        fillLocalCachePhotoData();
    }

    private void fillLocalCachePhotoData() {
        fillLocalCachePhotoList();

        fillLocalCachePhotoMap();
    }

    private void fillLocalCachePhotoList() {
        LocalCache.photoSliderList.clear();
        LocalCache.photoSliderList.addAll(mPhotos);

        Util.needRefreshPhotoSliderList = true;
    }

    private void fillLocalCachePhotoMap() {
        LocalCache.photoSliderMap.clear();
        for (Media media : LocalCache.photoSliderList) {
            LocalCache.photoSliderMap.put(media.getImageThumbUrl(mContext), media);
            LocalCache.photoSliderMap.put(media.getImageOriginalUrl(mContext), media);
        }
    }

    class MorePhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.photo_item)
        NetworkImageView mPhotoItem;
        @BindView(R.id.more_photo_item_layout)
        LinearLayout mMorelPhotoItemLayout;

        private Media media;

        MorePhotoViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void refreshView(final int position) {
            media = mPhotos.get(position);

            String imageUrl = media.getImageThumbUrl(mContext);
            mImageLoader.setShouldCache(!media.isLocal());
            mPhotoItem.setTag(imageUrl);
            mPhotoItem.setDefaultImageResId(R.drawable.placeholder_photo);
            mPhotoItem.setImageUrl(imageUrl, mImageLoader);

            mMorelPhotoItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LocalCache.photoSliderList.clear();
                    LocalCache.photoSliderList.addAll(mPhotos);

                    Util.needRefreshPhotoSliderList = true;

                    Intent intent = new Intent();
                    intent.putExtra(Util.INITIAL_PHOTO_POSITION, getAdapterPosition());
                    intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, true);
                    intent.setClass(mContext, PhotoSliderActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    class MorePhotoAdapter extends RecyclerView.Adapter<MorePhotoViewHolder> {
        @Override
        public int getItemCount() {
            return mPhotos == null ? 0 : mPhotos.size();
        }

        @Override
        public MorePhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.more_photo_item, parent, false);

            return new MorePhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MorePhotoViewHolder holder, int position) {

            holder.refreshView(position);
        }

    }
}

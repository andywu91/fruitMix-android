package com.winsun.fruitmix;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.LocalCache;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = SettingActivity.class.getSimpleName();

    @BindView(R.id.back)
    ImageView mBackImageView;
    @BindView(R.id.auto_upload_photos_switch)
    SwitchCompat mAutoUploadPhotosSwitch;
    @BindView(R.id.mobile_network_upload_switch)
    SwitchCompat mMobileNetworkUploadSwitch;
    @BindView(R.id.cache_size)
    TextView mCacheSize;
    @BindView(R.id.clear_cache_layout)
    ViewGroup mClearCacheLayout;
    @BindView(R.id.already_upload_media_count)
    TextView mAlreadyUploadMediaCountTextView;

    private boolean mAutoUploadOrNot = false;

    private int mAlreadyUploadMediaCount = -1;
    private int mTotalLocalMediaCount = 0;

    private long mTotalCacheSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ButterKnife.bind(this);

        mBackImageView.setOnClickListener(this);
        mClearCacheLayout.setOnClickListener(this);

        mAutoUploadOrNot = LocalCache.getAutoUploadOrNot(this);
        mAutoUploadPhotosSwitch.setChecked(mAutoUploadOrNot);

        calcAlreadyUploadMediaCountAndTotalCacheSize();

    }


    private void calcAlreadyUploadMediaCountAndTotalCacheSize() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                int alreadyUploadMediaCount = 0;
                int totalUploadMediaCount = 0;

                for (Media media : LocalCache.LocalMediaMapKeyIsThumb.values()) {

                    if (media.getUploadedDeviceIDs().contains(LocalCache.DeviceID)) {
                        alreadyUploadMediaCount++;
                    }

                    totalUploadMediaCount++;
                }

                mAlreadyUploadMediaCount = alreadyUploadMediaCount;
                mTotalLocalMediaCount = totalUploadMediaCount;

                mTotalCacheSize = FileUtil.getTotalCacheSize(SettingActivity.this);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mAlreadyUploadMediaCountTextView.setVisibility(View.VISIBLE);
                mAlreadyUploadMediaCountTextView.setText(String.format(getString(R.string.already_upload_media_count_text), mAlreadyUploadMediaCount, mTotalLocalMediaCount));

                mCacheSize.setText(FileUtil.formatFileSize(mTotalCacheSize));

            }
        }.execute();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        boolean isChecked = mAutoUploadPhotosSwitch.isChecked();

        if (mAutoUploadOrNot != isChecked) {
            LocalCache.setAutoUploadOrNot(this, isChecked);

            if (isChecked) {
                LocalCache.setCurrentUploadDeviceID(this, LocalCache.DeviceID);
                EventBus.getDefault().post(new RequestEvent(OperationType.START_UPLOAD, null));
            } else {
                EventBus.getDefault().post(new RequestEvent(OperationType.STOP_UPLOAD, null));
            }
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.clear_cache_layout:

                new AlertDialog.Builder(this).setMessage(getString(R.string.confirm_delete))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                FileUtil.clearAllCache(SettingActivity.this);
                                dialog.dismiss();
                                mCacheSize.setText(FileUtil.formatFileSize(FileUtil.getTotalCacheSize(SettingActivity.this)));
                            }
                        }).setNegativeButton(getString(R.string.cancel), null).create().show();

                break;
        }

    }

}
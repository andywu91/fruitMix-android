package com.winsun.fruitmix;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Share;
import com.winsun.fruitmix.services.LocalShareUploadService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Administrator on 2016/4/28.
 */
public class CreateAlbumActivity extends AppCompatActivity {

    public static final String TAG = CreateAlbumActivity.class.getSimpleName();

    TextInputLayout mTitleLayout;
    TextInputEditText tfTitle, tfDesc;
    CheckBox ckPublic;
    CheckBox ckSetMaintainer;
    TextView btOK;
    ImageView ivBack;
    TextView mLayoutTitle;

    String mSelectedImageUUIDStr;

    private Context mContext;

    private ProgressDialog mDialog;

    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        mSelectedImageUUIDStr = getIntent().getStringExtra("mSelectedImageUUIDStr");
        Log.d(TAG, "mSelectedImageUUIDStr:" + mSelectedImageUUIDStr);
        setContentView(R.layout.activity_create_album);

        tfTitle = (TextInputEditText) findViewById(R.id.title_edit);
        mTitleLayout = (TextInputLayout) findViewById(R.id.title_textlayout);
        tfDesc = (TextInputEditText) findViewById(R.id.desc);
        ckPublic = (CheckBox) findViewById(R.id.sPublic);
        btOK = (TextView) findViewById(R.id.ok);
        ivBack = (ImageView) findViewById(R.id.back);
        ckSetMaintainer = (CheckBox) findViewById(R.id.set_maintainer);
        mLayoutTitle = (TextView) findViewById(R.id.layout_title);
        mLayoutTitle.setText(getString(R.string.create_album_text));

        mTitle = String.format(getString(R.string.title_hint), new SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE).format(new Date(System.currentTimeMillis())));
        mTitleLayout.setHint(mTitle);

        ckPublic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ckSetMaintainer.setClickable(true);
                } else {
                    ckSetMaintainer.setClickable(false);
                    if (ckSetMaintainer.isChecked()) {
                        ckSetMaintainer.setChecked(false);
                    }
                }
            }
        });
        ckSetMaintainer.setClickable(false);

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                Util.hideSoftInput(CreateAlbumActivity.this);

                final boolean sPublic, sSetMaintainer;
                final String title, desc;

                sPublic = ckPublic.isChecked();
                sSetMaintainer = ckSetMaintainer.isChecked();

                if (tfTitle.getText().toString().equals("")) {
                    title = mTitleLayout.getHint().toString();
                } else {
                    title = tfTitle.getText().toString();
                }

                desc = tfDesc.getText().toString();

                new AsyncTask<Object, Object, Boolean>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);
                    }

                    @Override
                    protected Boolean doInBackground(Object... params) {
                        String data, viewers, maintainers;
                        String[] selectedUIDArr;
                        int i;

                        selectedUIDArr = mSelectedImageUUIDStr.split(",");
                        data = "";
                        for (i = 0; i < selectedUIDArr.length; i++) {
                            data += ",{\\\"type\\\":\\\"media\\\",\\\"digest\\\":\\\"" + selectedUIDArr[i] + "\\\"}";
                        }

                        viewers = "";
                        if (sPublic) {
                            for (String key : LocalCache.UsersMap.keySet()) {
                                viewers += ",\\\"" + key + "\\\"";
                            }
                        } else viewers = ",";
                        if (viewers.length() == 0) {
                            viewers += ",";
                        }

                        Log.i(TAG, "viewers:" + viewers);

                        maintainers = "";
                        if (sSetMaintainer) {
                            for (String key : LocalCache.UsersMap.keySet()) {
                                maintainers += ",\\\"" + key + "\\\"";
                            }
                        } else maintainers = ",\\\"" + FNAS.userUUID + "\\\"";

                        Log.i(TAG, "miantianers:" + maintainers);

                        createAlbumInLocalAlbumDatabase(sPublic, sSetMaintainer, title, desc, mSelectedImageUUIDStr);
                        FNAS.loadLocalShare();

                        return true;

                    }

                    @Override
                    protected void onPostExecute(Boolean sSuccess) {

                        mDialog.dismiss();
                        if (Util.getNetworkState(mContext)) {
                            LocalShareUploadService.startActionLocalShareTask(mContext);
                        }

                        if (sSuccess) {
                            setResult(RESULT_OK);
                            finish();
                        } else {

                            Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();

                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }

                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void createAlbumInLocalAlbumDatabase(boolean isPublic, boolean otherMaintianer, String title, String desc, String digest) {

        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

        Share share = new Share();
        share.setUuid(Util.createLocalUUid());

        Log.i(TAG, "create album digest:" + digest);

        share.setImageDigests(Arrays.asList(digest.split(",")));
        share.setTitle(title);
        share.setDesc(desc);

        if (isPublic) {
            share.setViewer(new ArrayList<>(LocalCache.UsersMap.keySet()));
        } else share.setViewer(Collections.<String>emptyList());

        if (otherMaintianer) {
            share.setMaintainer(new ArrayList<>(LocalCache.UsersMap.keySet()));
        } else {
            share.setMaintainer(Collections.singletonList(FNAS.userUUID));
        }

        share.setCreator(FNAS.userUUID);
        share.setTime(String.valueOf(System.currentTimeMillis()));
        share.setAlbum(true);
        dbUtils.insertLocalShare(share);

    }

}

package com.winsun.fruitmix.init.system;

import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.file.station.FileRepository;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.logged.in.user.LoggedInUserRepository;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.media.local.media.LocalMediaRepository;
import com.winsun.fruitmix.media.remote.media.StationMediaRepository;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2017/7/18.
 */

public class InitSystem {

    public static final String TAG = InitSystem.class.getSimpleName();

    public static void initSystem(Context context) {

        FileDownloadManager.getInstance().initDBUtils(DBUtils.getInstance(context));

        HttpRequestFactory.destroyInstance();

        ButlerService.startButlerService(context);

        UserDataRepository.destroyInstance();

        StationMediaRepository.destroyInstance();

        LocalMediaRepository.destroyInstance();

        MediaDataSourceRepository.destroyInstance();

        FileRepository.destroyInstance();

        LoggedInUserRepository.destroyInstance();

        boolean result = FileUtil.createDownloadFileStoreFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create download file store folder failed");
        }

        result = FileUtil.createLocalPhotoMiniThumbnailFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create local photo mini thumbnail folder failed");
        }

        result = FileUtil.createLocalPhotoThumbnailFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create local photo thumbnail folder failed");
        }

        result = FileUtil.createOriginalPhotoFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create shared photo folder failed");
        }

    }
}
package com.winsun.fruitmix.executor;

import android.content.Context;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2016/11/21.
 */

public class DeleteDownloadedFileTask implements Callable<Boolean> {

    private List<String> fileUUIDs;
    private DBUtils dbUtils;

    public DeleteDownloadedFileTask(DBUtils dbUtils, List<String> downloadedFileUUIDs) {
        this.dbUtils = dbUtils;
        fileUUIDs = new ArrayList<>(downloadedFileUUIDs.size());
        fileUUIDs.addAll(downloadedFileUUIDs);
    }

    /**
     * Starts executing the active part of the class' code. This method is
     * called when a thread is started that has been created with a class which
     * implements {@code Runnable}.
     */
    @Override
    public Boolean call() {

        for (String fileUUID : fileUUIDs) {
            dbUtils.deleteDownloadedFileByUUIDAndCreatorUUID(fileUUID, FNAS.userUUID);
        }

        FileDownloadManager.INSTANCE.deleteFileDownloadItem(fileUUIDs);

        EventBus.getDefault().post(new OperationEvent(Util.DOWNLOADED_FILE_DELETED, new OperationSuccess()));

        return true;
    }
}

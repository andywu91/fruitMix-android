package com.winsun.fruitmix.executor;

import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fileModule.interfaces.FileDownloadUploadInterface;
import com.winsun.fruitmix.http.retrofit.RetrofitInstance;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by Administrator on 2017/3/24.
 */

public class RetrieveSharedPhotoThumbTask implements Callable<Boolean> {

    public static final String TAG = RetrieveSharedPhotoThumbTask.class.getSimpleName();

    private List<Media> medias;
    private DBUtils dbUtils;

    public RetrieveSharedPhotoThumbTask(List<Media> medias, DBUtils dbUtils) {
        this.medias = medias;
        this.dbUtils = dbUtils;
    }

    @Override
    public Boolean call(){

        Log.d(TAG, "call: begin retrieve shared photo thumb task");

        FileDownloadUploadInterface fileDownloadUploadInterface = RetrofitInstance.INSTANCE.getRetrofitInstance().create(FileDownloadUploadInterface.class);

        for (Media media : medias) {
            Call<ResponseBody> call = fileDownloadUploadInterface.downloadFile(FNAS.Gateway + ":" + FNAS.PORT + Util.MEDIA_PARAMETER + "/" + media.getUuid() + "/download");

            boolean result = false;
            try {
                result = FileUtil.downloadMediaToSharedPhotoFolder(call.execute().body(), media);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (result)
                dbUtils.updateRemoteMedia(media);
        }

        Log.d(TAG, "call: finish retrieve shared photo thumb task");

        EventBus.getDefault().post(new OperationEvent(Util.SHARED_PHOTO_THUMB_RETRIEVED, new OperationSuccess()));

        return null;
    }
}

package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.MediaShareContent;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ModifyMediaInRemoteMediaShareService extends IntentService {

    private static final String TAG = ModifyMediaInLocalMediaShareService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_MODIFY_MEDIA = "com.winsun.fruitmix.services.action.modify_media";

    // TODO: Rename parameters
    private static final String EXTRA_ORIGINAL_MEDIASHARE = "com.winsun.fruitmix.services.extra.original_mediashare";
    private static final String EXTRA_MODIFIED_MEDIASHARE = "com.winsun.fruitmix.services.extra.modified_mediashare";

    public ModifyMediaInRemoteMediaShareService() {
        super("EditPhotoInMediaShareService");
    }

    private LocalBroadcastManager localBroadcastManager;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionEditPhotoInMediaShare(Context context, MediaShare originalMediashare, MediaShare modifiedMediashare) {
        Intent intent = new Intent(context, ModifyMediaInLocalMediaShareService.class);
        intent.setAction(ACTION_MODIFY_MEDIA);
        intent.putExtra(EXTRA_ORIGINAL_MEDIASHARE, originalMediashare);
        intent.putExtra(EXTRA_MODIFIED_MEDIASHARE, modifiedMediashare);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MODIFY_MEDIA.equals(action)) {
                MediaShare originalMediashare = intent.getParcelableExtra(EXTRA_ORIGINAL_MEDIASHARE);
                MediaShare modifiedMediashare = intent.getParcelableExtra(EXTRA_MODIFIED_MEDIASHARE);
                handleActionModifyMedia(originalMediashare, modifiedMediashare);
            }
        }
    }

    /**
     * Handle action edit photo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionModifyMedia(MediaShare originalMediaShare, MediaShare modifiedMediaShare) {

        List<MediaShareContent> originalMediaShareContents = originalMediaShare.getMediaShareContents();
        List<MediaShareContent> modifiedMediaShareContents = modifiedMediaShare.getMediaShareContents();

        List<MediaShareContent> differentContentsInOriginalMediaShare = findDifferentMediaShareContentInFirstMediaShareContents(originalMediaShareContents, modifiedMediaShareContents);
        List<MediaShareContent> differentContentsInModifiedMediaShare = findDifferentMediaShareContentInFirstMediaShareContents(modifiedMediaShareContents, originalMediaShareContents);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED);

        List<String> differentDigestsInModifiedMediaShare = new ArrayList<>();
        for (MediaShareContent mediaShareContent : differentContentsInModifiedMediaShare) {
            differentDigestsInModifiedMediaShare.add(mediaShareContent.getDigest());
        }

        if (!Util.uploadImageDigestsIfNotUpload(this, differentDigestsInModifiedMediaShare)) {

            intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.FAIL.name());

            Log.i(TAG, "edit photo in remote mediashare fail");

        } else {

            StringBuilder stringBuilder = new StringBuilder("{\"commands\": \"[");

            for (MediaShareContent mediaShareContent : differentContentsInOriginalMediaShare) {
                stringBuilder.append("{\\\"op\\\":\\\"remove\\\",\\\"path\\\":\\\"").append(modifiedMediaShare.getUuid()).append("\\\",\\\"value\\\":{\\\"digest\\\":\\\"").append(mediaShareContent.getDigest()).append("\\\"}},");

            }

            for (MediaShareContent mediaShareContent : differentContentsInModifiedMediaShare) {
                stringBuilder.append("{\\\"op\\\":\\\"add\\\",\\\"path\\\":\\\"").append(modifiedMediaShare.getUuid()).append("\\\",\\\"value\\\":{\\\"type\\\":\\\"media\\\",\\\"digest\\\":\\\"").append(mediaShareContent.getDigest()).append("\\\"}},");

            }

            if (stringBuilder.lastIndexOf(",") != -1)
                stringBuilder.replace(stringBuilder.lastIndexOf(","), stringBuilder.length(), "]\"}");

            try {
                String result = FNAS.PatchRemoteCall(Util.MEDIASHARE_PARAMETER, stringBuilder.toString());

                if (result.length() > 0) {
                    intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.SUCCEED.name());

                    Log.i(TAG, "modify media in remote mediashare which source is network succeed");

                    DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

                    long dbResult = 0;

                    for (MediaShareContent mediaShareContent : differentContentsInOriginalMediaShare) {
                        dbResult = dbUtils.deleteRemoteMediaShareContentByID(mediaShareContent.getId());
                    }

                    for (MediaShareContent mediaShareContent : differentContentsInModifiedMediaShare) {
                        dbResult = dbUtils.insertRemoteMediaShareContent(mediaShareContent, modifiedMediaShare.getUuid());
                    }

                    Log.i(TAG, "modify media in remote mediashare which source is network result:" + dbResult);

                    MediaShare mapResult = LocalCache.RemoteMediaShareMapKeyIsUUID.put(modifiedMediaShare.getUuid(), modifiedMediaShare);

                    Log.i(TAG, "modify media in remote mediashare in map result:" + (mapResult != null ? "true" : "false"));
                }


            } catch (Exception e) {

                e.printStackTrace();

                intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.FAIL.name());

                Log.i(TAG, "edit photo in remote mediashare fail");
            }

        }
        localBroadcastManager.sendBroadcast(intent);


    }

    private List<MediaShareContent> findDifferentMediaShareContentInFirstMediaShareContents(List<MediaShareContent> firstMediaShareContents, List<MediaShareContent> secondMediaShareContents) {
        List<MediaShareContent> differentMediaShareContents = new ArrayList<>();
        for (MediaShareContent firstMediaShareContent : firstMediaShareContents) {

            int i;
            for (i = 0; i < secondMediaShareContents.size(); i++) {
                MediaShareContent secondMediaShareContent = secondMediaShareContents.get(i);

                if (secondMediaShareContent.getDigest().equals(firstMediaShareContent.getDigest())) {
                    break;
                }
            }

            if (i == secondMediaShareContents.size()) {
                differentMediaShareContents.add(firstMediaShareContent);
            }

        }
        return differentMediaShareContents;
    }
}

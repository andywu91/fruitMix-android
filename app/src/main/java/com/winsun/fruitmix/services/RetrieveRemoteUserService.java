package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteUserParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RetrieveRemoteUserService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RETRIEVE_REMOET_USER = "com.winsun.fruitmix.services.action.retrieve_remote_user";

    public RetrieveRemoteUserService() {
        super("RetrieveRemoteUserService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRetrieveRemoteUser(Context context) {
        Intent intent = new Intent(context, RetrieveRemoteUserService.class);
        intent.setAction(ACTION_RETRIEVE_REMOET_USER);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RETRIEVE_REMOET_USER.equals(action)) {
                handleActionRetrieveRemoteUser();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRetrieveRemoteUser() {

        List<User> users;

        ConcurrentMap<String, User> userConcurrentMap;
        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

        try {

            String json = FNAS.loadUser();

            RemoteDataParser<User> parser = new RemoteUserParser();
            users = parser.parse(json);

            userConcurrentMap = LocalCache.BuildRemoteUserMapKeyIsUUID(users);

            dbUtils.deleteAllRemoteUser();
            dbUtils.insertRemoteUsers(userConcurrentMap);

        } catch (Exception e) {
            e.printStackTrace();

            users = dbUtils.getAllRemoteUser();

            userConcurrentMap = LocalCache.BuildRemoteUserMapKeyIsUUID(users);
        }

        LocalCache.RemoteUserMapKeyIsUUID.clear();

        LocalCache.RemoteUserMapKeyIsUUID.putAll(userConcurrentMap);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(Util.REMOTE_USER_RETRIEVED);
        intent.putExtra(Util.OPERATION_RESULT_NAME, OperationResult.SUCCEED.name());
        localBroadcastManager.sendBroadcast(intent);
    }

}

package com.winsun.fruitmix.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.operationResult.OperationSuccess;

import org.greenrobot.eventbus.EventBus;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Administrator on 2016/4/29.
 */
public class Util {

    public static final String SHOW_ALBUM_TIPS = "show_album_tips";
    public static final String SHOW_PHOTO_RETURN_TIPS = "show_photo_return_tips";
    public static final String EQUIPMENT_GROUP_NAME = "equipment_group_name";
    public static final String EQUIPMENT_CHILD_NAME = "equipment_child_name";
    public static final String JWT = "jwt";
    public static final String GATEWAY = "gateway";
    public static final String USER_UUID = "user_uuid";
    public static final String MEDIASHARE_UUID = "mediashare_uuid";
    public static final String PASSWORD = "password";
    public static final String EDIT_PHOTO = "edit_photo";
    public static final String UPDATED_ALBUM_TITLE = "updated_album_title";
    public static final String IMAGE_UUID = "image_uuid";
    public static final String FOLDER_UUID = "folder_uuid";
    public static final String FILE_UUID = "file_uuid";
    public static final String FILE_NAME = "file_name";

    public static final String LOCAL_SHARE_CREATED = "local_share_created";
    public static final String LOCAL_SHARE_MODIFIED = "local_share_modified";
    public static final String LOCAL_SHARE_DELETED = "local_share_deleted";

    public static final String REMOTE_SHARE_CREATED = "remote_share_created";
    public static final String REMOTE_SHARE_MODIFIED = "remote_share_modified";
    public static final String REMOTE_SHARE_DELETED = "remote_share_deleted";

    public static final String DOWNLOADED_FILE_DELETED = "downloaded_file_deleted";

    public static final String LOCAL_COMMENT_CREATED = "local_comment_created";
    public static final String REMOTE_COMMENT_CREATED = "remote_comment_created";
    public static final String LOCAL_COMMENT_DELETED = "local_comment_deleted";

    public static final String NEW_LOCAL_MEDIA_IN_CAMERA_RETRIEVED = "new_local_media_in_camera_retrieved";

    public static final String PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED = "photo_in_remote_media_share_modified";
    public static final String PHOTO_IN_LOCAL_MEDIASHARE_MODIFIED = "photo_in_local_media_share_modified";

    public static final String LOCAL_MEDIA_COMMENT_RETRIEVED = "local_media_comment_retrieved";
    public static final String REMOTE_MEDIA_COMMENT_RETRIEVED = "remote_media_comment_retrieved";
    public static final String REMOTE_MEDIA_SHARE_RETRIEVED = "remote_media_share_retrieved";
    public static final String LOCAL_MEDIA_SHARE_RETRIEVED = "local_media_share_retrieved";
    public static final String LOCAL_MEDIA_RETRIEVED = "local_media_retrieved";
    public static final String REMOTE_MEDIA_RETRIEVED = "remote_media_retrieved";
    public static final String REMOTE_USER_RETRIEVED = "remote_user_retrieved";
    public static final String REMOTE_TOKEN_RETRIEVED = "remote_token_retrieved";
    public static final String REMOTE_DEVICEID_RETRIEVED = "remote_deviceid_retrieved";
    public static final String REMOTE_FILE_RETRIEVED = "remote_file_retrieved";
    public static final String REMOTE_FILE_SHARE_RETRIEVED = "remote_file_share_retrieved";

    public static final String DOWNLOADED_FILE_RETRIEVED = "downloaded_file_retrieved";

    public static final String LOCAL_PHOTO_UPLOAD_STATE_CHANGED = "local_photo_upload_state_changed";

    public static final String OPERATION = "operation";
    public static final String OPERATION_RESULT_NAME = "operation_result";
    public static final String OPERATION_TYPE_NAME = "operation_type";
    public static final String OPERATION_TARGET_TYPE_NAME = "operation_target_type_name";
    public static final String OPERATION_IMAGE_UUID = "operation_image_uuid";
    public static final String OPERATION_COMMENT = "operation_comment";
    public static final String OPERATION_MEDIA = "operatin_media";
    public static final String OPERATION_MEDIASHARE = "operation_mediashare";
    public static final String OPERATION_ORIGINAL_MEDIASHARE_WHEN_EDIT_PHOTO = "operation_original_mediashare";
    public static final String OPERATION_MODIFIED_MEDIASHARE_WHEN_EDIT_PHOTO = "operation_modified_mediashare";

    public static final String NEED_SHOW_MENU = "need_show_menu";
    public static final String KEY_SHOW_COMMENT_BTN = "key_show_comment_btn";
    public static final String KEY_AUTHORIZATION = "Authorization";
    public static final String KEY_JWT_HEAD = "JWT ";
    public static final String KEY_BASE_HEAD = "Basic ";

    public static final String ADD = "add";
    public static final String DELETE = "delete";

    public static final int KEY_MODIFY_ALBUM_REQUEST_CODE = 100;
    public static final int KEY_EDIT_PHOTO_REQUEST_CODE = 101;
    public static final int KEY_CHOOSE_PHOTO_REQUEST_CODE = 102;
    public static final int KEY_LOGIN_REQUEST_CODE = 103;
    public static final int KEY_CREATE_ALBUM_REQUEST_CODE = 104;
    public static final int KEY_ALBUM_CONTENT_REQUEST_CODE = 105;
    public static final int KEY_CREATE_SHARE_REQUEST_CODE = 106;
    public static final int KEY_MANUAL_INPUT_IP_REQUEST_CODE = 107;


    public static final String HTTP = "http://";
    public static final String MEDIASHARE_PARAMETER = "/mediashare";
    public static final String MEDIA_PARAMETER = "/media";
    public static final String USER_PARAMETER = "/users";
    public static final String TOKEN_PARAMETER = "/token";
    public static final String LOGIN_PARAMETER = "/login";
    public static final String DEVICE_ID_PARAMETER = "/libraries";
    public static final String FILE_PARAMETER = "/files";
    public static final String FILE_SHARE_PARAMETER = "/share";

    public static final String FILE_SHARED_WITH_ME_PARAMETER = "/sharedWithMe";
    public static final String FILE_SHARED_WITH_OTHERS_PARAMETER = "/sharedWithOthers";

    public static final String FRUITMIX_SHAREDPREFERENCE_NAME = "fruitMix";

    public static final String DEVICE_ID_MAP_NAME = "deviceID";

    static final String HTTP_POST_METHOD = "POST";
    static final String HTTP_PATCH_METHOD = "PATCH";
    static final String HTTP_DELETE_METHOD = "DELETE";
    public static final int HTTP_CONNECT_TIMEOUT = 15 * 1000;

    public static final String INITIAL_PHOTO_POSITION = "initial_photo_position";
    public static final String CURRENT_PHOTO_POSITION = "current_photo_position";
    public static final String CURRENT_MEDIA_UUID = "current_media_uuid";
    public static final String CURRENT_MEDIASHARE_TIME = "current_mediashare_time";

    public static final String KEY_MEDIASHARE = "key_mediashare";

    public static final String KEY_NEW_SELECTED_IMAGE_UUID_ARRAY = "key_new_selected_image_uuid_array";

    public static final String KEY_ALREADY_SELECTED_IMAGE_UUID_ARRAYLIST = "key_already_selected_image_uuid_arraylist";

    public static final String KEY_TRANSITION_PHOTO_NEED_SHOW_THUMB = "key_transition_photo_need_show_thumb";

    public static final String KEY_NEED_TRANSITION = "key_need_transition";

    public static final String KEY_SHOW_SOFT_INPUT_WHEN_ENTER = "key_show_soft_input_when_enter";

    public static final String KEY_MODIFY_REMOTE_MEDIASHARE_REQUEST_DATA = "key_modify_remote_mediashare_request_data";

    public static final String KEY_MANUAL_INPUT_IP = "key_manual_input_ip";

    public static boolean loginState = false;

    public static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    /**
     * 将dp转化为px
     */
    public static int dip2px(Context context, float dip) {
        float v = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
        return (int) (v + 0.5f);
    }

    public static int calcScreenWidth(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);

        return metric.widthPixels;
    }

    public static String CalcSHA256OfFile(String fname) {
        MessageDigest md;
        FileInputStream fin;
        byte[] buffer;
        byte[] digest;
        String digits = "0123456789abcdef";
        int len, i;
        String st;

        try {
            buffer = new byte[15000];
            md = MessageDigest.getInstance("SHA-256");
            fin = new FileInputStream(fname);
            len = 0;
            while ((len = fin.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fin.close();
            digest = md.digest();
            st = "";
            for (i = 0; i < digest.length; i++) {
                st += digits.charAt((digest[i] >> 4) & 0xf);
                st += digits.charAt(digest[i] & 0xf);
            }
            return st;
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean getNetworkState(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED && loginState) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String createLocalUUid() {
        return UUID.randomUUID().toString();
    }

    /**
     * format time
     *
     * @param createTime milliseconds
     * @return formated time
     */
    public static String formatTime(Context context, long createTime) {

        StringBuilder builder = new StringBuilder();

        long currentTime = System.currentTimeMillis();

        Date date = new Date();
        long l = 24 * 60 * 60 & 1000;
        long timeDifference = date.getTime() - date.getTime() % l - 8 * 60 * 60 * 1000 - createTime;

        if (timeDifference < 0) {
            builder.append(new SimpleDateFormat("HH:mm:ss", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));

        } else if (timeDifference < 24 * 3600 * 1000) {
            builder.append(context.getString(R.string.yesterday));
            builder.append(new SimpleDateFormat("HH:mm", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));
        } else if (timeDifference < 4 * 24 * 3600 * 1000) {

            builder.append(new SimpleDateFormat("E", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));


        } else if (4 * 24 * 3600 * 1000 < timeDifference) {

            String createYear = new SimpleDateFormat("yyyy", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime));
            String currentYear = new SimpleDateFormat("yyyy", Locale.SIMPLIFIED_CHINESE).format(new Date(currentTime));

            String currentMonth = new SimpleDateFormat("MM", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime));
            if (createYear.equals(currentYear)) {

                if (currentMonth.startsWith("0")) {
                    builder.append(new SimpleDateFormat("M月dd日", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));
                } else {
                    builder.append(new SimpleDateFormat("MM月dd日", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));
                }

            } else {

                if (currentMonth.startsWith("0")) {
                    builder.append(new SimpleDateFormat("yyyy年M月dd日", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));
                } else {
                    builder.append(new SimpleDateFormat("yyyy年MM月dd日", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));
                }

            }

        }

        return builder.toString();
    }

    public static void hideSoftInput(Activity activity) {
        InputMethodManager methodManager = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            methodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static void showSoftInput(Activity activity, View view) {
        InputMethodManager methodManager = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        methodManager.showSoftInput(view, 0);
    }

    public static int[] formatPhotoWidthHeight(int width, int height) {
        if (width >= height) {
            width = width * 200 / height;
            height = 200;
        } else {
            height = height * 200 / width;
            width = 200;
        }

        return new int[]{width, height};
    }

    public static boolean checkRunningOnLollipopOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean uploadImageDigestsIfNotUpload(Context context, List<String> imageDigests) {
        boolean uploadFileResult = true;
        int uploadSucceedCount = 0;
        Media media;

        for (String imageDigest : imageDigests) {

            media = LocalCache.LocalMediaMapKeyIsUUID.get(imageDigest);
            if (media != null) {
                uploadFileResult = media.uploadIfNotDone(context);

                if (!uploadFileResult)
                    break;
                else {
                    uploadSucceedCount++;
                }
            }

        }

        if (uploadSucceedCount > 0) {

            EventBus.getDefault().post(new OperationEvent(Util.LOCAL_PHOTO_UPLOAD_STATE_CHANGED, new OperationSuccess()));
        }

        return uploadFileResult;
    }

    public static String removeWrap(String str) {
        return str.replaceAll("\r|\n", "");
    }
}

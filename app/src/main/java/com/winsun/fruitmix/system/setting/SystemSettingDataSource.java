package com.winsun.fruitmix.system.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/7/18.
 */

public class SystemSettingDataSource {

    public static final String TAG = SystemSettingDataSource.class.getSimpleName();

    private SharedPreferences sharedPreferences;

    private String showAutoUploadDialogKey = "showAutoUploadDialog";

    private static final String CURRENT_UPLOAD_USER_UUID = "current_upload_user_uuid";

    private static final String CURRENT_LOGIN_USER_UUID = "current_login_user_uuid";

    private static final String CURRENT_LOGIN_TOKEN = "current_login_token";

    private static final String CURRENT_WA_TOKEN = "current_wa_token";

    private static final String CURRENT_LOGIN_USER_GUID = "current_login_user_guid";

    private static final String CURRENT_LOGIN_STATION_ID = "current_login_station_id";

    private static final String CURRENT_EQUIPMENT_IP = "current_equipment_ip";

    private static final String ONLY_AUTO_UPLOAD_WHEN_CONNECTED_WITH_WIFI = "only_auto_upload_when_connected_with_wifi";

    private static final String LOGIN_WITH_WECHAT_CODE_OR_NOT = "login_with_wechat_code_or_not";

    private static final String OPEN_TORRENT_FILE_DEFAULT_BEHAVIOR = "open_torrent_file_default_behavior";

    private static final String ASK_IF_NEW_FIRMWARE_VERSION_OCCUR = "ask_if_new_firmware_version_occur";

    public static final int OPEN_TORRENT_FILE_BEHAVIOR_CREATE_DOWNLOAD_TASK = 1;
    public static final int OPEN_TORRENT_FILE_BEHAVIOR_UPLOAD_FILE = 2;

    private static final String SHOW_PHOTO_RETURN_TIPS = "show_photo_return_tips";

    private static final String SHOW_PRODUCT_INTRODUCTION = "show_product_introduction";

    private static SystemSettingDataSource instance;

    static SystemSettingDataSource getInstance(Context context) {

        if (instance == null)
            instance = new SystemSettingDataSource(context);

        return instance;
    }

    public void destroyInstance() {
        instance = null;
    }

    private SystemSettingDataSource(Context context) {

        sharedPreferences = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);

    }

    public String getCurrentUploadUserUUID() {

        return sharedPreferences.getString(CURRENT_UPLOAD_USER_UUID, "");
    }

    public void setCurrentUploadUserUUID(String currentUploadUserUUID) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putString(CURRENT_UPLOAD_USER_UUID, currentUploadUserUUID);
        editor.apply();
    }

    public boolean getAutoUploadOrNot() {

        return sharedPreferences.getBoolean(Util.AUTO_UPLOAD_OR_NOT, true);
    }

    public void setAutoUploadOrNot(boolean autoUploadOrNot) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putBoolean(Util.AUTO_UPLOAD_OR_NOT, autoUploadOrNot);
        editor.apply();
    }

    public boolean getOnlyAutoUploadWhenConnectedWithWifi() {

        return sharedPreferences.getBoolean(ONLY_AUTO_UPLOAD_WHEN_CONNECTED_WITH_WIFI, true);

    }

    public void setOnlyAutoUploadWhenConnectedWithWifi(boolean onlyAutoUploadWhenConnectedWithWifi) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putBoolean(ONLY_AUTO_UPLOAD_WHEN_CONNECTED_WITH_WIFI, onlyAutoUploadWhenConnectedWithWifi);
        editor.apply();

    }

    public void setShowAutoUploadDialog(boolean showAutoUploadDialog) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putBoolean(showAutoUploadDialogKey, showAutoUploadDialog);
        editor.apply();

    }

    public boolean getShowAutoUploadDialog() {

        return sharedPreferences.getBoolean(showAutoUploadDialogKey, false);

    }

    public String getCurrentLoginUserUUID() {

        return sharedPreferences.getString(CURRENT_LOGIN_USER_UUID, "");

    }

    public void setCurrentLoginUserUUID(String currentLoginUserUUID) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putString(CURRENT_LOGIN_USER_UUID, currentLoginUserUUID);
        editor.apply();

    }

    public String getCurrentLoginToken() {

        String token = sharedPreferences.getString(CURRENT_LOGIN_TOKEN, "");

        Log.d(TAG, "getCurrentLoginToken: " + token);

        return token;
    }

    public void setCurrentLoginToken(String token) {

        Log.d(TAG, "setCurrentLoginToken: " + token);

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putString(CURRENT_LOGIN_TOKEN, token);
        editor.apply();

    }

    public String getCurrentWAToken() {

        return sharedPreferences.getString(CURRENT_WA_TOKEN, "");

    }

    public void setCurrentWAToken(String token) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putString(CURRENT_WA_TOKEN, token);
        editor.apply();

    }

    public String getCurrentLoginUserGUID() {

        return sharedPreferences.getString(CURRENT_LOGIN_USER_GUID, "");

    }

    public void setCurrentLoginUserGUID(String guid) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putString(CURRENT_LOGIN_USER_GUID, guid);
        editor.apply();

    }


    public String getCurrentLoginStationID() {

        return sharedPreferences.getString(CURRENT_LOGIN_STATION_ID, "");

    }

    public void setCurrentLoginStationID(String currentLoginUserUUID) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putString(CURRENT_LOGIN_STATION_ID, currentLoginUserUUID);
        editor.apply();

    }

    public String getCurrentEquipmentIp() {

        return sharedPreferences.getString(CURRENT_EQUIPMENT_IP, "");

    }

    public void setCurrentEquipmentIp(String currentEquipmentIp) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putString(CURRENT_EQUIPMENT_IP, currentEquipmentIp);
        editor.apply();

    }

    public boolean getLoginWithWechatCodeOrNot() {

        return sharedPreferences.getBoolean(LOGIN_WITH_WECHAT_CODE_OR_NOT, false);

    }

    public void setLoginWithWechatCodeOrNot(boolean isLoginWithWeChatCodeOrNot) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putBoolean(LOGIN_WITH_WECHAT_CODE_OR_NOT, isLoginWithWeChatCodeOrNot);
        editor.apply();

    }

    public int getOpenTorrentFileBehavior() {
        return sharedPreferences.getInt(OPEN_TORRENT_FILE_DEFAULT_BEHAVIOR, 0);
    }

    public void setOpenTorrentFileDefaultBehavior(int behavior) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putInt(OPEN_TORRENT_FILE_DEFAULT_BEHAVIOR, behavior);
        editor.apply();

    }

    public void setAskIfNewFirmwareVersionOccur(boolean askIfNewFirmwareVersionOccur) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putBoolean(ASK_IF_NEW_FIRMWARE_VERSION_OCCUR, askIfNewFirmwareVersionOccur);
        editor.apply();

    }

    public boolean getAskIfNewFirmwareVersionOccur() {
        return sharedPreferences.getBoolean(ASK_IF_NEW_FIRMWARE_VERSION_OCCUR, true);
    }

    public boolean getShowPhotoReturnTipsValue() {

        return sharedPreferences.getBoolean(SHOW_PHOTO_RETURN_TIPS, true);
    }

    public void setShowPhotoReturnTipsValue(boolean value) {

        SharedPreferences.Editor editor;

        editor = sharedPreferences.edit();
        editor.putBoolean(SHOW_PHOTO_RETURN_TIPS, value);
        editor.apply();
    }

    public boolean needShowProductIntroduction() {
        return sharedPreferences.getBoolean(SHOW_PRODUCT_INTRODUCTION, true);
    }

    public void setShowProductIntroduction(boolean value) {

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(SHOW_PRODUCT_INTRODUCTION, value);
        editor.apply();
    }

}

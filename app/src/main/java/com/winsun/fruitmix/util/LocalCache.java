package com.winsun.fruitmix.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.winsun.fruitmix.component.BigLittleImageView;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2016/4/22.
 */
public class LocalCache {

    private static final String TAG = LocalCache.class.getSimpleName();

    private static String CacheRootPath;

    public static ConcurrentMap<String, List<Comment>> RemoteMediaCommentMapKeyIsImageUUID = null;
    public static ConcurrentMap<String, List<Comment>> LocalMediaCommentMapKeyIsImageUUID = null;
    public static ConcurrentMap<String, MediaShare> RemoteMediaShareMapKeyIsUUID = null;
    public static ConcurrentMap<String, MediaShare> LocalMediaShareMapKeyIsUUID = null;
    public static ConcurrentMap<String, User> RemoteUserMapKeyIsUUID = null;
    public static ConcurrentMap<String, Media> RemoteMediaMapKeyIsUUID = null;
    public static ConcurrentMap<String, Media> LocalMediaMapKeyIsOriginalPhotoPath = null;
    public static ConcurrentMap<String, AbstractRemoteFile> RemoteFileMapKeyIsUUID = null;
    public static List<AbstractRemoteFile> RemoteFileShareList = null;

    public static String DeviceID = null;

    public static String currentEquipmentName;

    public static List<LoggedInUser> LocalLoggedInUsers = null;

    public static List<String> mediaUUIDsInCreateAlbum = null;

    public static List<MediaShare> RecommendMediaShares = null;

    //optimize get media from db, modify send media info mode: use static list instead of put it into bundle

    // optimize photo list view refresh view when data is too large

    public static boolean DeleteFile(File file) {
        File[] files;
        int i;

        if (file.isDirectory()) {
            files = file.listFiles();
            for (i = 0; i < files.length; i++) {
                if (file.isFile() || file.isDirectory()) DeleteFile(files[i]);
            }
        }

        return file.delete();
    }

    public static void CleanAll(final Context context) {

        LocalCache.dropGlobalData(context, Util.DEVICE_ID_MAP_NAME);

        DeviceID = null;

        ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;
        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {
                DBUtils dbUtils = DBUtils.getInstance(context);
                dbUtils.deleteAllLocalShare();
                dbUtils.deleteAllLocalComment();
                dbUtils.deleteAllRemoteComment();
                dbUtils.deleteAllRemoteShare();
                dbUtils.deleteAllRemoteUser();
                dbUtils.deleteAllRemoteMedia();
            }
        });

    }

    public static void initCurrentEquipmentName() {

        if (LocalLoggedInUsers == null || FNAS.userUUID == null)
            return;

        for (LoggedInUser loggedInUser : LocalLoggedInUsers) {
            if (loggedInUser.getUser().getUuid().equals(FNAS.userUUID)) {

                currentEquipmentName = loggedInUser.getEquipmentName();
            }
        }

    }

    public static boolean Init() {

        if (RemoteMediaCommentMapKeyIsImageUUID == null)
            RemoteMediaCommentMapKeyIsImageUUID = new ConcurrentHashMap<>();
        else
            RemoteMediaCommentMapKeyIsImageUUID.clear();

        if (LocalMediaCommentMapKeyIsImageUUID == null)
            LocalMediaCommentMapKeyIsImageUUID = new ConcurrentHashMap<>();
        else
            LocalMediaCommentMapKeyIsImageUUID.clear();

        if (RemoteMediaShareMapKeyIsUUID == null)
            RemoteMediaShareMapKeyIsUUID = new ConcurrentHashMap<>();
        else
            RemoteMediaShareMapKeyIsUUID.clear();

        if (LocalMediaShareMapKeyIsUUID == null)
            LocalMediaShareMapKeyIsUUID = new ConcurrentHashMap<>();
        else
            LocalMediaShareMapKeyIsUUID.clear();

        if (RemoteUserMapKeyIsUUID == null)
            RemoteUserMapKeyIsUUID = new ConcurrentHashMap<>();
        else
            RemoteUserMapKeyIsUUID.clear();

        if (RemoteMediaMapKeyIsUUID == null)
            RemoteMediaMapKeyIsUUID = new ConcurrentHashMap<>();
        else
            RemoteMediaMapKeyIsUUID.clear();

        if (RemoteFileMapKeyIsUUID == null)
            RemoteFileMapKeyIsUUID = new ConcurrentHashMap<>();
        else
            RemoteFileMapKeyIsUUID.clear();

        if (RemoteFileShareList == null)
            RemoteFileShareList = new ArrayList<>();
        else
            RemoteFileShareList.clear();

        if (mediaUUIDsInCreateAlbum == null)
            mediaUUIDsInCreateAlbum = new ArrayList<>();
        else
            mediaUUIDsInCreateAlbum.clear();

        if (LocalMediaMapKeyIsOriginalPhotoPath == null)
            LocalMediaMapKeyIsOriginalPhotoPath = new ConcurrentHashMap<>();

        if (LocalLoggedInUsers == null)
            LocalLoggedInUsers = new ArrayList<>();
        else
            LocalLoggedInUsers.clear();

        if (RecommendMediaShares == null)
            RecommendMediaShares = new ArrayList<>();
        else
            RecommendMediaShares.clear();

        return true;
    }

    public static ConcurrentMap<String, MediaShare> BuildMediaShareMapKeyIsUUID(List<MediaShare> mediaShares) {

        ConcurrentMap<String, MediaShare> mediaShareConcurrentMap = new ConcurrentHashMap<>(mediaShares.size());
        for (MediaShare mediaShare : mediaShares) {
            mediaShareConcurrentMap.put(mediaShare.getUuid(), mediaShare);
        }
        return mediaShareConcurrentMap;
    }

    public static ConcurrentMap<String, User> BuildRemoteUserMapKeyIsUUID(List<User> users) {

        ConcurrentMap<String, User> userConcurrentMap = new ConcurrentHashMap<>(users.size());
        for (User user : users) {
            userConcurrentMap.put(user.getUuid(), user);
        }
        return userConcurrentMap;
    }

    public static ConcurrentMap<String, Media> BuildMediaMapKeyIsUUID(List<Media> medias) {

        ConcurrentMap<String, Media> mediaConcurrentMap = new ConcurrentHashMap<>(medias.size());
        for (Media media : medias) {
            mediaConcurrentMap.put(media.getUuid(), media);
        }
        return mediaConcurrentMap;
    }

    public static ConcurrentMap<String, Media> BuildMediaMapKeyIsThumb(List<Media> medias) {

        ConcurrentMap<String, Media> mediaConcurrentMap = new ConcurrentHashMap<>(medias.size());
        for (Media media : medias) {
            mediaConcurrentMap.put(media.getOriginalPhotoPath(), media);
        }
        return mediaConcurrentMap;
    }

    public static ConcurrentMap<String, Comment> BuildRemoteMediaCommentsAboutOneMedia(List<Comment> comments, String mediaUUID) {

        ConcurrentMap<String, Comment> commentConcurrentMap = new ConcurrentHashMap<>(comments.size());
        for (Comment comment : comments) {
            commentConcurrentMap.put(mediaUUID, comment);
        }
        return commentConcurrentMap;
    }

    public static Media findMediaInLocalMediaMap(String key) {

        Collection<Media> collection = LocalMediaMapKeyIsOriginalPhotoPath.values();

        for (Media media : collection) {
            if (media.getUuid().equals(key) || media.getOriginalPhotoPath().equals(key))
                return media;
        }

        return null;
    }

    public static MediaShare findMediaShareInLocalCacheMap(String mediaShareUUID) {

        MediaShare mediaShare = LocalMediaShareMapKeyIsUUID.get(mediaShareUUID);
        if (mediaShare == null)
            mediaShare = RemoteMediaShareMapKeyIsUUID.get(mediaShareUUID);

        return mediaShare;
    }

    public static String GetInnerTempFile() {
        return CacheRootPath + "/innerCache/" + ("" + Math.random()).replace(".", "");
    }

    public static boolean MoveTempFileToThumbCache(String tempFile, String key) {
        new File(tempFile).renameTo(new File(CacheRootPath + "/thumbCache/" + key));

        return true;
    }

    // get thumb bitmap
    public static void LoadRemoteBitmapThumb(final String key, final int width, final int height, final ImageView iv) {

        if (key == null) return;

        try {
            new AsyncTask<Object, Object, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Object... params) {
                    Bitmap bmp;
                    String key1, path1;
                    int sFind;


                    key1 = CalcDegist(key + "?" + width + "X" + height);
                    path1 = CacheRootPath + "/thumbCache/" + key1;
                    if (!new File(path1).exists()) {
                        sFind = FNAS.RetrieveFNASFile("/media/" + key + "?type=thumb&width=" + width + "&height=" + height, key1);
                        if (sFind == 0) return null;
                    }
                    bmp = BitmapFactory.decodeFile(path1);

                    return bmp;
                }

                @Override
                protected void onPostExecute(Bitmap bmp) {
                    try {
                        if (bmp != null) {
                            iv.setImageBitmap(bmp);
                            if (iv instanceof BigLittleImageView)
                                ((BigLittleImageView) iv).bigPic = bmp;
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } catch (java.util.concurrent.RejectedExecutionException e) {
            e.printStackTrace();
        }
    }


    //get full size bitmap
    public static void LoadRemoteBitmap(final String key, final BigLittleImageView iv) {

        if (key == null) return;

        new AsyncTask<Object, Object, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Object... params) {
                Bitmap bmp;
                String path;
                BitmapFactory.Options bmpOptions;
                BigLittleImageView.HotView2 = iv;
                path = CacheRootPath + "/thumbCache/" + key;
                if (!new File(path).exists()) {
                    FNAS.RetrieveFNASFile("/media/" + key + "?type=original", key);
                }

                if (BigLittleImageView.HotView2 == iv) {
                    bmpOptions = new BitmapFactory.Options();
                    bmpOptions.inJustDecodeBounds = true;
                    bmp = BitmapFactory.decodeFile(path, bmpOptions);
                    if (bmpOptions.outWidth > 4096 || bmpOptions.outHeight > 4096)
                        bmpOptions.inSampleSize = 2;
                    bmpOptions.inJustDecodeBounds = false;
                    bmp = BitmapFactory.decodeFile(path, bmpOptions);
                } else
                    bmp = null;

                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                if (bmp == null) return;
                if (BigLittleImageView.HotView2 != iv) {
                    //bmp.recycle();
                    bmp = null;
                    return;
                }
                try {
                    if (BigLittleImageView.HotView != null) {
                        if (BigLittleImageView.HotView.bigPic != null) {
                       /*     BigLittleImageView.HotView.bigPic.recycle();
                            BigLittleImageView.HotView.bigPic=null;
                            */
                        }
                        //BigLittleImageView.HotView.loadSmallPic();

                        //BigLittleImageView.HotView.setImageResource(R.drawable.yesshou);
                        System.gc();
                        Log.d(TAG, "Recycled");
                    }
                    iv.setImageBitmap(bmp);

                    iv.bigPic = bmp;
                    BigLittleImageView.HotView = iv;

                    if (iv.handler != null) iv.handler.sendEmptyMessage(100);

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public static String CalcDegist(String str) {
        MessageDigest mdInst;
        byte[] digest;
        String result;
        int i;
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(str.getBytes());
            digest = mdInst.digest();
            result = "";
            for (i = 0; i < 8; i++) {
                result += hexDigits[(digest[i] >> 4) & 0xf];
                result += hexDigits[digest[i] & 0xf];
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void LoadLocalBitmapThumb(final String path, final int width, final int height, final ImageView iv) {

        new AsyncTask<Object, Object, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Object... params) {
                String key, path1, pathTemp;
                Bitmap bmp;
                BitmapFactory.Options bmpOptions;
                int inSampleSize1, inSampleSize2;
                FileOutputStream fout;

                try {

                    key = CalcDegist(path + "?" + width + "X" + height);
                    path1 = CacheRootPath + "/thumbCache/" + key;
                    if (!new File(path1).exists()) {
                        bmpOptions = new BitmapFactory.Options();
                        bmpOptions.inJustDecodeBounds = true;
                        bmp = BitmapFactory.decodeFile(path, bmpOptions);
                        inSampleSize1 = bmpOptions.outWidth / width / 2;
                        inSampleSize2 = bmpOptions.outHeight / height / 2;
                        bmpOptions.inSampleSize = inSampleSize1 > inSampleSize2 ? inSampleSize2 : inSampleSize1;
                        bmpOptions.inJustDecodeBounds = false;
                        bmp = BitmapFactory.decodeFile(path, bmpOptions);
                        pathTemp = GetInnerTempFile();
                        fout = new FileOutputStream(new File(pathTemp));
                        bmp.compress(Bitmap.CompressFormat.JPEG, 80, fout);
                        fout.flush();
                        fout.close();
                        MoveTempFileToThumbCache(pathTemp, key);
                        return bmp;
                    } else {
//                        Log.d("winsun", "Cached!");
                        return BitmapFactory.decodeFile(path1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }


            @Override
            protected void onPostExecute(Bitmap bmp) {
                try {
                    if (bmp != null) {
                        iv.setImageBitmap(bmp);
                        if (iv instanceof BigLittleImageView)
                            ((BigLittleImageView) iv).bigPic = bmp;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public static void LoadLocalBitmap(final String path, final BigLittleImageView iv) {

        new AsyncTask<Object, Object, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Object... params) {
                Bitmap bmp;

                try {
                    bmp = BitmapFactory.decodeFile(path);
                    return bmp;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                try {
                    iv.setImageBitmap(bmp);
                    iv.bigPic = bmp;
                    BigLittleImageView.HotView = iv;

                    if (iv.handler != null) iv.handler.sendEmptyMessage(100);

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public static List<Media> PhotoList(Context context, String bucketName) {
        ContentResolver cr;
        String[] fields = {MediaStore.Images.Media._ID, MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION,
                MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE};
        Cursor cursor;
        List<Media> mediaList;
        Media media;
        File f;
        SimpleDateFormat df;
        Calendar date;

        String data = MediaStore.Images.Media.DATA;

        String thumbnailFolderPath = FileUtil.getLocalPhotoThumbnailFolderPath();
        String oldThumbnailFolderPath = FileUtil.getOldLocalPhotoThumbnailFolderPath();
        String thumbnailFolder200Path = FileUtil.getFolderPathForLocalPhotoThumbnailFolderName200();
        String originalFolderPath = FileUtil.getOriginalPhotoFolderPath();

        String selection = data + " not like ? and " + data + " not like ? and " + data + " not like ? and " + data + " not like ?";
        String[] selectionArgs = {thumbnailFolderPath + "%", oldThumbnailFolderPath + "%", thumbnailFolder200Path + "%", originalFolderPath + "%"};

        cr = context.getContentResolver();
//        cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "='" + bucketName + "'", null, null);

        cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, selection, selectionArgs, null);

        mediaList = new ArrayList<>();
        if (cursor == null || !cursor.moveToFirst()) return mediaList;

        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = Calendar.getInstance();

        Log.i(TAG, "PhotoList: cursor count: " + cursor.getCount());

        do {

            String originalPhotoPath = cursor.getString(cursor.getColumnIndexOrThrow(data));

            if (LocalMediaMapKeyIsOriginalPhotoPath == null)
                break;

            if (LocalCache.LocalMediaMapKeyIsOriginalPhotoPath.containsKey(originalPhotoPath)) {
                continue;
            }

            if (originalPhotoPath.contains(FileUtil.getLocalPhotoThumbnailFolderPath())) {
                continue;
            }

            if (originalPhotoPath.contains(FileUtil.getOldLocalPhotoThumbnailFolderPath())) {
                continue;
            }

            if (originalPhotoPath.contains(FileUtil.getFolderPathForLocalPhotoThumbnailFolderName200())) {
                continue;
            }

            if (originalPhotoPath.contains(FileUtil.getOriginalPhotoFolderPath()))
                continue;

            media = new Media();
            media.setOriginalPhotoPath(originalPhotoPath);
            media.setWidth(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)));
            media.setHeight(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)));

            f = new File(originalPhotoPath);
            date.setTimeInMillis(f.lastModified());
            media.setTime(df.format(date.getTime()));
            media.setSelected(false);
            media.setLoaded(false);

            int orientation = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION));

            switch (orientation) {
                case 0:
                    media.setOrientationNumber(1);
                    break;
                case 90:
                    media.setOrientationNumber(6);
                    break;
                case 180:
                    media.setOrientationNumber(4);
                    break;
                case 270:
                    media.setOrientationNumber(3);
                    break;
                default:
                    media.setOrientationNumber(1);
            }

            media.setLocal(true);
            media.setSharing(true);
            media.setUuid("");

            String longitude = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));
            String latitude = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));

            Log.d(TAG, "PhotoList: originalPhotoPath: " + media.getOriginalPhotoPath() + " longitude: " + longitude + " latitude: " + latitude);

            media.setLongitude(longitude);
            media.setLatitude(latitude);

            mediaList.add(media);

            Media mapResult = LocalCache.LocalMediaMapKeyIsOriginalPhotoPath.put(media.getOriginalPhotoPath(), media);

//            Log.i(TAG, "insert local media to map key is originalPhotoPath result:" + (mapResult != null ? "true" : "false"));

        }
        while (cursor.moveToNext());

        cursor.close();

        return mediaList;
    }

    private static List<Map<String, String>> getAllLocalMedia() {

        if (!FileUtil.checkExternalStorageState()) {
            return Collections.emptyList();
        }

        File file = new File(FileUtil.getExternalStorageDirectoryPath());

        SimpleDateFormat df;

        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        checkFile(file, df);

        return fileList;
    }

    private static List<Map<String, String>> fileList = new ArrayList<>();

    private static void checkFile(File file, SimpleDateFormat format) {

        if (file.isDirectory()) {

            File[] files = file.listFiles();

            if (files != null) {

                for (File f : files) {

                    checkFile(f, format);

                }

            }

        } else if (file.isFile()) {

            int dot = file.getName().lastIndexOf(".");

            if (dot > -1 && dot < file.getName().length()) {

                String extriName = file.getName().substring(dot, file.getName().length());

                if (extriName.equals(".gif")) {

                    Map<String, String> map = new HashMap<>();

                    map.put("thumb", file.getAbsolutePath());
                    map.put("width", "200");
                    map.put("height", "200");

                    Calendar date = Calendar.getInstance();
                    date.setTimeInMillis(file.lastModified());

                    map.put("lastModified", format.format(date.getTime()));

                    fileList.add(map);
                }

            }

        }

    }


    public static String getGlobalData(Context context, String name) {
        SharedPreferences sp;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getString(name, null);
    }

    public static void setGlobalData(Context context, String name, String data) {
        SharedPreferences sp;
        SharedPreferences.Editor mEditor;

        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        mEditor = sp.edit();
        mEditor.putString(name, data);
        mEditor.apply();
    }

    private static void dropGlobalData(Context context, String name) {
        SharedPreferences sp;
        SharedPreferences.Editor mEditor;

        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        mEditor = sp.edit();
        mEditor.putString(name, null);
        mEditor.apply();
    }

    public static void clearToken(Context context) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.JWT, null);
        editor.apply();
    }

    public static void saveToken(Context context, String jwt) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getApplicationContext().getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.JWT, jwt);
        editor.apply();
    }

    public static String getToken(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);

        return sp.getString(Util.JWT, null);
    }

    public static void saveGateway(Context context, String gateway) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.GATEWAY, gateway);
        editor.apply();
    }

    public static String getGateway(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);

        return sp.getString(Util.GATEWAY, null);
    }


    public static String getUserUUID(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);

        return sp.getString(Util.USER_UUID, "");

    }

    public static void saveUserUUID(Context context, String uuid) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.USER_UUID, uuid);
        editor.apply();
    }

    public static String getUserHome(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);

        return sp.getString(Util.USER_HOME, "");
    }

    public static User getUser(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);

        User user = new User();
        user.setUserName(sp.getString(Util.USER_NAME, ""));
        user.setDefaultAvatar(Util.getUserNameFirstLetter(user.getUserName()));
        user.setDefaultAvatarBgColor(sp.getInt(Util.USER_BG_COLOR, 0));
        user.setAdmin(sp.getBoolean(Util.USER_IS_ADMIN, false));
        user.setHome(sp.getString(Util.USER_HOME, ""));
        user.setUuid(sp.getString(Util.USER_UUID, ""));

        return user;
    }

    public static void saveUser(Context context, String userName, int userDefaultAvatarBgColor, boolean userIsAdmin, String userHome, String userUUID) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.USER_NAME, userName);
        editor.putInt(Util.USER_BG_COLOR, userDefaultAvatarBgColor);
        editor.putBoolean(Util.USER_IS_ADMIN, userIsAdmin);
        editor.putString(Util.USER_HOME, userHome);
        editor.putString(Util.USER_UUID, userUUID);
        editor.apply();
    }

    public static String getCurrentUploadDeviceID(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getString(Util.CURRENT_UPLOAD_DEVICE_ID, "");
    }

    public static void setCurrentUploadDeviceID(Context context, String currentUploadDeviceID) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.CURRENT_UPLOAD_DEVICE_ID, currentUploadDeviceID);
        editor.apply();
    }

    public static boolean getAutoUploadOrNot(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(Util.AUTO_UPLOAD_OR_NOT, true);
    }

    public static void setAutoUploadOrNot(Context context, boolean autoUploadOrNot) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putBoolean(Util.AUTO_UPLOAD_OR_NOT, autoUploadOrNot);
        editor.apply();
    }


}

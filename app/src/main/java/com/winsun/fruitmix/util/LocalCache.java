package com.winsun.fruitmix.util;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.winsun.fruitmix.BuildConfig;
import com.winsun.fruitmix.component.BigLittleImageView;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2016/4/22.
 */
public class LocalCache {

    private static final String TAG = LocalCache.class.getSimpleName();

    public static String CacheRootPath;
    static Application CurrentApp;

    public static ConcurrentMap<String, List<Comment>> RemoteMediaCommentMapKeyIsImageUUID = null;
    public static ConcurrentMap<String, List<Comment>> LocalMediaCommentMapKeyIsImageUUID = null;
    public static ConcurrentMap<String, MediaShare> RemoteMediaShareMapKeyIsUUID = null;
    public static ConcurrentMap<String, MediaShare> LocalMediaShareMapKeyIsUUID = null;
    public static ConcurrentMap<String, User> RemoteUserMapKeyIsUUID = null;
    public static ConcurrentMap<String, Media> RemoteMediaMapKeyIsUUID = null;
    public static ConcurrentMap<String, Media> LocalMediaMapKeyIsThumb = null;
    public static ConcurrentMap<String, Media> LocalMediaMapKeyIsUUID = null;

    public static String DeviceID = null;

    public static Map<String, Object> TransActivityContainer;

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

    public static void CleanAll() {

        LocalCache.DropGlobalData(Util.DEVICE_ID_MAP_NAME);

        DeviceID = null;

        ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;
        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {
                DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;
                dbUtils.deleteAllLocalShare();
                dbUtils.deleteAllLocalComment();
                dbUtils.deleteAllRemoteComment();
                dbUtils.deleteAllRemoteShare();
                dbUtils.deleteAllRemoteUser();
                dbUtils.deleteAllRemoteMedia();
                dbUtils.deleteAllLocalMedia();
            }
        });


    }

    public static boolean Init(Activity activity) {

        CurrentApp = activity.getApplication();

        TransActivityContainer = new HashMap<>();

        RemoteMediaCommentMapKeyIsImageUUID = new ConcurrentHashMap<>();
        LocalMediaCommentMapKeyIsImageUUID = new ConcurrentHashMap<>();
        RemoteMediaShareMapKeyIsUUID = new ConcurrentHashMap<>();
        LocalMediaShareMapKeyIsUUID = new ConcurrentHashMap<>();
        RemoteUserMapKeyIsUUID = new ConcurrentHashMap<>();
        RemoteMediaMapKeyIsUUID = new ConcurrentHashMap<>();
        LocalMediaMapKeyIsThumb = new ConcurrentHashMap<>();
        BuildLocalImagesMapsKeyIsUUID();

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
            mediaConcurrentMap.put(media.getThumb(), media);
        }
        return mediaConcurrentMap;
    }

    public static void BuildLocalImagesMapsKeyIsUUID() {
        Media itemRaw;

        LocalCache.LocalMediaMapKeyIsUUID = new ConcurrentHashMap<>();
        for (String key : LocalCache.LocalMediaMapKeyIsThumb.keySet()) {
            itemRaw = LocalCache.LocalMediaMapKeyIsThumb.get(key);
            LocalCache.LocalMediaMapKeyIsUUID.put(itemRaw.getUuid(), itemRaw);
        }
    }

    public static ConcurrentMap<String, Comment> BuildRemoteMediaCommentsAboutOneMedia(List<Comment> comments, String mediaUUID) {

        ConcurrentMap<String, Comment> commentConcurrentMap = new ConcurrentHashMap<>(comments.size());
        for (Comment comment : comments) {
            commentConcurrentMap.put(mediaUUID, comment);
        }
        return commentConcurrentMap;
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
                        Log.d("winsun", "Recycled");
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

    //get bucket photo list
    public static List<Map<String, String>> PhotoBucketList() {
        ContentResolver cr;
        String[] fields = {MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.PICASA_ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.SIZE, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Cursor cursor;
        Map<String, Map<String, String>> bucketMap;
        List<Map<String, String>> bucketList;
        Map<String, String> bucket;
        File f;
        SimpleDateFormat df;
        Calendar date;

        cr = CurrentApp.getContentResolver();
        cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, null, null, null);

        bucketList = new ArrayList<Map<String, String>>();
        if (!cursor.moveToFirst()) return bucketList;

        bucketMap = new HashMap<String, Map<String, String>>();

        do {
            bucket = bucketMap.get(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
            if (bucket == null) {
                bucket = new HashMap<String, String>();
                bucket.put("name", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
                bucket.put("thumb", "");
                bucket.put("lastModified", "0");
                bucketMap.put(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)), bucket);
            }
            f = new File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
            if (f.lastModified() > Long.parseLong(bucket.get("lastModified"))) {
                bucket.put("lastModified", f.lastModified() + "");
                bucket.put("thumb", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
            }
        }
        while (cursor.moveToNext());

        cursor.close();

        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = Calendar.getInstance();
        for (Map.Entry<String, Map<String, String>> entry : bucketMap.entrySet()) {
            date.setTimeInMillis(Long.parseLong(entry.getValue().get("lastModified")));
            entry.getValue().put("lastModified", df.format(date.getTime()));
            bucketList.add(entry.getValue());
        }

        return bucketList;
    }


    public static List<Map<String, String>> PhotoList(String bucketName) {
        ContentResolver cr;
        String[] fields = {MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.HEIGHT, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.PICASA_ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.SIZE, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Cursor cursor;
        List<Map<String, String>> imageList;
        Map<String, String> image;
        File f;
        SimpleDateFormat df;
        Calendar date;


        cr = CurrentApp.getContentResolver();
//        cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "='" + bucketName + "'", null, null);

        cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, null, null, null);


        imageList = new ArrayList<Map<String, String>>();
        if (!cursor.moveToFirst()) return imageList;

        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = Calendar.getInstance();

        do {
            image = new HashMap<String, String>();
            image.put("title", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
            image.put("bucket", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
            image.put("thumb", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
            image.put("width", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)));
            image.put("height", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)));
            f = new File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
            date.setTimeInMillis(f.lastModified());
            image.put("lastModified", df.format(date.getTime()));
            imageList.add(image);
        }
        while (cursor.moveToNext());

        cursor.close();

        return imageList;
    }

    public static String GetGlobalData(String name) {
        SharedPreferences sp;
        sp = CurrentApp.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getString(name, null);
    }

    public static void SetGlobalData(String name, String data) {
        SharedPreferences sp;
        SharedPreferences.Editor mEditor;

        sp = CurrentApp.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        mEditor = sp.edit();
        mEditor.putString(name, data);
        mEditor.apply();
    }

    public static void DropGlobalData(String name) {
        SharedPreferences sp;
        SharedPreferences.Editor mEditor;

        sp = CurrentApp.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        mEditor = sp.edit();
        mEditor.putString(name, null);
        mEditor.apply();
    }

    public static ConcurrentMap<String, MediaShare> GetGlobalMediaShareHashMap(String name) {
        String strData;
        ObjectInputStream ois;
        ConcurrentHashMap<String, MediaShare> dataList;

        try {
            strData = GetGlobalData(name);
            if (strData == null || strData.equals("")) return new ConcurrentHashMap<>();
            ois = new ObjectInputStream(new ByteArrayInputStream(Base64.decode(strData, Base64.DEFAULT)));
            dataList = (ConcurrentHashMap<String, MediaShare>) ois.readObject();
            ois.close();
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ConcurrentHashMap<>();
        }
    }

    public static void SetMediaShareGlobalHashMap(String name, ConcurrentMap<String, MediaShare> data) {
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.reset();
            oos.writeObject(data);
            oos.close();
            SetGlobalData(name, new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ConcurrentMap<String, Media> GetGlobalMediaHashMap(String name) {
        String strData;
        ObjectInputStream ois;
        ConcurrentHashMap<String, Media> dataList;

        try {
            strData = GetGlobalData(name);
            if (strData == null || strData.equals("")) return new ConcurrentHashMap<>();
            ois = new ObjectInputStream(new ByteArrayInputStream(Base64.decode(strData, Base64.DEFAULT)));
            dataList = (ConcurrentHashMap<String, Media>) ois.readObject();
            ois.close();
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ConcurrentHashMap<>();
        }
    }

    public static void SetGlobalMediaHashMap(String name, ConcurrentMap<String, Media> data) {
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.reset();
            oos.writeObject(data);
            oos.close();
            SetGlobalData(name, new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ConcurrentMap<String, User> GetGlobalUserHashMap(String name) {
        String strData;
        ObjectInputStream ois;
        ConcurrentHashMap<String, User> dataList;

        try {
            strData = GetGlobalData(name);
            if (strData == null || strData.equals("")) return new ConcurrentHashMap<>();
            ois = new ObjectInputStream(new ByteArrayInputStream(Base64.decode(strData, Base64.DEFAULT)));
            dataList = (ConcurrentHashMap<String, User>) ois.readObject();
            ois.close();
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ConcurrentHashMap<>();
        }
    }

    public static void SetGlobalUserHashMap(String name, ConcurrentMap<String, User> data) {
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.reset();
            oos.writeObject(data);
            oos.close();
            SetGlobalData(name, new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearGatewayUuidPasswordToken(Context context) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.GATEWAY, null);
        editor.putString(Util.USER_UUID, null);
        editor.putString(Util.PASSWORD, null);
        editor.putString(Util.JWT, null);
        editor.apply();
    }

    public static void clearToken(Context context) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.JWT, null);
        editor.apply();
    }


    public static void saveGateway(String gateway, Context context) {
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

    public static void saveJwt(String jwt, Context context) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.JWT, jwt);
        editor.apply();
    }

    public static String getJWT(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);

        return sp.getString(Util.JWT, null);
    }

    public static String getUuidValue(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getString(Util.USER_UUID, null);
    }

    public static String getPasswordValue(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getString(Util.PASSWORD, null);
    }

    public static String getUserNameValue(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getString(Util.EQUIPMENT_CHILD_NAME, null);
    }
}

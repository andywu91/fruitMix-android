package com.winsun.fruitmix.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.file.data.download.FileDownloadErrorState;
import com.winsun.fruitmix.file.data.download.FileDownloadFinishedState;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.download.FileDownloadingState;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.DownloadTask;
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.ErrorTaskState;
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.FinishTaskState;
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.StartingTaskState;
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.StateType;
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2016/10/25.
 */

public class FileUtil {

    public static final String TAG = FileUtil.class.getSimpleName();

    private static final String WISNUC_FOLDER_NAME = "winsuc";

    private static final String LOCAL_PHOTO_THUMBNAIL_FOLDER_NAME = "thumbnail_200";

    private static final String LOCAL_PHOTO_MINI_THUMBNAIL_FOLDER_NAME = "thumbnail_64";

    private static final String OLD_LOCAL_PHOTO_THUMBNAIL_FOLDER_NAME = "thumbnail";

    private static final String ORIGINAL_PHOTO_FOLDER_NAME = "originalPhoto";

    private static final String AUDIO_RECORD_FOLDER_NAME = "audioRecord";

    private static final String NO_MEDIA = ".nomedia";

    public static boolean checkExternalStorageState() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean checkExternalDirectoryForDownloadAvailableSizeEnough() {

        StatFs statFs = new StatFs(getExternalDirectoryPathForDownload());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            long availableBlocks = statFs.getAvailableBlocksLong();
            long blockSize = statFs.getBlockSizeLong();

            return availableBlocks * blockSize > 0;

        } else {
            long availableBlocks = statFs.getAvailableBlocks();
            long blockSize = statFs.getBlockSize();

            return availableBlocks * blockSize > 0;
        }
    }

    public static String getTemporaryDataFolderParentFolderPath(Context context) {

        return getExternalCacheDirPath(context) + File.separator + WISNUC_FOLDER_NAME;

    }

    private static String getExternalCacheDirPath(Context context) {

        if (checkExternalStorageState()) {

            File cacheDir = context.getExternalCacheDir();

            if (cacheDir != null)
                return cacheDir.getAbsolutePath();
            else
                return "";

        } else
            return "";
    }

    public static String getExternalStorageDirectoryPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    private static String getExternalDirectoryPathForDownload() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    public static boolean createDownloadFileStoreFolder() {
        return createFolder(getDownloadFileStoreFolderPath());
    }

    public static boolean createLocalPhotoMiniThumbnailFolder() {
        return createFolder(getLocalPhotoMiniThumbnailFolderPath());
    }

    public static boolean createLocalPhotoMiniThumbnailNoMediaFile(Context context) {

        String path = getLocalPhotoMiniThumbnailFolderPath() + File.separator + NO_MEDIA;

        boolean result = createFile(path);

        if (result)
            scanMediaStore(context, new File(path));

        return result;
    }

    public static boolean createLocalPhotoThumbnailFolder() {
        return createFolder(getLocalPhotoThumbnailFolderPath());
    }

    public static boolean createLocalPhotoThumbnailNoMediaFile(Context context) {
        String path = getLocalPhotoThumbnailFolderPath() + File.separator + NO_MEDIA;

        boolean result = createFile(path);

        if (result)
            scanMediaStore(context, new File(path));

        return result;
    }

    public static boolean createOriginalPhotoFolder() {
        return createFolder(getOriginalPhotoFolderPath());
    }

    public static boolean createAudioRecordFolder() {
        return createFolder(getAudioRecordFolderPath());
    }

    static boolean createFolderIfNotExist(String filePath) {

        File file = new File(filePath);

        boolean createTemporaryUserFolderResult;

        createTemporaryUserFolderResult = file.exists() || createFolder(file.getPath());

        return createTemporaryUserFolderResult;

    }

    public static boolean createFolderInDownloadFolder(String path) {

        return createFolder(getDownloadFileStoreFolderPath() + path);

    }

    private static boolean createFolder(String path) {
        if (!checkExternalStorageState()) {
            Log.i(TAG, "create folder: External storage not mounted");
            return false;
        }
        File folder = new File(path);

        return folder.mkdirs() || folder.isDirectory();
    }

    private static boolean createFile(String path) {

        if (!checkExternalStorageState()) {
            Log.i(TAG, "create file: External storage not mounted");
            return false;
        }

        File file = new File(path);

        try {
            return file.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

    }

    private static void scanMediaStore(Context context, File file) {

        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);

/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://"
                            + Environment.getExternalStorageDirectory())));
        }*/

    }


    public static String getDownloadFileStoreFolderPath() {
        return getExternalDirectoryPathForDownload() + File.separator + WISNUC_FOLDER_NAME + File.separator;
    }

    public static String getDownloadFileFolderPath(String fileCreateUserUUID) {
        return getDownloadFileStoreFolderPath() + fileCreateUserUUID + File.separator;
    }

    public static String getLocalPhotoMiniThumbnailFolderPath() {
        return getDownloadFileStoreFolderPath() + LOCAL_PHOTO_MINI_THUMBNAIL_FOLDER_NAME;
    }

    public static String getLocalPhotoThumbnailFolderPath() {
        return getDownloadFileStoreFolderPath() + LOCAL_PHOTO_THUMBNAIL_FOLDER_NAME;
    }

    public static String getOldLocalPhotoThumbnailFolderPath() {
        return getDownloadFileStoreFolderPath() + OLD_LOCAL_PHOTO_THUMBNAIL_FOLDER_NAME;
    }

    public static String getOriginalPhotoFolderPath() {
        return getDownloadFileStoreFolderPath() + ORIGINAL_PHOTO_FOLDER_NAME;
    }

    public static String getAudioRecordFolderPath() {
        return getDownloadFileStoreFolderPath() + AUDIO_RECORD_FOLDER_NAME;
    }

    public static boolean writeBitmapToLocalPhotoMiniThumbnailFolder(Media media) {

        if (!media.getMiniThumbPath().isEmpty())
            return false;

        checkIfNoExistThenCreateDownloadFileStoreFolder();

        File miniThumbnailFolder = new File(getLocalPhotoMiniThumbnailFolderPath());

        if (!miniThumbnailFolder.exists())
            createLocalPhotoMiniThumbnailFolder();

        String thumb = media.getOriginalPhotoPath();

        if (media.getUuid().isEmpty()) {
            media.setUuid(Util.calcSHA256OfFile(thumb));
        }

        String miniThumbName = media.getUuid() + ".jpg";

        File file = new File(getLocalPhotoMiniThumbnailFolderPath(), miniThumbName);

        if (file.exists()) {

            Log.d(TAG, "writeBitmapToLocalPhotoMiniThumbnailFolder: exist mini thumb: " + file.getAbsolutePath());

            media.setMiniThumbPath(file.getAbsolutePath());
            return true;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();

        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(thumb, decodeOptions);

        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;

        decodeOptions.inJustDecodeBounds = false;
        decodeOptions.inSampleSize = findBestSampleSize(actualWidth, actualHeight, 64, 64);
        Bitmap bitmap = BitmapFactory.decodeFile(thumb, decodeOptions);

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();

            media.setMiniThumbPath(file.getAbsolutePath());

            Log.d(TAG, "writeBitmapToLocalPhotoMiniThumbnailFolder: media mini thumb:" + media.getMiniThumbPath());

            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

            boolean result = file.delete();

            Log.d(TAG, "writeBitmapToLocalPhotoMiniThumbnailFolder: io exception occur,delete file: " + result);

        } finally {

            bitmap = null;

            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return false;

    }

    private static void checkIfNoExistThenCreateDownloadFileStoreFolder() {
        File root = new File(getDownloadFileStoreFolderPath());

        if (!root.exists())
            createDownloadFileStoreFolder();
    }

    public static boolean checkIsExistInDownloadFolder(String path) {

        File file = new File(getDownloadFileStoreFolderPath() + path);

        return file.exists();

    }

    @NonNull
    public static String renameFileName(int renameCode, String fileName) {
        int dotIndex = fileName.indexOf(".");

        if (dotIndex == -1)
            dotIndex = fileName.length();

        String fileNameWithEnd = fileName.substring(0, dotIndex);

        String end = fileName.substring(dotIndex, fileName.length());

        fileName = fileNameWithEnd + "(" + renameCode + ")" + end;

        return fileName;

    }

    public static boolean writeBitmapToFolder(Bitmap bitmap, File file) {

        FileOutputStream outputStream = null;

        try {

            if (file.createNewFile() || file.isFile()) {

                outputStream = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();

                Log.d(TAG, "writeBitmapToFolder fileName:" + file.getName());

                return true;

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

            boolean result = file.delete();

            Log.d(TAG, "writeBitmapToLocalPhotoThumbnailFolder: io exception occur,delete file: " + result);

        } catch (Exception e) {

            e.printStackTrace();

            boolean result = file.delete();

            Log.d(TAG, "writeBitmapToLocalPhotoThumbnailFolder: error,delete file: " + result);

        } finally {

            bitmap = null;

            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return false;


    }

    public static boolean generateLocalVideoMiniThumbnail(Video video) {

        checkIfNoExistThenCreateDownloadFileStoreFolder();

        File miniThumbnailFolder = new File(getLocalPhotoMiniThumbnailFolderPath());

        if (!miniThumbnailFolder.exists()) {
            createLocalPhotoMiniThumbnailFolder();
        }

        if (video.getMiniThumbPath().length() != 0)
            return false;

        String originalPhotoPath = video.getOriginalPhotoPath();

        if (video.getUuid().isEmpty()) {
            video.setUuid(Util.calcSHA256OfFile(originalPhotoPath));
        }

        String thumbName = video.getUuid() + ".jpg";

        File file = new File(getLocalPhotoMiniThumbnailFolderPath(), thumbName);

        if (file.exists()) {

            Log.d(TAG, "writeBitmapToLocalVideoMiniThumbnailFolder: exist mini thumb: " + file.getAbsolutePath());

            video.setMiniThumbPath(file.getAbsolutePath());
            return true;
        }

        Bitmap originalBitmap = ThumbnailUtils.createVideoThumbnail(originalPhotoPath, -1);

        Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(originalBitmap, 64, 64, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        boolean result = writeBitmapToFolder(thumbBitmap, file);

        if (result)
            video.setMiniThumbPath(file.getAbsolutePath());

        return result;

    }


    public static boolean generateLocalVideoThumbnail(Video video) {

        checkIfNoExistThenCreateDownloadFileStoreFolder();

        File thumbnailFolder = new File(getLocalPhotoThumbnailFolderPath());

        if (!thumbnailFolder.exists()) {
            createLocalPhotoThumbnailFolder();
        }

        if (video.getThumb().length() != 0)
            return false;

        String originalPhotoPath = video.getOriginalPhotoPath();

        if (video.getUuid().isEmpty()) {
            video.setUuid(Util.calcSHA256OfFile(originalPhotoPath));
        }

        String thumbName = video.getUuid() + ".jpg";

        File file = new File(getLocalPhotoThumbnailFolderPath(), thumbName);

        if (file.exists()) {

            Log.d(TAG, "writeBitmapToLocalVideoThumbnailFolder: exist thumb: " + file.getAbsolutePath());

            video.setThumb(file.getAbsolutePath());
            return true;
        }

        Bitmap originalBitmap = ThumbnailUtils.createVideoThumbnail(originalPhotoPath, -1);

        Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(originalBitmap, 200, 200, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        boolean result = writeBitmapToFolder(thumbBitmap, file);

        if (result)
            video.setThumb(file.getAbsolutePath());

        return result;

    }


    public static boolean writeBitmapToLocalPhotoThumbnailFolder(Media media) {

        checkIfNoExistThenCreateDownloadFileStoreFolder();

        File thumbnailFolder = new File(getLocalPhotoThumbnailFolderPath());

        if (!thumbnailFolder.exists()) {
            createLocalPhotoThumbnailFolder();
        }

        if (!media.getThumb().isEmpty())
            return false;

        String thumb = media.getOriginalPhotoPath();

        if (media.getUuid().isEmpty()) {
            media.setUuid(Util.calcSHA256OfFile(thumb));
        }

        String thumbName = media.getUuid() + ".jpg";

        File file = new File(getLocalPhotoThumbnailFolderPath(), thumbName);

        if (file.exists()) {

            Log.d(TAG, "writeBitmapToLocalPhotoThumbnailFolder: exist thumb: " + file.getAbsolutePath());

            media.setThumb(file.getAbsolutePath());
            return true;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();

        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(thumb, decodeOptions);

        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;

        decodeOptions.inJustDecodeBounds = false;
        decodeOptions.inSampleSize = findBestSampleSize(actualWidth, actualHeight, 200, 200);
        Bitmap bitmap = BitmapFactory.decodeFile(thumb, decodeOptions);

        if (bitmap == null)
            return false;

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();

            media.setThumb(file.getAbsolutePath());

            Log.d(TAG, "writeBitmapToLocalPhotoThumbnailFolder: media thumb:" + media.getThumb());

            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

            boolean result = file.delete();

            Log.d(TAG, "writeBitmapToLocalPhotoThumbnailFolder: io exception occur,delete file: " + result);

        } finally {

            bitmap = null;

            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return false;

    }

    private static int findBestSampleSize(
            int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }

    public static boolean downloadMediaToOriginalPhotoFolder(ResponseBody responseBody, Media media) {

        checkIfNoExistThenCreateDownloadFileStoreFolder();

        File originalPhotoFolder = new File(getOriginalPhotoFolderPath());

        if (!originalPhotoFolder.exists())
            createOriginalPhotoFolder();

        if (responseBody == null)
            return false;

        File file = new File(getOriginalPhotoFolderPath(), media.getUuid() + ".jpg");

        if (file.exists()) {
            media.setOriginalPhotoPath(file.getAbsolutePath());
            return true;
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;

        byte[] fileBuffer = new byte[4096];

        try {
            if (file.createNewFile() || file.isFile()) {

                inputStream = responseBody.byteStream();

                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileBuffer);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileBuffer, 0, read);

                }

                outputStream.flush();

                media.setOriginalPhotoPath(file.getAbsolutePath());

                Log.d(TAG, "downloadMediaToOriginalPhotoFolder: original photo path: " + media.getOriginalPhotoPath());

                return true;

            }
        } catch (IOException e) {
            e.printStackTrace();

            boolean result = file.delete();

            Log.d(TAG, "downloadMediaToOriginalPhotoFolder: io exception occur,delete file: " + result);

        } finally {

            try {

                responseBody.close();

                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

        return false;

    }

    public static boolean writeResponseBodyToFolder(ResponseBody responseBody, Task task, boolean deleteTemporaryFile) {

        StartingTaskState startingTaskState = (StartingTaskState) task.getCurrentState();

        AbstractRemoteFile abstractRemoteFile = (AbstractRemoteFile) task.getAbstractFile();

        StateType result = writeResponseBodyToFolder(responseBody, task, abstractRemoteFile,
                startingTaskState, deleteTemporaryFile);

        if (result == StateType.FINISH) {

            Log.d("task", "writeResponseBodyToFolder,set current state to finish task state");

            task.setCurrentState(new FinishTaskState(abstractRemoteFile.getSize(), task));

        } else if (result == StateType.ERROR)
            task.setCurrentState(new ErrorTaskState(task));

        if (result == StateType.ERROR)
            return false;
        else
            return true;

    }

    private static StateType writeResponseBodyToFolder(ResponseBody responseBody, Task task, AbstractRemoteFile abstractRemoteFile,
                                                       StartingTaskState startingTaskState, boolean deleteTemporaryFile) {

        checkIfNoExistThenCreateDownloadFileStoreFolder();

        File downloadFolder = new File(getDownloadFileFolderPath(task.getCreateUserUUID()));

        if (!downloadFolder.exists()) {
            Log.d(TAG, "download file folder not exist,create it");
            createFolder(getDownloadFileFolderPath(task.getCreateUserUUID()));
        }

        File temporaryDownloadFile = abstractRemoteFile.getTemporaryDownloadFile(task.getCreateUserUUID());

        if (deleteTemporaryFile) {
            boolean deleteResult = temporaryDownloadFile.delete();
            Log.d(TAG, "writeResponseBodyToFolder: delete temporary file result：" + deleteResult);
        }

        File downloadFile = abstractRemoteFile.getDownloadedFile(task.getCreateUserUUID());

        InputStream inputStream = null;
        OutputStream outputStream = null;
        FileChannel channel = null;
        RandomAccessFile randomAccessFile = null;

        byte[] fileBuffer = new byte[4096];

        try {

            long contentLength = responseBody.contentLength();

            Log.d(TAG, "writeResponseBodyToFolder: contentLength: " + contentLength);

            long fileDownloadedSize = 0;

            if (temporaryDownloadFile.exists()) {

                Log.d(TAG, "writeResponseBodyToFolder: temporaryDownloadFile exist,continue download");

                inputStream = responseBody.byteStream();

                fileDownloadedSize = temporaryDownloadFile.length();

                long remainSize = task.getAbstractFile().getSize() - fileDownloadedSize;

                randomAccessFile = new RandomAccessFile(temporaryDownloadFile, "rwd");

       /*         channel = randomAccessFile.getChannel();

                MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, fileDownloadedSize, remainSize);*/

                randomAccessFile.seek(fileDownloadedSize);

                while (true) {
                    int read = inputStream.read(fileBuffer);

                    if (read == -1) {
                        break;
                    }

                    randomAccessFile.write(fileBuffer, 0, read);

//                    mappedByteBuffer.put(fileBuffer, 0, read);

                    fileDownloadedSize += read;

                    Log.d(TAG, "writeResponseBodyToFolder: fileDownloadedSize: " + fileDownloadedSize +
                            " totalSize:" + task.getAbstractFile().getSize());

                    startingTaskState.setCurrentHandleFileSize(fileDownloadedSize);

                    Log.d(TAG, "writeResponseBodyToFolder: setCurrentHandleFileSize");

  /*                  task.setCurrentState(startingTaskState);

                    Log.d(TAG, "writeResponseBodyToFolder: setCurrentState");*/

                }

                Log.d(TAG, "writeResponseBodyToFolder: finish download file,rename temporary file");

                boolean renameResult = temporaryDownloadFile.renameTo(downloadFile);

                Log.d(TAG, "writeResponseBodyToFolder: renameResult: " + renameResult);

                if (renameResult)
                    return StateType.FINISH;
                else
                    return StateType.ERROR;

            } else if (temporaryDownloadFile.createNewFile()) {

                Log.d(TAG, "writeResponseBodyToFolder: temporaryDownloadFile create succeed,file name: " +
                        temporaryDownloadFile.getAbsolutePath());

                inputStream = responseBody.byteStream();

                outputStream = new FileOutputStream(temporaryDownloadFile);

                while (true) {
                    int read = inputStream.read(fileBuffer);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileBuffer, 0, read);

                    fileDownloadedSize += read;

                    Log.d(TAG, "writeResponseBodyToFolder: fileDownloadedSize: " + fileDownloadedSize +
                            " totalSize:" + task.getAbstractFile().getSize());

                    startingTaskState.setCurrentHandleFileSize(fileDownloadedSize);

                    Log.d(TAG, "writeResponseBodyToFolder: setCurrentHandleFileSize");

  /*                  task.setCurrentState(startingTaskState);

                    Log.d(TAG, "writeResponseBodyToFolder: setCurrentState");*/

                }

                outputStream.flush();

                Log.d(TAG, "writeResponseBodyToFolder: finish download file,rename temporary file");

                boolean renameResult = temporaryDownloadFile.renameTo(downloadFile);

                Log.d(TAG, "writeResponseBodyToFolder: renameResult: " + renameResult + " name: " + downloadFile.getName());

                if (renameResult)
                    return StateType.FINISH;
                else
                    return StateType.ERROR;

            } else {

                return StateType.ERROR;

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

            return StateType.ERROR;
        } catch (InterruptedIOException e) {

            e.printStackTrace();

            return StateType.PAUSE;

        } catch (IOException e) {

            e.printStackTrace();

            return StateType.ERROR;

        } finally {

            try {

                if (responseBody != null)
                    responseBody.close();

                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }

                if (channel != null)
                    channel.close();

                if (randomAccessFile != null)
                    randomAccessFile.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

    }

    /**
     * download folder add user uuid to avoid rename file name when different user download same name item
     *
     * @param responseBody
     * @param fileDownloadState
     * @return
     */
    @Deprecated
    public static boolean writeResponseBodyToFolder(ResponseBody responseBody, FileDownloadState fileDownloadState) {

        checkIfNoExistThenCreateDownloadFileStoreFolder();

        File downloadFile = new File(getDownloadFileStoreFolderPath(), fileDownloadState.getFileName());

        FileDownloadItem fileDownloadItem = fileDownloadState.getFileDownloadItem();

        FileDownloadState newFileDownloadState = new FileDownloadingState(fileDownloadItem);

        fileDownloadItem.setFileDownloadState(newFileDownloadState);

        InputStream inputStream = null;
        OutputStream outputStream = null;

        byte[] fileBuffer = new byte[4096];

        try {

            long contentLength = responseBody.contentLength();

            Log.d(TAG, "writeResponseBodyToFolder: contentLength: " + contentLength);

            long fileDownloadedSize = 0;

            if (downloadFile.createNewFile() || downloadFile.isFile()) {

                inputStream = responseBody.byteStream();

                outputStream = new FileOutputStream(downloadFile);

                while (true) {
                    int read = inputStream.read(fileBuffer);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileBuffer, 0, read);

                    fileDownloadedSize += read;

                    newFileDownloadState.setFileCurrentTaskSize(fileDownloadedSize);

                    Log.d(TAG, "writeResponseBodyToFolder: fileDownloadedSize: " + fileDownloadedSize);

                    newFileDownloadState.notifyDownloadStateChanged();
                }

                outputStream.flush();

                fileDownloadItem.setFileTime(System.currentTimeMillis());

                fileDownloadItem.setFileDownloadState(new FileDownloadFinishedState(fileDownloadItem));

                return true;

            } else {

                fileDownloadItem.setFileTime(System.currentTimeMillis());

                fileDownloadItem.setFileDownloadState(new FileDownloadErrorState(fileDownloadItem));

                return false;

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

            fileDownloadItem.setFileTime(System.currentTimeMillis());

            fileDownloadItem.setFileDownloadState(new FileDownloadErrorState(fileDownloadItem));

            return false;
        } catch (IOException e) {
            e.printStackTrace();

            fileDownloadItem.setFileTime(System.currentTimeMillis());

            fileDownloadItem.setFileDownloadState(new FileDownloadErrorState(fileDownloadItem));

            if (downloadFile.exists()) {

                boolean result = downloadFile.delete();

                Log.d(TAG, "writeResponseBodyToFolder: io exception occur,delete file: " + result);

            }

            return false;
        } finally {

            try {

                if (responseBody != null)
                    responseBody.close();

                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

    }

    public static boolean checkFileExistInDownloadFolder(String createUserUUID,String fileName) {

        File file = new File(FileUtil.getDownloadFileFolderPath(createUserUUID), fileName);

        return file.exists();

    }

    public static boolean openAbstractRemoteFile(Context context, String fileName) {

        File file = new File(FileUtil.getDownloadFileStoreFolderPath(), fileName);

        try {
            FileUtil.openFile(context, file);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private static void openFile(Context context, File file) throws Exception {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = getMIMEType(file.getName());
        intent.setDataAndType(Uri.fromFile(file), type);

        context.startActivity(filterIntent(context, intent));
    }

    private static Intent filterIntentForOpenFile(Context context, Intent intent) {

        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);

        if (resolveInfos.size() > 0) {

            List<Intent> targetIntents = new ArrayList<>();

            for (ResolveInfo resolveInfo : resolveInfos) {

                String packageName = resolveInfo.activityInfo.packageName.toLowerCase();

                if (!packageName.contains("com.winsun.fruitmix")) {

                    Intent targetIntent = new Intent();
                    targetIntent.setPackage(packageName);
                    targetIntent.setAction(intent.getAction());
                    targetIntent.setDataAndType(intent.getData(), intent.getType());
                    targetIntents.add(targetIntent);

                }

            }

            Intent chooseIntent = targetIntents.remove(0);

            if (chooseIntent == null)
                return intent;

            chooseIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toArray(new Parcelable[]{}));
            chooseIntent.addFlags(intent.getFlags());

            return chooseIntent;

        } else
            return intent;

    }

    private static Intent filterIntent(Context context, Intent intent) {

        PackageManager packageManager = context.getPackageManager();

        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);

        if (resolveInfos.size() > 0) {

            List<Intent> targetIntents = new ArrayList<>();

            String originalIntentAction = intent.getAction();

            for (ResolveInfo resolveInfo : resolveInfos) {

                ActivityInfo activityInfo = resolveInfo.activityInfo;

                String packageName = activityInfo.packageName.toLowerCase();

                if (!packageName.contains("com.winsun.fruitmix")) {

                    Intent targetIntent = new Intent();

                    Log.d(TAG, "filterIntent: label: " + activityInfo.loadLabel(packageManager));

                    Log.d(TAG, "resolve filterIntent: " + resolveInfo.loadLabel(packageManager));

                    targetIntent.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));

                    if (originalIntentAction != null) {

                        targetIntent.setAction(originalIntentAction);

                        switch (originalIntentAction) {
                            case Intent.ACTION_SEND:

                                targetIntent.putExtra(Intent.EXTRA_STREAM, intent.getParcelableExtra(Intent.EXTRA_STREAM));

                                break;
                            case Intent.ACTION_SEND_MULTIPLE:

                                targetIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM));

                                break;
                            default:

                        }

                    }

                    targetIntent.setDataAndType(intent.getData(), intent.getType());

                    targetIntents.add(targetIntent);

                }

            }

            Intent chooseIntent;

            if (originalIntentAction != null && (originalIntentAction.equals(Intent.ACTION_SEND_MULTIPLE) || originalIntentAction.equals(Intent.ACTION_SEND)))
                chooseIntent = Intent.createChooser(targetIntents.remove(0), context.getString(R.string.share_text));
            else
                chooseIntent = targetIntents.remove(0);

            if (chooseIntent == null)
                return intent;

            chooseIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toArray(new Parcelable[]{}));
            chooseIntent.addFlags(intent.getFlags());

            return chooseIntent;

        } else
            return intent;

    }

    public static boolean checkAppInstalledByPackageName(String packageName, Context context) {

        List<PackageInfo> packageInfos = context.getPackageManager().getInstalledPackages(0);

        if (packageInfos == null)
            return false;

        for (PackageInfo packageInfo : packageInfos) {

            if (packageInfo.packageName.equalsIgnoreCase(packageName)) {
                return true;
            }

        }

        return false;

    }


    public static void sendShareToOtherApp(Context context, List<String> filePaths) {
        ArrayList<Uri> uris = new ArrayList<>();

        List<String> types = new ArrayList<>();

        for (String filePath : filePaths) {

            File file = new File(filePath);

            Uri uri = Uri.fromFile(file);
            uris.add(uri);

            String type = getMIMEType(file.getName());

            int index = type.indexOf("/");
            type = type.substring(0, index) + "/*";

            if (!types.contains(type))
                types.add(type);

        }

        StringBuilder builder = new StringBuilder();
        for (String type : types) {
            builder.append(type);
            builder.append(";");
        }

        String shareType = builder.toString();

        Log.d(TAG, "sendShareToOtherApp: shareType: " + shareType);

        Intent intent = new Intent();

        if (uris.size() == 1) {
            intent.setAction(Intent.ACTION_SEND);

            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));

        } else {
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);

            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }

        intent.setType(shareType);

//        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_text)));

        context.startActivity(filterIntent(context, intent));

    }

    public static String getMIMEType(String fileName) {

        String type = "*/*";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        String end = fileName.substring(dotIndex, fileName.length()).toLowerCase();
        if (end.equals("")) return type;
        for (String[] aMIME_MapTable : MIME_MapTable) {
            if (end.equals(aMIME_MapTable[0]))
                type = aMIME_MapTable[1];
        }
        return type;
    }

    private static String[][] MIME_MapTable = {
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };

    public static String formatFileSize(double byteSize) {

        String formatFileSize = "";

        DecimalFormat decimalFormat = new DecimalFormat("####.00");

        if (byteSize < 0) {

            formatFileSize = "0 B";

        } else if (byteSize < 1024L) {

            formatFileSize = byteSize + " B";

        } else if (byteSize < 1024L * 1024L) {

            formatFileSize = decimalFormat.format(byteSize / (float) 1024) + " KB";

        } else if (byteSize < 1024L * 1024L * 1024L) {

            formatFileSize = decimalFormat.format(byteSize / (float) 1024 / 1024) + " MB";

        } else if (byteSize < 1024L * 1024L * 1024L * 1024L) {

            formatFileSize = decimalFormat.format(byteSize / (float) 1024 / 1024 / 1024) + " GB";

        } else if (byteSize < 1024L * 1024L * 1024L * 1024L * 1024L) {

            formatFileSize = decimalFormat.format(byteSize / (float) 1024 / 1024 / 1024 / 1024) + " TB";

        }

        return formatFileSize;

    }

    public static String formatFileSize(long fileSize) {

        return formatFileSize((double) fileSize);

    }

    public static long getTotalCacheSize(Context context) {
        long cacheSize = getFolderSize(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            cacheSize += getFolderSize(context.getExternalCacheDir());
        }
        return cacheSize;
    }

    private static long getFolderSize(File file) {
        long size = 0;

        File[] fileList = file.listFiles();
        int fileListLength;
        if (fileList != null) {
            fileListLength = fileList.length;
            for (int i = 0; i < fileListLength; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        }

        return size;
    }

    public static void clearAllCache(Context context) {
        deleteDir(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteDir(context.getExternalCacheDir());
        }
    }

    static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            int length;
            if (children != null) {
                length = children.length;
                for (int i = 0; i < length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }

        }
        return dir == null || dir.delete();
    }


    public static boolean checkFileIsVideo(String fileName) {

        return getMIMEType(fileName).startsWith("video");

    }

    public static boolean checkFileIsMedia(String fileName) {
        String type = getMIMEType(fileName);

        return type.startsWith("video") || type.contains("gif") || type.contains("jpeg") ||
                type.contains("png");
    }

    public static int getFileTypeResID(String fileName) {

        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex < 0)
            return R.drawable.file_icon;

        String end = fileName.substring(dotIndex, fileName.length()).toLowerCase();

        if (end.isEmpty())
            return R.drawable.file_icon;

        String type = "";
        for (String[] aMIME_MapTable : MIME_MapTable) {
            if (end.equals(aMIME_MapTable[0]))
                type = aMIME_MapTable[1];
        }

        if (end.startsWith(".xls"))
            return R.drawable.excel;
        else if (end.startsWith(".doc"))
            return R.drawable.word;
        else if (end.equals(".pdf"))
            return R.drawable.pdf;
        else if (end.startsWith(".ppt"))
            return R.drawable.power_point;
        else if (end.equals(".txt")) {
            return R.drawable.txt;
        } else if (type.startsWith("video")) {

            if (type.contains("mp4")) {
                return R.drawable.mp4;
            } else if (type.contains("quicktime")) {
                return R.drawable.mov;
            } else
                return R.drawable.video;

        } else if (type.startsWith("audio")) {
            return R.drawable.audio;
        } else if (type.contains("gif")) {
            return R.drawable.gif;
        } else if (type.contains("jpeg")) {
            return R.drawable.jpg;
        } else if (type.contains("png")) {
            return R.drawable.png;
        } else
            return R.drawable.file_icon;

    }

    public static String getFileType(String fileName, Context context) {
        return context.getString(getFileTypeStrResId(fileName));
    }

    private static int getFileTypeStrResId(String fileName) {

        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex < 0)
            return R.string.unknown_file;

        String end = fileName.substring(dotIndex, fileName.length()).toLowerCase();

        if (end.isEmpty())
            return R.string.unknown_file;

        String type = "";
        for (String[] aMIME_MapTable : MIME_MapTable) {
            if (end.equals(aMIME_MapTable[0]))
                type = aMIME_MapTable[1];
        }

        if (end.startsWith(".xls"))
            return R.string.file_type_excel;
        else if (end.startsWith(".doc"))
            return R.string.file_type_word;
        else if (end.equals(".pdf"))
            return R.string.file_type_pdf;
        else if (end.startsWith(".ppt"))
            return R.string.file_type_ppt;
        else if (end.equals(".txt")) {
            return R.string.file_type_txt;
        } else if (type.startsWith("video")) {

            if (type.contains("mp4")) {
                return R.string.file_type_mp4;
            } else if (type.contains("quicktime")) {
                return R.string.file_type_mov;
            } else
                return R.string.file_type_video;

        } else if (type.startsWith("audio")) {
            return R.string.file_type_audio;
        } else if (type.contains("gif")) {
            return R.string.file_type_gif;
        } else if (type.contains("jpeg")) {
            return R.string.file_type_jpeg;
        } else if (type.contains("png")) {
            return R.string.file_type_png;
        } else
            return R.string.unknown_file;

    }


    public static boolean checkFileIsTorrent(String filePath) {
        return filePath.endsWith("torrent");
    }


}

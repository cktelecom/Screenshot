package com.ckt.screenshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {
    private static final String TAG = "FileUtil";

    private static final String SCREEN_SHOT_PATH = File.separator + "Pictures" + File.separator
            + "Screenshots" + File.separator;
    private static final String SCREENSHOT_FILE_NAME_TEMPLATE = "Screenshot_%s.png";
    private static String mScreenshotDirAndName;
    private static Bitmap mBitmap;

    private static String getAppPath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory().toString();
        } else {
            return context.getFilesDir().toString();
        }
    }

    private static String getScreenShotDir(Context context) {
        StringBuffer stringBuffer = new StringBuffer(getAppPath(context));
        stringBuffer.append(SCREEN_SHOT_PATH);
        File file = new File(stringBuffer.toString());
        if (!file.exists()) {
            file.mkdirs();
        }
        return stringBuffer.toString();
    }

    /**
     * file name would be like "Screenshot_20170417_222222.png"
     */
    static void generateScreenshotName(Context context) {
        long currentTime = System.currentTimeMillis();
        String imageDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date(currentTime));
        String screenshotName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, imageDate);

        StringBuffer screenshotDir = new StringBuffer(getScreenShotDir(context));
        screenshotDir.append(screenshotName);
        setScreenshotDirAndName(screenshotDir.toString());
        Log.d(TAG, "generateScreenshotName: file name: " + screenshotDir.toString());
    }

    public static void setScreenshotDirAndName(String dirAndName) {
        mScreenshotDirAndName = dirAndName;
    }

    public static String getScreenshotDirAndName() {
        return mScreenshotDirAndName;
    }

    /**
     * save screenshot to sdcard
     *
     * @param bitmap the image source to be save
     */
    public static void saveScreenshotFile(Bitmap bitmap) {
        try {
            // Save
            OutputStream out = new FileOutputStream(mScreenshotDirAndName);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Log.d(TAG, "saveScreenshotFile: success");
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM'
            Log.e(TAG, "saveScreenshotFile: failed");
            e.printStackTrace();
        }
    }

    // save screenshot bitmap temporarily
    public static void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public static Bitmap getBitmap() {
        return mBitmap;
    }
}

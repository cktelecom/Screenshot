package com.ckt.screenshot;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Utils {
    private static final String TAG = "ScreenshotUtils";
    private static final String SCREENSHOT_FILE_NAME_TEMPLATE = "Screenshot_%s.png";
    private static final String mScreenshotDir = "/storage/emulated/0/Pictures/Screenshots/";
    private static String mImageFilePath;

    /**
     * file name would be like "Screenshot_20170417_222222.png"
     */
    public static void setFilePathAndName() {
        long imageTime = System.currentTimeMillis();
        String imageDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date(imageTime));
        String imageFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, imageDate);
        mImageFilePath = new File(mScreenshotDir, imageFileName).getAbsolutePath();
        mImageFilePath = mScreenshotDir + imageFileName;
        Log.d(TAG, "setFilePathAndName: file name: " + mImageFilePath);
    }

    /**
     * @return screenshot image file's absolute path and name
     */
    public static String getImageFilePath() {
        return mImageFilePath;
    }


    /**
     * save screenshot to sdcard
     * @param bitmap the image source to be save
     */
    public static void saveScreenshotFile(Bitmap bitmap) {
        try {
            // Save
            OutputStream out = new FileOutputStream(getImageFilePath());
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
}
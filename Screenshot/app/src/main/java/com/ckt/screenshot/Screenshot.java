package com.ckt.screenshot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.widget.ScrollView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;

/**
 * Created by Star on 2017/04/14   .
 */

public class Screenshot {
    private static String TAG = "Screenshot";

    /**
     * 普通截屏
     */
    public static Bitmap takeScreen(String path) {
        savecreen(path);
        Bitmap bitmap = getDiskBitmap(path);
        return bitmap;
    }

    /**
     * 截取scrollview的屏幕
     **/
    public static Bitmap getScrollViewBitmap(ScrollView scrollView) {
        int h = 0;
        Bitmap bitmap;
        // 获取recyclerview实际高度
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();
        }
        // 创建对应大小的bitmap
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);

        return bitmap;
    }

    public static void savecreen(String name) {
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "savecreen: "+name);
        String cmd = "screencap -p " + name;
        try {
            // 权限设置
            Process p = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = p.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(
                    outputStream);
            // 将命令写入
            dataOutputStream.writeBytes(cmd);
            // 提交命令
            dataOutputStream.flush();
            // 关闭流操作
            dataOutputStream.close();
            outputStream.close();
            p.waitFor();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static Bitmap getDiskBitmap(String pathString) {
        Bitmap bitmap = null;
        try {
            File file = new File(pathString);
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(pathString);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return bitmap;
    }
}

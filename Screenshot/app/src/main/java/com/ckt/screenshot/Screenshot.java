package com.ckt.screenshot;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by Star on 2017/04/14   .
 */

public class Screenshot {
    private static String TAG = "Screenshot";

    /**
     * 普通截屏
     * **/
    public static Bitmap takeScreen(int width, int height, View view) {
        // validate view.width and view.height
        view.layout(0, 0, width, height);
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        // validate view.measurewidth and view.measureheight
        view.measure(measuredWidth, measuredHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        return bitmap;
    }

    /**
     * 截取scrollview的屏幕
     * **/
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
}

package com.ckt.screenshot;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotEditorService extends Service implements View.OnClickListener {

    private static String TAG = "ScreenshotEditorService";
    private Context mContext;
    private View mainLayout;
    private WindowManager wm;
    private boolean isShowing = false;
    private ImageView mImgScreenshot;
    private Bitmap mBitmap;
    private Handler mainHandler;
    private static String mScreenshotPath;
    private static final String SCREENSHOT_FILE_NAME_TEMPLATE = "Screenshot_%s.png";
    private static final String mScreenshotDir = "/storage/emulated/0/ScreenCapture/Screenshots";
    private static int COLOR_PANEL = 0;
    private static int BRUSH = 0;
    private ImageButton mColorPanel;
    private ImageButton mBrush;
    private ImageButton mUndo;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ScreenshotEditor");
        mContext = ScreenshotEditorService.this;
        mainHandler = new Handler(getMainLooper());
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout mLinear = new LinearLayout(getApplicationContext()) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                        || event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
                    ScreenshotEditorService.this.removeView();
                }
                return super.dispatchKeyEvent(event);
            }
        };

        mLinear.setFocusable(true);
        mainLayout = inflater.inflate(R.layout.crop_view_wm, mLinear);
        mainLayout.setSystemUiVisibility(getUiOptions());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                //takeScreenshot();
//                saveScreenshot();
                addView();
                loadScreenshot();
            }
        });
        //这个有必要吗？
        mainLayout.setVisibility(View.VISIBLE);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainHandler = null;
        mBitmap = null;
        System.gc();
    }

    private void addView() {
        Log.d(TAG, "addView: ");
        if (!isShowing) {
            initViews();
        }
        mainHandler.post(new Runnable() {
            public void run() {
                if (!isShowing) {
                    wm.addView(mainLayout, getParams());
                } else {
                    wm.updateViewLayout(mainLayout, getParams());
                }
                isShowing = true;
            }
        });
    }

    private void removeView() {
        Log.d(TAG, "removeView: ");
        if (isShowing) {
            wm.removeView(mainLayout);
        }
        isShowing = false;
        stopSelf();
    }

    private void initViews() {
        mainLayout.setSystemUiVisibility(getUiOptions());
        mainLayout.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                mainLayout.setSystemUiVisibility(getUiOptions());
            }
        });
        mImgScreenshot = (ImageView) mainLayout.findViewById(R.id.img_screenshot);
        mBrush = (ImageButton) mainLayout.findViewById(R.id.brush);
        mColorPanel = (ImageButton) mainLayout.findViewById(R.id.color_panel);
        mUndo = (ImageButton) mainLayout.findViewById(R.id.undo);

        final ImageButton shareButton = (ImageButton) mainLayout.findViewById(R.id.btn_share);
        final ImageButton cancelButton = (ImageButton) mainLayout.findViewById(R.id.btn_cancel);
        final ImageButton saveButton = (ImageButton) mainLayout.findViewById(R.id.btn_save);
        final Button paintBtn = (Button) mainLayout.findViewById(R.id.paint);
        final Button longScreenshotBtn = (Button) mainLayout.findViewById(R.id.long_screenshot);
        mBrush.setOnClickListener(this);
        mColorPanel.setOnClickListener(this);
        mUndo.setOnClickListener(this);
        shareButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        paintBtn.setOnClickListener(this);
        longScreenshotBtn.setOnClickListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_share:
                shareScreenshot();
                removeView();
                break;
            case R.id.btn_cancel:
                deleteScreenshot();
                removeView();
                break;
            case R.id.btn_save:
                removeView();
                onDestroy();
                break;
            case R.id.paint:
                mainLayout.findViewById(R.id.bot_bar).setVisibility(View.GONE);
                mainLayout.findViewById(R.id.paint_bar).setVisibility(View.VISIBLE);
                break;
            case R.id.brush:
                mBrush.setImageResource(COLOR_PANEL == 0 ? R.drawable.ic_pen : R.drawable.ic_brush);
                COLOR_PANEL = 1 - COLOR_PANEL;
                break;
            case R.id.color_panel:
                mColorPanel.setImageResource(BRUSH == 0 ? R.drawable.ic_color_blue : R.drawable.ic_color_red);
                BRUSH = 1 - BRUSH;
                break;
            default:
        }
    }

    public void takeScreenshot() {
        Log.d(TAG, "takeScreenshot: ");

        long imageTime = System.currentTimeMillis();
        String imageDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date(imageTime));
        String imageFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, imageDate);
        mScreenshotPath = new File(mScreenshotDir, imageFileName).getAbsolutePath();
        Log.d(TAG, "getScreenshotPath: file name: " + mScreenshotPath);

        String cmd = "screencap -p " + mScreenshotPath;
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
            Log.d(TAG, "saved: ");
        } catch (Throwable t) {
            t.printStackTrace();
        }

//        DisplayMetrics dm = new DisplayMetrics();
//        int width = dm.widthPixels;
//        int height = dm.heightPixels;
//        mBitmap = Screenshot.takeScreen(width, height, getWindow().getDecorView());

//        系统截图
//        mBitmap = SurfaceControl.screenshot(width, height);
//        mImgScreenshot.setBackgroundResource(R.drawable.);
    }

    public void loadScreenshot() {

        Log.d(TAG, "loadScreenshot: exists");
        Bitmap bitmap = ScreenshotActivity.getBitmap();
        mImgScreenshot.setImageBitmap(bitmap);

//        mImgScreenshot.setImageResource(R.drawable.sc_example);
    }

    /**
     * save screenshot to sdcard/Pictures/Screenshots/
     */
    public void saveScreenshot() {
        Log.d(TAG, "saveScreenshot to storage");
        //        如何解决耗时问题
        try {
            // Save
            OutputStream out = new FileOutputStream(mScreenshotPath);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Log.d(TAG, "saveScreenshotFile: success");
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM'
            Log.e(TAG, "saveScreenshotFile: can't sav screenshot");
            e.printStackTrace();
        }
    }

    public void shareScreenshot() {
        Log.d(TAG, "shareScreenshot pics");
        final File imageFile = new File(mScreenshotPath);
        // Create a shareScreenshot intent
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("image/png");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
        startActivity(Intent.createChooser(sharingIntent, getResources().getText(R.string.share)));
    }

    public void deleteScreenshot() {
        String msg;
        boolean deleted = new File(mScreenshotPath).delete();
        if (deleted)
            msg = getString(R.string.action_delete_success);
        else
            msg = getString(R.string.action_delete_failed);
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(new File(mScreenshotPath))));
        removeView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mainHandler.post(new Runnable() {
            public void run() {
                if (isShowing)
                    wm.updateViewLayout(mainLayout, getParams());
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean canWeDrawOurOverlay(Context mContext) {
        return Settings.canDrawOverlays(mContext);
    }

    private WindowManager.LayoutParams getParams() {
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                PixelFormat.TRANSLUCENT);

        params.dimAmount = 0.7f;
        params.flags += WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        return params;
    }

    private int getUiOptions() {
        return View.SYSTEM_UI_FLAG_FULLSCREEN ////仅这一个 全屏但是有个status bar收上去的动画，内容跟着上拉
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN//仅这一个 全屏内容稳定，但是status bar显示
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                ;
    }
}

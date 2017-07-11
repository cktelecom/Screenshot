package com.ckt.screenshot;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static android.graphics.Bitmap.CompressFormat.PNG;

public class ScreenshotEditorService extends Service implements View.OnClickListener {

    private static String TAG = "ScreenshotEditorService";
    private Context mContext;
    private View mainLayout;
    private WindowManager wm;
    private boolean isShowing = false;
    private DrawingView mImgScreenshot;
    private Handler mainHandler;
    private static String mScreenshotPath;
    private static int COLOR_PANEL = 0;
    private static int BRUSH = 0;
    private ImageButton mColorPanel;
    private ImageButton mBrush;
    private ImageButton mUndo;
    private CropImageView mCropImageView;
    private LinearLayout mLinearLayout;
    private LinearLayout mLinearLayoutsure;
    private Button mButtonCut;

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
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                addView();
                loadScreenshot();
            }
        }, 0);
        //这个有必要吗？
        mainLayout.setVisibility(View.VISIBLE);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainHandler = null;
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
        mImgScreenshot = (DrawingView) mainLayout.findViewById(R.id.img_screenshot);
        mBrush = (ImageButton) mainLayout.findViewById(R.id.brush);
        mColorPanel = (ImageButton) mainLayout.findViewById(R.id.color_panel);
        mUndo = (ImageButton) mainLayout.findViewById(R.id.undo);
        mCropImageView = (CropImageView) mainLayout.findViewById(R.id.crop_imageview);
        mLinearLayout = (LinearLayout) mainLayout.findViewById(R.id.bot_bar);
        mLinearLayoutsure = (LinearLayout) mainLayout.findViewById(R.id.mlinearlayout_sure);
        mButtonCut = (Button) mainLayout.findViewById(R.id.button_cut);
        final ImageButton shareButton = (ImageButton) mainLayout.findViewById(R.id.btn_share);
        final ImageButton cancelButton = (ImageButton) mainLayout.findViewById(R.id.btn_cancel);
        final ImageButton saveButton = (ImageButton) mainLayout.findViewById(R.id.btn_save);
        final Button paintBtn = (Button) mainLayout.findViewById(R.id.paint);
        final Button longScreenshotBtn = (Button) mainLayout.findViewById(R.id.long_screenshot);
        final Button mButtonSure = (Button) mainLayout.findViewById(R.id.button_sure);
        mBrush.setOnClickListener(this);
        mColorPanel.setOnClickListener(this);
        mUndo.setOnClickListener(this);
        mButtonCut.setOnClickListener(this);
        shareButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        paintBtn.setOnClickListener(this);
        longScreenshotBtn.setOnClickListener(this);
        mButtonSure.setOnClickListener(this);
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
                saveScreenshot();
                shareScreenshot();
                removeView();
                break;
            case R.id.btn_cancel:
//                deleteScreenshot();
                removeView();
                break;
            case R.id.btn_save:
                saveScreenshot();
                removeView();
                onDestroy();
                break;
            case R.id.paint:
                changeToPaintMode();
                break;
            case R.id.brush:
                mBrush.setImageResource(BRUSH == 0 ? R.drawable.ic_brush : R.drawable.ic_pen);
                mImgScreenshot.setPenSize(BRUSH == 0 ? 40 : 10);
                BRUSH = 1 - BRUSH;
                break;
            case R.id.color_panel:
                mColorPanel.setImageResource(COLOR_PANEL == 0 ? R.drawable.ic_color_blue : R.drawable.ic_color_red);
                mImgScreenshot.setPenColor(COLOR_PANEL == 0 ? getColor(R.color.blue) : getColor(R.color.red));
                COLOR_PANEL = 1 - COLOR_PANEL;
                break;
            case R.id.undo:
                mImgScreenshot.undo();
                break;
            case R.id.button_cut:
                cutStart();
                break;
            case R.id.button_sure:
                cutEnd();
                break;
            default:
                break;
        }
    }

    private void cutEnd() {
        mImgScreenshot.setImageBitmap(mCropImageView.getCropImage());
        mImgScreenshot.setVisibility(View.VISIBLE);
        mLinearLayout.setVisibility(View.VISIBLE);
        mLinearLayoutsure.setVisibility(View.GONE);
        mCropImageView.setVisibility(View.GONE);
    }

    private void cutStart() {
        Bitmap mBitmap = mImgScreenshot.getImageBitmap();
        mImgScreenshot.setVisibility(View.GONE);
        mLinearLayout.setVisibility(View.GONE);
        mLinearLayoutsure.setVisibility(View.VISIBLE);
        mCropImageView.setDrawable(mBitmap, 200, 300);
        mCropImageView.setVisibility(View.VISIBLE);
    }

    private void changeToPaintMode() {
        mainLayout.findViewById(R.id.bot_bar).setVisibility(View.GONE);
        mainLayout.findViewById(R.id.paint_bar).setVisibility(View.VISIBLE);
        mImgScreenshot.initializePen();
        mImgScreenshot.setPenSize(10);
        mImgScreenshot.setPenColor(getColor(R.color.red));
    }

    /**
     * need root access
     */
    public void takeScreenshot() {
        Log.d(TAG, "takeScreenshot: ");

        mScreenshotPath = FileUtil.getScreenshotDirAndName();

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
    }

    public void loadScreenshot() {

        Log.d(TAG, "loadScreenshot: exists");
        Bitmap bitmap = FileUtil.getBitmap();
        mImgScreenshot.setImageBitmap(bitmap);
    }

    /**
     * save screenshot to sdcard/Pictures/Screenshots/
     */
    public void saveScreenshot() {
        Log.d(TAG, "saveScreenshot to storage");
        mScreenshotPath = FileUtil.getScreenshotDirAndName();
        try {
            // Save
            OutputStream out = new FileOutputStream(mScreenshotPath);
            mImgScreenshot.getImageBitmap().compress(PNG, 100, out);
            out.flush();
            out.close();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(new File(mScreenshotPath))));
            Log.d(TAG, "saveScreenshotFile: success");
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM'
            Log.e(TAG, "saveScreenshotFile: failed");
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

package com.ckt.screenshot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


public class TransitActivity extends Activity {
    private static final String TAG = "TransitActivity";
    public static final int REQUEST_DRAW_OVER = 1;
    public static final int REQUEST_MEDIA_PROJECTION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: finish this activity immediately");

        requestPermission();
    }

    public void requestDrawOverPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(TransitActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_DRAW_OVER);
            } else {
                requestCapturePermission();
            }
        } else {
            requestCapturePermission();
        }
    }

    public void requestCapturePermission() {
        Log.d(TAG, "requestCapturePermission");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_DRAW_OVER:
                if (Settings.canDrawOverlays(TransitActivity.this)) {
                    requestCapturePermission();
                } else {
                    finish();
                }
                break;
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == RESULT_OK && data != null) {
                    TakeScreenshotService.setResultData(data);
                    startService(new Intent(getApplicationContext(), TakeScreenshotService.class));
                    finish();
                } else {
                    finish();
                }
                break;
        }
    }

    private void requestPermission() {
        Log.d(TAG, "requestPermission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            requestDrawOverPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestDrawOverPermission();
        } else {
            finish();
        }
    }
}

package com.ckt.screenshot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.File;

public class ScreenshotActivity extends AppCompatActivity {
    private static final String TAG = "ScreenshotActivity";
    private static String mScreenshotPath;
    private ImageView mImgScreenshot;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        takeScreenshot();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_view);
        mImgScreenshot = (ImageView) findViewById(R.id.img_screenshot);

        loadScreenshot();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            shareScreenshot();
            finish();
            return true;
        }
        if (id == R.id.action_cancel) {
            Log.d(TAG, "cancel screenshot");
            deleteScreenshot();
            finish();
            return true;
        }
        if (id == R.id.action_done) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadScreenshot() {
//        mImgScreenshot.setImageResource(R.drawable.sc_example);
        mImgScreenshot.setImageBitmap(mBitmap);

    }

    public void takeScreenshot() {
        Log.d(TAG, "takeScreenshot: ");

        mScreenshotPath = Utils.getScreenshotPath();

        mBitmap = Screenshot.takeScreen(mScreenshotPath);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(new File(mScreenshotPath))));
    }

    /**
     * save screenshot to sdcard/Pictures/Screenshots/
     */
    public void saveScreenshot() {
        Log.d(TAG, "saveScreenshot to storage");
        Utils.saveScreenshotFile(mBitmap);
    }

    /**
     * shareScreenshot screenshot to other apps
     */
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
        if (deleted) {
            msg = getString(R.string.action_delete_success);
        }
        else {
            msg = getString(R.string.action_delete_failed);
        }
        Log.d(TAG, "deleteScreenshot: " + msg);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(new File(mScreenshotPath))));
    }
}

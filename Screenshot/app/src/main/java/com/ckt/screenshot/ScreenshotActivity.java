package com.ckt.screenshot;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class ScreenshotActivity extends AppCompatActivity {
    private static final String TAG = "ScreenshotActivity";
    private ImageView imgScreenshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_view);
        imgScreenshot = (ImageView) findViewById(R.id.img_screenshot);

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
            share();
            return true;
        }
        if (id == R.id.action_cancel) {
            Log.d(TAG, "cancel screenshot");
            finish();
            return true;
        }
        if (id == R.id.action_done) {
            takeScreenshot();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadScreenshot() {
        imgScreenshot.setImageResource(R.drawable.sc_example);

    }

    public void share() {
        Log.d(TAG, "share: ");
    }

    public void takeScreenshot() {
        Log.d(TAG, "takeScreenshot: ");
    }
}

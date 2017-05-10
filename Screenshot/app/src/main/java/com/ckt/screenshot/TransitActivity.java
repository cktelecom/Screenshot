package com.ckt.screenshot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class TransitActivity extends Activity {
    private static final String TAG = "TransitActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: finish this activity immediately");
        Intent startIntent = new Intent(this, ScreenshotEditorService.class);
        startService(startIntent);
        finish();
    }
}

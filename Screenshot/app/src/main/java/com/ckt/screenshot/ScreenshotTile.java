package com.ckt.screenshot;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.util.Log;


@RequiresApi(api = Build.VERSION_CODES.N)
public class ScreenshotTile extends TileService {

    private static final String TAG = "ScreenshotTile";

    @Override
    public void onTileAdded() {
        Log.d(TAG, "onTileAdded");
        getQsTile().updateTile();
    }

    @Override
    public void onTileRemoved() {
        Log.d(TAG, "onTileRemoved");
    }

    @Override
    public void onClick() {
        Log.d(TAG, "onClick: QS Tile clicked.");

        Intent intent = new Intent();
        intent.setClass(this, TransitActivity.class);
        startActivityAndCollapse(intent);

        getQsTile().updateTile();
    }

}
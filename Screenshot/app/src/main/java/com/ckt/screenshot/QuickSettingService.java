package com.ckt.screenshot;

import android.os.Build;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.util.Log;


@RequiresApi(api = Build.VERSION_CODES.N)
public class QuickSettingService extends TileService {

    private static final String TAG = "QuickSettingService";

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
        getQsTile().updateTile();
    }
}
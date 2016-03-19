package com.example.android.pathfinder.Widget;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.example.android.pathfinder.Database.RouteProvider;
import com.example.android.pathfinder.MainActivity;
import com.example.android.pathfinder.RouteManager;
import com.example.android.pathfinder.RoutePoints;

import java.text.ParseException;


/**
 * Created by kenm on 8/10/2015.
 */
public class widgetIntentService extends IntentService {
    public static final String LOG_TAG = "widgetIntentService";

    public static final String PARAM_WIDGETID = "WidgetID";
    public static final String PARAM_MAPID    = "MapID";

    public static final String PARAM_IN_COMMAND   = "inCommand";

    public static final String PARAM_OUT_START_ADDRESS = "startAddress";
    public static final String PARAM_OUT_END_ADDRESS   = "endAddress";

    public static final int COMMAND_UPDATE    = 0x0001;
    public static final int COMMAND_NEXT      = 0x0002;
    public static final int COMMAND_PREVIOUS  = 0x0003;
    public static final int COMMAND_LAST      = 0x0004;

//    private RouteManager      mRouteManager      = null;
    private SharedPreferences mSharedPreferences = null;
    Cursor mCursor = null;

    public widgetIntentService() {
        super("widgetIntentService");
        Log.d(LOG_TAG, "widgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        int command    = intent.getIntExtra(PARAM_IN_COMMAND, COMMAND_UPDATE);
        int mapID      = intent.getIntExtra(PARAM_MAPID, 0);
        int widgetID   = intent.getIntExtra(PARAM_WIDGETID, 0);

        Log.d(LOG_TAG, "onHandleIntent widgetID = " + widgetID + " mapID = " + mapID);

        Bundle bundle = getMapData(command, mapID);
        sendResponse(widgetID, bundle);
    }

    private void sendResponse(int widgetID, Bundle bundle) {
        Log.d(LOG_TAG, "sendResponse");

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MapWidgetProvider.ResponseReceiver.ACTION_RESP);

        Log.d(LOG_TAG, "sendResponse widgetID = " + widgetID);
        broadcastIntent.putExtra(PARAM_WIDGETID, widgetID);

        if (bundle != null) {
            broadcastIntent.putExtra(PARAM_MAPID, bundle.getInt(PARAM_MAPID));
            broadcastIntent.putExtra(PARAM_OUT_START_ADDRESS, bundle.getString(RouteManager.KEY_START_ADDRESS));
            broadcastIntent.putExtra(PARAM_OUT_END_ADDRESS, bundle.getString(RouteManager.KEY_END_ADDRESS));

            Log.d(LOG_TAG, "sendResponse mapID = " + bundle.getInt(PARAM_MAPID));
            Log.d(LOG_TAG, "sendResponse startAddress = " + bundle.getString(RouteManager.KEY_START_ADDRESS));
            Log.d(LOG_TAG, "sendResponse endAddress = " + bundle.getString(RouteManager.KEY_END_ADDRESS));
        }

        sendBroadcast(broadcastIntent);
    }

    private Bundle getMapData(int command, int mapID) {
        Bundle bundle = null;

        restoreRoutes();

        switch(command) {
            case COMMAND_UPDATE: {
                bundle = update(mapID);
                break;
            }
            case COMMAND_NEXT: {
                bundle = getNext(mapID);
                break;
            }
            case COMMAND_PREVIOUS: {
                bundle = getPrev(mapID);
                break;
            }
            case COMMAND_LAST: {
                bundle = getLast();
                break;
            }
        }

        return bundle;
    }

    private int getMapID(String mapIDString) {
        int ret = 0;

        if (mapIDString != null) {
            try {
                ret = Integer.parseInt(mapIDString);
            }
            catch (NumberFormatException e) {
                Log.e(LOG_TAG, "getMapID parse exception = " + e);
            }
        }

        return ret;
    }

    private Bundle getNext(int mapID) {
        Bundle bundle = null;

        int count = mCursor.getCount();
        if (count > 0) {
            if (++mapID >= count) {
                mapID = 0;
            }

            bundle = getRoute(mapID);
            if (bundle !=  null) {
                bundle.putInt(PARAM_MAPID, mapID);
            }
        }

        return bundle;
    }

    private Bundle getPrev(int mapID) {
        Bundle bundle = null;

        int count = mCursor.getCount();
        if (count > 0) {
            if (--mapID < 0) {
                mapID = count-1;
            }

            bundle = getRoute(mapID);
            if (bundle !=  null) {
                bundle.putInt(PARAM_MAPID, mapID);
            }
        }

        return bundle;
    }

    private Bundle update(int mapID) {
        Bundle bundle = getRoute(mapID);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        if (bundle !=  null) {
            bundle.putInt(PARAM_MAPID, mapID);
            intent.putExtra(MainActivity.START_BUNDLE, bundle);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        return bundle;
    }

    private Bundle getLast() {
        Bundle bundle = getRoute(0);
        if (bundle !=  null) {
            bundle.putInt(PARAM_MAPID, 0);
        }

        return bundle;
    }

    private void restoreRoutes() {
        mCursor = getContentResolver().query(RouteProvider.CONTENT_URI, null, null, null, null);
    }

    private Bundle getRoute(int index) {
        Bundle bundle = null;

        if ((mCursor != null) && (mCursor.getCount() > 0)) {
            mCursor.moveToFirst();

            for (int i=0; i<mCursor.getCount(); i++) {
                if (i == index) {
                    bundle = new Bundle();

                    String startAddress = mCursor.getString(mCursor.getColumnIndex(RouteProvider.startAddressField));
                    String endAddress   = mCursor.getString(mCursor.getColumnIndex(RouteProvider.endAddressField));

                    double startLat = mCursor.getDouble(mCursor.getColumnIndex(RouteProvider.startLatField));
                    double startLon = mCursor.getDouble(mCursor.getColumnIndex(RouteProvider.startLonField));
                    double endLat = mCursor.getDouble(mCursor.getColumnIndex(RouteProvider.endLatField));
                    double endLon = mCursor.getDouble(mCursor.getColumnIndex(RouteProvider.endLonField));

                    bundle.putString(RouteManager.KEY_START_ADDRESS, startAddress);
                    bundle.putString(RouteManager.KEY_END_ADDRESS, endAddress);

                    bundle.putDouble(RouteManager.KEY_START_LAT, startLat);
                    bundle.putDouble(RouteManager.KEY_START_LON, startLon);
                    bundle.putDouble(RouteManager.KEY_END_LAT, endLat);
                    bundle.putDouble(RouteManager.KEY_END_LON, endLon);
                    break;
                }

                mCursor.moveToNext();
            }
        }

        return bundle;
    }
}

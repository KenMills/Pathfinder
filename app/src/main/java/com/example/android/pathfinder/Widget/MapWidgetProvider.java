package com.example.android.pathfinder.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.android.pathfinder.MainActivity;
import com.example.android.pathfinder.R;

/**
 * Created by kenm on 8/3/2015.
 */
public class MapWidgetProvider extends AppWidgetProvider {
    public static final String LOG_TAG = "MapWidgetProvider";

    private static String CURRENT_MAPS = "CurrentMaps";
    private static String SPECIFIC_MAP = "SpecificMap";

    private static final String PREVIOUS_CLICKED    = "previousMapWidgetButtonClick";
    private static final String NEXT_CLICKED        = "nextMapWidgetButtonClick";
    private static final String MAP_CLICKED         = "matchMapWidgetButtonClick";

    public MapWidgetProvider() {
        Log.d(LOG_TAG, "MapWidgetProvider");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOG_TAG, "onUpdate");

        for (int i=0; i<appWidgetIds.length; i++) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_map);

            int mapID = getCurrrentMapID(context, appWidgetIds[i]);

            Log.d(LOG_TAG, "onUpdate widgetID = " + appWidgetIds[i] + " mapID = " + mapID);
            Log.d(LOG_TAG, "onUpdate getPackageName = " + context.getPackageName());

            // send update command
            sendIntent(context, widgetIntentService.COMMAND_LAST, mapID, appWidgetIds[i]);

            Intent intent = new Intent(context, MapWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            views.setOnClickPendingIntent(R.id.linearLayout, getPendingSelfIntent(context, MAP_CLICKED +appWidgetIds[i]));
            views.setOnClickPendingIntent(R.id.buttonPrev, getPendingSelfIntent(context, PREVIOUS_CLICKED+appWidgetIds[i]));
            views.setOnClickPendingIntent(R.id.buttonNext, getPendingSelfIntent(context, NEXT_CLICKED+appWidgetIds[i]));

            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        Log.d(LOG_TAG, "onReceive action = " + action);

        if (action.contains(MAP_CLICKED)) {
            Log.d(LOG_TAG, "onReceive MAP_CLICKED");
            handleMapWidgetClicked(context, action);
        }
        else if (action.contains(PREVIOUS_CLICKED)) {
            Log.d(LOG_TAG, "onReceive PREVIOUS_CLICKED");
            handlePrevClicked(context, action);
        }
        else if (action.contains(NEXT_CLICKED)) {
            Log.d(LOG_TAG, "onReceive NEXT_CLICKED");
            handleNextClicked(context, action);
        }
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, MapWidgetProvider.class);

        Log.d(LOG_TAG, "getPendingSelfIntent class = " + MapWidgetProvider.class.toString());

        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void handleMapWidgetClicked(Context context, String action) {
        String id = action.replace(MAP_CLICKED, "");
        int intID = Integer.parseInt(id);

        // get map ID for this widget
        int mapID = getCurrrentMapID(context, intID);

        Log.d(LOG_TAG, "handleMapWidgetClicked widgetID = " + intID + " mapID = " + mapID);

        // send update command
        sendIntent(context, widgetIntentService.COMMAND_UPDATE, mapID, intID);

        // start map app
//        Intent startActivityIntent = new Intent(context, MainActivity.class);
//        startActivity(startActivityIntent);

    }

    private void handlePrevClicked(Context context, String action) {
        String id = action.replace(PREVIOUS_CLICKED, "");
        int intID = Integer.parseInt(id);

        // get map ID for this widget
        int mapID = getCurrrentMapID(context, intID);

        // send prev command
        sendIntent(context, widgetIntentService.COMMAND_PREVIOUS, mapID, intID);
        Log.d(LOG_TAG, "handlePrevClicked COMMAND_PREVIOUS widgetID = " + intID + " mapID = " + mapID);
    }

    private void handleNextClicked(Context context, String action) {
        String id = action.replace(NEXT_CLICKED, "");
        int intID = Integer.parseInt(id);

        // get map ID for this widget
        int mapID = getCurrrentMapID(context, intID);

        Log.d(LOG_TAG, "handleNextClicked widgetID = " + intID + " matchID = " + mapID);

        // send next command
        sendIntent(context, widgetIntentService.COMMAND_NEXT, mapID, intID);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(LOG_TAG, "onDeleted appWidgetIds.length = " + appWidgetIds.length);

        if (appWidgetIds != null) {
            for (int i=0; i<appWidgetIds.length; i++) {
                deleteMap(context, appWidgetIds[i]);
            }
        }

        super.onDeleted(context, appWidgetIds);
    }

    private static int getCurrrentMapID(Context context, int widgetID) {
        SharedPreferences prefs = context.getSharedPreferences(CURRENT_MAPS + widgetID, context.MODE_PRIVATE);
        int ret = prefs.getInt(SPECIFIC_MAP, 0);

        return ret;
    }

    private static void saveMatchID(Context context, int widgetID, int mapID) {
        SharedPreferences.Editor editPrefs = context.getSharedPreferences(CURRENT_MAPS + widgetID, context.MODE_PRIVATE).edit();

        Log.d(LOG_TAG, "saveMapID widgetID = " + widgetID + " mapID = " + mapID);

        editPrefs.putInt(SPECIFIC_MAP, mapID);
        editPrefs.commit();
    }

    private void deleteMap(Context context, int widgetID) {
        Log.d(LOG_TAG, "deleteMap widgetID = " + widgetID);

        SharedPreferences.Editor editPrefs = context.getSharedPreferences(CURRENT_MAPS + widgetID, context.MODE_PRIVATE).edit();
        editPrefs.remove(SPECIFIC_MAP);
        editPrefs.commit();
    }

    private static void sendIntent(Context context, int command, int mapID, int widgetID) {
        Intent msgIntent = new Intent(context, widgetIntentService.class);
        msgIntent.putExtra(widgetIntentService.PARAM_IN_COMMAND, command);
        msgIntent.putExtra(widgetIntentService.PARAM_MAPID, mapID);
        msgIntent.putExtra(widgetIntentService.PARAM_WIDGETID, widgetID);

        context.startService(msgIntent);
    }

    private static void updateView(Context context, Intent intent, int widgetID) {
        String startAddress = intent.getStringExtra(widgetIntentService.PARAM_OUT_START_ADDRESS);
        String endAddress = intent.getStringExtra(widgetIntentService.PARAM_OUT_END_ADDRESS);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_map);

        if ((startAddress != null) && (startAddress != null)) {
            views.setTextViewText(R.id.start_textview, "Start Address: " + startAddress);
            views.setTextViewText(R.id.dest_textview, "Destination Address:" + endAddress);
        }
        else {
            views.setTextViewText(R.id.start_textview, "No Route Data");
            views.setTextViewText(R.id.dest_textview, "");
        }

        Log.d(LOG_TAG, "updateView getPackageName = " + context.getPackageName());
        Log.d(LOG_TAG, "updateView widgetID = " + widgetID);
        Log.d(LOG_TAG, "updateView startAddress = " + startAddress);
        Log.d(LOG_TAG, "updateView endAddress = " + endAddress);

        views.setOnClickPendingIntent(R.id.linearLayout, getPendingSelfIntentEx(context, MAP_CLICKED + widgetID));
        views.setOnClickPendingIntent(R.id.buttonPrev, getPendingSelfIntentEx(context, PREVIOUS_CLICKED + widgetID));
        views.setOnClickPendingIntent(R.id.buttonNext, getPendingSelfIntentEx(context, NEXT_CLICKED + widgetID));

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(widgetID, views);
    }

    protected static PendingIntent getPendingSelfIntentEx(Context context, String action) {
        Log.d(LOG_TAG, "getPendingSelfIntentEx action = " + action);

        Intent intent = new Intent(context, MapWidgetProvider.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    public static class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP   = "pathfinder.intent.action.MESSAGE_PROCESSED";
        public static final String LOG_TAG = "ResponseReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(LOG_TAG, "onReceive action = " + action);

            if (action.equals(ACTION_RESP)) {
                int widgetID = intent.getIntExtra(widgetIntentService.PARAM_WIDGETID, 0);
                int mapID = intent.getIntExtra(widgetIntentService.PARAM_MAPID, 0);
                Log.d(LOG_TAG, "onReceive mapID = " + mapID);

                saveMatchID(context, widgetID, mapID);
                updateView(context, intent, widgetID);
            }
        }
    }

}

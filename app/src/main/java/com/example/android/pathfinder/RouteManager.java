package com.example.android.pathfinder;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.example.android.pathfinder.Database.RouteProvider;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kenm on 3/1/2016.
 */
public class RouteManager {
    private final String LOG_TAG = "RouteManager";

    private ArrayDeque<RoutePoints> routes = new ArrayDeque<>();
    private RoutePoints mCurrentRoute = new RoutePoints();
    private int dequeMaxSize = 5;
    private int mMaxRoutes = 5;

    public static final String KEY_ROUTES        = "Routes";
    public static final String KEY_START_LAT     = "StartLat";
    public static final String KEY_START_LON     = "StartLon";
    public static final String KEY_END_LAT       = "EndLat";
    public static final String KEY_END_LON       = "EndLon";
    public static final String KEY_START_ADDRESS = "StartAddress";
    public static final String KEY_END_ADDRESS   = "EndAddres";

    private static RouteManager ourInstance = new RouteManager();

    public static RouteManager getInstance() {
        return ourInstance;
    }

    private RouteManager() {
        Log.v(LOG_TAG, "RouteManager()");
    }

    private void NewRoute() {
        mCurrentRoute = new RoutePoints();
    }

    public void SetRoutesSaved(int numRoutes) {
        mMaxRoutes = numRoutes;
    }

    public void UpdateCurrentRoute(LatLng startingPoint, LatLng endingPoint) {
        if (mCurrentRoute != null) {
            mCurrentRoute.startingPoint = startingPoint;
            mCurrentRoute.endingPoint = endingPoint;
        }
    }

    public void UpdateCurrentRoute(String startingAddr, String endingAddr) {
        if (mCurrentRoute != null) {
            mCurrentRoute.startAddress = startingAddr;
            mCurrentRoute.endAddress = endingAddr;
        }
    }

    public void UpdateStartPoint(LatLng startingPoint) {
        if (mCurrentRoute != null) {
            mCurrentRoute.startingPoint = startingPoint;
        }
    }

    public void UpdateEndPoint(LatLng endingPoint) {
        if (mCurrentRoute != null) {
            mCurrentRoute.endingPoint = endingPoint;
        }
    }

    public void UpdateStartAddress(String startingAddr) {
        if (mCurrentRoute != null) {
            mCurrentRoute.startAddress = startingAddr;
        }
    }

    public void UpdateEndAddress(String endingAddr) {
        if (mCurrentRoute != null) {
            mCurrentRoute.endAddress = endingAddr;
        }
    }

    public LatLng GetCurrentStartPoint() {
        LatLng latLng = null;

        if (mCurrentRoute != null) {
            latLng = mCurrentRoute.startingPoint;
        }

        return latLng;
    }

    public String GetCurrentStart() {
        String ret = "";

        if (mCurrentRoute != null) {
            ret = mCurrentRoute.startAddress;
        }

        return ret;
    }

    public String GetCurrentEnd() {
        String ret = "";

        if (mCurrentRoute != null) {
            ret = mCurrentRoute.endAddress;
        }

        return ret;
    }

    public int GetCount() {
        int ret = 0;

        if (routes != null) {
            ret = routes.size();
        }

        return ret;
    }

    public RoutePoints PopRoute() {
        RoutePoints route = null;
        if (routes != null) {
            route = routes.removeFirst();
        }

        return route;
    }

    public RoutePoints PeekLast() {
        RoutePoints route = null;
        if (routes != null) {
            route = routes.peekLast();
        }

        return route;
    }
    public void SaveRoute() {
        if (mCurrentRoute != null) {
            routes.push(mCurrentRoute);

            // need to save the start/end addresses in case one doesn't change for the next route
            String start = mCurrentRoute.startAddress;
            String end = mCurrentRoute.endAddress;
            NewRoute();
            mCurrentRoute.startAddress = start;
            mCurrentRoute.endAddress = end;

            Log.d(LOG_TAG, "SaveRoute() start = " + mCurrentRoute.startAddress + " end = " + mCurrentRoute.endAddress);

            if (routes.size() > dequeMaxSize) {
                routes.removeLast();
            }
        }
    }

    public void AddRoute() {
        if (mCurrentRoute != null) {
            Log.d(LOG_TAG, "AddRoute() start = " + mCurrentRoute.startAddress + " end = " + mCurrentRoute.endAddress);

            routes.addLast(mCurrentRoute);
        }
    }

    // the list is small, so we'll quickly copy the elements we want to keep...
    private void RemoveRoute(int index) {
        ArrayDeque<RoutePoints> tempRoutes = new ArrayDeque<>();
        int i = 0;
        for (RoutePoints route : routes) {
            if (i++ != index) {
                tempRoutes.addLast(route);
            }
        }

        routes = tempRoutes;
    }

    public void AddRoute(RoutePoints route) {
        NewRoute();
        UpdateCurrentRoute(route.startingPoint, route.endingPoint);
        UpdateCurrentRoute(route.startAddress, route.endAddress);
        AddRoute();
    }

    public void clear() {
        if (routes != null) {
            routes.clear();
        }
    }

    public String[] GetRoutes() {
        String[] ret = null;

        if (routes.size() > 0) {
            ret = new String[routes.size()];
            int i = 0;
            for (RoutePoints route : routes) {
                ret[i++] = route.startAddress + " , " + route.endAddress;
            }
        }

        return ret;
    }

    public Bundle GetRoute(int routeIndex) {
        Bundle bundle = new Bundle();

        if (routes.size() > 0) {
            int i = 0;

            for (RoutePoints route : routes) {
                if (i == routeIndex) {
                    bundle.putString(KEY_START_ADDRESS, route.startAddress);
                    bundle.putString(KEY_END_ADDRESS, route.endAddress);

                    bundle.putDouble(KEY_START_LAT, route.startingPoint.latitude);
                    bundle.putDouble(KEY_START_LON, route.startingPoint.longitude);
                    bundle.putDouble(KEY_END_LAT, route.endingPoint.latitude);
                    bundle.putDouble(KEY_END_LON, route.endingPoint.longitude);

                    RemoveRoute(i);
                    break;
                }

                i++;
            }
        }

        return bundle;
    }

    private void buildTestData() {
        buildRoute(1, 2);
        buildRoute(2, 3);
        buildRoute(3, 4);
        buildRoute(4, 5);
        buildRoute(5, 6);
        buildRoute(6, 7);
        buildRoute(7, 8);
        buildRoute(8, 9);

        String json = getRoutes();
        buildRoutes(json);
    }

    private void buildRoute(double startPoint, double endPoint) {

        LatLng latLngStart = new LatLng(startPoint, startPoint);
        LatLng latLngEnd = new LatLng(endPoint, endPoint);
        String startAdd = "start " +startPoint;
        String endAdd = "end " +endPoint;

        NewRoute();
        UpdateCurrentRoute(latLngStart, latLngEnd);
        UpdateCurrentRoute(startAdd, endAdd);
        SaveRoute();
    }

    public String getRoutes() {
        JSONArray jsonArray = new JSONArray();

        for (RoutePoints route : routes) {
            HashMap<String, String> map = new HashMap<String, String>();

            if (route.startingPoint != null) {
                map.put(KEY_START_LAT, String.valueOf(route.startingPoint.latitude));
                map.put(KEY_START_LON, String.valueOf(route.startingPoint.longitude));
            }

            if (route.endingPoint != null) {
                map.put(KEY_END_LAT, String.valueOf(route.endingPoint.latitude));
                map.put(KEY_END_LON, String.valueOf(route.endingPoint.longitude));
            }

            map.put(KEY_START_ADDRESS, route.startAddress);
            map.put(KEY_END_ADDRESS, route.endAddress);

            JSONObject jsonObject = new JSONObject(map);
            jsonArray.put(jsonObject);
        }

        String ret = jsonArray.toString();

        Log.v(LOG_TAG, "getRoutes() temp = " + ret);
        return ret;
    }

    public void buildRoutes(String jsonString) {
        routes.clear();

        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            for(int j=0;j<jsonArray.length();j++) {
                NewRoute();

                String strLon = (String)((JSONObject) jsonArray.get(j)).get(KEY_START_LON);
                double startLon = Double.parseDouble(strLon);
                String strLat = (String)((JSONObject) jsonArray.get(j)).get(KEY_START_LAT);
                double startLat = Double.parseDouble(strLat);

                strLon = (String)((JSONObject) jsonArray.get(j)).get(KEY_END_LON);
                double endLon = Double.parseDouble(strLon);
                strLat = (String)((JSONObject) jsonArray.get(j)).get(KEY_END_LAT);
                double endLat = Double.parseDouble(strLat);

                LatLng latLngStart = new LatLng(startLon, startLat);
                LatLng latLngEnd = new LatLng(endLon, endLat);
                UpdateCurrentRoute(latLngStart, latLngEnd);

                String startAddr = (String)((JSONObject) jsonArray.get(j)).get(KEY_START_ADDRESS);
                String endAddr = (String)((JSONObject) jsonArray.get(j)).get(KEY_END_ADDRESS);
                UpdateCurrentRoute(startAddr, endAddr);

                AddRoute();
            }

        }
        catch (JSONException e) {
            Log.v(LOG_TAG, "buildRoutes() JSONException = " + e.toString());
        }
        catch (NumberFormatException e) {
            // p did not contain a valid double
        }

        Log.v(LOG_TAG, "buildRoutes() successful");
    }
}

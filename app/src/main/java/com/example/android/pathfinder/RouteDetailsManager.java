package com.example.android.pathfinder;

import android.util.Log;

import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kenm on 3/8/2016.
 */
public class RouteDetailsManager {
    private final String LOG_TAG = "RouteDetailsManager";
    private List<RouteDetails> routeDetails = new ArrayList<RouteDetails>();

    private static RouteDetailsManager ourInstance = new RouteDetailsManager();

    public static RouteDetailsManager getInstance() {
        return ourInstance;
    }

    private RouteDetailsManager() {
        Log.v(LOG_TAG, "RouteDetailsManager()");
    }

    public void add(Route route, PolylineOptions lineOptions) {
        RouteDetails details = new RouteDetails();
        details.route = route;
        details.lineOptions = lineOptions;
        routeDetails.add(details);

        Log.v(LOG_TAG, "add() new size = " + routeDetails.size());
    }

    public void add(int index, Route route) {
        RouteDetails details = null;

        if (index < routeDetails.size()) {
            details = routeDetails.get(index);
            details.route = route;
            routeDetails.set(index, details);
        }
        else {
            details = new RouteDetails();
            details.route = route;
            routeDetails.add(details);
        }


        Log.v(LOG_TAG, "add() updated route new size = " + routeDetails.size());
    }

    public void add(int index, PolylineOptions lineOptions) {
        RouteDetails details = null;

        if (index < routeDetails.size()) {
            details = routeDetails.get(index);
            details.lineOptions = lineOptions;
            routeDetails.set(index, details);
        }
        else {
            details = new RouteDetails();
            details.lineOptions = lineOptions;
            routeDetails.add(details);
        }


        Log.v(LOG_TAG, "add() updated lineOptions new size = " + routeDetails.size());
    }

    public void clear() {
        Log.v(LOG_TAG, "clear()");

        routeDetails.clear();
    }

    public int size() {
        int ret = routeDetails.size();

        return ret;
    }

    public RouteDetails GetRouteDetails(int index) {
        RouteDetails details = null;

        if (index < routeDetails.size()) {
            details = routeDetails.get(index);
        }

        return details;
    }

    public PolylineOptions GetRoutePolyline(int index) {
        PolylineOptions polylineOptions = null;

        if (index < routeDetails.size()) {
            polylineOptions = routeDetails.get(index).lineOptions;
        }

        return polylineOptions;
    }

    public Route GetRoute(int index) {
        Route route = null;

        if (index < routeDetails.size()) {
            route = routeDetails.get(index).route;
        }

        return route;
    }

    public class RouteDetails {
        Route route;
        PolylineOptions lineOptions;
    }
}

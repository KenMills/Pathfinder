package com.example.android.pathfinder.MapUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.android.pathfinder.RouteDetailsManager;
import layout.Settings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kenm on 2/17/2016.
 */
public class RouteUtil {
    private final String LOG_TAG = "RouteUtil";
    public static final int MSG_ROUTE_COMPLETE   = 3;    // msgs 1, 2, 4, 5 are from location util

    private String defaultUrl = "https://maps.googleapis.com/maps/api/directions/json";
    private String routeKey = "key=AIzaSyAem4z8OngMrzIvDVMrwGt14MOSmUowhKA";

    private Options mOptions = null;

    private Handler mHandler = null;
    // https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=AIzaSyAem4z8OngMrzIvDVMrwGt14MOSmUowhKA

    public RouteUtil(final Handler handler) {
        mHandler = handler;
    }

    public String getDirectionsUrl(LatLng origin, LatLng dest){

        // String for the origin of the route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest   = "destination=" + dest.latitude + "," + dest.longitude;

        // Enable transit mode
        String mode = "mode=transit";

        // We build the parameters for our URL string
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String options = BuildOptionsString();

        if (options.length() > 0) {
            parameters += options;
        }

        // Construction of the entire URL to the Directions API.
        String url = defaultUrl + "?" + parameters + "&" + routeKey;;
        Log.v(LOG_TAG, "getDirectionsUrl() url =" + url);

        new GetRouteTask().execute(url);
        return url;
    }

    private void SendMsg() {
        if (mHandler != null) {
            Message message = Message.obtain();
            message.setTarget(mHandler);
            message.what = MSG_ROUTE_COMPLETE;
            Bundle bundle = new Bundle();
            bundle.putString("address", "test");
            message.setData(bundle);
            message.sendToTarget();
        }
    }
    private String downloadUrl(String strUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();

//        Log.d(LOG_TAG, "downloadUrl strUrl= " + strUrl);
        Request request = new Request.Builder()
                .url(strUrl)
                .build();

        Response response = client.newCall(request).execute();

        return response.body().string();
    }

    public Options GetOptions() {
        Options options = new Options();
        return options;
    }

    public void SetOptions(Options options) {
        mOptions = options;
    }

    private String BuildOptionsString() {
        String ret = "";

        if (mOptions != null) {
            if (mOptions.alternatives) {
                ret += "&alternatives=true";
            }
            else {
                ret += "&alternatives=false";
            }

            if (mOptions.transit_routing_preference == mOptions.BEST_ROUTE) {
                ret += "&transit_routing_preference=less_walking";
            }
            else if (mOptions.transit_routing_preference == mOptions.LESS_WALKING) {
                ret += "&transit_routing_preference=less_walking";
            }
            else if (mOptions.transit_routing_preference == mOptions.FEWER_TRANSFERS) {
                ret += "&transit_routing_preference=fewer_transfers";
            }

            if (mOptions.leaving == mOptions.LEAVING_AT) {
                String time = getTime(mOptions.date.month, mOptions.date.day, mOptions.date.year, mOptions.time.hours, mOptions.time.minutes);
                ret += "&departure_time=" + time;
            }
            else if (mOptions.leaving == mOptions.ARRIVING_AT) {
                String time = getTime(mOptions.date.month, mOptions.date.day, mOptions.date.year, mOptions.time.hours, mOptions.time.minutes);
                ret += "&arrival_time=" + time;
            }

            ret += getTransitMode();
        }

        return ret;
    }

    private String getTransitMode() {
        String ret = "";
        int added = 0;

        if (mOptions == null) {
            return ret;
        }

        if (mOptions.transit_mode.bus) {
            added++;
            ret = "&transit_mode=bus";
        }

        if (mOptions.transit_mode.subway) {
            if (added++ > 0) {
                ret += "|subway";
            }
            else {
                ret = "&transit_mode=subway";
            }
        }

        if (mOptions.transit_mode.train) {
            if (added++ > 0) {
                ret += "|train";
            }
            else {
                ret = "&transit_mode=train";
            }
        }

        if (mOptions.transit_mode.tram) {
            if (added++ > 0) {
                ret += "|tram";
            }
            else {
                ret = "&transit_mode=tram";
            }
        }

        if (mOptions.transit_mode.rail) {
            if (added++ > 0) {
                ret += "|rail";
            }
            else {
                ret = "&transit_mode=rail";
            }
        }

        return ret;
    }

    public class Options {
        public int BEST_ROUTE      = 0;  // default to less walking for now...
        public int LESS_WALKING    = 0;
        public int FEWER_TRANSFERS = 1;

        public int LEAVING_NOW = 0;
        public int LEAVING_AT  = 1;
        public int ARRIVING_AT = 2;

        public boolean alternatives = false;
        public int transit_routing_preference = BEST_ROUTE;
        public int leaving = LEAVING_NOW;
        public TransitMode transit_mode = new TransitMode();
        public TravelTime time = new TravelTime();
        public TravelDate date = new TravelDate();

        public class TransitMode {
            public boolean bus = true;
            public boolean subway = false;
            public boolean train = false;
            public boolean tram = false;
            public boolean rail = false;
        }

        public class TravelTime {
            public int hours = 0;
            public int minutes = 0;
        }

        public class TravelDate {
            public int year = 2016;
            public int month = 1;
            public int day = 1;
        }
    }

    private String getTime(int month, int day, int year, int hour, int minute) {
        String ret = "";

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);

        Date date = calendar.getTime();
        long secondsInDate = date.getTime()/1000;    // convert millsec to sec

        ret = Long.toString(secondsInDate);

        return ret;
    }

    private class GetRouteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.e(LOG_TAG, "Background Task " + e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
//            Log.d(LOG_TAG, "onPostExecute result= " + result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            RouteDetailsManager detailsManager = RouteDetailsManager.getInstance();

            if (result != null) {
                Log.v(LOG_TAG, "ParserTask::onPostExecute() number of routes =" + result.size());

                // Traversing through all the routes
                for(int i=0;i<result.size();i++){
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for(int j=0;j<path.size();j++){
                        HashMap<String,String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(8);
                    lineOptions.color(Settings.SettingsFragment.getRouteHighlight());

                    detailsManager.add(i, lineOptions);
                }

                SendMsg();
            }

            // Drawing polyline in the Google Map for the i-th route
            //map.addPolyline(lineOptions);
        }
    }
}

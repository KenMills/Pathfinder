package com.example.android.pathfinder.MapUtils;

import android.util.Log;

import com.example.android.pathfinder.Route;
import com.example.android.pathfinder.RouteDetailsManager;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kenm on 2/18/2016.
 */
public class DirectionsJSONParser {
    private static String LOG_TAG = "DirectionsJSONParser";

    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        JSONArray walkingSteps = null;
        RouteDetailsManager detailsManager = RouteDetailsManager.getInstance();
        detailsManager.clear();

        Log.d(LOG_TAG, "parse()++");

        try {
            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                Route route = new Route();
                route.fareText = (String)((JSONObject)((JSONObject)jRoutes.get(i)).get("fare")).get("text");

                route.legs = new Route.Leg[jLegs.length()];

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                    route.legs[j] = new Route.Leg();
                    route.legs[j].arrivalTime = (String)((JSONObject)((JSONObject)jLegs.get(j)).get("arrival_time")).get("text");
                    route.legs[j].departureTime = (String)((JSONObject)((JSONObject)jLegs.get(j)).get("departure_time")).get("text");
                    route.legs[j].distanceText = (String)((JSONObject)((JSONObject)jLegs.get(j)).get("distance")).get("text");
                    route.legs[j].durationText = (String)((JSONObject)((JSONObject)jLegs.get(j)).get("duration")).get("text");
                    route.legs[j].startAddress = (String)((JSONObject)jLegs.get(j)).get("start_address");
                    route.legs[j].endAddress = (String)((JSONObject)jLegs.get(j)).get("end_address");

                    route.legs[j].distanceValue = ((JSONObject)((JSONObject)jLegs.get(j)).get("distance")).getInt("value");
                    route.legs[j].durationValue = ((JSONObject)((JSONObject)jLegs.get(j)).get("duration")).getInt("value");

                    route.legs[j].steps = new Route.Step[jSteps.length()];

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        route.legs[j].steps[k] = new Route.Step();

                        String travelMode = (String)((JSONObject)jSteps.get(k)).get("travel_mode");
                        if (travelMode.equalsIgnoreCase("WALKING")) {
                            walkingSteps = ( (JSONObject)jSteps.get(j)).getJSONArray("steps");

                            route.legs[j].steps[k].walkingSteps = new Route.WalkingSteps[walkingSteps.length()];

                            for(int m=0;m<walkingSteps.length();m++){
                                route.legs[j].steps[k].walkingSteps[m] = new Route.WalkingSteps();

                                route.legs[j].steps[k].walkingSteps[m].distanceText = (String)((JSONObject)((JSONObject)walkingSteps.get(m)).get("distance")).get("text");
                                route.legs[j].steps[k].walkingSteps[m].durationText = (String)((JSONObject)((JSONObject)walkingSteps.get(m)).get("duration")).get("text");
                                route.legs[j].steps[k].walkingSteps[m].travelMode = (String)((JSONObject)walkingSteps.get(m)).get("travel_mode");
                                route.legs[j].steps[k].walkingSteps[m].htmlInstructions = (String)((JSONObject)walkingSteps.get(m)).get("html_instructions");

                                route.legs[j].steps[k].walkingSteps[m].distanceValue = ((JSONObject)((JSONObject)walkingSteps.get(m)).get("distance")).getInt("value");
                                route.legs[j].steps[k].walkingSteps[m].durationValue = ((JSONObject)((JSONObject)walkingSteps.get(m)).get("duration")).getInt("value");
                            }
                        }
                        else if (travelMode.equalsIgnoreCase("TRANSIT")) {
                            route.legs[j].steps[k].transit = new Route.Transit();

                            route.legs[j].steps[k].transit.arrivalStop = (String)((JSONObject) ((JSONObject)((JSONObject)jSteps.get(k)).get("transit_details")).get("arrival_stop")).get("name");
                            route.legs[j].steps[k].transit.arrivalTime = (String)((JSONObject) ((JSONObject)((JSONObject)jSteps.get(k)).get("transit_details")).get("arrival_time")).get("text");
                            route.legs[j].steps[k].transit.departureStop = (String)((JSONObject) ((JSONObject)((JSONObject)jSteps.get(k)).get("transit_details")).get("departure_stop")).get("name");
                            route.legs[j].steps[k].transit.departureTime = (String)((JSONObject) ((JSONObject)((JSONObject)jSteps.get(k)).get("transit_details")).get("departure_time")).get("text");
                            route.legs[j].steps[k].transit.vehicleName = (String)((JSONObject) ((JSONObject)((JSONObject)jSteps.get(k)).get("transit_details")).get("line")).get("short_name");
                            route.legs[j].steps[k].transit.vehicleType = (String)((JSONObject)((JSONObject) ((JSONObject)((JSONObject)jSteps.get(k)).get("transit_details")).get("line")).get("vehicle")).get("name");
                        }
                        else {
                            route.legs[j].steps[k] = new Route.Step();
                        }

                        route.legs[j].steps[k].distanceText = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("distance")).get("text");
                        route.legs[j].steps[k].durationText = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("duration")).get("text");
                        route.legs[j].steps[k].travelMode = travelMode;
                        route.legs[j].steps[k].htmlInstructions = (String)((JSONObject)jSteps.get(k)).get("html_instructions");

                        route.legs[j].steps[k].distanceValue = ((JSONObject)((JSONObject)jSteps.get(k)).get("distance")).getInt("value");
                        route.legs[j].steps[k].durationValue = ((JSONObject)((JSONObject)jSteps.get(k)).get("duration")).getInt("value");

                        /** Traversing all points */
                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }

                detailsManager.add(i, route);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
            Log.d(LOG_TAG, "Exception detected result = " + e.toString());
        }

        Log.d(LOG_TAG, "parse()--");

        return routes;
    }
    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

}

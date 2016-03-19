package com.example.android.pathfinder.MapUtils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by kenm on 2/16/2016.
 */
public class LocationUtil {
    private static final String TAG = "LocationAddress";
    public static final int MARKER_MSG_START = 1;
    public static final int MARKER_MSG_DEST  = 2;
    // msg 3 is reserved for routeUtil
    public static final int POINT_MSG_START  = 4;
    public static final int POINT_MSG_DEST   = 5;

    public static final String ADDRESS_KEY   = "address";
    public static final String LATITUDE_KEY  = "latitude";
    public static final String LONGITUDE_KEY = "longitude";

    public static void getAddressFromLocation(final int locationType, final double latitude, final double longitude,
                                              final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);

                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        StringBuilder sb = new StringBuilder();
                        int maxLines = address.getMaxAddressLineIndex();

                        for (int i = 0; i < maxLines; i++) {
                            if (i == (maxLines-1)) {
                                sb.append(address.getAddressLine(i));
                            }
                            else {
                                sb.append(address.getAddressLine(i)).append("\n");
                            }
                        }

                        // city, state, and zip code are currently in the address already...
//                        sb.append(address.getLocality()).append("\n");
//                        sb.append(address.getPostalCode()).append("\n");
//                        sb.append(address.getCountryName());
                        result = sb.toString();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable connect to Geocoder", e);
                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (result != null) {
                        message.what = locationType;
                        Bundle bundle = new Bundle();
//                        result = "Latitude: " + latitude + " Longitude: " + longitude +
//                                "\n\nAddress:\n" + result;
                        bundle.putString("address", result);
                        message.setData(bundle);
                    } else {
                        message.what = locationType;
                        Bundle bundle = new Bundle();
                        result = "Latitude: " + latitude + " Longitude: " + longitude +
                                "\n Unable to get address for this lat-long.";
                        bundle.putString(ADDRESS_KEY, result);
                        message.setData(bundle);
                    }

                    message.sendToTarget();
                }
            }
        };

        thread.start();
    }

    public void getLocationFromAddress(final String strAddress, final int locationType, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder coder = new Geocoder(context);
                List<Address> address;
                double longitude = 0.0;
                double latitude = 0.0;

                try {
                    address = coder.getFromLocationName(strAddress, 5);
                    if ((address != null) && (address.size() > 0)) {
                        Address location = address.get(0);
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
                catch (IOException e) {
                    Log.e(TAG, "getLocationFromAddress() exception", e);
                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    message.what = locationType;

                    Bundle bundle = new Bundle();
                    bundle.putDouble(LATITUDE_KEY, latitude);
                    bundle.putDouble(LONGITUDE_KEY, longitude);
                    message.setData(bundle);

                    message.sendToTarget();
                }
            }
        };

        thread.start();
    }
}
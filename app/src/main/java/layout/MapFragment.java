package layout;

import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.pathfinder.MapUtils.LocationUtil;
import com.example.android.pathfinder.MapUtils.RouteUtil;
import com.example.android.pathfinder.R;
import com.example.android.pathfinder.RouteDetailsManager;
import com.example.android.pathfinder.RouteManager;
import com.example.android.pathfinder.SetDate;
import com.example.android.pathfinder.SetTime;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.Calendar;

public class MapFragment extends Fragment
    implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        LocationListener {
    private final String LOG_TAG = "MapFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_START_ADDRESS = "StartAddress";
    private static final String ARG_END_ADDRESS = "EndAddress";

    // TODO: Rename and change types of parameters
    private String mParamStart = null;
    private String mParamEnd = null;

    private OnMapFragmentListener mListener;
    View mRootView;

    private boolean mCameraInit = false;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation = null;
    private Location mInitialLocation = null;
    private LocationRequest mLocationRequest;
    private Marker startMarker = null;
    private Marker endMarker = null;
    private RouteUtil mRouteUtil = null;
    private Polyline mCurrentPolyLine = null;
    private int mRouteTime = Settings.SettingsFragment.LEAVING_BTN_LEAVE_NOW;

    private RouteManager mRouteManager = null;
    private Bundle timeBundle = null;
    private Bundle dateBundle = null;

    private final String MARKER_START = "Start";
    private final String MARKER_DEST  = "Destination";
    private boolean mHandleInitialLocation = true;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public interface OnMapFragmentListener {
        void onStartMarkerUpdate(String string);
        void onEndMarkerUpdate(String string);
        void onRouteCalcComplete(int numRoutes);
        void onMapConnected();
    }

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_START_ADDRESS, param1);
        args.putString(ARG_END_ADDRESS, param2);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamStart = getArguments().getString(ARG_START_ADDRESS);
            mParamEnd = getArguments().getString(ARG_END_ADDRESS);
            if ((mParamStart != null) && (mParamEnd != null)) {
                mHandleInitialLocation = false;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView()");

        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_map, container, false);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        setUpMapIfNeeded();
        mRouteManager = RouteManager.getInstance();

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.v(LOG_TAG, "onAttach()");

        if (context instanceof OnMapFragmentListener) {
            mListener = (OnMapFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Log.v(LOG_TAG, "onAttach()");

        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.v(LOG_TAG, "onPause()");

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.v(LOG_TAG, "onResume()");

        setUpMapIfNeeded();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    private void initCamera(Location location) {

        if (mCameraInit == false) {
            Log.v(LOG_TAG, "initCamera location = " + location.toString());

            Float zoomLevel = Settings.SettingsFragment.getZoomLevel();
            CameraPosition position = CameraPosition.builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(zoomLevel)
                    .bearing(0.0f)
                    .tilt(0.0f)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);

            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED  ||
                    ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }

            mMap.setTrafficEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mCameraInit = true;
        }
    }

    private void setUpMapIfNeeded() {
        Log.v(LOG_TAG, "setUpMapIfNeeded");

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            com.google.android.gms.maps.MapFragment mapFragment = (com.google.android.gms.maps.MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.mapView);
            mapFragment.getMapAsync(this);
        }
    }

    private void initListeners() {
        Log.d(LOG_TAG, "initListeners()");

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    public void setTimeBundle(Bundle bundle) {
        timeBundle = bundle;
    }
    public void setDateBundle(Bundle bundle) {
        dateBundle = bundle;
    }

    public void setRouteTime(int time) {
        mRouteTime = time;
    }

    // Map overrides
    //*********************************************************************

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(LOG_TAG, "Location services connected.");

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED  ||
                ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mCurrentLocation == null) {
                // Blank for a moment...
                Log.v(LOG_TAG, "onConnected mCurrentLocation is null");

                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            else {
                Log.v(LOG_TAG, "onConnected mCurrentLocation = " + mCurrentLocation.toString());
                handleNewLocation(mCurrentLocation);

                initCamera(mCurrentLocation);
            }
        }

        if (mListener != null) {
            mListener.onMapConnected();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(LOG_TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "onLocationChanged()");

        handleNewLocation(location);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(LOG_TAG, "onConnectionFailed()");

        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(LOG_TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.v(LOG_TAG, "onMapClick latLng = " + latLng.toString());
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.v(LOG_TAG, "onMapLongClick latLng = " + latLng.toString());

        if (endMarker == null) {
            markDestinationLocation(latLng.latitude, latLng.longitude);

            LocationUtil locationAddress = new LocationUtil();
            locationAddress.getAddressFromLocation(LocationUtil.MARKER_MSG_DEST, latLng.latitude, latLng.longitude, getActivity().getApplicationContext(), new GeocoderHandler());

            getRoute();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.v(LOG_TAG, "onMapReady");

        if (mMap == null) {
            mMap = googleMap;

            initListeners();

            if ((mParamStart != null) && (mParamEnd != null)) {
                MoveStartMarker(mParamStart);
/* sometimes the geocoder fails to resolve an address.... tried adding delays
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        MoveEndMarker(mParamEnd);;
                    }
                }, 2000);
*/
                MoveEndMarker(mParamEnd);
            }
        }
    }

    private void AdjustZoom(LatLng location) {
        Log.v(LOG_TAG, "AdjustZoom()");

        if (location != null) {
            Float zoomLevel = Settings.SettingsFragment.getZoomLevel();
            Log.v(LOG_TAG, "AdjustZoom Lat = " +location.latitude + " Lon = " +location.longitude + " Zoom = " +zoomLevel);

            CameraPosition position = CameraPosition.builder()
                    .target(location)
                    .zoom(zoomLevel)
                    .bearing(0.0f)
                    .tilt(0.0f)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.v(LOG_TAG, "onMarkerClick marker = " + marker.toString());

        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.v(LOG_TAG, "onMarkerDragStart marker = " + marker.toString());
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Log.v(LOG_TAG, "onMarkerDrag marker = " + marker.toString());
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        String title = marker.getTitle();
        Log.v(LOG_TAG, "onMarkerDragEnd title = " + title);

        LatLng position = marker.getPosition();
        double lat = position.latitude;
        double lon = position.longitude;

        LocationUtil locationAddress = new LocationUtil();

        // recalculate the route when either marker moved
        getRoute();

        if (title.equals(MARKER_START)) {
            Log.v(LOG_TAG, "onMarkerDragEnd START MARKER END lat = " +lat + ", lon = " +lon);
            locationAddress.getAddressFromLocation(LocationUtil.MARKER_MSG_START, lat, lon, getActivity().getApplicationContext(), new GeocoderHandler());
        }

        if (title.equals(MARKER_DEST)) {
            Log.v(LOG_TAG, "onMarkerDragEnd Destination MARKER END lat = " +lat + ", lon = " +lon);
            locationAddress.getAddressFromLocation(LocationUtil.MARKER_MSG_DEST, lat, lon, getActivity().getApplicationContext(), new GeocoderHandler());
        }
    }

    private void setRouteOptions() {
        if (mRouteUtil != null) {
            RouteUtil.Options routeOptions = mRouteUtil.GetOptions();

            // leaving at...
            if (mRouteTime == Settings.SettingsFragment.LEAVING_BTN_LEAVE_NOW) {
                routeOptions.leaving = routeOptions.LEAVING_NOW;
            }
            else if (mRouteTime == Settings.SettingsFragment.LEAVING_BTN_LEAVE_AT) {
                routeOptions.leaving = routeOptions.LEAVING_AT;
            }
            else if (mRouteTime == Settings.SettingsFragment.LEAVING_BTN_ARRIVE_AT) {
                routeOptions.leaving = routeOptions.ARRIVING_AT;
            }

            CheckTime();    // if only one set, default the other to today/now

            // date
            if (dateBundle != null) {
                routeOptions.date.year = dateBundle.getInt(SetDate.KEY_YEAR);
                routeOptions.date.month = dateBundle.getInt(SetDate.KEY_MONTH);
                routeOptions.date.day = dateBundle.getInt(SetDate.KEY_DAY);
            }

            // time
            if (timeBundle != null) {
                routeOptions.time.hours = timeBundle.getInt(SetTime.KEY_HOUR);
                routeOptions.time.minutes = timeBundle.getInt(SetTime.KEY_MINUTE);
            }

            // route pref
            routeOptions.transit_routing_preference = Settings.SettingsFragment.getRouteType();

            // alt routes
            routeOptions.alternatives = Settings.SettingsFragment.getAlternateRoutes();

            // travel pref
            Settings.SettingsFragment.TravelPref travelPref = Settings.SettingsFragment.getSelectedRoutePref();
            if (travelPref != null) {
                routeOptions.transit_mode.bus = travelPref.bus;
                routeOptions.transit_mode.subway = travelPref.subway;
                routeOptions.transit_mode.train = travelPref.train;
                routeOptions.transit_mode.tram = travelPref.tram;
                routeOptions.transit_mode.rail = travelPref.tram;
            }

            mRouteUtil.SetOptions(routeOptions);

        }
    }

    private void CheckTime() {
        Calendar calendar = Calendar.getInstance();

        if ((dateBundle != null) && (timeBundle == null)) {
            // default to now
            timeBundle = new Bundle();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            timeBundle.putInt(SetTime.KEY_HOUR, hour);
            timeBundle.putInt(SetTime.KEY_MINUTE, minute);
        }

        if ((dateBundle == null) && (timeBundle != null)) {
            // default to now
            dateBundle = new Bundle();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            dateBundle.putInt(SetDate.KEY_YEAR, year);
            dateBundle.putInt(SetDate.KEY_MONTH, month);
            dateBundle.putInt(SetDate.KEY_DAY, day);
        }
    }

    public void ReCalcRoute() {
        Log.v(LOG_TAG, "ReCalcRoute()");

        getRoute();
    }

    public void UpdatePolyline(int routeNumber) {
        if (mCurrentPolyLine != null) {
            mCurrentPolyLine.remove();
        }

        RouteDetailsManager routeDetails = RouteDetailsManager.getInstance();
        int numRoutes = routeDetails.size();
        if (routeNumber < numRoutes) {
            mCurrentPolyLine = mMap.addPolyline(routeDetails.GetRoutePolyline(routeNumber));
        }
    }

    private void getRoute() {
        Log.d(LOG_TAG, "getRoute()++");

        mRouteUtil = new RouteUtil(new GeocoderHandler());

        if (startMarker != null) {
            LatLng originLatLng = new LatLng(startMarker.getPosition().latitude, startMarker.getPosition().longitude);

            if (endMarker != null) {
                LatLng destinationLatLng = new LatLng(endMarker.getPosition().latitude, endMarker.getPosition().longitude);

                setRouteOptions();
                String route = mRouteUtil.getDirectionsUrl(originLatLng, destinationLatLng);

                mRouteManager.UpdateCurrentRoute(originLatLng, destinationLatLng);

                Log.v(LOG_TAG, "getRoute route = " +route);
            }
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(LOG_TAG, "handleNewLocation() location=" + location.toString());

        if ((mInitialLocation == null) && (mHandleInitialLocation)){
            Log.d(LOG_TAG, "handleNewLocation() initial location=" + location.toString());
            mInitialLocation = location;
            markInitialLocation(location);

            // Init camera, it won't reinit if already initialized...
            if (mCameraInit == false) {
                initCamera(location);
            }
        }

        mCurrentLocation = location;
    }

    private void markInitialLocation(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        Log.d(LOG_TAG, "markInitialLocation() location=" + location.toString());

        startMarker = markLocation(currentLatitude, currentLongitude, "Start");

        LocationUtil locationAddress = new LocationUtil();
        locationAddress.getAddressFromLocation(LocationUtil.MARKER_MSG_START, currentLatitude, currentLongitude, getActivity().getApplicationContext(), new GeocoderHandler());
    }

    public void MoveStartMarker(String address) {
        Log.d(LOG_TAG, "MoveStartMarker() address=" + address);

        LocationUtil locationAddress = new LocationUtil();
        locationAddress.getLocationFromAddress(address, LocationUtil.POINT_MSG_START, getActivity().getApplicationContext(), new GeocoderHandler());
    }

    public void MoveEndMarker(String address) {
        Log.d(LOG_TAG, "MoveEndMarker() address=" + address);

        LocationUtil locationAddress = new LocationUtil();
        locationAddress.getLocationFromAddress(address, LocationUtil.POINT_MSG_DEST, getActivity().getApplicationContext(), new GeocoderHandler());
    }

    public void markStartLocation(double latitude, double longitude) {
        Log.d(LOG_TAG, "markStartLocation()");

        if (startMarker != null) {
            startMarker.remove();
        }

        startMarker = markLocation(latitude, longitude, MARKER_START);
    }

    public void markDestinationLocation(double latitude, double longitude) {
        Log.d(LOG_TAG, "markDestinationLocation()");

        if (endMarker != null) {
            endMarker.remove();
        }

        endMarker = markLocation(latitude, longitude, MARKER_DEST);
    }

    private Marker markLocation(double latitude, double longitude, String title) {
        Marker returnMarker;
        LatLng latLng = new LatLng(latitude, longitude);

        float hue = BitmapDescriptorFactory.HUE_AZURE;
        if (title == MARKER_DEST) {
            hue = BitmapDescriptorFactory.HUE_RED;
            mRouteManager.UpdateEndPoint(latLng);
        }
        else {
            mRouteManager.UpdateStartPoint(latLng);
        }

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(hue));

        returnMarker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        return returnMarker;
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {

            Log.v(LOG_TAG, "GeocoderHandler handleMessage()");

            switch (message.what) {
                case LocationUtil.MARKER_MSG_START: {
                    String locationAddress;
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString(LocationUtil.ADDRESS_KEY);
                    String address = locationAddress.replace("\n", ", ");

                    Log.v(LOG_TAG, "GeocoderHandler MARKER_MSG_START");
                    updateStart(address);
                }
                break;
                case LocationUtil.MARKER_MSG_DEST: {
                    String locationAddress;
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString(LocationUtil.ADDRESS_KEY);
                    String address = locationAddress.replace("\n", ", ");

                    Log.v(LOG_TAG, "GeocoderHandler MARKER_MSG_DEST");
                    updateDestination(address);
                }
                break;
                case RouteUtil.MSG_ROUTE_COMPLETE: {
                    Log.v(LOG_TAG, "GeocoderHandler RouteUtil.MSG_ROUTE_COMPLETE");

                    if (mCurrentPolyLine != null) {
                        mCurrentPolyLine.remove();
                    }

                    RouteDetailsManager routeDetails = RouteDetailsManager.getInstance();
                    int numRoutes = routeDetails.size();
                    if (numRoutes > 0) {
                        mCurrentPolyLine = mMap.addPolyline(routeDetails.GetRoutePolyline(0));
                        mRouteManager.SaveRoute();
                    }

                    DisplayRoutesFound(numRoutes);
//kmm                    AdjustZoom(mRouteManager.GetCurrentStartPoint());

                    if (mListener != null) {
                        mListener.onRouteCalcComplete(numRoutes);
                    }
                }
                break;
                case LocationUtil.POINT_MSG_START: {
                    Bundle bundle = message.getData();
                    double latitude = bundle.getDouble(LocationUtil.LATITUDE_KEY);
                    double longitude = bundle.getDouble(LocationUtil.LONGITUDE_KEY);

                    updateStartMarker(latitude, longitude);
                    Log.v(LOG_TAG, "GeocoderHandler LocationUtil.POINT_MSG_START latitude = " + latitude + " longitude = " + longitude);
                }
                break;
                case LocationUtil.POINT_MSG_DEST: {
                    Bundle bundle = message.getData();
                    double latitude = bundle.getDouble(LocationUtil.LATITUDE_KEY);
                    double longitude = bundle.getDouble(LocationUtil.LONGITUDE_KEY);

                    updateEndMarker(latitude, longitude);
                    Log.v(LOG_TAG, "GeocoderHandler LocationUtil.POINT_MSG_DEST latitude = " + latitude +" longitude = " + longitude);
                }
                break;

            }
        }

        private void DisplayRoutesFound(int numRoutes) {
            String toastMsg;

            if (numRoutes == 0) {
                toastMsg = "No Routes found.";
            }
            else if (numRoutes == 1) {
                toastMsg = "1 Route found.";
            }
            else {
                toastMsg = numRoutes + " Routes found.";
            }

//            Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_SHORT).show();
            Snackbar.make(mRootView, toastMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        private void updateStart(String address) {
            Log.d(LOG_TAG, "updateStart() address = " +address);

            mRouteManager.UpdateStartAddress(address);

            if (mListener != null) {
                mListener.onStartMarkerUpdate(address);
            }
        }

        private void updateDestination(String address) {
            Log.d(LOG_TAG, "updateDestination() address = " +address);

            mRouteManager.UpdateEndAddress(address);

            if (mListener != null) {
                mListener.onEndMarkerUpdate(address);
            }
        }

        private void updateStartMarker(double lat, double lon) {
            Log.d(LOG_TAG, "updateStartMarker()");

            if ((lon != 0.0) && (lat != 0.0)) {
                if (startMarker != null) {
                    startMarker.remove();
                }

                LatLng latLng = new LatLng(lat, lon);
                mRouteManager.UpdateStartPoint(latLng);
                startMarker = markLocation(lat, lon, MARKER_START);

                //kmm
                AdjustZoom(latLng);

                getRoute();
            }
            else {
                Log.v(LOG_TAG, "updateStartMarker() invalid point latitude = " + lat +" longitude = " + lon);
            }
        }

        private void updateEndMarker(double lat, double lon) {
            Log.d(LOG_TAG, "updateEndMarker()");

            if ((lon != 0.0) && (lat != 0.0)) {
                if (endMarker != null) {
                    endMarker.remove();
                }

                mRouteManager.UpdateEndPoint(new LatLng(lat, lon));
                endMarker = markLocation(lat, lon, MARKER_DEST);
                getRoute();
            }
            else {
                Log.v(LOG_TAG, "updateEndMarker() invalid point latitude = " + lat +" longitude = " + lon);
            }
        }
    }
}

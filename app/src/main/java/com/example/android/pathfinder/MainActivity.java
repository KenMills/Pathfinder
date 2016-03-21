package com.example.android.pathfinder;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.example.android.pathfinder.Database.RouteProvider;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;

import layout.MapDetailsFragment;
import layout.MapFragment;
import layout.MapSearchFragment;
import layout.RouteDetailsFragment;
import layout.Settings;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MapSearchFragment.OnSearchFragmentListener,
        MapDetailsFragment.OnLaunchRouteDetailsListener,
        RouteDetailsFragment.OnRouteDetailsListener,
        MapFragment.OnMapFragmentListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        Settings.SettingsFragment.OnSettingsChangedListener {

    private final String LOG_TAG       = "MainActivity";

    FloatingActionButton mFab;
    private SharedPreferences mSharedPreferences;
    private SubMenu recentRoutesMenu = null;
    private final String SUBMENU_RECENT_ROUTES  = "Recent Routes";
    private int ROUTE_GROUP = 1;

    public static int PREFERENCE_MODE_PRIVATE = 0;
    public static String PREFERENCE_FILE = "Pathfinder";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private RouteManager mRouteManager = null;
    private String FRAGMENT_TAG_SEARCH  = "MapSearchFragment";
    private String FRAGMENT_TAG_DETAILS = "MapDetailsFragment";
    private String FRAGMENT_TAG_ROUTE   = "RouteDetailsFragment";
    private String FRAGMENT_TAG_MAP     = "MapFragment";
    public static String START_BUNDLE   = "StartBundle";

    private static final int FRAGMENT_STATE_SEARCH        = 0;
    private static final int FRAGMENT_STATE_MAP_DETAILS   = 1;
    private static final int FRAGMENT_STATE_ROUTE_DETAILS = 2;
    private int mFragmentState = FRAGMENT_STATE_SEARCH;

    private int MAP_DETAILS_MAP_HEIGHT = 900;

    private CursorLoader mCursorLoader;
    private int ROUTE_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(LOG_TAG, "onCreate");

        if (isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Make to run your application only in LANDSCAPE mode
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Make to run your application only in portrait mode
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchMapDetails();
            }
        });

        mFab.hide();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Settings.SettingsFragment.readDefaults(getApplicationContext());
        mRouteManager = RouteManager.getInstance();

        // add recent routes to menu drawer
        Menu m = navigationView.getMenu();
        recentRoutesMenu = m.addSubMenu(SUBMENU_RECENT_ROUTES);

        Bundle intentBundle = GetIntentBundle();
        if (CheckGooglePlayService() == ConnectionResult.SUCCESS) {
            LoadFragment(GetMapFragment(intentBundle), R.id.map_container, true, FRAGMENT_TAG_MAP);
        }

        LoadFragment(GetSearchFragment(intentBundle), R.id.container, true, FRAGMENT_TAG_SEARCH);

        mSharedPreferences = getSharedPreferences(PREFERENCE_FILE, PREFERENCE_MODE_PRIVATE);

        Settings.SettingsFragment.SetupListener(this);

        getLoaderManager().initLoader(ROUTE_LOADER_ID, null, this);
    }

    private Bundle GetIntentBundle() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(START_BUNDLE);

        return bundle;
    }
    private MapFragment GetMapFragment(Bundle bundle) {
        MapFragment fragment = null;

        if (bundle == null) {
            fragment = new MapFragment();
        }
        else {
            String startAddress = bundle.getString(RouteManager.KEY_START_ADDRESS);
            String endAddress = bundle.getString(RouteManager.KEY_END_ADDRESS);

            Log.v(LOG_TAG, "GetMapFragment() startAddress = " + startAddress + " endAddress= " +endAddress);

            fragment = MapFragment.newInstance(startAddress, endAddress);
        }

        return fragment;
    }

    private MapSearchFragment GetSearchFragment(Bundle bundle) {
        MapSearchFragment fragment = null;

        if (bundle == null) {
            fragment = new MapSearchFragment();
        }
        else {
            String startAddress = bundle.getString(RouteManager.KEY_START_ADDRESS);
            String endAddress = bundle.getString(RouteManager.KEY_END_ADDRESS);

            fragment = MapSearchFragment.newInstance(startAddress, endAddress);
        }

        return fragment;
    }

    private void LoadFragment(Fragment fragment, int container, boolean first, String tag) {
        Log.v(LOG_TAG, "LoadFragment fragment = " + fragment.toString());

        if (fragment.getClass() == MapSearchFragment.class) {
            mFragmentState = FRAGMENT_STATE_SEARCH;
        }
        else if (fragment.getClass() == MapDetailsFragment.class) {
            mFragmentState = FRAGMENT_STATE_MAP_DETAILS;
        }
        else if (fragment.getClass() == RouteDetailsFragment.class) {
            mFragmentState = FRAGMENT_STATE_ROUTE_DETAILS;
        }

        if (fragment != null) {
            if (first) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(container, fragment, tag)
                        .commit();
            }
            else {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(container, fragment, tag)
//                        .addToBackStack(null) //handling this manually...
                        .commit();
            }

        }
    }

    private void LaunchMapDetails() {
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAP);
        if ((isTablet() == false) && (mapFragment != null)) {
            ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
            params.height = MAP_DETAILS_MAP_HEIGHT;
            mapFragment.getView().setLayoutParams(params);
        }

        mFab.hide();

        LoadFragment(new MapDetailsFragment(), R.id.container, true, FRAGMENT_TAG_DETAILS);
    }

    // SearchFragment interface methods
    //******************************************************************
    @Override
    public void onStartAddressUpdate(String string) {
        Log.v(LOG_TAG, "onStartAddressUpdate string = " + string);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAP);
        if (mapFragment != null) {
            mapFragment.MoveStartMarker(string);
        }
    }

    @Override
    public void onEndAddressUpdate(String string) {
        Log.v(LOG_TAG, "onEndAddressUpdate string = " + string);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAP);
        if (mapFragment != null) {
            mapFragment.MoveEndMarker(string);
        }
    }

    @Override
    public void onRouteTimeUpdate(int routeTime) {
        Log.v(LOG_TAG, "onRouteTimeUpdate routeTime = " + routeTime);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAP);
        if (mapFragment != null) {
            if (routeTime == MapSearchFragment.LEAVING_BTN_LEAVE_NOW) {
                mapFragment.setTimeBundle(null);
                mapFragment.setDateBundle(null);
            }

            mapFragment.setRouteTime(routeTime);
            mapFragment.ReCalcRoute();
        }
    }

    @Override
    public void onDateUpdate(Bundle bundle) {
        Log.v(LOG_TAG, "onDateUpdate bundle = " + bundle.toString());
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAP);
        if (mapFragment != null) {
            mapFragment.setDateBundle(bundle);
            mapFragment.ReCalcRoute();
        }
    }

    @Override
    public void onTimeUpdate(Bundle bundle) {
        Log.v(LOG_TAG, "onTimeUpdate bundle = " + bundle.toString());
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAP);
        if (mapFragment != null) {
            mapFragment.setTimeBundle(bundle);
            mapFragment.ReCalcRoute();
        }
    }
    //******************************************************************

    public void onLaunchDetails(String string) {
        Log.d(LOG_TAG, "onLaunchDetails");
    }

    @Override
    public void onUpdatePolyline(int position) {
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAP);
        if (mapFragment != null) {
            mapFragment.UpdatePolyline(position);
        }
    }

    @Override
    public void onLaunchRouteDetails(int position) {
        Log.d(LOG_TAG, "onLaunchRouteDetails");
        RouteDetailsFragment fragment = new RouteDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(RouteDetailsFragment.ARG_PARAM1, position);
        fragment.setArguments(bundle);

        LoadFragment(fragment, R.id.container, false, FRAGMENT_TAG_ROUTE);
    }

    @Override
    public void onRouteDetails(int position) {
        Log.d(LOG_TAG, "onRouteDetails");

        if (isTablet() == false) {
            RouteDetailsFragment fragment = new RouteDetailsFragment();

            Bundle bundle = new Bundle();
            bundle.putInt(RouteDetailsFragment.ARG_PARAM1, position);
            fragment.setArguments(bundle);
        }
    }

    // MapFragment interfaces
    //**********************************************************************
    @Override
    public void onRouteCalcComplete(int numRoutes) {
        Log.d(LOG_TAG, "onRouteCalcComplete");

        if (numRoutes > 0) {
            mFab.show();
        }
        else {
            mFab.hide();
        }

        int maxRoutes = Settings.SettingsFragment.getSavedRoutes();
        int routeCount = mRouteManager.GetCount();
        RoutePoints route = null;
        if (routeCount < maxRoutes) {
            route = mRouteManager.PeekLast();
            insertRoute(route);
        }
        else {
            deleteAll();    // delete all in db

            for (int i=0; i< routeCount; i++) {
                route = mRouteManager.PopRoute();
                insertRoute(route);
            }
        }

        // reset loader
        getLoaderManager().restartLoader(ROUTE_LOADER_ID, null, this);
    }

    @Override
    public void onStartMarkerUpdate(String string) {
        Log.d(LOG_TAG, "onStartMarkerUpdate");

        MapSearchFragment mapSearchFragment = (MapSearchFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_SEARCH);
        if (mapSearchFragment != null) {
            mapSearchFragment.UpdateStartAddress(string);
        }
    }

    @Override
    public void onEndMarkerUpdate(String string) {
        Log.d(LOG_TAG, "onEndMarkerUpdate");

        MapSearchFragment mapSearchFragment = (MapSearchFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_SEARCH);
        if (mapSearchFragment != null) {
            mapSearchFragment.UpdateEndAddress(string);
        }
    }

    @Override
    public void onMapConnected() {
    }
    //*********************************************************************

    @Override
    public void onBackPressed() {
        Log.v(LOG_TAG, "onBackPressed");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            handleOnBackPress();
        }
    }

    private void handleOnBackPress() {

        switch (mFragmentState) {
            case FRAGMENT_STATE_SEARCH: {
                Log.v(LOG_TAG, "handleOnBackPress state = FRAGMENT_STATE_SEARCH");
                super.onBackPressed();
            }
            break;
            case FRAGMENT_STATE_MAP_DETAILS: {
                Log.v(LOG_TAG, "handleOnBackPress state = FRAGMENT_STATE_MAP_DETAILS");
                LoadFragment(new MapSearchFragment(), R.id.container, true, FRAGMENT_TAG_SEARCH);
            }
            break;
            case FRAGMENT_STATE_ROUTE_DETAILS: {
                Log.v(LOG_TAG, "handleOnBackPress state = FRAGMENT_STATE_ROUTE_DETAILS");
                LoadFragment(new MapDetailsFragment(), R.id.container, true, FRAGMENT_TAG_DETAILS);
            }
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.v(LOG_TAG, "onOptionsItemSelected id=" + id);

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        int group = item.getGroupId();

        Log.v(LOG_TAG, "onNavigationItemSelected group = " + group + " id= " + id);

        if (group == ROUTE_GROUP) {
            updateRoute(mRouteManager.GetRoute(id));
        }
        else {
            if (id == R.id.nav_home) {
                showHome();
            }
            else if (id == R.id.nav_settings) {
                showSettings();
            }
            else if (id == R.id.nav_help) {
                showHelp();
            }
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void showHome() {
        if (mFragmentState != FRAGMENT_STATE_SEARCH) {
            LoadFragment(new MapSearchFragment(), R.id.container, true, FRAGMENT_TAG_SEARCH);
        }
    }

    private void showSettings() {
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }

    private void showHelp() {
        Intent i = new Intent(this, HelpActivity.class);
        startActivity(i);
    }

    private void updateRoute(Bundle bundle) {
        double startLat, startLon, endLat, endLon;
        String startAddress, endAddress;

        startAddress = bundle.getString(RouteManager.KEY_START_ADDRESS);
        endAddress = bundle.getString(RouteManager.KEY_END_ADDRESS);

        startLat = bundle.getDouble(RouteManager.KEY_START_LAT);
        startLon = bundle.getDouble(RouteManager.KEY_START_LON);
        endLat = bundle.getDouble(RouteManager.KEY_END_LAT);
        endLon = bundle.getDouble(RouteManager.KEY_END_LON);

        onStartMarkerUpdate(startAddress);
        onEndMarkerUpdate(endAddress);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAP);
        if (mapFragment != null) {
            mapFragment.markStartLocation(startLat, startLon);
            mapFragment.markDestinationLocation(endLat, endLon);
            mapFragment.ReCalcRoute();
        }

        mRouteManager.UpdateCurrentRoute(startAddress, endAddress);
        showHome();
    }

    private boolean isTablet() {
        return getResources().getBoolean(R.bool.isTablet);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(LOG_TAG, "onPause()");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "onResume()");
    }

    private int CheckGooglePlayService() {
        int ret;
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        ret = availability.isGooglePlayServicesAvailable(getApplicationContext());

        if (ret != ConnectionResult.SUCCESS){
            availability.showErrorDialogFragment(this, ret, 0);
        }

        return ret;
    }

    //***********************************************************************
    // Loader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader()");

        mCursorLoader = new CursorLoader(this, RouteProvider.CONTENT_URI, null, null, null, null);

        return mCursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(LOG_TAG, "onLoadFinished()");

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            recentRoutesMenu.clear();
            int max = Settings.SettingsFragment.getSavedRoutes();
            if (max > cursor.getCount()) {
                max = cursor.getCount();
            }

            for (int i=0; i<max; i++) {
                RoutePoints route = new RoutePoints();

                route.startAddress = cursor.getString(cursor.getColumnIndex(RouteProvider.startAddressField));
                route.endAddress = cursor.getString(cursor.getColumnIndex(RouteProvider.endAddressField));

                double startLat = cursor.getDouble(cursor.getColumnIndex(RouteProvider.startLatField));
                double startLon = cursor.getDouble(cursor.getColumnIndex(RouteProvider.startLonField));
                double endLat = cursor.getDouble(cursor.getColumnIndex(RouteProvider.endLatField));
                double endLon = cursor.getDouble(cursor.getColumnIndex(RouteProvider.endLonField));

                route.startingPoint = new LatLng(startLat, startLon);
                route.endingPoint = new LatLng(endLat, endLon);

                // onLoadFinished, update drawer and manager
                mRouteManager.AddRoute(route);
                AddRouteToDrawer(route, i);

                cursor.moveToNext();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset()");
    }

    private void AddRouteToDrawer(RoutePoints route, int index) {
        if ((route != null) && (recentRoutesMenu != null)) {
            String temp = route.startAddress + " , " + route.endAddress;
            recentRoutesMenu.add(ROUTE_GROUP, index, 0, temp);
        }
    }

    private void insertRoute(RoutePoints route) {
        if (route != null) {
            ContentValues contentValues = new ContentValues();

            if (route.startingPoint != null) {
                contentValues.put(RouteProvider.startLatField, route.startingPoint.latitude);
                contentValues.put(RouteProvider.startLonField, route.startingPoint.longitude);
            }

            if (route.endingPoint != null) {
                contentValues.put(RouteProvider.endLatField, route.endingPoint.latitude);
                contentValues.put(RouteProvider.endLonField, route.endingPoint.longitude);
            }

            contentValues.put(RouteProvider.startAddressField, route.startAddress);
            contentValues.put(RouteProvider.endAddressField, route.endAddress);

            // Insert a new route record to the content provider
            Uri uri = getContentResolver().insert(RouteProvider.CONTENT_URI, contentValues);
            Log.d(LOG_TAG, "insertRoute() uri = " + uri.toString());
        }
    }

    private void deleteAll() {
        ContentResolver cr = getContentResolver();
        cr.delete(RouteProvider.CONTENT_URI, null, null);
    }
    // Loader
    //***********************************************************************

    //***********************************************************************
    // Settings
    @Override
    public void onClearCashedRoutes() {
        if (recentRoutesMenu != null) {
            recentRoutesMenu.clear();
        }

        deleteAll();
        mRouteManager.clear();
        getLoaderManager().restartLoader(ROUTE_LOADER_ID, null, this);
    }
    // Settings
    //***********************************************************************
}

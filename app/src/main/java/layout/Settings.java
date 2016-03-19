package layout;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.pathfinder.R;

import java.util.Map;
import java.util.Set;

/**
 * Created by kenm on 2/24/2016.
 */
public class Settings extends PreferenceActivity{
    private final String LOG_TAG = "Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(LOG_TAG, "onCreate");

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }


    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static final String LOG_TAG = "SettingsFragment";

        private static String mRouteTypeName = "";
        private static int mRouteTypeValue = 0;

        private static boolean  mAlternateRoutes = true;
        private static String [] mSelectedRoutePref = null;
        private static boolean  mClearCachedRoutes = true;
        private static String mSavedRoutes = "";
        private static String mRouteHighlight = "";
        private static String mZoomLevel = "";

        private static final String ZOOM_LEVEL_HOUSE_KEY = "House";
        private static final String ZOOM_LEVEL_STREET_KEY = "Street";
        private static final String ZOOM_LEVEL_NEIGHBOR_KEY = "Neighborhood";
        private static final String ZOOM_LEVEL_CITY_KEY = "City";
        private static final String ZOOM_LEVEL_REGION_KEY = "Region";

        private static Float ZOOM_LEVEL_HOUSE_VAL = 16.0f;
        private static Float ZOOM_LEVEL_STREET_VAL = 14.0f;
        private static Float ZOOM_LEVEL_NEIGHBOR_VAL = 12.0f;
        private static Float ZOOM_LEVEL_CITY_VAL = 10.0f;
        private static Float ZOOM_LEVEL_REGION_VAL = 8.0f;

        private static final String COLOR_BLACK_KEY = "Black";
        private static final String COLOR_BLUE_KEY = "Blue";
        private static final String COLOR_CYAN_KEY = "Cyan";
        private static final String COLOR_GRAY_KEY = "Gray";
        private static final String COLOR_MAGENTA_KEY = "Magenta";

        private static final String ROUTE_TYPE_BEST   = "Best Route";
        private static final String ROUTE_TYPE_FEWEST = "Fewest Transfers";
        private static final String ROUTE_TYPE_LESS   = "Less Walking";

        private static int ROUTE_TYPE_VALUE_BEST   = 0;
        private static int ROUTE_TYPE_VALUE_FEWEST = 1;
        private static int ROUTE_TYPE_VALUE_LESS   = 2;

        private static final String TRAVEL_PREF_BUS     = "Bus";
        private static final String TRAVEL_PREF_SUBWAY  = "Subway";
        private static final String TRAVEL_PREF_TRAIN   = "Train";
        private static final String TRAVEL_PREF_TRAM    = "Tram/Light rail";

        public static final int LEAVING_BTN_LEAVE_NOW = 0;
        public static final int LEAVING_BTN_LEAVE_AT  = 1;
        public static final int LEAVING_BTN_ARRIVE_AT = 2;

        public static class TravelPref {
            public static boolean bus = false;
            public static boolean subway = false;
            public static boolean train = false;
            public static boolean tram = false;
        }

        public interface OnSettingsChangedListener {
            void onClearCashedRoutes();
        }

        private static OnSettingsChangedListener mListener = null;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onPause() {
            super.onPause();

            Log.d(LOG_TAG, "onPause");

            // Unregister the listener whenever a key changes
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();

            Log.d(LOG_TAG, "onResume");

            // Set up a listener whenever a key changes
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.v(LOG_TAG, "onSharedPreferenceChanged key=" + key);

            switch (key) {
                case "routeType":
                    mRouteTypeName = sharedPreferences.getString(key, "");
                    Log.v(LOG_TAG, "onSharedPreferenceChanged value=" + mRouteTypeName);

                    switch (mRouteTypeName) {
                        case ROUTE_TYPE_BEST: { mRouteTypeValue = ROUTE_TYPE_VALUE_BEST;} break;
                        case ROUTE_TYPE_LESS: { mRouteTypeValue = ROUTE_TYPE_VALUE_LESS;} break;
                        case ROUTE_TYPE_FEWEST: { mRouteTypeValue = ROUTE_TYPE_VALUE_FEWEST;} break;
                    }
                    break;
                case "alternateRoutes":
                    mAlternateRoutes = sharedPreferences.getBoolean(key, true);
                    Log.v(LOG_TAG, "onSharedPreferenceChanged value=" + mAlternateRoutes);
                    break;
                case "routePreference":
                    {
                        Set<String> selections = sharedPreferences.getStringSet("routePreference", null);
                        mSelectedRoutePref = selections.toArray(new String[]{});
                        for (int i = 0; i < mSelectedRoutePref.length ; i++){
                            Log.d(LOG_TAG, "onSharedPreferenceChanged() routePreference" + i + " : " + mSelectedRoutePref[i]);
                        }
                    }
                    break;
                case "clearCachedRoutes":
                    mClearCachedRoutes = sharedPreferences.getBoolean(key, true);

                    if (mClearCachedRoutes && (mListener != null)) {
                        mListener.onClearCashedRoutes();
                    }

                    ClearCachedRoutesFlag();
                    Log.v(LOG_TAG, "onSharedPreferenceChanged value=" + mClearCachedRoutes);
                    break;
                case "savedRoutes":
                    mSavedRoutes = sharedPreferences.getString(key, "");
                    Log.v(LOG_TAG, "onSharedPreferenceChanged value=" + mSavedRoutes);
                    break;
                case "routeHighlight":
                    mRouteHighlight = sharedPreferences.getString(key, "");
                    Log.v(LOG_TAG, "onSharedPreferenceChanged value=" + mRouteHighlight);
                    break;
                case "zoomLevel":
                    mZoomLevel = sharedPreferences.getString(key, "");
                    Log.v(LOG_TAG, "onSharedPreferenceChanged value=" + mZoomLevel);
                    break;
            }
        }

        public static void readDefaults(Context context) {
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);

            mRouteTypeName = SP.getString("routeType", "");
            Log.d(LOG_TAG, "getDefaultValues() routeType=" + mRouteTypeName);

            mAlternateRoutes = SP.getBoolean("alternateRoutes", true);
            Log.d(LOG_TAG, "getDefaultValues() alternateRoutes=" + mAlternateRoutes);

            Set<String> selections = SP.getStringSet("routePreference", null);
            if (selections != null) {
                mSelectedRoutePref= selections.toArray(new String[]{});
                for (int i = 0; i < mSelectedRoutePref.length ; i++){
                    Log.d(LOG_TAG, "getDefaultValues() routePreference" + i + " : " + mSelectedRoutePref[i]);
                }
            }

            mClearCachedRoutes = SP.getBoolean("clearCachedRoutes",false);
            Log.d(LOG_TAG, "getDefaultValues() clearCachedRoutes=" + mClearCachedRoutes);

            mSavedRoutes = SP.getString("savedRoutes", "");
            Log.d(LOG_TAG, "getDefaultValues() savedRoutes=" + mSavedRoutes);

            mRouteHighlight = SP.getString("routeHighlight","");
            Log.d(LOG_TAG, "getDefaultValues() routeHighlight=" + mRouteHighlight);

            mZoomLevel = SP.getString("zoomLevel","");
            Log.d(LOG_TAG, "getDefaultValues() zoomLevel=" + mZoomLevel);
        }

        public static int getRouteType() {
            Log.d(LOG_TAG, "getRouteType() mRouteTypeValue=" + mRouteTypeValue);
            return mRouteTypeValue;
        }
        public static boolean getAlternateRoutes() {
            Log.d(LOG_TAG, "getAlternateRoutes() mAlternateRoutes=" + mAlternateRoutes);
            return mAlternateRoutes;
        }
        public static TravelPref getSelectedRoutePref() {
            TravelPref travelPref = new TravelPref();

            if (mSelectedRoutePref != null) {
                Log.d(LOG_TAG, "getSelectedRoutePref() mSelectedRoutePref=" + mSelectedRoutePref.toString());

                for (int i = 0; i < mSelectedRoutePref.length ; i++){
                    if (mSelectedRoutePref[i].equalsIgnoreCase(TRAVEL_PREF_BUS)) {
                        travelPref.bus = true;
                    }
                    if (mSelectedRoutePref[i].equalsIgnoreCase(TRAVEL_PREF_SUBWAY)) {
                        travelPref.subway = true;
                    }
                    if (mSelectedRoutePref[i].equalsIgnoreCase(TRAVEL_PREF_TRAIN)) {
                        travelPref.train = true;
                    }
                    if (mSelectedRoutePref[i].equalsIgnoreCase(TRAVEL_PREF_TRAM)) {
                        travelPref.tram = true;
                    }
                }
            }

            return travelPref;
        }

        public static boolean getClearCachedRoutes() {
            Log.d(LOG_TAG, "getClearCachedRoutes() mClearCachedRoutes=" + mClearCachedRoutes);
            return mClearCachedRoutes;
        }

        public static int getSavedRoutes() {
            int ret = 0;
            try {
                ret = Integer.parseInt(mSavedRoutes);
            }
            catch (Exception e) {
                Log.d(LOG_TAG, "getSavedRoutes() parse exception=" + e.toString());
            }

            Log.d(LOG_TAG, "getSavedRoutes() mSavedRoutes=" + ret);

            return ret;
        }

        public static int getRouteHighlight() {
            int ret = Color.MAGENTA;

            switch (mRouteHighlight) {
                case COLOR_BLACK_KEY:
                    ret = Color.BLACK;
                    break;
                case COLOR_BLUE_KEY:
                    ret = Color.BLUE;
                    break;
                case COLOR_CYAN_KEY:
                    ret = Color.CYAN;
                    break;
                case COLOR_GRAY_KEY:
                    ret = Color.GRAY;
                    break;
                case COLOR_MAGENTA_KEY:
                    ret = Color.MAGENTA;
                    break;
            }

            Log.d(LOG_TAG, "getRouteHighlight() mRouteHighlight=" + mRouteHighlight);

            return ret;
        }

        public static Float getZoomLevel() {
            Float ret = 0.0f;

            switch (mZoomLevel) {
                case ZOOM_LEVEL_HOUSE_KEY:
                    ret = ZOOM_LEVEL_HOUSE_VAL;
                    break;
                case ZOOM_LEVEL_STREET_KEY:
                    ret = ZOOM_LEVEL_STREET_VAL;
                    break;
                case ZOOM_LEVEL_NEIGHBOR_KEY:
                    ret = ZOOM_LEVEL_NEIGHBOR_VAL;
                    break;
                case ZOOM_LEVEL_CITY_KEY:
                    ret = ZOOM_LEVEL_CITY_VAL;
                    break;
                case ZOOM_LEVEL_REGION_KEY:
                    ret = ZOOM_LEVEL_REGION_VAL;
                    break;
                default:
                    ret = ZOOM_LEVEL_NEIGHBOR_VAL;
                    break;
            }

            Log.d(LOG_TAG, "getZoomLevel() mZoomLevel=" + ret);

            return ret;
        }

        public void ClearCachedRoutesFlag() {
            mClearCachedRoutes = false;

            SharedPreferences settings = getPreferenceScreen().getSharedPreferences();
            SharedPreferences.Editor editor = settings.edit();

            editor.putBoolean("clearCachedRoutes", false);
            editor.apply();
        }

        public static void SetupListener(Context context) {

            Log.v(LOG_TAG, "onAttach()");

            if (context instanceof OnSettingsChangedListener) {
                mListener = (OnSettingsChangedListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnSettingsChangedListener");
            }
        }

    }
}

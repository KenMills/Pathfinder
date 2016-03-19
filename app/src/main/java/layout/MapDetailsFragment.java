package layout;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.android.pathfinder.MapDetailsAdapter;
import com.example.android.pathfinder.MapDetailsItem;
import com.example.android.pathfinder.R;
import com.example.android.pathfinder.Route;
import com.example.android.pathfinder.RouteDetailsManager;

import java.util.ArrayList;
import java.util.List;

public class MapDetailsFragment extends Fragment {
    private static String LOG_TAG = "MapDetailsFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnLaunchRouteDetailsListener mListener;

    private RecyclerView mRecyclerView;
    private MapDetailsAdapter mAdapter;

    private static String mFare;
    private static String mWalking;

    public MapDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapDetailsFragment newInstance(String param1, String param2) {
        MapDetailsFragment fragment = new MapDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map_details, container, false);
        mFare = getResources().getString(R.string.map_details_fare);
        mWalking = getResources().getString(R.string.map_details_walkingtime);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.map_details_recyclerview);
        mAdapter = new MapDetailsAdapter(getActivity(), getData(), mListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    public static List<MapDetailsItem> getData() {
        List<MapDetailsItem> details = new ArrayList<MapDetailsItem>();

        RouteDetailsManager detailsManager = RouteDetailsManager.getInstance();

        for (int i=0; i<detailsManager.size(); i++) {
            MapDetailsItem detailItem = new MapDetailsItem();
            Route route = detailsManager.GetRoute(i);

            detailItem.cost = mFare + route.fareText;
            detailItem.duration = route.legs[0].durationText;
            detailItem.trip = route.legs[0].departureTime + " - " + route.legs[0].arrivalTime;

            float walkingTime = CalculateWalkingTime(route);
            detailItem.walkingTime = mWalking + String.format("%.2f", walkingTime) + " min";

            details.add(detailItem);
        }

        return details;
    }

    private static float CalculateWalkingTime(Route route) {
        float walkingTime = 0;

        for (int legs=0; legs<route.legs.length; legs++) {
            for (int steps=0; steps<route.legs[legs].steps.length; steps++) {
                String travelMode = route.legs[legs].steps[steps].travelMode;
                if (travelMode.equalsIgnoreCase(Route.KEY_WALKING)) {
                    walkingTime += route.legs[legs].steps[steps].durationValue;
                }
            }
        }

        return walkingTime/60;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLaunchRouteDetailsListener) {
            mListener = (OnLaunchRouteDetailsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnLaunchRouteDetailsListener {
        void onUpdatePolyline(int position);
        void onLaunchRouteDetails(int position);
    }
}

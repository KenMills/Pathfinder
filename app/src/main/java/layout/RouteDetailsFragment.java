package layout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.android.pathfinder.MapDetailsAdapter;
import com.example.android.pathfinder.MapDetailsItem;
import com.example.android.pathfinder.R;
import com.example.android.pathfinder.Route;
import com.example.android.pathfinder.RouteDetailsAdapter;
import com.example.android.pathfinder.RouteDetailsItem;
import com.example.android.pathfinder.RouteDetailsManager;
import com.example.android.pathfinder.RouteManager;

import java.util.ArrayList;
import java.util.List;


public class RouteDetailsFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PARAM1 = "param1";

    private int mParam1;

    private OnRouteDetailsListener mListener;
    FloatingActionButton mFab;

    private RecyclerView mRecyclerView;
    private RouteDetailsAdapter mAdapter;
    private static int mRouteNumber;

    private static String FORMAT_BREAK = "<br>";
    private static String mStops;
    private static String mDepart;
    private static String mArrive;

    private List<RouteDetailsItem> mRouteData;

    public RouteDetailsFragment() {
        // Required empty public constructor
    }

    public static RouteDetailsFragment newInstance(int param1, String param2) {
        RouteDetailsFragment fragment = new RouteDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRouteNumber = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_route_details, container, false);

        mStops = getResources().getString(R.string.route_details_stops);
        mDepart = getResources().getString(R.string.route_detail_depart);
        mArrive = getResources().getString(R.string.route_detail_arrive);

        mRouteData = getData();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.route_details_recyclerview);
        mAdapter = new RouteDetailsAdapter(getActivity(), mRouteData, mListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mFab = (FloatingActionButton) view.findViewById(R.id.fab_share);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchShare();
            }
        });

        return view;
    }

    private void LaunchShare() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        RouteDetailsManager detailsManager = RouteDetailsManager.getInstance();
        Route route = detailsManager.GetRoute(mRouteNumber);

        // not sure how much to share...
        // all of the details are saved in mRouteData and I could iterate through it
        // but that could be a lot of info...
        String shareBody;
        shareBody = getResources().getString(R.string.share_from) + route.legs[0].startAddress;
        shareBody += "\n";
        shareBody += getResources().getString(R.string.share_to) + route.legs[0].endAddress;
        shareBody += "\n";
        shareBody += getResources().getString(R.string.share_depart) + route.legs[0].departureTime;
        shareBody += "\n";
        shareBody += getResources().getString(R.string.share_arrive) + route.legs[0].arrivalTime;
        shareBody += "\n";
        shareBody += "\n";
        shareBody += "\n";

        int steps = route.legs[0].steps.length;
        for (int i=0; i<steps; i++) {
            shareBody += route.legs[0].steps[i].htmlInstructions;
            shareBody += "\n";
            shareBody += "\n";
        }

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_route));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_share_via)));
    }

    public static List<RouteDetailsItem> getData() {
        List<RouteDetailsItem> details = new ArrayList<RouteDetailsItem>();

        RouteDetailsManager detailsManager = RouteDetailsManager.getInstance();
        Route route = detailsManager.GetRoute(mRouteNumber);
        int steps = route.legs[0].steps.length;

        for (int i=0; i<steps; i++) {
            RouteDetailsItem detailItem = new RouteDetailsItem();

            detailItem.instructions = route.legs[0].steps[i].htmlInstructions;
            detailItem.distance = route.legs[0].steps[i].distanceText;
            detailItem.duration = route.legs[0].steps[i].durationText;

            String travelMode = route.legs[0].steps[i].travelMode;
            String temp = FORMAT_BREAK;
            if (travelMode.equalsIgnoreCase(Route.KEY_WALKING)) {
                for (int j=0; j<route.legs[0].steps[i].walkingSteps.length; j++) {
                    temp += route.legs[0].steps[i].walkingSteps[j].htmlInstructions;
                    temp += FORMAT_BREAK;
                    temp += route.legs[0].steps[i].walkingSteps[j].distanceText;
                    temp += "    ";
                    temp += route.legs[0].steps[i].walkingSteps[j].durationText;
                    temp += FORMAT_BREAK;
                    temp += FORMAT_BREAK;
                }

            }
            else if (travelMode.equalsIgnoreCase(Route.KEY_TRANSIT)) {
                temp += route.legs[0].steps[i].htmlInstructions;
                temp += FORMAT_BREAK;

                temp += route.legs[0].steps[i].transit.vehicleType + " ";
                temp += route.legs[0].steps[i].transit.vehicleName + mStops;
                temp += route.legs[0].steps[i].transit.numStops;
                temp += FORMAT_BREAK;
                temp += FORMAT_BREAK;

                temp += mDepart;
                temp += route.legs[0].steps[i].transit.departureStop;
                temp += "    ";
                temp += route.legs[0].steps[i].transit.departureTime;
                temp += FORMAT_BREAK;

                temp += mArrive;
                temp += route.legs[0].steps[i].transit.arrivalStop;
                temp += "    ";
                temp += route.legs[0].steps[i].transit.arrivalTime;
                temp += FORMAT_BREAK;
                temp += FORMAT_BREAK;
            }

            detailItem.travelInfo = temp;
            details.add(detailItem);
        }

        return details;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRouteDetailsListener) {
            mListener = (OnRouteDetailsListener) context;
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

    public interface OnRouteDetailsListener {
        void onRouteDetails(int position);
    }
}

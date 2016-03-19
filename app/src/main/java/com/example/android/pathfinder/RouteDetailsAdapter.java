package com.example.android.pathfinder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import layout.RouteDetailsFragment;

/**
 * Created by kenm on 3/9/2016.
 */
public class RouteDetailsAdapter extends RecyclerView.Adapter<RouteDetailsAdapter.RouteDetailsViewHolder> {
    private final String LOG_TAG = "RouteDetailsAdapter";

    private LayoutInflater inflater;
    List<RouteDetailsItem> details = Collections.emptyList();
    RouteDetailsFragment.OnRouteDetailsListener mListener;

    public RouteDetailsAdapter(Context context, List<RouteDetailsItem> details, RouteDetailsFragment.OnRouteDetailsListener listener) {
        inflater = LayoutInflater.from(context);
        this.details = details;
        this.mListener = listener;
    }

    @Override
    public RouteDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.route_details_item, parent, false);

        RouteDetailsViewHolder routeDetailsViewHolder = new RouteDetailsViewHolder(view);

        return routeDetailsViewHolder;
    }

    @Override
    public void onBindViewHolder(RouteDetailsViewHolder holder, int position) {
        if (position < details.size()) {
            RouteDetailsItem current = details.get(position);
            holder.instructions.setText(current.instructions);
            holder.distance.setText(current.distance);
            holder.duration.setText(current.duration);

            Spanned result = Html.fromHtml(current.travelInfo);
            holder.travelinfo.setText(result);
            holder.travelinfo.setMaxLines(0);
            holder.travelinfo.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    class RouteDetailsViewHolder extends RecyclerView.ViewHolder{
        TextView instructions, distance, duration, travelinfo;
        public View view;

        public RouteDetailsViewHolder(View itemView) {
            super(itemView);

            instructions = (TextView) itemView.findViewById(R.id.route_instructions_textview);
            distance = (TextView) itemView.findViewById(R.id.distance_textview);
            duration = (TextView) itemView.findViewById(R.id.duration_textview);
            travelinfo = (TextView) itemView.findViewById(R.id.travel_instructions_textview);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {

                    // item clicked
                    TextView travelinfo = (TextView)v.findViewById(R.id.travel_instructions_textview);
                    if (travelinfo.getVisibility() == View.VISIBLE) {
                        travelinfo.setVisibility(View.INVISIBLE);
                        travelinfo.setMaxLines(0);
                    }
                    else {
                        travelinfo.setVisibility(View.VISIBLE);
                        travelinfo.setMaxLines(100);
                    }
                }
            });
        }
    }
}


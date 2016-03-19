package com.example.android.pathfinder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import layout.MapDetailsFragment;

/**
 * Created by kenm on 3/7/2016.
 */
public class MapDetailsAdapter extends RecyclerView.Adapter<MapDetailsAdapter.MapDetailsViewHolder> {
    private LayoutInflater inflater;
    List<MapDetailsItem> details = Collections.emptyList();
    MapDetailsFragment.OnLaunchRouteDetailsListener mListener;

    public MapDetailsAdapter(Context context, List<MapDetailsItem> details, MapDetailsFragment.OnLaunchRouteDetailsListener listener) {
        inflater = LayoutInflater.from(context);
        this.details = details;
        this.mListener = listener;
    }

    @Override
    public MapDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.map_details_item, parent, false);

        MapDetailsViewHolder mapDetailsViewHolder = new MapDetailsViewHolder(view);

        return mapDetailsViewHolder;
    }

    @Override
    public void onBindViewHolder(MapDetailsViewHolder holder, int position) {
        if (position < details.size()) {
            MapDetailsItem current = details.get(position);
            holder.trip.setText(current.trip);
            holder.duration.setText(current.duration);
            holder.cost.setText(current.cost);
            holder.walkingTime.setText(current.walkingTime);
        }
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    class MapDetailsViewHolder extends RecyclerView.ViewHolder{
        private final String LOG_TAG = "MapDetailsViewHolder";
        TextView trip, duration, cost, walkingTime;

        public MapDetailsViewHolder(View itemView) {
            super(itemView);

            trip = (TextView) itemView.findViewById(R.id.trip_textview);
            duration = (TextView) itemView.findViewById(R.id.trip_duration_textview);
            cost = (TextView) itemView.findViewById(R.id.trip_cost_textview);
            walkingTime = (TextView) itemView.findViewById(R.id.walking_time_textview);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {

                    if (mListener != null) {
                        mListener.onUpdatePolyline(getAdapterPosition());
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener != null) {
                        mListener.onUpdatePolyline(getAdapterPosition());
                        mListener.onLaunchRouteDetails(getAdapterPosition());
                    }

                    return false;
                }
            });
        }
    }
}

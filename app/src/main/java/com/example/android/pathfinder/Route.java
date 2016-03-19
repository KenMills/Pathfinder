package com.example.android.pathfinder;

/**
 * Created by kenm on 3/8/2016.
 */
public class Route {
    public String fareText;
    public String polyLine;
    public Leg[] legs;

    public static String KEY_WALKING = "WALKING";
    public static String KEY_TRANSIT = "TRANSIT";

    public static class Leg {
        public String arrivalTime;
        public String departureTime;
        public String distanceText;
        public int distanceValue;
        public String durationText;
        public int durationValue;
        public String startAddress;
        public String endAddress;
        public Step[] steps;
    }

    public static class Step {
        public String distanceText;
        public int distanceValue;
        public String durationText;
        public int durationValue;
        public String travelMode;
        public String htmlInstructions;

        public WalkingSteps[] walkingSteps;
        public Transit transit;
    }

    public static class WalkingSteps {
        public String distanceText;
        public int distanceValue;
        public String durationText;
        public int durationValue;
        public String travelMode;
        public String maneuver;
        public String htmlInstructions;
    }

    public static class Transit {
        public String arrivalStop;
        public String arrivalTime;
        public String departureStop;
        public String departureTime;
        public String vehicleName;
        public String vehicleType;
        public int numStops;
    }
}

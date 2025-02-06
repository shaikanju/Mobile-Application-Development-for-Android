package com.anju.ctabustracker;

public class Prediction {

    private String vehicleId;

    private String routeDir;
    private String destination;
    private String predictedTime;
    private String countdown;
    private boolean delayed;

    public Prediction( String vehicleId, String routeDir, String destination, String predictedTime, String countdown, boolean delayed) {

        this.vehicleId = vehicleId;

        this.routeDir = routeDir;
        this.destination = destination;
        this.predictedTime = predictedTime;
        this.countdown = countdown;
        this.delayed = delayed;
    }

    // Getters

    public String getVehicleId() { return vehicleId; }

    public String getRouteDir() { return routeDir; }
    public String getDestination() { return destination; }
    public String getPredictedTime() { return predictedTime; }
    public String getCountdown() { return countdown; }
    public boolean isDelayed() { return delayed; }
}

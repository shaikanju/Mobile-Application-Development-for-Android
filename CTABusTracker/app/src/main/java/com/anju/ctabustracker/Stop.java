package com.anju.ctabustracker;

public class Stop {
    private String stopId;
    private String stopName;
    private double latitude;
    private double longitude;
    private double distance;

private String directionfrom;

    public Stop(String stopId, String stopName, double latitude, double longitude,double distance,String directionfrom) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.directionfrom= directionfrom;

    }

    public String getStopId() {
        return stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public double getDistance() { // Getter for distance
        return distance;
    }
    public String getDirectionfrom(){
        return directionfrom;
    }

}


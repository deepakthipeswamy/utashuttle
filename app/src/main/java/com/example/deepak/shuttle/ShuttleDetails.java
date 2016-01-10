package com.example.deepak.shuttle;

/**
 * Created by Deepak on 1/1/2016.
 */
public class ShuttleDetails {

    private String color;
    private String busStopName;
    private int order;
    private double longitude;
    private double latitude;
    private int timeRequiredFromPrevStop;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public ShuttleDetails(String name, String color, int order, double longitude, double latitude, int timeRequiredFromPrevStop) {
        this.busStopName = name;
        this.color = color;
        this.latitude = latitude;
        this.longitude = longitude;
        this.order = order;
        this.timeRequiredFromPrevStop = timeRequiredFromPrevStop;
    }

    public String getBusStopName() {
        return busStopName;
    }

    public void setBusStopName(String busStopName) {
        this.busStopName = busStopName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    public int getTimeRequiredFromPrevStop() {
        return timeRequiredFromPrevStop;
    }

    public void setTimeRequiredFromPrevStop(int timeRequiredFromPrevStop) {
        this.timeRequiredFromPrevStop = timeRequiredFromPrevStop;
    }
}

package com.example.deepak.shuttle;

/**
 * Created by Deepak on 1/5/2016.
 */
public class ShuttleConstants {
    private double takeAWalkDistance;

    public ShuttleConstants(double takeAWalkDistance){
        this.takeAWalkDistance=takeAWalkDistance;
    }


    public double getTakeAWalkDistance() {
        return takeAWalkDistance;
    }

    public void setTakeAWalkDistance(double takeAWalkDistance) {
        this.takeAWalkDistance = takeAWalkDistance;
    }
}

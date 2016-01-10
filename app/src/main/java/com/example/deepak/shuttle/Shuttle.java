package com.example.deepak.shuttle;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by Deepak on 1/1/2016.
 */
public class Shuttle extends Activity {

    private String busStopString;
    private Map<String, ShuttleDetails> mainMap = new HashMap<String, ShuttleDetails>();
    private ShuttleConstants shuttleConstants;

    public Shuttle(String myjsonstring){
        this.busStopString = myjsonstring;
        shuttleConstants = new ShuttleConstants(0.3);
        build();
    }

    public Shuttle() {
        super();
    }

    public Map<String, ShuttleDetails> getMainMap() {
        return mainMap;
    }

    public void setMainMap(Map<String, ShuttleDetails> mainMap) {
        this.mainMap = mainMap;
    }

    private void build() {

        try {
            // Creating JSONObject from String
            JSONObject jsonObjMain = new JSONObject(busStopString);

            // Creating JSONArray from JSONObject
            JSONArray shuttlesArray = jsonObjMain.getJSONArray("shuttles");
            // JSONArray has x JSONObject
            for (int i = 0; i < shuttlesArray.length(); i++) {
                // Creating JSONObject from JSONArray
                JSONObject jsonObj = shuttlesArray.getJSONObject(i);
                // Getting data from individual JSONObject
                String name = jsonObj.getString("name");
                int order = jsonObj.getInt("order");
                double longitude = jsonObj.getDouble("longitude");
                double latitude = jsonObj.getDouble("latitude");
                String color = jsonObj.getString("color");
                int timeRequiredFromPrevStop = jsonObj.getInt("time");

                ShuttleDetails busDetails = new ShuttleDetails(name, color, order, longitude, latitude, timeRequiredFromPrevStop);
                mainMap.put(name, busDetails);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Double getDistance(String busStopName, double lati, double longi){
        ShuttleDetails shuttleDetails = mainMap.get(busStopName);
        return dist(shuttleDetails.getLatitude(), shuttleDetails.getLongitude(), lati, longi);
    }

    public Double getDistanceByBusStopNames(String fromBusStopName, String toBusStopName){
        ShuttleDetails fromShuttleDetails = mainMap.get(fromBusStopName);
        ShuttleDetails toShuttleDetails = mainMap.get(toBusStopName);
        return dist(fromShuttleDetails.getLatitude(), fromShuttleDetails.getLongitude(), toShuttleDetails.getLatitude(), toShuttleDetails.getLongitude());
    }

    public double dist(double latitude1, double longitude1, double latitude2, double longitude2)
    {
        int R = 6371; // km
        double dLat = toRad(latitude2-latitude1);
        double dLon = toRad(longitude2-longitude1);
        double lat1 = toRad(latitude1);
        double lat2 = toRad(latitude2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d;
    }

    // Converts numeric degrees to radians
    public double toRad(double Value)
    {
        return Value * Math.PI / 180;
    }

    public String[] getClosestStopFromMyLocation(double myLati, double myLongi){

        String closestBlueStop = null;
        String closestGreenStop = null;
        String[] closestStopsArray = new String[4];

        double blueDistance = 9999.0;
        double greenDistance = 9999.0;

        double tempDistance = 0.0;

        for(String stop: mainMap.keySet()){
            tempDistance = getDistance(mainMap.get(stop).getBusStopName(), myLati, myLongi);
            if("blue".equalsIgnoreCase(mainMap.get(stop).getColor())){
                if(blueDistance > tempDistance){
                    blueDistance = tempDistance;
                    closestBlueStop = mainMap.get(stop).getBusStopName();
                }
            } else {
                if (greenDistance > tempDistance) {
                    greenDistance = tempDistance;
                    closestGreenStop = mainMap.get(stop).getBusStopName();
                }
            }
        }

        closestStopsArray[0] = closestBlueStop;
        closestStopsArray[1] = Double.toString(blueDistance);
        closestStopsArray[2] = closestGreenStop;
        closestStopsArray[3] = Double.toString(greenDistance);;

        return closestStopsArray;
    }


    public int[] calculateTime(String[] closestStopArray,String target){

        int time1=0;
        int time2=0;
        int result[]=new int[2];
        Double currentgreenStopDistance=(Double.parseDouble((closestStopArray[3])));
        Double currentBlueStopDistance=(Double.parseDouble((closestStopArray[1])));

        time1+=(int)getWalkingTimefromCurrentToBusStop(currentgreenStopDistance);
        time1+=(int)getWalkingTimefromCurrentToBusStop(currentBlueStopDistance);


        // change all naming conventions, give meaningful variable names
        // consider 24hr format and take hr into consideration
        // add the walking time to current time = estimated reaching time to the bus stop
        // waiting time = bus time - estimated time

       // int minutes=Calendar.getInstance().get(Calendar.MINUTE);  //current minute
        time1+=calculateWaitingTimeAtBusStop(15,time1);
        time2+=calculateWaitingTimeAtBusStop(20,time2);
        int temp_green=0;
        int temp_blue=0;

        String[] greenBusDetails=new String[10];
        String[] blueBusDetails=new String[11];

        boolean isDestiInGreen=false;
        boolean isDestiInBlue=false;
        int getGreenClosestBusStopOrder=mainMap.get(closestStopArray[2]).getOrder();
        int getBlueClosestBusStopOrder=mainMap.get(closestStopArray[0]).getOrder();
        int getGreenTargetOrder=mainMap.get(target).getOrder();
        int getBlueTargetOrder=mainMap.get(target).getOrder();

        for(Map.Entry<String,ShuttleDetails> entry:mainMap.entrySet()) {
            ShuttleDetails d=entry.getValue();

            if (d.getColor().equals("green")) {
                List b=splitHashMap(d,"green",target,closestStopArray[2]);
                int temp=(int) b.get(1);
                if(temp==1){
                    isDestiInGreen=true;
                }


                greenBusDetails[(int) b.get(0)]=d.getBusStopName();
               // if((int)b.get(3)!=0)
               // getGreenTargetOrder=(int)b.get(3);
                }
            else if (d.getColor().equals("blue")) {
                List b=splitHashMap(d,"blue",target,closestStopArray[0]);
                int temp=(int) b.get(1);
                if(temp==1){
                    isDestiInBlue=true;
                }
                blueBusDetails[(int) b.get(0)]=d.getBusStopName();
               // if((int)b.get(3)!=0)
                //getBlueTargetOrder=(int)b.get(3);

            }


        }
        int temp1=0;
        time1+=calculateTravelTimeInBus(getGreenClosestBusStopOrder,getGreenTargetOrder,target,greenBusDetails,blueBusDetails,isDestiInGreen,"green");
        time2+=calculateTravelTimeInBus(getBlueClosestBusStopOrder,getBlueTargetOrder,target,greenBusDetails,blueBusDetails,isDestiInBlue,"blue");

 //int[] reuslts={time1,time2};

        result[0]=time1;
        result[1]=time2;
        return result;

    }
    private int calculateTravelTimeInBus(int sourceBusStopOrder,int targetBusStopOrder,String target, String[] greenBusStops,String[] blueBusStops,boolean isDestinationColorSameAsBusStopColor,String color) {
        int time=0;
        String[] busStops=color.equalsIgnoreCase("green")?greenBusStops:blueBusStops;
        double latitude=mainMap.get(target).getLatitude();
        double longitude=mainMap.get(target).getLongitude();
        if (getDistance(busStops[sourceBusStopOrder],latitude,longitude)<= shuttleConstants.getTakeAWalkDistance()) {

            return 0;
        }else {
            if (isDestinationColorSameAsBusStopColor) {

                if ((targetBusStopOrder - sourceBusStopOrder) > 0)   // on the way to destination
                {
                    // use the distance between bus stops for this calc
                    //use ShuttleConstants.getTakeAWalkDistance();

                    //int time=0;
                    for (int i = sourceBusStopOrder + 1; i <= targetBusStopOrder; i++) {

                        time += mainMap.get(busStops[i]).getTimeRequiredFromPrevStop();

                    }
                    //return time;
                } else {
                    int y = sourceBusStopOrder;
                    //int time = 0;
                    while (true) {
                        y = y + 1;
                        if (y > busStops.length - 1)
                            y = (y - (busStops.length - 1));
                        if (busStops[y].equalsIgnoreCase(target)) {
                            time += mainMap.get(busStops[y]).getTimeRequiredFromPrevStop();
                            break;
                        } else {
                            time += mainMap.get(busStops[y]).getTimeRequiredFromPrevStop();
                        }
                    }
                    //return time;
                }
                //return time;
            } else {

                if (color.equalsIgnoreCase("green")) {
                    time += calculateTravelTimeInBus(sourceBusStopOrder, mainMap.get("SMART HOSPITAL").getOrder(),"SMART HOSPITAL", greenBusStops, blueBusStops, true, "green");
                    time += 120;
                    time += calculateTravelTimeInBus(mainMap.get("MAC").getOrder(), targetBusStopOrder,target, greenBusStops, blueBusStops, true, "blue");
                } else {
                    time += calculateTravelTimeInBus(sourceBusStopOrder, mainMap.get("MAC").getOrder(),"MAC", greenBusStops, blueBusStops, true, "blue");
                    time += 120;
                    time += calculateTravelTimeInBus(mainMap.get("SMART HOSPITAL").getOrder(), targetBusStopOrder,target, greenBusStops, blueBusStops, true, "green");

                }
            }
        }
        return time;
    }
    public double getWalkingTimefromCurrentToBusStop(double closestStopArray){
     return (closestStopArray/0.1)*300;
    }
    public List splitHashMap(ShuttleDetails d,String busStopColor,String target,String closestBusStop){

        List a=new ArrayList();
        //if (d.getColor().equalsIgnoreCase(busStopColor)) {
            //green_bus[d.getOrder()]=d;
            //greenBusDetails[d.getOrder()]=d.getBusStopName();
            a.add(d.getOrder());

            if(d.getBusStopName().equalsIgnoreCase(target)){
                a.add(1);
                a.add(d.getOrder());
            }
            else{
                a.add(0);
                a.add(0);
            }

            if(d.getBusStopName().equalsIgnoreCase(closestBusStop)) {
                a.add(d.getOrder());
            }else{
                a.add(0);
            }

            //green.put(entry.getValue().getBusStopName(), entry.getValue().getTime());
        //}
        return a;
    }
    public int calculateWaitingTimeAtBusStop(int busIntervalTime,int walkingTime){
        int currentMinute=Calendar.getInstance().get(Calendar.MINUTE);
        currentMinute=currentMinute%busIntervalTime;
        busIntervalTime*=60;  //convert to seconds
        int MinutesInSeconds=(currentMinute*60)+walkingTime;
        if(MinutesInSeconds>=0 && MinutesInSeconds<busIntervalTime){
            if(MinutesInSeconds==0)
                return 0;                // the bus arrives exactly when you reach
            return busIntervalTime-MinutesInSeconds;
        }
        if(MinutesInSeconds>=busIntervalTime && MinutesInSeconds<(2*busIntervalTime)){
            if(MinutesInSeconds==busIntervalTime)
                return 0;   //the bus arrives exactly when you reach.
            return (2*busIntervalTime)-MinutesInSeconds;
        }
        // test this scenario
       /* if(minutes>=(2*busIntervalTime) && minutes<(3*busIntervalTime)){
            return (3*busIntervalTime)-minutes;
        }
        // test the code and remove if not necessary
        if(busIntervalTime==15 &&(minutes>=(3*busIntervalTime) && minutes<(4*busIntervalTime))){
            return (4*busIntervalTime)-minutes;
        }*/
        return 0;
    }
    public boolean isDestinationInSameColor(ShuttleDetails d,String target){
        if(d.getBusStopName().equalsIgnoreCase(target)){
            return true;
        }
        return false;
    }
    public int getClosestBusStopOrder(ShuttleDetails d, String closestBusStop){

        return d.getOrder();
    }
   /* public String splitMinMap(ShuttleDetails d,String[] busStop,String order,String target,String closestBusStop) {
        if (order.equals("green")) {
            busStop[d.getOrder()] = d.getBusStopName();
            //busStop[d.getOrder()]=d.getBusStopName();



            if (d.getBusStopName().equalsIgnoreCase(closestBusStop))
                get_green_closest_bus_stop_order = d.getOrder();


        }
    }*/
}


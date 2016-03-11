/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservice;

import com.firebase.client.Firebase;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Bart
 */
public class DirectionCalculaterThread extends Thread {

    private String userId;
    private double currentLat=0, currentLon=0;
    private boolean listening = true;
    private Map locationsMap;
    
    public DirectionCalculaterThread(String userid, Map locations){
        userId=userid;
        locationsMap = locations;
    }
    
    public double calculateAngle(double lat, double lon){
        double bearing = 0;
        if(currentLat!=0 && currentLon!=0){
            double startLat = Math.toRadians(currentLat);
            double startLong = Math.toRadians(currentLon);
            double endLat = Math.toRadians(lat);
            double endLong = Math.toRadians(lon);

            double dLong = endLong - startLong;

            double dPhi = Math.log(Math.tan(endLat/2.0+Math.PI/4.0)/Math.tan(startLat/2.0+Math.PI/4.0));
            if (Math.abs(dLong) > Math.PI)
                if(dLong > 0.0)
                    dLong = -(2.0 * Math.PI - dLong);
            else
                dLong = (2.0 * Math.PI + dLong);

            bearing = (Math.toDegrees(Math.atan2(dLong, dPhi)) + 360.0) % 360.0;            
        }
        currentLat = lat;
        currentLon = lon;
        return bearing;
    }
    
    @Override
    public void run() {
        Firebase firebase = new Firebase("https://locationdatabase.firebaseio.com/locations/"+userId);
        Iterator it = locationsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Map data = (Map) locationsMap.get(pair.getKey());
            String activity = (String) data.get("status");
            if((activity==null) || (!activity.equals("Still") && !activity.equals("Sleeping"))){
                // Get recorded latitude and longitude
                Map mCoordinate = (HashMap) data.get("location");
                double currentLatitude = (double) (mCoordinate.get("latitude"));
                double currentLongitude = (double) (mCoordinate.get("longitude"));                   
                double angle = calculateAngle(currentLatitude, currentLongitude);
                data.put("angle", angle);
            }
        }
        firebase.updateChildren(locationsMap);
    }
    
}

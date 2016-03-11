/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservice;

import com.firebase.client.Firebase;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Bart
 */
public class UpdateGeofenceThread extends Thread{

    private String userId;
    Map locationsMap, geofencesMap;
    
    public UpdateGeofenceThread(String userId, Map locationsMap, Map geofencesMap){
        this.userId = userId;
        this.geofencesMap=geofencesMap;
        this.locationsMap=locationsMap;
    }
    
    
    
    
    @Override
    public void run() {        
        Firebase locations = new Firebase("https://locationdatabase.firebaseio.com/locations/"+userId);
        Iterator it = locationsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Map data = (Map) locationsMap.get(pair.getKey());
            Map loc = (Map) data.get("location");
            double latitude = (double) loc.get("latitude");
            double longitude = (double) loc.get("longitude");
            Iterator itGeofence = geofencesMap.entrySet().iterator();
            while (itGeofence.hasNext()) {
                Map.Entry pairGeofence = (Map.Entry)itGeofence.next();
                Map geofence = (Map) geofencesMap.get(pairGeofence.getKey());
                Map locGeofence = (Map) geofence.get("location");
                double latitudeGeofence = (double) locGeofence.get("latitude");
                double longitudeGeofence = (double) locGeofence.get("longitude");
                double radius = (double) geofence.get("radius");
                String place = (String) geofence.get("id");
                if(GPSDistance.distFrom(latitude, longitude, latitudeGeofence, longitudeGeofence)<=radius){
                    data.put("place", place);
                }
            }
        }
        locations.updateChildren(locationsMap);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bart
 */
public class ImportantPlacesThread implements Callable {

   private List<double[]> points = null;
   private final Map locationMap,geofencesMap, notImportantGeofences;
   
   public ImportantPlacesThread(Map locationMap, Map geofencesMap, Map notImportantGeofences){
       this.notImportantGeofences = notImportantGeofences;
       this.geofencesMap= geofencesMap;
       this.locationMap = locationMap;
   }
   
   
   public void checkIfGeofenceExists(double lat, double lon, double radius){  
       Iterator<double[]> iterator = points.iterator();
        while (iterator.hasNext()) {
            double[] p= iterator.next();
            double distance = GPSDistance.distFrom(p[0],p[1],lat,lon);
            if (distance < radius) {
                iterator.remove();
            }
        }
   }

    @Override
    public Object call() throws InterruptedException {
        ImportantPlacesWorker worker = new ImportantPlacesWorker(this, locationMap);
        points = worker.getImportantPlaces();        
        ArrayList<Map> geofenceData = new ArrayList<>();
        Iterator it = geofencesMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Map data =  (Map) geofencesMap.get(pair.getKey());              
            geofenceData.add(data);            
        }
        it = notImportantGeofences.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Map data =  (Map) notImportantGeofences.get(pair.getKey());              
            geofenceData.add(data);            
        }
        for(Map data : geofenceData ){
            double radius;
            try{
                radius =(double) data.get("radius");
            }
            catch(Exception e){
                radius=100.0;
                Logger.getLogger(ImportantPlacesThread.class.getName()).log(Level.SEVERE, null, e);
            }
            Map mCoordinate = (HashMap) data.get("location");
            double latitude = (double) (mCoordinate.get("latitude"));
            double longitude = (double) (mCoordinate.get("longitude"));
            checkIfGeofenceExists(latitude, longitude, radius);
        }
        return points;
    }
}

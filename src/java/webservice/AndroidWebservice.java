/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservice;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *
 * @author Bart
 */


@Path("Android")
public class AndroidWebservice {
    String googleacc = "google:102029683504157611080";
    String facebookacc = "facebook:10208455398612199";
    private ExecutorService executor;
    
    public AndroidWebservice(){
        executor = Executors.newFixedThreadPool(20);
    }
    
    public String startImportantPlacesThread(String userid){
        try {
            RetrieveDataThread t = new RetrieveDataThread(userid, "locations");
            Future<Map> locationsFuture= executor.submit(t);
            Map locations = locationsFuture.get();
            RetrieveDataThread geofenceThread = new RetrieveDataThread(userid,"geofences");
            Future<Map> geofencesFuture = executor.submit(geofenceThread);
            Map geofences = geofencesFuture.get();
            RetrieveDataThread noImportantGeofenceThread = new RetrieveDataThread(userid,"noImportantPlaces");
            Future<Map> noImportantGeofenceFuture = executor.submit(noImportantGeofenceThread);
            Map noImportantGeofences = noImportantGeofenceFuture.get();
            ImportantPlacesThread thread = new ImportantPlacesThread(locations, geofences, noImportantGeofences);
            Future<List<double[]>> fut = executor.submit(thread);
            List<double[]> centerpoints = fut.get();
            String text = "";
            for(double[] d:centerpoints){
                text+=+d[0]+"," + d[1]+",";
            }
            return text;
        } catch (InterruptedException ex) {
            Logger.getLogger(AndroidWebservice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(AndroidWebservice.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "There went something wrong startImportantPlacesThread";
    }
    
    @GET
    @Path("getImportantPlaces")
    @Produces("text/plain")
    public String getImportantPlaces(){
        System.out.println("start");
        return startImportantPlacesThread(googleacc);  
    }
    
    @GET
    @Produces("text/plain")
    @Path("getImportantPlaces/{id}")
    public String getImportantPlaces(@PathParam("id") String id){
        System.out.println("start");
        return startImportantPlacesThread(id);
    }
    
    public void startAngleCalculationThread(String userid){
        try {
            RetrieveDataThread t = new RetrieveDataThread(userid, "locations");
            Future<Map> locationsFuture= executor.submit(t);
            Map locations = locationsFuture.get();
            DirectionCalculaterThread thread = new DirectionCalculaterThread(userid, locations);
            executor.submit(thread);
            System.out.println("Done angle calculating");
        } catch (InterruptedException ex) {
            Logger.getLogger(AndroidWebservice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(AndroidWebservice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @GET
    @Produces("text/plain")
    @Path("calculateAngle")
    public void calculateAngle() {
        System.out.println("Start angle calculation");
        startAngleCalculationThread(googleacc);
    }
    
    @GET
    @Produces("text/plain")
    @Path("calculateAngle/{id}")
    public void calculateAngle(@PathParam("id") String id) {
        System.out.println("Angle calulating");
        startAngleCalculationThread(id);
    }
    
    public void startUpdateGeofenceThread(String id){
        try {
            RetrieveDataThread t = new RetrieveDataThread(id, "locations");
            Future<Map> locationsFuture= executor.submit(t);
            Map locations = locationsFuture.get();
            RetrieveDataThread geofenceThread = new RetrieveDataThread(id, "geofences");
            Future<Map> geofenceFuture= executor.submit(geofenceThread);
            Map geofences = geofenceFuture.get();
            UpdateGeofenceThread thread = new UpdateGeofenceThread(googleacc,locations, geofences);        
            executor.submit(thread);
            System.out.println("Done Update Geofence");
        } catch (InterruptedException ex) {
            Logger.getLogger(AndroidWebservice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(AndroidWebservice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @GET
    @Produces("text/plain")
    @Path("updateGeofence")
    public void updateGeofence(){
        System.out.println("UpdateGeofence");
        startUpdateGeofenceThread(googleacc);
    }
    
    @GET
    @Produces("text/plain")
    @Path("updateGeofence/{id}")
    public void updateGeofence(@PathParam("id") String id){
        System.out.println("UpdateGeofence");
        startUpdateGeofenceThread(id);
    }
    
    public void startUpdatePlace(String id){
        try {
            RetrieveDataThread t = new RetrieveDataThread(id, "locations");
            Future<Map> locationsFuture= executor.submit(t);
            Map locations = locationsFuture.get();
            UpdateOnTheWayThread thread = new UpdateOnTheWayThread(googleacc,locations);        
            executor.submit(thread);
            System.out.println("Done Update On The Way");
        } catch (InterruptedException ex) {
            Logger.getLogger(AndroidWebservice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(AndroidWebservice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @GET
    @Produces("text/plain")
    @Path("updatePlace")
    public void updatePlace(){
        System.out.println("UpdatePlace");
        startUpdatePlace(googleacc);
    }
}

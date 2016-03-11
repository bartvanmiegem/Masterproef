/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

/**
 *
 * @author Bart
 */
public class ImportantPlacesWorker {

    private double previousLatitude, previousLongitude, currentLongitude, currentLatitude;
    private Date previousDate;
    public static String[] daysOfTheWeek = {"Monday", "Tuesday", "Wednessday", "Thursday", "Friday", "Saterday", "Sunday"};
    private final List<DoublePoint> points;
    private final DBSCANClusterer dbscan;
    public static int NUMBER_OF_POINTS = 5;
    private List<double[]> centerPoints;
    private Map locationsMap;

    public ImportantPlacesWorker(ImportantPlacesThread thread, Map locations) {
        this.locationsMap = locations;
        dbscan = new DBSCANClusterer(30, NUMBER_OF_POINTS, new GPSDistance());
        points = new ArrayList<>();
    }

    public static double[] centroid(List<DoublePoint> points) {
        double[] centroid = {0, 0};

        for (int i = 0; i < points.size(); i++) {
            centroid[0] += points.get(i).getPoint()[0];
            centroid[1] += points.get(i).getPoint()[1];
        }

        int totalPoints = points.size();
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;

        return centroid;
    }

    public List<double[]> getCenterPoints() {
        return centerPoints;
    }

    private String calculateSpeed(long t1, double lat1, double lng1, long t2, double lat2, double lng2) {
        int R = 6371000; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        double speed = Math.round((distance / (t2 - t1)) * 3.6);
        if (0 <= speed && speed < 5) {
            return "0-5";
        } else if (5 <= speed && speed < 15) {
            return "5-15";
        } else if (15 <= speed && speed < 50) {
            return "15-50";
        } else if (50 <= speed && speed < 120) {
            return "50-120";
        } else {
            return "120<=x";
        }
    }

    private String getTimeZone(int hours) {
        if (0 <= hours && hours < 2) {
            return "late_night";
        } else if (2 <= hours && hours < 6) {
            return "early_morning";
        } else if (6 <= hours && hours < 10) {
            return "forenoon";
        } else if (10 <= hours && hours < 14) {
            return "noon";
        } else if (14 <= hours && hours < 18) {
            return "afternoon";
        } else if (18 <= hours && hours < 22) {
            return "night";
        } else {
            return "late_night";
        }
    }

    public static double[] createPoint(double a, double b) {
        return new double[]{a, b};
    }

    public void clusterPoints() {
        List<Cluster<DoublePoint>> cluster = dbscan.cluster(points);
        centerPoints = new ArrayList<>();
        for (Cluster<DoublePoint> c : cluster) {
            //System.out.println(c.getPoints().size());
            if (c.getPoints().size() >= NUMBER_OF_POINTS) {
                centerPoints.add(centroid(c.getPoints()));
                double[] d = centroid(c.getPoints());
                System.out.println(d[0] + "," + d[1]);
            }
            System.out.println("");
        }        
    }

    public List<double[]> getImportantPlaces() {
        Iterator it = locationsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Map data =  (Map) locationsMap.get(pair.getKey());
            String timestamp = (String) data.get("timestamp");
            Date currentDate = new Date(timestamp);
            int hours = currentDate.getHours();
            timestamp = getTimeZone(hours);
            String activity = "undefined";
            String place = "undefined";
            String day_of_the_week = daysOfTheWeek[currentDate.getDay()];
            if (data.get("status") != null) {
                activity = (String) data.get("status");
            }
            if (data.get("place") != null) {
                place = (String) data.get("place");
            }
            // Get recorded latitude and longitude
            Map mCoordinate = (HashMap) data.get("location");
            currentLatitude = (double) (mCoordinate.get("latitude"));
            currentLongitude = (double) (mCoordinate.get("longitude"));
            String speed = "0-5";
            if (previousLatitude != 0 && previousLongitude != 0 && previousDate != null) {
                speed = calculateSpeed(previousDate.getTime() / 1000, previousLatitude, previousLongitude, currentDate.getTime() / 1000, currentLatitude, currentLongitude);
            }
            previousDate = currentDate;
            previousLatitude = currentLatitude;
            previousLongitude = currentLongitude;
            // writeToFile(writerPoints, currentLatitude +"," + currentLongitude+"\n");
            //System.out.println(ctivity);
            if (activity.equals("Still") || activity.equals("Sleeping")) {
                points.add(new DoublePoint(createPoint(currentLatitude, currentLongitude)));
            }

        }
        clusterPoints();
        return centerPoints;
    }
}

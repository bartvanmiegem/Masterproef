/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservice;

import javax.faces.bean.SessionScoped;
import org.apache.commons.math3.ml.distance.DistanceMeasure;

/**
 *
 * @author Bart
 */
public class GPSDistance implements DistanceMeasure
{
    @Override
    public double compute(double[] doubles, double[] doubles1) {
        double lat1 = doubles[0];
        double long1 = doubles[1];
        double lat2 = doubles1[0];
        double long2 = doubles1[1];
        return distFrom((float)lat1,(float)long1,(float)lat2,(float)long2);
    }
    
    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }
}

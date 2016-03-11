/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservice;

import com.firebase.client.Firebase;
import java.util.Map;

/**
 *
 * @author Bart
 */
public class UpdateOnTheWayThread extends Thread{
    
    private Map locationsMap;
    private String lastKnownPlace="";
    private String userId;
    
    public UpdateOnTheWayThread(String userid, Map locationsMap){
        userId = userid;
        this.locationsMap = locationsMap;
    }

    @Override
    public void run() {        
        Object[] it = locationsMap.entrySet().toArray();
        int j =0;
        for(int i = 0; i<it.length; i++){
            Map.Entry pair = (Map.Entry)it[i];
            Map data = (Map) locationsMap.get(pair.getKey());
            String place = (String) data.get("place");
            if(place!=null){
                if(lastKnownPlace.equals("") || place.equals(lastKnownPlace)){
                    lastKnownPlace=place;
                    j=i;
                }
                else{
                    Map.Entry pairLastKnown = (Map.Entry)it[j];
                    while (j<=i) {   
                        Map dataLastKnown = (Map) locationsMap.get(pairLastKnown.getKey());
                        lastKnownPlace = (String) dataLastKnown.get("place");
                        if(lastKnownPlace==null){
                            dataLastKnown.put("place", "Towards "+place);   
                            lastKnownPlace = place;
                        }                        
                        pairLastKnown = (Map.Entry)it[j++];
                    }
                }
            }
        }
        
        Firebase locationFirebase = new Firebase("https://locationdatabase.firebaseio.com/locations/"+userId);
        locationFirebase.updateChildren(locationsMap);
    }
}

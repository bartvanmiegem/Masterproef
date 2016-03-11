/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservice;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Bart
 */
public class RetrieveDataThread implements Callable{
    private final String userId;
    private Map map;
    private final Semaphore semaphore = new Semaphore(0);
    private final String retrieve;
    
    public RetrieveDataThread(String id, String retrieve){
        userId = id;
        this.retrieve = retrieve;
    }
    
    @Override
    public Object call() throws Exception {
        Firebase locations = new Firebase("https://locationdatabase.firebaseio.com/"+retrieve+"/" + userId);       
        locations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {                
                map = (Map) ds.getValue();
                semaphore.release();
            }

            @Override
            public void onCancelled(FirebaseError fe) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        semaphore.acquire();
        map = new TreeMap<>(map);
        return map;
    }
    
}

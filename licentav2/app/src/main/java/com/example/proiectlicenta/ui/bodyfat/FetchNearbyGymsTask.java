
package com.example.proiectlicenta.ui.bodyfat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FetchNearbyGymsTask {

    private final GoogleMap mMap;
    private final ExecutorService executorService;
    private final Handler handler;

    public FetchNearbyGymsTask(GoogleMap mMap) {
        this.mMap = mMap;
        this.executorService = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void fetchGyms(String url) {
        executorService.execute(() -> {
            String response = "";
            try {
                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                reader.close();
            } catch (Exception e) {
                Log.e("FetchNearbyGymsTask", "eroare obtinere date: ", e);
            }

            String finalResponse = response;
            handler.post(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(finalResponse);
                    JSONArray resultsArray = jsonObject.getJSONArray("results");

                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject placeObject = resultsArray.getJSONObject(i);
                        String name = placeObject.getString("name");
                        String address = placeObject.getString("vicinity");
                        JSONObject location = placeObject.getJSONObject("geometry").getJSONObject("location");
                        double lat = location.getDouble("lat");
                        double lng = location.getDouble("lng");
                        LatLng gymLocation = new LatLng(lat, lng);

                        mMap.addMarker(new MarkerOptions()
                                .position(gymLocation)
                                .title(name)
                                .snippet(address)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); // coloram markerul cu verde
                    }
                } catch (Exception e) {
                    Log.e("FetchNearbyGymsTask", "eroare parsare JSON: ", e);
                }
            });
        });
    }
}
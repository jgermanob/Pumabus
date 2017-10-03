package com.germanco.notifications;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.UUID;

public class ApplicationController extends Application {
    private BeaconManager beaconManager;
    private BeaconManager beaconManager2;
    int contador=0;

    /**
     * Log or request TAG
     */
    public static final String TAG = "VolleyPatterns";

    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static ApplicationController sInstance;


    @Override
    public void onCreate() {
        super.onCreate();
        // initialize the singleton
        sInstance = this;
        beaconManager= new BeaconManager(getApplicationContext());
        beaconManager.setBackgroundScanPeriod(50000,1000);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region("Pumabus", UUID.fromString("a7520298-7dbc-40fe-a657-85a2c1bdb6f8"),null,null));
            }
        });
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> beacons) {
                Log.d("Region","Puma 1");
                for(int i=0; i<beacons.size(); i++){
                    showNotification("Pumabus","Has llegado a la parada con Minor= "+beacons.get(i).getMinor(),beacons.get(i).getMinor());
                }
            }

            @Override
            public void onExitedRegion(Region region) {
                showNotification("Pumabus","Te has ido de la parada de Pumabus",0);
            }
        });

        beaconManager2= new BeaconManager(getApplicationContext());
        beaconManager2.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager2.startMonitoring(new Region("Pumabus 2",UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),null,null));
            }
        });
        beaconManager2.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                Log.d("Region","Puma 2");
                showNotification("Pumabus","Has llegado a la parada con Minor= "+list.get(0).getMinor(),list.get(0).getMinor());
            }

            @Override
            public void onExitedRegion(Region region) {

            }
        });
    }

    public void showNotification(String titulo, String mensaje, int minor){
        Intent notificacionIntent= new Intent(this, MainActivity.class);
        notificacionIntent.putExtra("minor", String.valueOf(minor));
        Log.d("Estado",""+String.valueOf(minor));
        notificacionIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,notificacionIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification= new Notification.Builder(this)
                .setSmallIcon(R.drawable.busstop)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                //.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000} )
                .build();
        notification.defaults |=Notification.DEFAULT_ALL;
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(contador,notification);
        contador++;
    }


    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized ApplicationController getInstance() {
        return sInstance;
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}

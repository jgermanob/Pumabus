package com.germanco.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.UUID;

public class PumabusService extends Service {
    BeaconManager beaconManager;
    Region region;
    int numerador;
    Double latDestino, lngDestino;
    Bundle bundle;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Estado","Servicio creado");


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Estado","Servicio Iniciado");
        if(intent==null){
            Log.d("Estado","Vacio");
        }else {
            Log.d("Estado","Lleno");
            String extra=intent.getStringExtra("latDestino");
            Log.d("Estado",extra);
        }
        Log.d("LAT RECIBIDA",""+latDestino);
        System.out.println("INTENT: "+intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Estado","Servicio Destuido");
    }

    public void showNotification(String titulo, String mensaje, int minor){
        Intent notificacionIntent= new Intent(this, MainActivity.class);
        notificacionIntent.putExtra("minor", String.valueOf(minor));
        Log.d("Estado",""+String.valueOf(minor));
        notificacionIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,notificacionIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification= new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000})
                .build();
        // notification.defaults |=Notification.DEFAULT_ALL;
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(numerador,notification);
        numerador++;
    }
}

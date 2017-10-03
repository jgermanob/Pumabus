package com.germanco.notifications;

import android.*;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.JsonObject;
import com.estimote.sdk.repackaged.retrofit_v1_9_0.retrofit.http.GET;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Policy;
import java.util.ArrayList;
import java.util.List;

public class MapRoutesActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    int contador;
    List<LatLng> coordenadas, ruta;
    List<ParadaPumabus> paradaPumabuses;
    Spinner spinnerOrigen;
    Spinner spinnerDestino;
    Button botonRuta;
    LatLng latLngOrigen, latLngDestino;
    String url;
    List<LatLng> rutaDibujada= new ArrayList<>();
    ProgressDialog progressDialog;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private GoogleApiClient client;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    Button iniciarRuta;
    String urlMatrix;
    TextView distanceText, timeText;
    PumabusService pumabusService;
    Boolean distanceResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_routes);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client= new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        spinnerOrigen=(Spinner)findViewById(R.id.spinnerOrigen);
        spinnerDestino=(Spinner)findViewById(R.id.spinnerDestino);
        botonRuta=(Button)findViewById(R.id.botonRuta);
        iniciarRuta=(Button)findViewById(R.id.iniciarRuta);
        distanceText=(TextView)findViewById(R.id.distanceText);
        timeText=(TextView)findViewById(R.id.durationText);
       // pumabusService= new PumabusService();
        Context context=getApplicationContext();
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            Log.d("Luces","Disponibles");
        }
        coordenadas=MainActivity.coord;
        paradaPumabuses=MainActivity.paradasPumabus;
        ArrayAdapter<ParadaPumabus> arrayAdapter= new ArrayAdapter<ParadaPumabus>(this,android.R.layout.simple_spinner_dropdown_item,paradaPumabuses);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrigen.setAdapter(arrayAdapter);
        spinnerDestino.setAdapter(arrayAdapter);
        spinnerOrigen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                System.out.println("ELEMENTO SELECCIONADO: "+paradaPumabuses.get(position).getNombre());
                latLngOrigen=paradaPumabuses.get(position).getLatLng();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("Estado","onNothingSelected()");
            }
        });
        spinnerDestino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                latLngDestino=paradaPumabuses.get(i).getLatLng();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("Estado","onNothingSelected()");
            }
        });
        botonRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog= new ProgressDialog(MapRoutesActivity.this);
                progressDialog.setMessage("Obteniendo ruta...");
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                routeTask();
            }
        });

        iniciarRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(MapRoutesActivity.this, PumabusService.class);
                intent.putExtra("latDestino",String.valueOf(latLngDestino.latitude));
                intent.putExtra("lngDestino",String.valueOf(latLngDestino.longitude));
                startService(intent);
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.clear();
        LatLng unam= new LatLng(19.32889,-99.18722);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(unam));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
    }

    public void routeTask(){
        Log.d("Estado","routeTask()");
        final List<LatLng> coord= new ArrayList<>();
        url="http://maps.yamblet.com/pumabus?start="+latLngOrigen.latitude+","+latLngOrigen.longitude+"&end="+latLngDestino.latitude+","+latLngDestino.longitude;
        Log.d("URL",""+url);
        final MarkerOptions markerOptions= new MarkerOptions();
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    Log.d("Estado","onResponse()");
                    JSONArray rutaArray= jsonObject.getJSONArray("ruta");
                    for(int i=0; i<rutaArray.length(); i++){
                        JSONObject rutaObject= rutaArray.getJSONObject(i);
                        String nombreRuta= rutaObject.getString("name");
                        Log.d("Nombre Ruta",""+nombreRuta);
                        JSONArray valuesArray=rutaObject.getJSONArray("values");
                        Log.d("Tamaño Values",""+valuesArray.length());
                        for(int j=0; j<valuesArray.length(); j++){
                            JSONObject valuesObject=valuesArray.getJSONObject(j);
                            JSONObject location= valuesObject.getJSONObject("location");
                            LatLng latLng= new LatLng(location.getDouble("lat"),location.getDouble("lng"));
                            Log.d("Coordenadas",""+latLng.latitude+","+latLng.longitude);
                            coord.add(latLng);
                        }
                    }
                    drawMap(coord);
                    timeTask();
                    JSONArray nodosArray= jsonObject.getJSONArray("nodos");
                    for(int j=1; j<(nodosArray.length()-1); j++){
                        JSONObject nodoObject=nodosArray.getJSONObject(j);
                        String nombreNodo=nodoObject.getString("nombre");
                        JSONObject location= nodoObject.getJSONObject("location");
                        LatLng nodo= new LatLng(Double.parseDouble(location.getString("lat")),Double.parseDouble(location.getString("lng")));
                        nombreNodo=nombreNodo.replace("\u00fa","ú");
                        mMap.addMarker(markerOptions.position(nodo).title(nombreNodo));
                    }
                    Boolean prueba=getDistance(latLngDestino.latitude,latLngDestino.longitude,latLngOrigen.latitude,latLngOrigen.longitude);
                    Log.d("Distancia",""+prueba);
                    progressDialog.dismiss();
                }catch (JSONException jsonException){
                    jsonException.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error){
                error.printStackTrace();

            }

        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        ApplicationController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void drawMap(List<LatLng> list){
        Log.d("Estado","drawMap()");
        mMap.clear();
        PolylineOptions polylineOptions= new PolylineOptions();
        MarkerOptions markerOptions= new MarkerOptions();
        for(int l=0; l<list.size(); l++){
            mMap.addPolyline(polylineOptions.add(list.get(l)).color(Color.RED).width(5).clickable(false).geodesic(true));
        }
        mMap.addMarker(markerOptions.position(latLngOrigen).icon(BitmapDescriptorFactory.fromResource(R.drawable.startflag)));
        mMap.addMarker(markerOptions.position(latLngDestino).icon(BitmapDescriptorFactory.fromResource(R.drawable.finishflag)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngOrigen));
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        distanceResult=getDistance(latLng.latitude,latLng.longitude,latLngDestino.latitude,latLngDestino.longitude);
        /*if(distanceResult==true){
            //Mostrar notificación
        }*/
        for(int i=0; i<5;i++){
            Log.d("Entra","for");
            if(i==5){
                showNotification("Mi Pumabús","Bajas en la siguiente parada",7);
            }
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public void timeTask(){
        Log.d("Estado","TimeTask()");
        urlMatrix="https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins="+latLngOrigen.latitude+","+latLngOrigen.longitude+"&destinations="+latLngDestino.latitude+","+latLngDestino.longitude+"&key=AIzaSyCSI1GJfaKWfB8L2XWT6z7AzdrqYey247s";
        Log.d("URL",""+urlMatrix);
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET, urlMatrix, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    Log.d("Estado","onResponse()");
                    JSONArray rows=jsonObject.getJSONArray("rows");
                    for(int i=0; i<rows.length(); i++){
                        JSONObject rowsObject=rows.getJSONObject(i);
                        JSONArray elements=rowsObject.getJSONArray("elements");
                        for(int j=0; j<elements.length(); j++){
                            JSONObject elementsObject=elements.getJSONObject(j);
                            JSONObject distanceObject=elementsObject.getJSONObject("distance");
                            String distance=distanceObject.getString("text");
                            JSONObject durationObject=elementsObject.getJSONObject("duration");
                            String duration=durationObject.getString("text");
                            Log.d("Values",""+distance+" "+duration);
                            distanceText.setText(distance);
                            timeText.setText(duration);

                        }
                    }

                }catch (JSONException jsonException){
                    jsonException.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error){
                error.printStackTrace();

            }

        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        ApplicationController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public boolean getDistance(Double lat1, Double lon1, Double lat2, Double lon2){
        double R= 6371;
        double dLat, dLon, a, c, distance;
        boolean result=false;
        Log.d("Coords",""+lat1+","+lon1+" "+lat2+","+lon2);
        dLat=deg2rad(lat2-lat1);
        Log.d("dlat",""+dLat);
        dLon=deg2rad(lon2-lon1);
        Log.d("dlon",""+dLon);
        a=Math.sin(dLat/2)*Math.sin(dLat/2)+Math.cos(deg2rad(lat1))*Math.cos(deg2rad(lat2))*Math.sin(dLon/2)*Math.sin(dLon/2);
        Log.d("a",""+a);
        c=2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        Log.d("c",""+dLat);
        distance= R*c;
        Log.d("Distance",""+distance);
        if(distance<0.03){
            result=true;
        }else{
            Log.d("Estado","No estás cerca");
        }
        return result;

    }

    public double deg2rad(Double deg){
        double result=deg*(Math.PI/180);
        Log.d("Resultado",""+result);
        return result;
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
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000} )
                .build();
       // notification.defaults |=Notification.DEFAULT_ALL;
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(contador,notification);
        contador++;
    }



}

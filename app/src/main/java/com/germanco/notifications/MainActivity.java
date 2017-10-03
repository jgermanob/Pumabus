package com.germanco.notifications;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    String minorRecibido;
    Intent intent;
    List<LatLng> coordenadasPorPunto= new ArrayList<>();
    double lat, lon;
    LatLng latLng;
    List<RutasPumabus> ruta9= new ArrayList<>();
   public static List<ParadaPumabus> paradasPumabus= new ArrayList<>();
    int i=0, j=0;
    public static List<LatLng> coord= new ArrayList<>();
    Button botonMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Estado", "onCreate()");
        botonMapa=(Button)findViewById(R.id.botonMapa);
        botonMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(MainActivity.this, MapRoutesActivity.class);
                startActivity(intent);

            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        new obtenInfo().execute();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Estado","onResume()");
        try {
            intent=this.getIntent();
            minorRecibido=intent.getStringExtra("minor");
            Log.d("Estado",""+Integer.parseInt(minorRecibido));
            switch (Integer.parseInt(minorRecibido)){
                case 3:
                    Log.d("Estado","Has llegado a ruta 3");
                    break;
                case 8:
                    Log.d("Estado","Has llegado a ruta 8");
                    break;
                default:
                    Log.d("Estado","Ruta desconocida");
                    break;
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
    }

    public class obtenInfo extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            leeJSON("arcos.geojson");
            obtenenEstaciones();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            System.out.println("TAMAÑO RUTA 9: "+ruta9.size());
        }

        public String obtenerJSON(String jsonName){
            String json;
            try{
                InputStream is= getAssets().open(jsonName);
                int size=is.available();
                byte[] buffer= new byte[size];
                is.read(buffer);
                is.close();
                json=new String(buffer, "UTF-8");
            }catch (IOException ex){
                ex.printStackTrace();
                return null;
            }
            return json;
        }

        public void leeJSON(String jsonName){
            try {
                int contador=0, tam;
                JSONObject jsonObject= new JSONObject(obtenerJSON(jsonName));
                JSONArray jsonArray= jsonObject.getJSONArray("features");
                for(int i=0; i<jsonArray.length();i++){
                    JSONObject feature=jsonArray.getJSONObject(i);
                    JSONObject propiedades=feature.getJSONObject("properties");
                    String nombreRuta=propiedades.getString("name");
                    if(nombreRuta.equals("Ruta 9")){
                        contador++;
                        Log.d("ESTADO","RUTA 9");
                        String idElemento=propiedades.getString("id");
                        String fromIDRuta=propiedades.getString("from_estacion");
                        String toIDRuta=propiedades.getString("to_estacion");
                        JSONObject geometry=feature.getJSONObject("geometry");
                        JSONArray coordinates=geometry.getJSONArray("coordinates");
                        tam=coordinates.getJSONArray(0).length();
                        System.out.println("TAMAÑO CORDINATES "+contador+": "+tam);
                        for(int j=0; j<tam; j++){
                            System.out.println("COORDENADA "+(j+1)+": "+coordinates.getJSONArray(0).getJSONArray(j));
                            lat=coordinates.getJSONArray(0).getJSONArray(j).getDouble(1);
                            lon=coordinates.getJSONArray(0).getJSONArray(j).getDouble(0);
                            System.out.println("LATLNG "+(j+1)+": "+lat+","+lon);
                            latLng= new LatLng(lat,lon);
                            coordenadasPorPunto.add(latLng);
                            coord.add(latLng);
                        }
                        RutasPumabus rutasPumabus= new RutasPumabus();
                        rutasPumabus.setId(idElemento);
                        rutasPumabus.setFromId(fromIDRuta);
                        rutasPumabus.setToId(toIDRuta);
                        rutasPumabus.setCoordArco(coordenadasPorPunto);
                        ruta9.add(rutasPumabus);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void obtenenEstaciones(){
            try {
                Log.d("ESTADO","obtenerEstaciones()");
                JSONObject jsonObject= new JSONObject(obtenerJSON("nodos.geojson"));
                JSONArray jsonArray=jsonObject.getJSONArray("features");
                for(i=0; i<jsonArray.length(); i++){
                    ParadaPumabus paradaPumabus= new ParadaPumabus();
                    JSONObject feature=jsonArray.getJSONObject(i);
                    JSONObject propiedades=feature.getJSONObject("properties");
                    String nombreEstacion=propiedades.getString("name");
                    JSONObject geometry=feature.getJSONObject("geometry");
                    JSONArray coordinates=geometry.getJSONArray("coordinates");
                    lat=coordinates.getJSONArray(0).getDouble(1);
                    lon=coordinates.getJSONArray(0).getDouble(0);
                    Log.d("Lat",""+lat);
                    Log.d("Lon",""+lon);
                    String[] verifica;
                    verifica=nombreEstacion.split(" ");
                    if(verifica[0].equals("Pumabús")){
                        paradaPumabus.setNombre(nombreEstacion);
                        paradaPumabus.setLatLng(new LatLng(lat,lon));
                        paradasPumabus.add(paradaPumabus);
                    }
                }
                Log.d("Paradas Pumabús",""+paradasPumabus.size());
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

}


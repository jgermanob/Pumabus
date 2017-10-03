package com.germanco.notifications;
import com.google.android.gms.maps.model.LatLng;

public class ParadaPumabus {
    String nombre;
    LatLng latLng;
    String uuid;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString(){
        return nombre;
    }
}

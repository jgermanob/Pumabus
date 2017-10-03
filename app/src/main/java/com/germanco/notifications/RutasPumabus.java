package com.germanco.notifications;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class RutasPumabus {

    String id, toId, fromId;
    List<LatLng> coordArco= new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public List<LatLng> getCoordArco() {
        return coordArco;
    }

    public void setCoordArco(List<LatLng> coordArco) {
        this.coordArco = coordArco;
    }
}

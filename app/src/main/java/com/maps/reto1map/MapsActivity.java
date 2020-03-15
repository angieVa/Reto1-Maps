package com.maps.reto1map;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationProvider.OnLocationReceivedListener, OnMapClickListener {

    private GoogleMap mMap;
    private Marker actual;
    private TextView textViewInfo;
    private FloatingActionButton floatingActionButton;
    private Geocoder geocoder;
    private Marker tempo;
    private ArrayList<Marker> marcadores;
    private ArrayList<Double> distancias;
    private int posMenor;

    private LocationProvider gpsProvider;
    private LocationProvider networkProvider;

    private double minAccuracy = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(this, Locale.getDefault());
        textViewInfo = findViewById(R.id.textViewInfo);
        marcadores = new ArrayList<Marker>();
        distancias = new ArrayList<Double>();

        floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(
                (v)->{
                    if(tempo !=null) {
                        Intent i = new Intent(this, AddMarkers.class);
                        Marcador m = new Marcador( tempo.getTitle());
                        i.putExtra("marcador", m);
                        startActivityForResult(i, 11);
                    }
                }
        );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 11 && resultCode == RESULT_OK){

            if(data != null){

                Serializable serializable = data.getExtras().getSerializable("marcador");
                Marcador marcador = (Marcador) serializable;

                //añade un nuevo marcador
                Marker nuevo = mMap.addMarker(new MarkerOptions().position(tempo.getPosition())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.amarker)).title(marcador.getTitulo()).snippet(marcador.getDireccion()));

                //calcula a cuantos metros esta del marcador
                double distanciaActual = SphericalUtil.computeDistanceBetween(actual.getPosition(), nuevo.getPosition());
                nuevo.setSnippet("Usted se encuentra a "+ (int)distanciaActual+"m del lugar");

                tempo.remove();
                tempo = null;
                marcadores.add(nuevo);
                distancias.add(distanciaActual);

                //determina si es el más cercano
                double menor = 0;

                    for (int i = 0; i < distancias.size(); i++) {
                        if (i == 0) {
                            menor = distancias.get(i);
                            posMenor = i;
                        }

                        if (menor > distancias.get(i)) {
                            menor = distancias.get(i);
                            posMenor = i;
                        }
                    }
                    if(distanciaActual == menor){
                        //radio de 100 metros
                        if(distanciaActual <= 100) {
                            textViewInfo.setText("Usted se encuentra en " + nuevo.getTitle());
                        }else{
                            textViewInfo.setText("El lugar más cercano es " + nuevo.getTitle());
                        }
                    }

            } else {

                textViewInfo.setText("Error en agregar marcador");
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LocationManager manager = (LocationManager)getSystemService(LOCATION_SERVICE);
        mMap.setOnMapClickListener(this);

        //obtiene la posición actual
        setInitialPos( manager.getLastKnownLocation(LocationManager.GPS_PROVIDER) );


        this.networkProvider = new LocationProvider();
        networkProvider.setListener(this);
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500,2, networkProvider);

        this.gpsProvider = new LocationProvider();
        gpsProvider.setListener(this);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500,2, gpsProvider);
    }

    //Metodo para ubicar el marcador en la posición inicial
    public void setInitialPos(Location lastKnownLocation){

        List<Address> direccion;


        try {
            direccion = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1); // 1 representa la cantidad de resultados a obtener
            String address = direccion.get(0).getAddressLine(0);

            Log.e("ubicacion", lastKnownLocation.getProvider());
            LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            actual = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon)).title(address));
            textViewInfo.setText("Usted se encuentra en " + address);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //En este metodo recibimos los locations de ambos providers
    @Override
    public void OnLocationReceived(Location location) {
        if(location.getAccuracy() <= minAccuracy){

            minAccuracy = location.getAccuracy();
            actual.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(actual.getPosition(), 17));

            //calcula el marcador más cercano
            if(marcadores.size() >=1) {

                for (int i = 0; i < marcadores.size(); i++) {
                    double n = SphericalUtil.computeDistanceBetween(actual.getPosition(), marcadores.get(i).getPosition());
                    marcadores.get(i).setSnippet("Usted se encuentra a " + (int) n + "m del lugar");
                    distancias.set(i, n);
                }

                double menor = 0;
                for (int i = 0; i < distancias.size(); i++) {
                    if (i == 0) {
                        menor = distancias.get(i);
                        posMenor = i;
                    }

                    if (menor > distancias.get(i)) {
                        menor = distancias.get(i);
                        posMenor = i;
                    }
                }
                    //radio de 100 metros
                if (menor <= 100) {
                    textViewInfo.setText("Usted se encuentra en " + marcadores.get(posMenor).getTitle());
                } else {
                    textViewInfo.setText("El lugar más cercano es " + marcadores.get(posMenor).getTitle());
                }
            }
        }

    }

    //metodo que pone un marcador donde el usuario de click en el mapa
    @Override
    public void onMapClick(LatLng latLng) {

        List<Address> direccion;

        try {
            direccion = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // 1 representa la cantidad de resultados a obtener
            String address = direccion.get(0).getAddressLine(0);

            if(tempo != null){
                tempo.remove();
            }
            tempo = mMap.addMarker(new MarkerOptions() .position( new LatLng(
                    latLng.latitude, latLng.longitude)).title(address));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

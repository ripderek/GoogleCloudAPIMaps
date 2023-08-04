package com.example.googlecloud;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.googlecloud.WebService.Asynchtask;
import com.example.googlecloud.WebService.WebService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, Asynchtask {

    GoogleMap mMap;
    Double distance = 0.00;
    List<LatLng> Posiciones;
    PolylineOptions lineas;
    AlertDialog.Builder builder;
    Integer count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Posiciones = new ArrayList<>();
        lineas =  new PolylineOptions();
        lineas.width(8);
        lineas.color(Color.RED);
        builder = new AlertDialog.Builder(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng madrid = new LatLng(40.417325, -3.683081);
        CameraPosition camPos = new CameraPosition.Builder()
                .target(madrid)
                .zoom(19)
                .bearing(45) //noreste arriba
                .tilt(70) //punto de vista de la cámara 70 grados
                .build();
        CameraUpdate camUpd3 =
                CameraUpdateFactory.newCameraPosition(camPos);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.animateCamera(camUpd3);
        mMap.setOnMapClickListener(this);

    }

    @Override
    public void onMapClick(LatLng latLng) {
        LatLng punto = new LatLng(latLng.latitude,
                latLng.longitude);
        mMap.addMarker(new
                MarkerOptions().position(punto)
                .title("Marker in Sydney"));
        //aqui se envia las coordenadas
        lineas.add(latLng);
        //guardando los puntos en un array aparte
        /*
        if(Posiciones.size() == 6){
            PolylineOptions lineas = new
                    PolylineOptions()
                    .add(new LatLng(Posiciones.get(0).latitude, Posiciones.get(0).longitude))
                    .add(new LatLng(Posiciones.get(1).latitude, Posiciones.get(1).longitude))
                    .add(new LatLng(Posiciones.get(2).latitude, Posiciones.get(2).longitude))
                    .add(new LatLng(Posiciones.get(3).latitude, Posiciones.get(3).longitude))
                    .add(new LatLng(Posiciones.get(4).latitude, Posiciones.get(4).longitude))
                    .add(new LatLng(Posiciones.get(5).latitude, Posiciones.get(5).longitude))
                    .add(new LatLng(Posiciones.get(0).latitude, Posiciones.get(0).longitude));

            lineas.width(8);
            lineas.color(Color.RED);
            mMap.addPolyline(lineas);
        }
        */
        //los puntos guardados en el mismo array de lineas desde el constructor
        if (lineas.getPoints().size()==6){
            lineas.add(lineas.getPoints().get(0));
            //recorrer las lineas y verlas en la consola
            for(int i=1; i<lineas.getPoints().size();i++){

                //preguntar si i es el ultimo elemento para enviar como destino la primer posicion
                if (i==lineas.getPoints().size()) {
                    Enviar_WebService(lineas.getPoints().get(0).latitude+","+lineas.getPoints().get(0).longitude,
                            lineas.getPoints().get(i).latitude+","+lineas.getPoints().get(i).longitude);
                }
                Enviar_WebService(lineas.getPoints().get(i).latitude+","+lineas.getPoints().get(i).longitude,
                        lineas.getPoints().get(i-1).latitude+","+lineas.getPoints().get(i-1).longitude);

            }
            mMap.addPolyline(lineas);
            lineas.getPoints().clear();
        }

    }
    public void Enviar_WebService(String param1, String param2){
        //param 1 Destino
        //param 2 Origien
        Log.i("Coordenadas2","aqui envio");
        String link = "https://maps.googleapis.com/maps/api/distancematrix/json?destinations="+
                param1+"&origins="+
                param2+
                "&units=meters&key=AIzaSyDMmRXHBYOjJyXZruXemR11tl7uiJ2T_Q8";

                Map<String, String> datos = new HashMap<String, String>();
                WebService ws= new WebService(link,
                        datos, MainActivity.this, MainActivity.this);
                ws.execute("GET");

        Log.i("Coordenadas2",link);
    }
    @Override
    public void processFinish(String result) throws JSONException {
        JSONObject jObject = new JSONObject(result);
        JSONArray jResult = jObject.getJSONArray("rows");
        JSONObject respuesta = jResult.getJSONObject(0);

        JSONArray row = respuesta.getJSONArray("elements");
        for (int i = 0; i < row.length(); i++){
            JSONObject jdistance = row.getJSONObject(i);
            JSONObject valor = jdistance.getJSONObject("distance");
            distance += Double.parseDouble(valor.getString("value"));
        }
        Log.i("DISTANCIA", "La distancia actual es"+distance.toString());
        count++;
        if(count==6){
            count=0;
            String messge = distance.toString() + " metros";
            builder.setTitle("Distancia calculada")
                    .setMessage(messge)
                    .setCancelable(true).show();
            builder.show();
        }
    }
}
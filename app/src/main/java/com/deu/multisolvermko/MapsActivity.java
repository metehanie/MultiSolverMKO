package com.deu.multisolvermko;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    Integer sehirSayisi;
    TextView mapsTextView;
    int a = 0 ;
    int[] distances;
    Python py;
    PyObject pyobj;
    ProgressBar progressBar;

    ArrayList<Double> latdouble,londouble;
    ArrayList<Float> goToPy;
    Location loc1,loc2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();

        sehirSayisi = intent.getIntExtra("sehir",1);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        py = Python.getInstance();
        pyobj = py.getModule("gezginfly");

        mapsTextView = findViewById(R.id.maps_textView);
        progressBar = findViewById(R.id.progressBarMaps);
        progressBar.setVisibility(View.INVISIBLE);

        latdouble = new ArrayList<>();
        londouble = new ArrayList<>();

        goToPy = new ArrayList<>();



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        if (a < sehirSayisi ){
            String aciklama = a+". Konum";
            mMap.addMarker(new MarkerOptions().position(latLng).title(aciklama)).showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            Double lat = latLng.latitude;
            Double lon = latLng.longitude;

            latdouble.add(lat);
            londouble.add(lon);


            if (a == sehirSayisi-1){

                Toast.makeText(MapsActivity.this, "Hesaplanıyor", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.VISIBLE);

                ExampleThread2 thread2 = new ExampleThread2();
                thread2.start();

                mapsTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        progressBar.setVisibility(View.INVISIBLE);

                    }
                });

            }

        }else{
            Toast.makeText(this, "Sehir sayisini aştınız.", Toast.LENGTH_SHORT).show();
        }

        a=a+1;

    }

    public void hesapla(){
        for (int c =1 ; c<=sehirSayisi; c++){
            for (int d =1; d<=sehirSayisi ; d++){

                loc1 = new Location("");
                loc1.setLatitude(latdouble.get(c-1));
                loc1.setLongitude(londouble.get(c-1));

                loc2 = new Location("");
                loc2.setLatitude(latdouble.get(d-1));
                loc2.setLongitude(londouble.get(d-1));

                float distance = loc1.distanceTo(loc2);

                goToPy.add(distance);

            }
        }
    }

    public void arrayToDizi(){
        distances = new int[sehirSayisi*sehirSayisi];

        for (int i =0; i<sehirSayisi*sehirSayisi;i++){
            distances[i]=Math.round(goToPy.get(i));
        }

    }

    class ExampleThread2 extends Thread{

        @Override
        public void run() {

            hesapla();
            arrayToDizi();
            Python py = Python.getInstance();
            final PyObject pyobj = py.getModule("gezginfly");
            final PyObject obj = pyobj.callAttr("main123", distances);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapsTextView.setText(obj.toString());
                }
            });

        }
    }
}
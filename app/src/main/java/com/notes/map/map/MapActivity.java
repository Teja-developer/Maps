package com.notes.map.map;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.notes.map.map.DirectionsParser.dir;
import static com.notes.map.map.DirectionsParser.dist;
import static com.notes.map.map.DirectionsParser.latLngs;


public class MapActivity extends AppCompatActivity {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int REQUEST_CODE = 123;
    private Boolean mLocationPermissionGranted = false;

    private static final long MIN_TIME = 2;
    private static final float MIN_DISTANCE = 30;

    private GoogleMap mMap;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 17f;

    final String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;

    public TextView directions;

    Location currentLocation;

    String desplace;

    LatLng des;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        directions = findViewById(R.id.direct);

        getLocationPermission();
        if (mLocationPermissionGranted) {
            getDeviceLocation();

        }

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                desplace = place.getName().toString();
                Log.i("Map", "Place: " + place.getName());
               String sample = place.getAddress().toString();
               des = place.getLatLng();

                Log.v("Map", sample);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Map", "An error occurred: " + status);
            }
        });

        Button navi = (Button) findViewById(R.id.nav);
        navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+des.latitude+","+des.longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });


    }



    private void initMap() {
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                Log.v("Map", "Entered initMap");
                Toast.makeText(MapActivity.this, "Attack", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Get the current location
    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {

            final com.google.android.gms.tasks.Task<Location> location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<Location> task) {
                    if (task.isSuccessful()) {
                        currentLocation = location.getResult();
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                    }
                }
            });

        } catch (SecurityException e) {
            Log.v("Map", "Security Exception" + e);
        }
    }

    //Camera Zoom
    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        //For disabling location button
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }


    private void  getLocationPermission()
    {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.CAMERA};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                mLocationPermissionGranted=true;
                initMap();
            }
        }
        else {
            ActivityCompat.requestPermissions(this, permissions ,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode)
        {
            case LOCATION_PERMISSION_REQUEST_CODE:
            {
                if(grantResults.length>0){
                    for(int i=0; i<grantResults.length;i++)
                    {
                        if(grantResults[i]!=PackageManager.PERMISSION_GRANTED)
                        {
                            mLocationPermissionGranted=false;
                            return;
                        }
                    }
                    mLocationPermissionGranted=true;
                    //Open Map
                    initMap();
                }
            }
        }
    }

    //For marking the destination address
    public void destination(View v)
    {
//        Geocoder geocoder = new Geocoder(this);
//        try
//        {
//            List<Address> myList = geocoder.getFromLocationName(desplace, 5);
//            Address address = myList.get(0);
//            Log.v("Map" , address.toString());
//            String locality = address.getLocality();
//            Log.v("Map" , locality);
//            double lat =  address.getLatitude();
//            double lon = address.getLongitude();
//            goToLocation(lat, lon , 18);
//
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.title(locality);
//            markerOptions.position(new LatLng(lat,lon));
//            mMap.addMarker(markerOptions);
//
//        }catch (IOException e)
//        {
//            e.printStackTrace();
//        }
        double lat = des.latitude;
        double lon = des.longitude;
        goToLocation(des.latitude , des.longitude , 18);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat,lon));
        mMap.addMarker(markerOptions);



        //For getting the direction
        String url = requestUrl(des);
        TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
        taskRequestDirections.execute(url);


    }

    //Moves the camera to the location which is selected by the user
    public  void goToLocation(double latitude , double longitude , int zoom)
    {
            LatLng latLng1 = new LatLng(latitude,longitude);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng1 , zoom);
            mMap.moveCamera(cameraUpdate);
    }


    //Kiran
    private String requestUrl(LatLng des) {
        //Taking default origin location temporarily...

        String origin = "origin="+currentLocation.getLatitude()+","+currentLocation.getLongitude();
        String des1 ="destination="+des.latitude+","+des.longitude;
        String sensor ="sensor=false";
        String mode ="mode=driving";
        String param = origin+"&"+des1+"&"+sensor+"&"+mode;
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+param;
        Log.v("Map", url);
        return url;
    }



    public class TaskRequestDirections extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            String responseString = null;

            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String,Void,List <List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject object = null;
            List<List<HashMap<String, String>>> routes =null;
            try {
                object = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(object);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            super.onPostExecute(lists);

            ArrayList points = null;
            PolylineOptions options = null;

            for( List<HashMap<String, String>> path: lists){
                points = new ArrayList();
                options = new PolylineOptions();

                for( HashMap<String, String> point: path){

                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat,lon));
                }
                double longitude = currentLocation.getLongitude();
                double latitude = currentLocation.getLatitude();

                //Adding current location to head of the latLngs
                latLngs.add(0,new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()));

                Log.i("Map",""+longitude+" "+latitude);
                Log.i("Map",""+latLngs.get(0).longitude+" "+latLngs.get(0).latitude);



                for(int i=0;i<dir.length;i++){
                    if(longitude==latLngs.get(i).longitude && latitude==latLngs.get(i).latitude) {
                        Log.i("Maps","Equalss....................................."+latLngs.size()+" "+dist.length+" "+dir.length);
                        directions.setText(""+dir[i]);
                        //Need to continue
                    }
                }

                LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                LocationListener mLocationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        Log.i("Map", "onLocationChanged() callback received");
                        double longitude = (location.getLongitude());
                        double latitude = (location.getLatitude());

                        Log.i("Map", "longitude is: " + longitude);
                        Log.i("Map", "latitude is: " + latitude);

                        //if(steps.getDuration - (Longitude.getDIrection(end,start,kdla,alskd)<=10)
                        for(int i=0;i<dir.length;i++){
//                            if(longitude==latLngs.get(i).longitude && latitude==latLngs.get(i).latitude) {
//                                directions.setText(""+dir[i]);
//                                Log.i("Maps",""+dir[i]);
//                                //Need to continue
//                            }

                            Location temp = new Location("temp");
                            temp.setLatitude(latLngs.get(i+1).latitude);
                            temp.setLongitude(latLngs.get(i+1).longitude);
                            Log.i("Map",""+location.getLatitude()+" "+location.getLongitude()+"\n "+dist[i]+" "+location.distanceTo(temp));

                            if(dist[i] - location.distanceTo(temp)<50&&dist[i] - location.distanceTo(temp)>0){
                                Log.i("Map","Reached!");
                                directions.setText(dir[i]);
                                //Log.i("Map","This is from locationListener");
                            }
                        }

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        // Log statements to help you debug your app.
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.i("Map", "onProviderDisabled() callback received. Provider: " + provider);
                    }

                };

                // This is the permission check to access (fine) location.

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions((Activity) getApplicationContext(),
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
                    return;
                }

                Log.v("Map", "End");
                mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
                options.addAll(points);
                options.width(12);
                options.color(R.color.colorPrimaryDark);
                options.geodesic(true);
            }
            if(options!=null)
                mMap.addPolyline(options);
            else
                Toast.makeText(getApplicationContext(),"Directions not found :(",Toast.LENGTH_SHORT).show();
        }
    }


    private String requestDirection(String reqUrl) throws IOException {

        String responseString = null;
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;


        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line= "";
            StringBuffer stringBuffer = new StringBuffer();

            while((line=bufferedReader.readLine())!=null){
                stringBuffer.append(line);
            }
            responseString = stringBuffer.toString();
            inputStreamReader.close();
            bufferedReader.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(inputStream!=null)
                inputStream.close();
            httpURLConnection.disconnect();
        }
        return responseString;
    }


}

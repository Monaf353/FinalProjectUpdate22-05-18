package com.develop.windexit.finalproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.windexit.finalproject.Common.Common;
import com.develop.windexit.finalproject.Helper.DirectionJSONParser;
import com.develop.windexit.finalproject.Model.Request;
import com.develop.windexit.finalproject.Model.ShippingInformation;
import com.develop.windexit.finalproject.Remote.IGoogleService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback, ValueEventListener {

    private GoogleMap mMap;
    FirebaseDatabase database;
    DatabaseReference request, shippingOrders;
    Request currentOrder;

    IGoogleService mService;
    Marker shippingMarker;


    Polyline polyline;
    TextView tvDistance,tvTime;
    ImageView satteliteView,normalView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto_Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_tracking_order);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        database = FirebaseDatabase.getInstance();
        request = database.getReference("Request");
        shippingOrders = database.getReference("ShippingOrders");
        shippingOrders.addValueEventListener(this);

        mService = Common.getGoogleMapAPI();

        tvDistance = (TextView)findViewById(R.id.tvDistance);
        tvTime =(TextView) findViewById(R.id.tvTime);

        satteliteView = findViewById(R.id.satelliteView);
        normalView = findViewById(R.id.normalView);

        normalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        satteliteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });

    }

    @Override
    protected void onStop() {
        shippingOrders.removeEventListener(this);
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        boolean isSuccess = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_style));
        if (!isSuccess) {
            Log.e("ERROR", "Map style load filed");
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);

        trackingLocation();
    }

    private void trackingLocation() {
        request.child(Common.currentKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentOrder = dataSnapshot.getValue(Request.class);
                        //if order has address
                        if (currentOrder.getAddress() != null && !currentOrder.getAddress().isEmpty()) {
                            mService.getLocationFromAddress(new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?address=")
                                    .append(currentOrder.getAddress()).toString())
                                    .enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            try {
                                                JSONObject jsonObject = new JSONObject(response.body());
                                                String lat = ((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lat").toString();

                                                String lng = ((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lng").toString();


                                                //Get location from order side .. string convert to LatLng
                                                LatLng location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                                                MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker())
                                                        .title("Order destination")
                                                        .position(location);
                                                mMap.addMarker(markerOptions);


                                                //Set Shipper location
                                                shippingOrders.child(Common.currentKey)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);

                                                                LatLng shipperLocation;

                                                                if (shippingInformation != null) {

                                                                    shipperLocation = new LatLng(shippingInformation.getLat(), shippingInformation.getLng());

                                                                    if (shippingMarker == null) {
                                                                        shippingMarker = mMap.addMarker(new MarkerOptions()
                                                                                .position(shipperLocation)
                                                                                .title("Shipper #" + shippingInformation.getOrderId())
                                                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                                                                    } else {
                                                                        shippingMarker.setPosition(shipperLocation);
                                                                    }

                                                                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                            .target(shipperLocation)
                                                                            .zoom(17)
                                                                            .bearing(0)
                                                                            .tilt(45)
                                                                            .build();
                                                                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                                    //draw routes
                                                                    if (polyline != null)
                                                                        polyline.remove();
                                                                    mService.getDirections(shipperLocation.latitude + "," + shipperLocation.longitude,
                                                                            currentOrder.getAddress())
                                                                            .enqueue(new Callback<String>() {
                                                                                @Override
                                                                                public void onResponse(Call<String> call, Response<String> response) {
                                                                                    new ParserTask().execute(response.body().toString());
                                                                                }

                                                                                @Override
                                                                                public void onFailure(Call<String> call, Throwable t) {

                                                                                }
                                                                            });


                                                                }
                                                                else {
                                                                    Toast.makeText(TrackingOrder.this,"Status not shipping or Shipper still now offline!",Toast.LENGTH_SHORT).show();
                                                                }


                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });


                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {

                                        }
                                    });

                        }
                        //if order has latLng
                        else if (currentOrder.getLatLan() != null && !currentOrder.getLatLan().isEmpty()) {

                            mService.getLocationFromAddress(new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?latlng=")
                                    .append(currentOrder.getLatLan()).toString())
                                    .enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            try {
                                                JSONObject jsonObject = new JSONObject(response.body());
                                                String lat = ((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lat").toString();

                                                String lng = ((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lng").toString();


                                                //Get location from order side .. string convert to LatLng
                                                LatLng location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                                                MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                                        .title("Order destination")
                                                        .position(location);
                                                mMap.addMarker(markerOptions);


                                                //Set Shipper location
                                                shippingOrders.child(Common.currentKey)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot)
                                                    {
                                                        ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);
                                                        LatLng shipperLocation = null;
                                                        if (shippingInformation != null) {
                                                            shipperLocation = new LatLng(shippingInformation.getLat(), shippingInformation.getLng());

                                                        if (shippingMarker == null)
                                                            {
                                                                shippingMarker = mMap.addMarker(new MarkerOptions()
                                                                        .position(shipperLocation)
                                                                        .title("Shipper # "+shippingInformation.getOrderId())
                                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                                                            }
                                                            else
                                                                {
                                                                shippingMarker.setPosition(shipperLocation);
                                                            }

                                                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                .target(shipperLocation)
                                                                .zoom(17)
                                                                .bearing(0)
                                                                .tilt(45)
                                                                .build();
                                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                       //draw routes
                                                        if (polyline != null)
                                                            polyline.remove();

                                                        mService.getDirections(shipperLocation.latitude+","+shipperLocation.longitude,
                                                                currentOrder.getLatLan())
                                                                .enqueue(new Callback<String>() {
                                                                    @Override
                                                                    public void onResponse(Call<String> call, Response<String> response) {
                                                                        new ParserTask().execute(response.body().toString());
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<String> call, Throwable t) {

                                                                    }
                                                                });


                                                        }
                                                        else {
                                                            Toast.makeText(TrackingOrder.this,"Status not shipping or Shipper still now offline!",Toast.LENGTH_SHORT).show();
                                                        }

                                                   }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });


                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        trackingLocation();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        ProgressDialog mDialog = new ProgressDialog(TrackingOrder.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please waiting.....");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            String distance = "";
            String duration = "";

            if (lists.size() < 1) {
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < lists.size(); i++) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = lists.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }


                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                polylineOptions.addAll(points);
                polylineOptions.width(12);
                polylineOptions.color(Color.parseColor("#f9452e"));
                polylineOptions.geodesic(true);
            }

            tvDistance.setText("Distance: " + distance);
            tvTime.setText("Driving duration: " + duration);


            // tvDistanceDuration.setText("Distance: " + distance + ", Duration: " + duration);
            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(polylineOptions);
        }
    }

}

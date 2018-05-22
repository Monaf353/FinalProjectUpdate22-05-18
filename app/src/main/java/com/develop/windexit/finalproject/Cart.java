package com.develop.windexit.finalproject;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.windexit.finalproject.Common.Common;
import com.develop.windexit.finalproject.Common.Config;
import com.develop.windexit.finalproject.Database.Database;
import com.develop.windexit.finalproject.Model.MyResponse;
import com.develop.windexit.finalproject.Model.Notification;
import com.develop.windexit.finalproject.Model.Order;
import com.develop.windexit.finalproject.Model.Request;
import com.develop.windexit.finalproject.Model.Sender;
import com.develop.windexit.finalproject.Model.Token;
import com.develop.windexit.finalproject.Model.User;
import com.develop.windexit.finalproject.Remote.APIService;
import com.develop.windexit.finalproject.Remote.IGoogleService;
import com.develop.windexit.finalproject.ViewHolder.CardAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import in.shadowfax.proswipebutton.ProSwipeButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;


    DatabaseReference requests;

    ProSwipeButton btnPlace;


    public TextView txtTotalPrice;

    List<Order> cart = new ArrayList<>();
    CardAdapter adapter;


    //PAYPAL payment
    private static final int PAYPAL_REQUEST_CODE = 9999;
    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)    //use sendBox  we test , change it late if you..
            .clientId(Config.PAYPAL_CLIENT_ID);

    String address, comment, newAddress;

    Place shippingAddress;


    //Location

    private GoogleMap mMap;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private final static int LOCATION_PERMISSION_REQUEST = 1001;

    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    //google map api rertrofitservice
    IGoogleService mGoogleMapService;
    APIService mService;
    // private IGeoCoordinates mService;


   /* String date;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;*/

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

        setContentView(R.layout.activity_cart);


        //init
        mGoogleMapService = Common.getGoogleMapAPI();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request runtime permission
            requestRuntimePermission();

        } else {

            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();

            }
        }

        // displayLocation();


        //Init paypal
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        mService = Common.getFCMService();

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Request");


        //init
        recyclerView = findViewById(R.id.listCart);
        // recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(), R.anim.layout_from_right);
        recyclerView.setLayoutAnimation(controller);

        txtTotalPrice = (TextView) findViewById(R.id.total);

        btnPlace = (ProSwipeButton) findViewById(R.id.btnPlaceOrder);
        btnPlace.setOnSwipeListener(new ProSwipeButton.OnSwipeListener() {
            @Override
            public void onSwipeConfirm() {
                // user has swiped the btn. Perform your async operation now
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // task success! show TICK icon in ProSwipeButton
                        if (cart.size() > 0) {
                            showAlertDialog();
                            btnPlace.showResultIcon(true);
                        } else {
                            Toast.makeText(Cart.this, "Your card is empty !!", Toast.LENGTH_SHORT).show();
                           btnPlace.showResultIcon(false);
                        }

                        // false if task failed
                    }
                }, 1000);
            }
        });

        btnPlace.setSwipeDistance(0.6f);
      /*  btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your card is empty !!", Toast.LENGTH_SHORT).show();
            }
        });*/

        loadListFood();


    }

    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(this, new String[]
                {
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, LOCATION_PERMISSION_REQUEST);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;

        } else {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {


                Log.d("Location", "Your location : " + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
                // double latitude = mLastLocation.getLatitude();
                //double longitude = mLastLocation.getLongitude();

                // LatLng yourLocation = new LatLng(latitude, longitude);

              /*  mMap.addMarker(new MarkerOptions().position(yourLocation)
                        .title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));*/

                // drawRoute(yourLocation, Common.currentRequest.getAddress());

            } else {
                //Toast.makeText(this, "Couldn't get the location", Toast.LENGTH_SHORT).show();
                Log.d("ERRORRRRRRRRRRRRRRR", "Cannot get your location");
            }

        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    //Ctrl + o
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        //displayLocation();
                    }
                }
                break;
        }
    }


    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Address and Payment method");
        alertDialog.setMessage("Enter or select any one (googleMap/custom) address!");

       /* final EditText edtAddress = new EditText(Cart.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        edtAddress.setLayoutParams(lp);
        alertDialog.setView(edtAddress);*/

        final LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);

        final MaterialEditText editAddress = order_address_comment.findViewById(R.id.edtAddress);


        final PlaceAutocompleteFragment edtAddress = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        //Hide search icon before fragment
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);


        //set Hint for Autocomplete Edit text
        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Search address (GoogleMap)");
        //text size
        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(20);

        //Get address from place auto comple
        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                shippingAddress = place;
            }

            @Override
            public void onError(Status status) {
                Log.e("error", status.getStatusMessage());
            }
        });

        final MaterialEditText edtComment = order_address_comment.findViewById(R.id.edtComment);

        //radio
        final RadioButton rdiShipToAddress = (RadioButton) order_address_comment.findViewById(R.id.rdishipToAddress);
        final RadioButton rdiHomeAddress = (RadioButton) order_address_comment.findViewById(R.id.rdihomeToAddress);


        //Payment
        final RadioButton rdiCOD = (RadioButton) order_address_comment.findViewById(R.id.rdiCOD);
        final RadioButton rdiPaypal = (RadioButton) order_address_comment.findViewById(R.id.rdiPaypal);
        final RadioButton rdiBuyItBlance = (RadioButton) order_address_comment.findViewById(R.id.rdiBuyItBalance);


        //eventradio
        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Common.currentUser.getHomeAddress() != null || !TextUtils.isEmpty(Common.currentUser.getHomeAddress())) {
                        address = Common.currentUser.getHomeAddress();
                        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                .setText(address);
                    } else {
                        Toast.makeText(Cart.this, "Please update your home address", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });


        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mGoogleMapService.getAdressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false",
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {

                                    try {
                                        JSONObject jsonObject = new JSONObject(response.body().toString());

                                        JSONArray jsonArray = jsonObject.getJSONArray("results");

                                        JSONObject firstObject = jsonArray.getJSONObject(0);

                                        address = firstObject.getString("formatted_address");

                                        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                                .setText(address);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(Cart.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }
        });


        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_add_shopping_cart_black_24dp);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                newAddress = editAddress.getText().toString();


                if (!rdiShipToAddress.isChecked() && !rdiHomeAddress.isChecked()) {

                    if (shippingAddress != null && !newAddress.isEmpty()) {
                        Toast.makeText(Cart.this, "Not both, Please input any one!", Toast.LENGTH_SHORT).show();
                        //Remove fragment
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();
                        return;
                    }
                    if (shippingAddress == null && newAddress.isEmpty()) {
                        Toast.makeText(Cart.this, "Please input any one or Select option", Toast.LENGTH_SHORT).show();
                        //Remove fragment
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();
                        return;
                    }

                    ///This One
                    if (shippingAddress != null && newAddress.isEmpty()) {
                        address = shippingAddress.getAddress().toString();
                    } else if (!newAddress.isEmpty() && shippingAddress == null) {
                        address = newAddress;
                    }
                    //and this one


                   /* else
                    {
                        Toast.makeText(Cart.this, "please select option", Toast.LENGTH_SHORT).show();
                        //Remove fragment
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();
                        return;
                    }*/


                }
                if (TextUtils.isEmpty(address)) {
                    Toast.makeText(Cart.this, "Select option Or Enter address", Toast.LENGTH_SHORT).show();
                    //Remove fragment
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                }


                // address = shippingAddress.getAddress().toString();
                comment = edtComment.getText().toString();


                //check payment
                if (!rdiCOD.isChecked() && !rdiPaypal.isChecked() && !rdiBuyItBlance.isChecked()) //if both COD and Paypal and Buy Balance not checked
                {
                    Toast.makeText(Cart.this, "Please select option", Toast.LENGTH_SHORT).show();
                    //Remove fragment
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                } else if (rdiPaypal.isChecked()) {

                    //show paypal to payment
                    String formatAmount = txtTotalPrice.getText().toString()
                            .replace("BDT", "$")
                            .replace("$", "")
                            .replace(",", "");
                    // float amount = Float.parseFloat(formatAmount);
                    PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(formatAmount),
                            "USD",
                            "Final Project Order",
                            PayPalPayment.PAYMENT_INTENT_SALE);

                    Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                    intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                    intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                    startActivityForResult(intent, PAYPAL_REQUEST_CODE);
                } else if (rdiCOD.isChecked()) {
                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            address,
                            txtTotalPrice.getText().toString(),
                            "0",
                            comment,
                            "COD",
                            "Unpaid",
                            //jsonObject.getJSONObject("response").getString("state"),
                            //latLng string format... mLastLocation.getLongitude hole own location hoito but now
                            //String.format("%s,%s",shippingAddress.getLatLng().latitude,shippingAddress.getLatLng().longitude),
                            String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                            Common.date(),
                            cart);


                    //Common.currentRequest = request;

                    String order_number = String.valueOf(System.currentTimeMillis());
                    //submit firebase
                    requests.child(order_number)
                            .setValue(request);

                    //delete cart
                    new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                    // adapter.notifyDataSetChanged();

                    sendNotificationOrder(order_number);

                    Toast.makeText(Cart.this, "Thank you, Order Place!!", Toast.LENGTH_LONG).show();
                    finish();
                } else if (rdiBuyItBlance.isChecked()) {
                    double amount = 0;
                    //first we will get total price from txttotalprice

                    //amount = Double.parseDouble(txtTotalPrice.getText().toString());
                    Locale locale = new Locale("bd", "BD");
                    try {
                        amount = Common.formatCurrency(txtTotalPrice.getText().toString(), locale).doubleValue();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (Double.parseDouble(Common.currentUser.getBalance().toString()) >= amount) {

                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                "0",
                                comment,
                                "Desh Balance",
                                "Paid",
                                //jsonObject.getJSONObject("response").getString("state"),
                                //latLng string format... mLastLocation.getLongitude hole own location hoito but now
                                //String.format("%s,%s",shippingAddress.getLatLng().latitude,shippingAddress.getLatLng().longitude),
                                String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                                Common.date(),
                                cart);

                        //Common.currentRequest = request;


                        final String order_number = String.valueOf(System.currentTimeMillis());
                        //submit firebase
                        requests.child(order_number)
                                .setValue(request);

                        //delete cart
                        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                        // adapter.notifyDataSetChanged();

                        //Update balance
                        double balance = Double.parseDouble(Common.currentUser.getBalance().toString()) - amount;
                        Map<String, Object> update_balance = new HashMap<>();
                        update_balance.put("balance", balance);

                        FirebaseDatabase.getInstance().getReference("User")
                                .child(Common.currentUser.getPhone())
                                .updateChildren(update_balance)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseDatabase.getInstance().getReference("User")
                                                    .child(Common.currentUser.getPhone())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Common.currentUser = dataSnapshot.getValue(User.class);
                                                            sendNotificationOrder(order_number);
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                        }
                                    }
                                });
                        Toast.makeText(Cart.this, "Thank you, order Place", Toast.LENGTH_LONG).show();
                        finish();

                    } else {
                        Toast.makeText(Cart.this, "Your balance not enough , Please choose other payment", Toast.LENGTH_LONG).show();

                    }
                }
                //Remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();


            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //Remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();


            }
        });
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);

                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                "0",
                                comment,
                                "Paypal",
                                jsonObject.getJSONObject("response").getString("state"),
                                //latLng string format... mLastLocation.getLongitude hole own location hoito but now
                                String.format("%s,%s", shippingAddress.getLatLng().latitude, shippingAddress.getLatLng().longitude),
                                Common.date(),
                                cart);

                        //Common.currentRequest = request;

                        String order_number = String.valueOf(System.currentTimeMillis());
                        //submit firebase
                        requests.child(order_number)
                                .setValue(request);

                        //delete cart
                        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                        // adapter.notifyDataSetChanged();

                        sendNotificationOrder(order_number);

                        Toast.makeText(Cart.this, "Thank you, order Place", Toast.LENGTH_LONG).show();
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(Cart.this, "payment cancle", Toast.LENGTH_LONG).show();
            else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
                Toast.makeText(Cart.this, "invalid payment", Toast.LENGTH_LONG).show();
        }
    }

    private void sendNotificationOrder(final String order_number) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");

        final Query data = tokens.orderByChild("serverToken").equalTo(true); //get all node with isServerToken is true
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {

                    Token serverToken = postSnapShot.getValue(Token.class);

                    Notification notification = new Notification("CLIENT", "You have new order" + order_number);

                    // assert serverToken != null;
                    Sender content = new Sender(serverToken.getToken(), notification);

                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(Cart.this, "Thank you , order Place", Toast.LENGTH_LONG).show();
                                            finish();
                                        } else {
                                            Toast.makeText(Cart.this, "Failed", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("Error", t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood() {

        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CardAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //calculate total price
        int total = 0;
        for (Order order : cart) {
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
        }

        // Locale locale = new Locale("en", "US");
        Locale locale = new Locale("bd", "BD");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        txtTotalPrice.setText(fmt.format(total));
        //txtTotalPrice.setText(total);

        /*adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);*/

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return super.onContextItemSelected(item);
    }

    private void deleteCart(int position) {
        cart.remove(position);
        new Database(this).cleanCart(Common.currentUser.getPhone());

        for (Order item : cart)
            new Database(this).addToCart(item);
        loadListFood();
    }


}

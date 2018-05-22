package com.develop.windexit.finalproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.develop.windexit.finalproject.Common.Common;
import com.develop.windexit.finalproject.Interface.ItemCliclListener;
import com.develop.windexit.finalproject.Model.Request;
import com.develop.windexit.finalproject.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OrderStatus extends AppCompatActivity {


    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference requests;
    SwipeRefreshLayout swipeRefreshLayout;

    TextView totalCount, todayOrder;
    long size, size2;

    Spinner spinner;
    int mYear, mMonth, mDay;
    String startDate, endDate;
    TextView edtStartDate, edtEndDate;
    Button selectDateStart, selectDateEnd;

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

        setContentView(R.layout.activity_order_status);

        /* toolbar =  findViewById(R.id.sylBack);
        setSupportActionBar(toolbar);*/
        getSupportActionBar().setTitle("Orders");
        setTitleColor(Color.WHITE);
        // toolbar.setTitleTextColor(Color.WHITE);

        //setTitleColor();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //view
        swipeRefreshLayout = findViewById(R.id.swipe_layout_order_status);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getIntent() == null) {
                    loadOrders(Common.currentUser.getPhone());
                } else {
                    if (getIntent().getStringExtra("userPhone") == null) {
                        loadOrders(Common.currentUser.getPhone());
                    } else {
                        loadOrders(getIntent().getStringExtra("userPhone"));
                    }
                }
            }
        });

        //Default, load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (getIntent() == null) {
                    loadOrders(Common.currentUser.getPhone());
                } else {
                    if (getIntent().getStringExtra("userPhone") == null) {
                        loadOrders(Common.currentUser.getPhone());
                    } else {
                        loadOrders(getIntent().getStringExtra("userPhone"));
                    }
                }
            }
        });


        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Request");

        recyclerView = findViewById(R.id.listOrders);
        // recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(), R.anim.layout_bottom_up);
        recyclerView.setLayoutAnimation(controller);

        totalCount = (TextView) findViewById(R.id.totalCount);
        todayOrder = findViewById(R.id.todayOrderCount);

        requests.orderByChild("phone")
                .equalTo(Common.currentUser.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        size = dataSnapshot.getChildrenCount();
                        totalCount.setText("Total order: " + size);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        requests.orderByChild("date")
                .equalTo(Common.date())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        size2 = dataSnapshot.getChildrenCount();
                        todayOrder.setText("Today order: " + size2);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }


    private void loadOrders(String phone) {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
              requests.orderByChild("phone").equalTo(phone)
                ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, Request model, int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());
                viewHolder.order_date.setText(model.getDate());

                if (model.getStatus().equals("1")) {
                    viewHolder.txtOrderComment.setText(model.getComment());
                    viewHolder.txtOrderComment.setTextColor(getResources().getColor(R.color.colorAccent));
                } else if (model.getStatus().equals("2")) {
                    viewHolder.txtOrderComment.setText("Your status shipping, product will arrive at very soon!");
                    viewHolder.txtOrderComment.setTextColor(getResources().getColor(R.color.colorAccent));
                } else if (model.getStatus().equals("3")) {
                    viewHolder.txtOrderComment.setText("Your status shipped, Thank You!");
                    viewHolder.txtOrderComment.setTextColor(getResources().getColor(R.color.colorAccent));
                } else {
                    viewHolder.txtOrderComment.setText("Your Comment will reply very soon!");
                }

                viewHolder.setItemCliclListener(new ItemCliclListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Common.currentKey = adapter.getRef(position).getKey();
                        startActivity(new Intent(OrderStatus.this, TrackingOrder.class));
                    }
                });

            }

        };
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

       /* requests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                size =  dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        totalOrder.setText((int) size);*/
    }


    /*

       spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(OrderStatus.this,
                R.layout.custom_layout_item,
                getResources().getStringArray(R.array.oder_view));

        myAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(myAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner.getSelectedItem().toString().equals("Today Orders"))
                {

                    //view
                    swipeRefreshLayout = findViewById(R.id.swipe_layout_order_status);
                    swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark,
                            android.R.color.holo_orange_dark,
                            android.R.color.holo_blue_dark);
                    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            if (getIntent() == null)
                            {
                                todayOrder(Common.currentUser.getPhone());
                            }
                            else
                            {
                                if (getIntent().getStringExtra("userPhone") == null)
                                {
                                    todayOrder(Common.currentUser.getPhone());
                                }
                                else
                                {
                                    todayOrder(getIntent().getStringExtra("userPhone"));
                                }
                            }
                        }
                    });

                    //Default, load for first time
                    swipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            if (getIntent() == null)
                            {
                                todayOrder(Common.currentUser.getPhone());
                            } else {
                                if (getIntent().getStringExtra("userPhone") == null) {
                                    todayOrder(Common.currentUser.getPhone());
                                }else {
                                    todayOrder(getIntent().getStringExtra("userPhone"));
                                }
                            }
                        }
                    });

                    //Toast.makeText(OrderStatus.this,"All work",Toast.LENGTH_SHORT).show();
                } else if (spinner.getSelectedItem().toString().equals("All Orders")) {

                    //view
                    swipeRefreshLayout = findViewById(R.id.swipe_layout_order_status);
                    swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark,
                            android.R.color.holo_orange_dark,
                            android.R.color.holo_blue_dark);
                    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            if (getIntent() == null)
                            {
                                loadOrders(Common.currentUser.getPhone());
                            }
                            else
                            {
                                if (getIntent().getStringExtra("userPhone") == null)
                                {
                                    loadOrders(Common.currentUser.getPhone());
                                }
                                else
                                {
                                    loadOrders(getIntent().getStringExtra("userPhone"));
                                }
                            }
                        }
                    });

                    //Default, load for first time
                    swipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            if (getIntent() == null)
                            {
                                loadOrders(Common.currentUser.getPhone());
                            } else {
                                if (getIntent().getStringExtra("userPhone") == null) {
                                    loadOrders(Common.currentUser.getPhone());
                                }else {
                                    loadOrders(getIntent().getStringExtra("userPhone"));
                                }
                            }
                        }
                    });

                } else if (spinner.getSelectedItem().toString().equals("Date Picker")) {

                    showDatePickerDialog();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/


        /*if (getIntent() == null) {
            loadOrders(Common.currentUser.getPhone());
        } else {
            loadOrders(getIntent().getStringExtra("userPhone"));
        }

        */




    /*
     private void showDatePickerDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Select Date");
        alertDialog.setMessage("Please select start date and end date");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_date = inflater.inflate(R.layout.order_date_picker, null);

        edtStartDate = add_date.findViewById(R.id.edtStartDate);
        selectDateStart = add_date.findViewById(R.id.btnStartDate);
        selectDateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(OrderStatus.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                edtStartDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                startDate = String.valueOf(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        edtEndDate = add_date.findViewById(R.id.edtEndDate);
        selectDateEnd = add_date.findViewById(R.id.btnEndDate);
        selectDateEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(OrderStatus.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                edtEndDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                endDate = String.valueOf(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        alertDialog.setView(add_date);

        //alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //view
                swipeRefreshLayout = findViewById(R.id.swipe_layout_order_status);
                swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark,
                        android.R.color.holo_orange_dark,
                        android.R.color.holo_blue_dark);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (getIntent() == null)
                        {
                            loadOrdersDatePicker(Common.currentUser.getPhone(),startDate,endDate);
                        }
                        else
                        {
                            if (getIntent().getStringExtra("userPhone") == null)
                            {
                                loadOrdersDatePicker(Common.currentUser.getPhone(),startDate,endDate);
                            }
                            else
                            {
                                loadOrdersDatePicker(getIntent().getStringExtra("userPhone"),startDate,endDate);
                            }
                        }
                    }
                });

                //Default, load for first time
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        if (getIntent() == null)
                        {
                            loadOrdersDatePicker(Common.currentUser.getPhone(),startDate,endDate);
                        } else {
                            if (getIntent().getStringExtra("userPhone") == null) {
                                loadOrdersDatePicker(Common.currentUser.getPhone(),startDate,endDate);
                            }else {
                                loadOrdersDatePicker(getIntent().getStringExtra("userPhone"),startDate,endDate);
                            }
                        }
                    }
                });


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();


    }



    private void todayOrder(String phone) {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests.orderByChild("phone").equalTo(phone).orderByChild("date").equalTo(Common.date())) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, Request model, int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());
                if(model.getStatus().equals("1")){
                    viewHolder.txtOrderComment.setText(model.getComment());
                }else if(model.getStatus().equals("2")) {
                    viewHolder.txtOrderComment.setText("Your status shipping, product will arrive at very soon!");
                }else if(model.getStatus().equals("3")) {
                    viewHolder.txtOrderComment.setText("Your status Shipped, Thank You!");
                }else {
                    viewHolder.txtOrderComment.setText("Your Comment will reply very soon!");
                }

                viewHolder.setItemCliclListener(new ItemCliclListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Common.currentKey = adapter.getRef(position).getKey();
                        startActivity(new Intent(OrderStatus.this, TrackingOrder.class));
                    }
                });

            }

        };
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

       *//* requests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                size =  dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        totalOrder.setText((int) size);*//*
    }

    private void loadOrdersDatePicker(String phone,String startDate,String endDate) {

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests.child("phone").equalTo(phone).orderByChild("date").startAt(startDate).endAt(endDate).limitToLast(5)) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, Request model, int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());
                if(model.getStatus().equals("1")){
                    viewHolder.txtOrderComment.setText(model.getComment());
                }else if(model.getStatus().equals("2")) {
                    viewHolder.txtOrderComment.setText("Your status shipping, product will arrive at very soon!");
                }else if(model.getStatus().equals("3")) {
                    viewHolder.txtOrderComment.setText("Your status Shipped, Thank You!");
                }else {
                    viewHolder.txtOrderComment.setText("Your Comment will reply very soon!");
                }

                viewHolder.setItemCliclListener(new ItemCliclListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Common.currentKey = adapter.getRef(position).getKey();
                        startActivity(new Intent(OrderStatus.this, TrackingOrder.class));
                    }
                });

            }

        };
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);


    }*/

}





/*
 Query query = requests.orderByChild("phone").equalTo(phone);
        //create option
        FirebaseRecyclerOptions<Request> Options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(query, Request.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(Options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, int position, @NonNull Request model) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());

                viewHolder.setItemCliclListener(new ItemCliclListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        ////later
                    }
                });
            }

            @Override
            public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_layout,parent,false);

                return new OrderViewHolder(itemView);
            }

 */
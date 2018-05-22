package com.develop.windexit.finalproject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.windexit.finalproject.Common.Common;
import com.develop.windexit.finalproject.Model.User;
import com.facebook.FacebookSdk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    // FButton msign,mregister;
    TextView signIn, register;

    MaterialEditText mphone, mpassword;
    CheckBox ckbRemember;

    TextView forgotPassword;
    TextView birthdayTextView;
    Button btnSelectBirthday;

    String birthday;
    int mYear, mMonth, mDay;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference table_user;
    FirebaseDatabase database;

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

        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        //userID = firebaseUser.getUid();
        table_user = database.getReference("User");

        //Facebook Init
        FacebookSdk.sdkInitialize(getApplicationContext());
        printKeyHash();

        //Init paper
        Paper.init(this);
        signIn = findViewById(R.id.btnsign);
        register = findViewById(R.id.btnregister);


        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent i = new Intent(MainActivity.this, Signin.class);
                startActivity(i);*/
                showSignInDialog();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SignUp.class);
                startActivity(i);
            }
        });

        //Check remeber
        String user = Paper.book().read(Common.USER_KEY);
        String pwd = Paper.book().read(Common.PWD_KEY);
        if (user != null && pwd != null) {
            if (!user.isEmpty() && !pwd.isEmpty())
                login(user, pwd);
        }

    }



    private void showSignInDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Sign in");
        dialog.setMessage("Please use phone number to sign in");
        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.sign_in_layout, null);

        mphone = login_layout.findViewById(R.id.phoneSign);
        mpassword = login_layout.findViewById(R.id.passwordSign);
        ckbRemember = (CheckBox) login_layout.findViewById(R.id.ckbRememberSign);

        forgotPassword = login_layout.findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog();
            }
        });

        dialog.setView(login_layout);
        dialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (Common.isConnectedToINternet(getBaseContext())) {
                    //Save user and password
                    if (ckbRemember.isChecked()) {
                        Paper.book().write(Common.USER_KEY, mphone.getText().toString());
                        Paper.book().write(Common.PWD_KEY, mpassword.getText().toString());
                    }

                    final ProgressDialog mdialog = new ProgressDialog(MainActivity.this);
                    mdialog.setMessage("please wait...");
                    mdialog.show();

                  /*  if (firebaseUser != null && !firebaseUser.getUid().isEmpty() && !firebaseUser.getPhoneNumber().isEmpty()){

                        table_user.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child(mphone.getText().toString()).exists()) {

                                    mdialog.dismiss();
                                    User user = dataSnapshot.child(mphone.getText().toString()).getValue(User.class);

                                    user.setPhone(mphone.getText().toString());

                                    if (user.getPassword().equals(mpassword.getText().toString())) {
                                        Intent i = new Intent(MainActivity.this, Home.class);
                                        Common.currentUser = user;
                                        startActivity(i);
                                        finish();
                                    } else {
                                        Toast.makeText(MainActivity.this, "signin error..", Toast.LENGTH_LONG).show();
                                    }

                                } else {
                                    mdialog.dismiss();
                                    Toast.makeText(MainActivity.this, "user not exist..", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {

                    */
                        table_user.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child(mphone.getText().toString()).exists())
                                {

                                    mdialog.dismiss();
                                    User user = dataSnapshot.child(mphone.getText().toString()).getValue(User.class);
                                    user.setPhone(mphone.getText().toString());

                                    if (user.getPassword().equals(mpassword.getText().toString()))
                                    {
                                        Intent i = new Intent(MainActivity.this, Home.class);
                                        Common.currentUser = user;
                                        startActivity(i);
                                        finish();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Wrong password..", Toast.LENGTH_LONG).show();
                                    }

                                } else {
                                    mdialog.dismiss();
                                    Toast.makeText(MainActivity.this, "User not exist. Please Sign up first", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

//}
                } else {
                    Toast.makeText(MainActivity.this, "please check your internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface1, int which) {
                dialogInterface1.dismiss();
            }
        });
        dialog.show();


    }


    private void showForgotPasswordDialog() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Forgot Password");
        dialog.setMessage("Please input phone number and select your birthday!");
        LayoutInflater inflater = LayoutInflater.from(this);
        View forgot_layout = inflater.inflate(R.layout.forgot_password_layout, null);

        final MaterialEditText edtphone = forgot_layout.findViewById(R.id.phoneSign);
        btnSelectBirthday = forgot_layout.findViewById(R.id.btnSelectBirthday);
        birthdayTextView = forgot_layout.findViewById(R.id.birthdayTextView);

        btnSelectBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                birthdayTextView.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                birthday = String.valueOf(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        dialog.setView(forgot_layout);

        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final ProgressDialog mdialog = new ProgressDialog(MainActivity.this);
                mdialog.setMessage("please wait...");
                mdialog.show();

                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(edtphone.getText().toString()).exists())
                        {
                            mdialog.dismiss();
                            User user = dataSnapshot.child(edtphone.getText().toString()).getValue(User.class);
                            user.setPhone(edtphone.getText().toString());

                            if (user.getBirthday().equals(birthday))
                            {
                                Toast.makeText(MainActivity.this, "Your password : "+user.getPassword(), Toast.LENGTH_LONG).show();
                               /* Intent i = new Intent(MainActivity.this, Home.class);
                                Common.currentUser = user;
                                startActivity(i);
                                finish();*/
                            } else {
                                Toast.makeText(MainActivity.this, "Incorrect birthday", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            mdialog.dismiss();
                            Toast.makeText(MainActivity.this, "User not exist. Please sign up first", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface1, int which) {
                dialogInterface1.dismiss();
            }
        });
        dialog.show();


    }
    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.develop.windexit.finalproject", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void login(final String phone, final String pwd) {


      /*  final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = mAuth.getCurrentUser();
        final String userID = firebaseUser.getUid();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        */

        if (Common.isConnectedToINternet(getBaseContext()))
        {
            final ProgressDialog mdialog = new ProgressDialog(MainActivity.this);
            mdialog.setMessage("please wait...");
            mdialog.show();
         /*   if (firebaseUser != null && !firebaseUser.getUid().isEmpty() && !firebaseUser.getPhoneNumber().isEmpty()) {
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(phone).exists()) {
                            mdialog.dismiss();
                            User user = dataSnapshot.child(phone).getValue(User.class);
                            user.setPhone(phone);
                            if (user.getPassword().equals(pwd)) {
                                Intent i = new Intent(MainActivity.this, Home.class);
                                Common.currentUser = user;
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Wrong Password..", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            mdialog.dismiss();
                            Toast.makeText(MainActivity.this, "user not exist..", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {*/
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(phone).exists()) {

                            mdialog.dismiss();
                            User user = dataSnapshot.child(phone).getValue(User.class);
                            user.setPhone(phone);
                            if (user.getPassword().equals(pwd)) {
                                Intent i = new Intent(MainActivity.this, Home.class);
                                Common.currentUser = user;
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Wrong Password..", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            mdialog.dismiss();
                            Toast.makeText(MainActivity.this, "User not exist. Please sign up first!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            //}
        } else {
            Toast.makeText(MainActivity.this, "please check your internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
    }

}










/*


 @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOGIN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            //sucessfully signed in
            if (requestCode == RESULT_OK) {
                if (!FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().isEmpty()) {
                    startActivity(new Intent(this,Signin.class)
                            .putExtra("phonenew", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().isEmpty()));
                    finish();
                    return;
                } else {
                    if (response == null) {
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                        Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                        Toast.makeText(this, "Unknown Error", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Toast.makeText(this, "Unknown Sign In error", Toast.LENGTH_SHORT).show();
            }
        }
    }

*/







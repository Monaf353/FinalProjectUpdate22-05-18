package com.develop.windexit.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rengwuxian.materialedittext.MaterialEditText;

import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignUp extends AppCompatActivity {
    MaterialEditText edtName, edtPassword,edtEmail,edtHomeAddress;

    FButton signUp;
    Button btnSelect,btnUload;
    // TextView textView;
    FirebaseStorage storage;
    StorageReference storageReference;

    Uri saveUri;

    public String temporary;
    public String userid;
    public static int cx, cy;
    private static DatabaseReference mUserDatabase;


    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;

    private static DatabaseReference myRef;

    private int MODE_PRIVATE;

     String userID;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto_Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        startauthentication();

        setContentView(R.layout.activity_sign_up);

     /* toolbar =  findViewById(R.id.sylBack);
        setSupportActionBar(toolbar);*/
        getSupportActionBar().setTitle("Sign Up");
        // toolbar.setTitleTextColor(Color.WHITE);

        //setTitleColor();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


     /*   // edtPhone = findViewById(R.id.edtphone);
        edtName = findViewById(R.id.edtName);
        edtPassword = findViewById(R.id.edtPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtHomeAddress = findViewById(R.id.edtHomeAddress);
        signUp = findViewById(R.id.signUp);

        btnSelect = findViewById(R.id.btnSelect);
        btnUload = findViewById(R.id.btnUpload);

        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Let user select image from and save Uri of this image

            }
        });

        btnUload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();

            }
        });*/

      /*  final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToINternet(getBaseContext()))
                {

                    final ProgressDialog mdialog = new ProgressDialog(SignUp.this);
                    mdialog.setMessage("please wait...");
                    mdialog.show();

                    final FirebaseUser userww = mAuth.getCurrentUser();
                    //check if user exist or not in Database
                    userID = userww.getUid();
                    final String userPhone = userww.getPhoneNumber();

                    table_user.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            mdialog.dismiss();
                            User user = new User();
                            user.setName(edtName.getText().toString());
                            user.setPassword(edtPassword.getText().toString());
                            user.setBalance(String.valueOf(0.0));
                            user.setEmail(edtEmail.getText().toString());
                            user.setHomeAddress(edtHomeAddress.getText().toString());

                            table_user.child(userPhone).setValue(user);
                            Toast.makeText(SignUp.this, "Sign Up successfully", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(SignUp.this, "please check your internet  connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });*/

        SharedPreferences mPreferences;
        mPreferences = SignUp.this.getSharedPreferences("users", MODE_PRIVATE);
        temporary = mPreferences.getString("saveuserid", "");

        if (temporary != null && !temporary.isEmpty())
        {

            mAuth = FirebaseAuth.getInstance();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            myRef = mFirebaseDatabase.getReference();

            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            userid = currentFirebaseUser.getUid();

        } else {

        }

    }
    public void startauthentication() {
        SharedPreferences mPreferences;
        mPreferences = getSharedPreferences("users", MODE_PRIVATE);
        temporary = mPreferences.getString("saveuserid", "");

        if (temporary != null && !temporary.isEmpty()) {
        } else {
            Intent y = new Intent(SignUp.this, PhoneAuthActivity.class);
            y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            y.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(y);

        }
    }

 /*   private void uploadImage() {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading....");
            mDialog.show();
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(SignUp.this, "Uploaded!!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                   User user = new User();
                                    //set value for newCategory if image upload and we can get download link
                                    //newCategory = new Category(edtName.getText().toString(), uri.toString());
                                    user.setImage(uri.toString());

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(SignUp.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded " + progress + "%");
                        }
                    });
        }
    }

    //Press Ctrl+O
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {
            saveUri = data.getData();
            btnSelect.setText("Image Selected !");
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image*//*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }*/


}








  /* if (dataSnapshot.child(userID).exists())
                            {*/


                            /* mdialog.dismiss();
                                Toast.makeText(SignUp.this, "Phone Number already register", Toast.LENGTH_SHORT).show();*/
// }
                           /* else
                                {*/
                                   /* FirebaseUser user = mAuth.getCurrentUser();
                                    //check if user exist or not in Database

                                    userID = user.getUid();

                                    mdialog.dismiss();
                                User user = new User(edtName.getText().toString(), edtPassword.getText().toString());
                                table_user.child(edtPhone.getText().toString()).setValue(user);

                                Toast.makeText(SignUp.this, "Sign Up successfully", Toast.LENGTH_SHORT).show();
                                finish();*/
// }





   /* public void signoutbutton(View s) {
        if (s.getId() == R.id.sign_out) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Do you really want to Log Out ?").setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            SharedPreferences mPreferences;

                            mPreferences = getSharedPreferences("User", MODE_PRIVATE);
                            SharedPreferences.Editor editor = mPreferences.edit();
                            editor.clear();
                            editor.apply();
                            mAuth.signOut();

                            Intent y = new Intent(MainActivity.this, PhoneAuthActivity.class);
                            y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            y.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(y);

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.setTitle("Confirm");
            dialog.show();

        }
    }*/

//setContentView(R.layout.activity_sign_up);

       /* textView = findViewById(R.id.txtPhone);
        if(getIntent()!= null){
            textView.setText(getIntent().getStringExtra("phonenew"));
        }
*/
package com.example.healthmanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Button logout;
    ImageView image;
    String usertype;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );
        mAuth = FirebaseAuth.getInstance();

        logout =(Button ) findViewById ( R.id.logout );
        logout.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                mAuth.signOut ();
                Toast.makeText( MainActivity.this, "Successfully Signed Out", Toast.LENGTH_SHORT ).show();
                startActivity(new Intent (MainActivity.this, authenticate.class));
                finish();
            }
        } );

        image = (ImageView) findViewById ( R.id.setimage );
        Bundle bundle = getIntent().getExtras();
        if (bundle!= null) {
            usertype = bundle.getString("usertype");
        }
        else{
            usertype="Homepage";
        }
        if (usertype.equals ( "Patient" ))
        {
            image.setImageResource ( R.drawable.doctor_location );
        }
        if (usertype.equals ( "Doctor" ))
        {
            image.setImageResource ( R.drawable.listofdoctor );
        }
        if (usertype.equals ( "Pharmacy" ))
        {
            image.setImageResource ( R.drawable.pharmacypage );
        }
        if (usertype.equals ( "Homepage" ))
        {
            image.setImageResource ( R.drawable.homepage );
        }
    }
}
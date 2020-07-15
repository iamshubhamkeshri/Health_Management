package com.example.healthmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class authenticate extends AppCompatActivity {

    private PinView pinView;
    private Button next;
    private TextView topText,textU,textM,Resend;
    private EditText userName, userPhone;
    private String phone,OTP;
    private ConstraintLayout  second;
    private LinearLayout first;

    String countrycode;
    String item;
    NiceSpinner niceSpinner_login;
    private String verificationid;
    private FirebaseAuth mAuth;
    private ProgressBar pb;
    CountryCodePicker ccp;
    private EditText otp;
    private Button verify;
    private static final String TAG = "PhoneAuth";
    private ProgressBar progressBar;
    private int progressStatus = 0,timer=0;
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_authenticate );

        mAuth= FirebaseAuth.getInstance();

        topText = findViewById(R.id.topText);
        pinView = findViewById(R.id.pinView);
        next = findViewById(R.id.button);
        userPhone = findViewById(R.id.userPhone);
        Resend=findViewById(R.id.resend);
        first = findViewById(R.id.first_step);
        second = findViewById(R.id.secondStep);
        textM=findViewById(R.id.textView_shownum);
        textU = findViewById(R.id.textView_noti);
        first.setVisibility(View.VISIBLE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        niceSpinner_login = (NiceSpinner) findViewById(R.id.nice_spinner_login);
        List<String> dataset = new LinkedList<> ( Arrays.asList(" ","Patient", "Doctor", "Pharmacy"));
        niceSpinner_login.attachDataSource(dataset);
        niceSpinner_login.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener () {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                // This example uses String, but your type can be any
                item= (String) parent.getItemAtPosition(position);
                Toast.makeText ( authenticate.this,item, Toast.LENGTH_SHORT ).show ();
            }
        });
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                Toast.makeText(authenticate.this, "Updated " + ccp.getSelectedCountryName (), Toast.LENGTH_SHORT).show();
                countrycode=ccp.getSelectedCountryCode ().toString ().trim ();
            }
        });
        // shubham keshri ...
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (next.getText().equals("Get OTP")) {
                    phone = userPhone.getText().toString();


                    if ( !TextUtils.isEmpty(phone) && phone.length() >=9 &&  item!=null ) {
                        next.setText("Verify");
                        first.setVisibility(View.GONE);
                        second.setVisibility(View.VISIBLE);
                        if(countrycode==null)
                        {
                            countrycode="91";
                        }
                        phone="+"+countrycode+phone;

                        sendVerificationcode(phone);

                        topText.setText("Verify Your Mobile Number");
                        textM.setText(" ("+phone+") " );
                        textM.setTextColor(Color.GREEN);
                    } else if (TextUtils.isEmpty(phone)||phone.length() != 10) {
                        userPhone.setError("Enter Valid Number");
                        userPhone.requestFocus();
                        return;
                    }else if (item==null)
                    {
                        Toast.makeText ( authenticate.this, "Please select User Type", Toast.LENGTH_SHORT ).show ();
                    }

                } else if (next.getText().equals("Verify")) {
                    OTP = pinView.getText().toString();
                    Resend.setTextColor(Color.GRAY);
                    Resend.setEnabled(false);

                    if((OTP.isEmpty()||OTP.length()<6)){
                        textU.setText("X Enter 6 Digit OTP");
                        textU.setTextColor(Color.RED);
                        pinView.setLineColor(Color.RED);
                        return;
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                    next.setEnabled(false);
                    verifycode(OTP);
                } else if(next.getText().equals("Retry")){
                    OTP = pinView.getText().toString();
                    verifycode(OTP);
                }

            }
        });
        Resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationcode(phone);
                progressStatus=0;
                funprogressbar();
                enableresend();
                Resend.setEnabled(false);
                Resend.setTextColor(Color.WHITE);
            }
        });
    }

    protected void onStart(){
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser()!= null){

            Intent j= new Intent(authenticate.this,MainActivity.class);
            j.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(j);
        }
    }

    private void sendVerificationcode(String number)
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mcallBack
        );
    }

    private void verifycode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationid,code);
        signInWithCredential(credential);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mcallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public  void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationid=s;
            Toast.makeText(authenticate.this,"OTP sent successfully",Toast.LENGTH_SHORT).show();
            next.setEnabled(true);
            progressBar.setVisibility( View.VISIBLE);
            funprogressbar();
            enableresend();

        }
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code!=null) {
                verifycode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            //Toast.makeText(verifyotp.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            if (e instanceof FirebaseAuthInvalidCredentialsException){
                Log.d(TAG,"Invalid OTP: "+ e.getLocalizedMessage());
            }else if (e instanceof FirebaseTooManyRequestsException){
                //sms quota Exceeded
                Log.d(TAG,"SMS Quota exceeded.");
            }

        }
    };

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult> () {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            finish();
                            textU.setText(" OTP Verified");
                            textU.setTextColor( Color.GREEN);
                            pinView.setLineColor(Color.GREEN);
                            FirebaseUser user= task.getResult().getUser();
                            Bundle bundle = new Bundle();
                            bundle.putString("usertype",item);
                            Intent intent=new Intent(authenticate.this,MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                //Toast.makeText(verifyotp.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                Toast.makeText(authenticate.this,"Invalid Code",Toast.LENGTH_SHORT).show();
                                textU.setText("OTP you entered is Wrong");
                                pinView.setLineColor(Color.RED);
                                textU.setTextColor( Color.RED);
                                next.setText("Retry");
                                next.setEnabled(true);
                            }

                        }
                    }
                });
    }

    public void funprogressbar() {
        new Thread(new Runnable() {
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
                while (progressStatus < 100) {
                    progressStatus += 1;
                    // Update the progress bar and display the
                    //current value in the text view
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });

                    try {
                        // Sleep for 200 milliseconds.
                        sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }
    public void enableresend(){
        new Handler ().postDelayed( new Runnable() {
            @Override
            public void run() {
                Resend.setTextColor(Color.GREEN);
                Resend.setEnabled(true);
            }
        },20000);
    }
}
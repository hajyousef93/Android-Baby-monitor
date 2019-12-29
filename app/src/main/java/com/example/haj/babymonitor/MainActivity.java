package com.example.haj.babymonitor;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    Button but_alarm;
    ImageView imageView;
    Intent intent_alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int permissions_All=1;
        String [] permissions={Manifest.permission.CALL_PHONE,Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.SEND_SMS,Manifest.permission.READ_SMS,Manifest.permission.RECEIVE_SMS};
        if (!hasPermissions(this,permissions)){
            ActivityCompat.requestPermissions(this,permissions,permissions_All);
        }
        imageView= (ImageView) findViewById(R.id.imageView);
        but_alarm = (Button) findViewById(R.id.but_alarm);
        Animation fadeIn1 = new AlphaAnimation(0, 1);
        fadeIn1.setDuration(1300);
        AnimationSet animation1 = new AnimationSet(true);
        animation1.addAnimation(fadeIn1);
        Animation fadeIn2 = new AlphaAnimation(0, 1);
        fadeIn2.setStartOffset(700);
        fadeIn2.setDuration(1000);
        AnimationSet animation2 = new AnimationSet(true);
        animation2.addAnimation(fadeIn2);
        imageView.setAnimation(animation1);
        but_alarm.setAnimation(animation2);
        but_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent_alarm = new Intent(getApplicationContext(), Main2Activity.class);
                startActivity(intent_alarm);
            }
        });

    }
    public static boolean hasPermissions(Context context, String... permissions){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M &&context!=null &&permissions!=null){
            for (String permission:permissions){
                if (ActivityCompat.checkSelfPermission(context,permission)!= PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }
}

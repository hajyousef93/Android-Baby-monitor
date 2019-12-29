package com.example.haj.babymonitor;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class Main2Activity extends AppCompatActivity {
    private static final int PICK_CONTACT = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACT = 123;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int REQUEST_makeCall_PERMISSION = 124;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

//    private boolean permissionToRecordAccepted = false;
//    private String [] permissions = {Manifest.permission.RECORD_AUDIO,Manifest.permission.CALL_PHONE};

    Button  but_Start;
    ImageButton but_search;
    String phonenumber, message;
    TextView  txt_progress;
    EditText txt_phonenum;
    SeekBar seekBar_sensivity;
    RadioButton select;
    RadioGroup radioGroup;
    int progress = 1000;
    Intent intent_starting;
    Boolean iscall=false,issms=false;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        but_search = (ImageButton) findViewById(R.id.but_search);
        txt_phonenum = (EditText) findViewById(R.id.edit_phonnumber);
        txt_progress = (TextView) findViewById(R.id.txt_progress);
        but_Start = (Button) findViewById(R.id.but_Start);
        radioGroup=(RadioGroup)findViewById(R.id.radiogrouph);
        but_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//               ActivityCompat.requestPermissions(Main2Activity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
           //     ActivityCompat.requestPermissions(Main2Activity.this, permissions, REQUEST_makeCall_PERMISSION);
                if (phonenumber!=null ) {
                    intent_starting = new Intent(getApplicationContext(), Main3Activity.class);
                    intent_starting.putExtra("phonenumber",phonenumber);
                    int selectid=radioGroup.getCheckedRadioButtonId();
                    select=(RadioButton)findViewById(selectid);
                    if(selectid ==R.id.radioBut_call){
                        iscall=true;
                        intent_starting.putExtra("iscall",iscall);
                    }else{
                        issms=true;
                        intent_starting.putExtra("issms",issms);
                    }
                    String sensevity=txt_progress.getText().toString();
                    intent_starting.putExtra("sensevity",sensevity);
                    startActivity(intent_starting);
                }else {
//                    phonenumber=txt_phonenum.getText().toString();
//                    intent_starting = new Intent(getApplicationContext(), Main3Activity.class);
//                    intent_starting.putExtra("phonenumber",phonenumber);
//                    int selectid=radioGroup.getCheckedRadioButtonId();
//                    select=(RadioButton)findViewById(selectid);
//                    if(selectid ==R.id.radioBut_call){
//                        iscall=true;
//                        intent_starting.putExtra("iscall",iscall);
//                    }else{
//                        issms=true;
//                        intent_starting.putExtra("issms",issms);
//                    }
//                    String sensevity=txt_progress.getText().toString();
//                    intent_starting.putExtra("sensevity",sensevity);
//                    startActivity(intent_starting);
                    Toast.makeText(getApplicationContext(), "choose phonenumber " + phonenumber,
                            Toast.LENGTH_LONG).show();
                }

            }
        });
        but_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call_Cantact();

            }
        });
        seekBar_sensivity = (SeekBar) findViewById(R.id.seek_sensivity);
        seekBar_sensivity.setMax(10000);

        seekBar_sensivity.setProgress(progress);
        seekBar_sensivity.incrementProgressBy(50);
        txt_progress.setText("" + progress);
        seekBar_sensivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
               progress =i ;
                txt_progress.setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    public void Call_Cantact() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACT);
            }

        } else {
            Intent intent_Cantact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent_Cantact, PICK_CONTACT);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT)
            if (resultCode == AppCompatActivity.RESULT_OK) {
                Uri contactdata = data.getData();
                Cursor cursor = getContentResolver().query(contactdata, null, null, null, null);
                if (cursor.moveToFirst()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    cursor.close();
                    Cursor phonecursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? and " + ContactsContract.CommonDataKinds.Phone.TYPE + " = " + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, new String[]{id}, null);

                    if (phonecursor.moveToFirst()) {
                        phonenumber = phonecursor.getString(phonecursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    }
                    phonecursor.close();
                    txt_phonenum.setText(name);

                }
            }
    }
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode){
//            case REQUEST_RECORD_AUDIO_PERMISSION:
//                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                break;
//            case REQUEST_makeCall_PERMISSION:
//                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                break;
//        }
//        if (!permissionToRecordAccepted )
//            finish();
//
//    }
//

    //    ////send sms
    protected void sendSMSMessage() {
        message = "hello world";
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        } else {         //already has permission granted
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phonenumber, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent." + phonenumber,
                    Toast.LENGTH_LONG).show();

        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    SmsManager smsManager = SmsManager.getDefault();
//                    smsManager.sendTextMessage(phonenumber, null, message, null, null);
//                    Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
//                    return;
//                }
//            }
//            case MY_PERMISSIONS_REQUEST_READ_CONTACT: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Intent intent_Cantact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//                    startActivityForResult(intent_Cantact, PICK_CONTACT);
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            "Contact faild, please try again.", Toast.LENGTH_LONG).show();
//                    return;
//
//                }
//            }
//            case MY_PERMISSIONS_REQUEST_MAKE_CALL: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Intent callIntent = new Intent(Intent.ACTION_CALL);
//                    callIntent.setData(Uri.parse("tel:" + phonenumber));
//                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                        // TODO: Consider calling
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                        //                                          int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
//                        // for ActivityCompat#requestPermissions for more details.
//                        return;
//                    }
//                    startActivity(callIntent);
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            "make call is erorr, please try again.", Toast.LENGTH_LONG).show();
//                    return;
//                }
//
//            }
//        }
//    }
    /////make call...
//    protected void makeCall()
//    {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.CALL_PHONE)
//                != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.CALL_PHONE)) {
//
//            } else {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.CALL_PHONE},
//                        REQUEST_makeCall_PERMISSION);
//            }
//        }
//        else{
//            Intent callIntent = new Intent(Intent.ACTION_CALL);
//            callIntent.setData(Uri.parse("tel:" + phonenumber));
//            startActivity(callIntent);
//        }
//    }
}




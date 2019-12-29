package com.example.haj.babymonitor;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;

public class Emergency extends AppCompatActivity {
public String phoneEmergency;
    TextView textViewResponse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        textViewResponse= (TextView) findViewById(R.id.textViewResponse);
        Bundle bundle=getIntent().getExtras();
        if(bundle !=null)
        {
            phoneEmergency=bundle.getString("phoneEmergency");
        }
        videoCaptuer();
    }
    private String VideoPath,videopathshow;
    private final String URLSHOW="http://192.168.43.14:8080/VideoUpload/uploads/";//"http://169.254.228.84/VideoUpload/show.php?name="
    private void videoCaptuer()
    {
        Intent intent=new Intent(this,VideoCaptuer2hard.class);
        startActivityForResult(intent,1234);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1234)
        {
            VideoPath= data.getStringExtra("videopath");
            videopathshow=data.getStringExtra("videopathshow");
            // Toast.makeText(Main3Activity.this,videopathshow,Toast.LENGTH_LONG).show();
            uploadVideo();
            sendSMSMessage(phoneEmergency, "Baby is Alarm Click in link to show " + URLSHOW + videopathshow);
            finish();
           // log("Send SMS is Done");

        }
    }

    private void uploadVideo() {
        class UploadVideo extends AsyncTask<Void, Void, String> {

            ProgressDialog uploading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                uploading = ProgressDialog.show(Emergency.this, "Uploading File", "Please wait...", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                uploading.dismiss();
                textViewResponse.setText(Html.fromHtml("<b>Uploaded at <a href='" + s + "'>" + s + "</a></b>"));
                // textViewResponse.setMovementMethod(LinkMovementMethod.getInstance());
            }

            @Override
            protected String doInBackground(Void... params) {
                Upload u = new Upload();
                String msg = u.uploadVideo(VideoPath);///////////////////////
                return msg;
            }
        }
        UploadVideo uv = new UploadVideo();
        uv.execute();
    }
    public void sendSMSMessage(String phone,String Sms) {

        try {

            SmsManager smsManager = SmsManager.getDefault();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }///
            smsManager.sendTextMessage(phone, null, Sms, null, null);
            Toast.makeText(getApplicationContext(), "Send SmS is Done.", Toast.LENGTH_LONG).show();
            //finish();

        }
        catch (Exception e) {
            e.printStackTrace();
//            log("Error in send sms(): " + e.toString());
        }
    }
}

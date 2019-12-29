package com.example.haj.babymonitor;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.ProgressBar;
import java.util.Timer;
import java.util.TimerTask;

public class Main3Activity extends Activity {
    // internal constants
    //  private static final String PREFERENCES_NAME = "babyphonPrefs";
    //   private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int STATUS_NEVER_REACHED = -99; // trigger initial invalidate
    private static final int STATUS_ERROR = -4;
    private static final int STATUS_CALLING_DISABLED = -3;
    private static final int  STATUS_SEND_SMS_DISABLED = -5;
    private static final int STATUS_DISABLED_IN_PATIENCE_PERIOD = -2;
    private static final int STATUS_UNSET = -1;
    private static final int STATUS_GREEN = 0;
    private static final int STATUS_ALARM = 1;

    Handler globalUpdateHandler;
    Handler currentLoudnessUpdateHandler;

    // update frequency
    private static final short UPDATE_INTERVAL_MILLISECONDS = 500;

    // misc globals
    private Timer geteNewMicValueTimer;
    private TimerTask globalScreenUpdateTask;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private Date gracePeriodStart;
    private static  final short PATIENCE_PERIOD_TOTAL_SECONDS =10;//////////////
    private float patiencePeriodSecondsLeft;
    private PowerManager powermanager;
    private PowerManager.WakeLock wakelock;
    private int lastStatus = STATUS_NEVER_REACHED;


    // screen elements
    //  private CheckBox screenDoCallStatus;
    private Button screenStatusIndicator;
    private TextView screenLoudnessCurrent;
    private TextView screenLoudnessLimit;
    private TextView screenLog;
    private  TextView textViewResponse;

    // screen elements' initial content
    //private Boolean doCallStatusBoolean = false;
    private int loudnessCurrentInt = (int) 0;
    private int loudnessLimitInt = (int) 500;
    private Boolean alarmStatusBoolean = false;
    String ph_number,sensevity;
    ProgressBar progressBar;
    boolean iscalling,isSendSms;
    //    private Camera mCamera;
    private static String mCrrentPhotoPath;
    private static String UPLOAD_URL;
    // mic input object

    MicrophoneInput micInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        ///
        Intent intent_result=getIntent();
        ph_number=intent_result.getStringExtra("phonenumber");
        sensevity=intent_result.getStringExtra("sensevity");
        iscalling=intent_result.getExtras().getBoolean("iscall");
        isSendSms=intent_result.getExtras().getBoolean("issms");
        textViewResponse= (TextView) findViewById(R.id.textViewResponse);
////
        progressBar=(ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(Integer.valueOf(sensevity )+200);
        progressBar.setSecondaryProgress(Integer.valueOf(sensevity));
//        mCamera = Camera.open();
//        mCamera.startPreview();
        // get telephone status (detect ended call)
        //  phoneStateListener = new CallEndedListener();
        telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        screenStatusIndicator = (Button) findViewById(R.id.ButtonStatusIndicator);
        screenLoudnessCurrent = (TextView) findViewById(R.id.TextViewLoudnessCurrent);

        screenLoudnessLimit = (TextView) findViewById(R.id.TextViewLoudnessLimit);

        screenStatusIndicator.setText("");
        screenLoudnessLimit.setText(sensevity);


        // handle button "Do Call"
        if (iscalling){
//            log("Calling activated.");
            // wait
            gracePeriodStart = new Date();
        }else if(isSendSms){
//            log("Send Sms activated");
            gracePeriodStart = new Date();
        }
        screenLoudnessLimit.setText(sensevity);
    }
//    private void savePrefs()
//    {
//        // save data using SharedPreferences
//        // (cf. android-sdk-linux_86/docs/guide/topics/data/data-storage.html#pref)
//        //
//        // We need an Editor object to make preference changes.
//        // All objects are from android.context.Context
//        SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putString("phoneNumber",ph_number);
//        editor.putInt("loudnessLimit", Integer.valueOf(sensevity).intValue());
//        editor.putString("log",screenLog.getText().toString());
//        editor.putBoolean("doCallStatus", iscalling);
//        editor.putBoolean("dosendsms",isSendSms);
//        // Commit the edits!
//        editor.commit();
//    }
//       private void restorePrefs() {
//        // Restore preferences
//        SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
//      //  screenPhoneNumber.setText(settings.getString("phoneNumber", "unset"));
//        loudnessLimitInt = settings.getInt("loudnessLimit", 500);
//        screenLog.setText(settings.getString("log", ""));
//        alarmStatusBoolean = settings.getBoolean("alarmStatus", false);
//        lastStatus = STATUS_UNSET;
//         //  iscalling = settings.getBoolean("doCallStatus", false);
//       // screenDoCallStatus.setChecked(doCallStatusBoolean);
//
//        //cut down log
//        String[] logArray = screenLog.getText().toString().split("\\n");
//        String logString = "";
//        if (logArray.length > 20) {
//            for (int i = 0; i < 20; i++) {
//                logString += logArray[i] + "\n";
//            }
//        }
//        screenLog.setText(logString);
//    }

    // perform after app comes to foreground (may be on initial startup)
    @Override
    public void onResume() {
        super.onResume();

        // load settings
        //     restorePrefs();

        // don't sleep too deep
        // SCREEN_DIM_WAKE_LOCK: "Wake lock that ensures that the screen is on (but may be dimmed);
        //                        the keyboard backlight will be allowed to go off."
//       powermanager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//         wakelock = powermanager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
//        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        try {
//            wakelock.acquire();
//            log("Preventing screen off (may dim).");
//        } catch (Exception e) {
//            log("Could not prevent screen sleep. Error: " + e.toString());
//        }

        // (re-)set grace period
        gracePeriodStart = new Date();
        micInput = new MicrophoneInput();
        // timer for screen updates
        geteNewMicValueTimer = new Timer();
        globalScreenUpdateTask = new TimerTask() {
            public void run() {
                globalScreenUpdate();
            }
        };

        loudnessCurrentInt = 0;

        // start global screen update "thread"
        geteNewMicValueTimer.scheduleAtFixedRate(globalScreenUpdateTask, 0,
                UPDATE_INTERVAL_MILLISECONDS);

        // handle screen update ticks
        globalUpdateHandler = new Handler();
        globalUpdateHandler.post(new Runnable() {
            @Override
            public void run() {
                // get recent loudness
                try {
                    loudnessCurrentInt = micInput.getCurrentLoudness();
                } catch (Exception e) {
                    System.out.print(e.toString());
                    loudnessCurrentInt = -10;
                }
                // set loudness text
                screenLoudnessCurrent.setText(String.valueOf(loudnessCurrentInt));
                progressBar.setProgress(loudnessCurrentInt);
                // alarm triggered?
                if (loudnessCurrentInt > Integer.parseInt(sensevity))
                    alarmStatusBoolean = true;
                else
                    alarmStatusBoolean = false;

                // determine current status (lowest reason first

                int currentStatus = STATUS_GREEN; // assume OK first
                if (alarmStatusBoolean) currentStatus = STATUS_ALARM;
                // update patience period value
                if (inPatiencePeriod()) currentStatus = STATUS_DISABLED_IN_PATIENCE_PERIOD;
                //if (!iscalling ) currentStatus = STATUS_CALLING_DISABLED;
                if (loudnessCurrentInt < 0) currentStatus = STATUS_ERROR;

                // update status field on status change or when in patience period
                if ((currentStatus != lastStatus) || (currentStatus == STATUS_DISABLED_IN_PATIENCE_PERIOD)) {
                    // update lastStatus
                    lastStatus = currentStatus;

                    // update status field
                    int newColor = Color.GREEN;
                    String newText = "";
                    switch (currentStatus) {
                        case STATUS_GREEN:
                            newColor = Color.GREEN;
                            newText = "Monitoring. Everything's silent.";
                            break;
                        case STATUS_ALARM:
                            newColor = Color.RED;
                            newText = "Alarm! (Loudness limit exceeded)";
                            break;
                        case STATUS_DISABLED_IN_PATIENCE_PERIOD:
                            newColor = Color.YELLOW;
                            newText = "Calling is suspended for "
                                    + ((int) PATIENCE_PERIOD_TOTAL_SECONDS - (int) patiencePeriodSecondsLeft)
                                    + " more seconds.";
                            break;
                        case STATUS_CALLING_DISABLED:
                            newColor = Color.YELLOW;
                            newText = "Calling not activated.";
                            break;

                        case STATUS_ERROR:
                            newColor = Color.RED;
                            newText = "Error. App malfunctioning. Please restart.";

                    }
                    screenStatusIndicator.getBackground().setColorFilter(new LightingColorFilter(newColor,newColor ));
                    //  screenStatusIndicator.setBackgroundColor(newColor);
                    screenStatusIndicator.setText(newText);
                }

                // handle alarm
                if (alarmStatusBoolean) {
                     if (inPatiencePeriod()){}
////                        log("Alarm ignored (" + loudnessCurrentInt + ")");
////                    else if (!iscalling)
////                        log("Alarm ignored (" + loudnessCurrentInt + ")");
                     else if(iscalling){
//                        log("Alarm triggered (" + loudnessCurrentInt + ")");
                        // call
//                        log("Calling  " +ph_number);
                        //    savePrefs();
                        //   performCall(ph_number);
                        makeCall(ph_number);
                    }
                    else {
//                        log("Alarm triggered (" + loudnessCurrentInt + ")");
//                        log("Sending " +ph_number+" :sms is ");
                        //   savePrefs();
                        videoCaptuer();

                       // Toast.makeText(Main3Activity.this,URLSHOW,Toast.LENGTH_LONG).show();

                    }
                }

                // TODO: check for (valid) phone number
                // && (screenPhoneNumber.getText().toString().matches("[[0-9] ]*"));
                globalUpdateHandler.postDelayed(this, 50);
            }
        });

    }
    // if other app comes to foreground
    @Override
    public void onPause() {
        super.onPause();
        //    savePrefs();
        // disable mic
        micInput.stop();
        // stop wake lock
//        try {
//            wakelock.release();
//        } catch (Exception e) {
//            log("Could not stop wake lock. Error: " + e.toString());
//        }
//        if (mCamera != null) {
//            mCamera.stopPreview();
//            mCamera.release();
//        }
    }

    private void globalScreenUpdate() {
        // call screen update handler
        globalUpdateHandler.sendEmptyMessage(1);
    }

    //    public void performCall(String phoneNumber) {
//        // listen to call's begin and end
//        telephonyManager.listen(phoneStateListener,
//                PhoneStateListener.LISTEN_CALL_STATE);
//
//        // Start the call
//        try {
//            globalScreenUpdateTask.cancel();
//            geteNewMicValueTimer.purge();
//            geteNewMicValueTimer.cancel();
//            loudnessCurrentInt = 0;
//            //SystemClock.sleep(1000);
//            Intent myCall = new Intent(Intent.ACTION_CALL);
//            myCall.setData(Uri.parse("tel:" +phoneNumber));////
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }///
//            startActivity(myCall);
//        } catch (Exception e) {
//            e.printStackTrace();
//            log("Error in performCall(): " + e.toString());
//        }
//    }
    public void sendSMSMessage(String phone,String Sms) {

        try {
            globalScreenUpdateTask.cancel();
            geteNewMicValueTimer.purge();
            geteNewMicValueTimer.cancel();
            loudnessCurrentInt = 0;

            //globalScreenUpdateTask.run();

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


//    private void log(String message) {
//        java.util.Date now = new java.util.Date();
//        screenLog.setText(now.toLocaleString() + " " + message + "\n" + screenLog.getText().toString());
//    }

    // after an alarm was triggered, a "patience period" is started
    // where no alarm can be re-triggered until after that period
    //فترة النتظار
    private boolean inPatiencePeriod() {
        patiencePeriodSecondsLeft = (new Date().getTime() - gracePeriodStart.getTime()) / 1000;
        if (patiencePeriodSecondsLeft < PATIENCE_PERIOD_TOTAL_SECONDS)
            return true;
        else
            return false;
    }

    //    class CallEndedListener extends PhoneStateListener {
//        boolean called = false;
//
//        @Override
//        public void onCallStateChanged(int state, String incomingNumber) {
//            super.onCallStateChanged(state, incomingNumber);
//
//            // run only after call is started
//            if (state == TelephonyManager.CALL_STATE_OFFHOOK)
//                called = true;
//
//            // Call has ended -- now bring the activity back to front
//            if (called && state == TelephonyManager.CALL_STATE_IDLE) {
//                called = false;
//                telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
//                startActivity(new Intent(getApplicationContext(),MainActivity.class));
//            }
//        }
//    }
//    public void uploadMultipart() {
//        //  UPLOAD_URL="http://"+serverIp+":9090/android_upload/insert_image.php";
//        UPLOAD_URL="http://192.168.43.162:9090/android_upload/insert_image.php";
//        String caption="asdf";
//        String path=mCrrentPhotoPath;
//        //Uploading code
//        try {
//            String uploadId = UUID.randomUUID().toString();
//
//            //Creating a multi part request
//
//            new MultipartUploadRequest(this, uploadId, UPLOAD_URL)
//                    .addFileToUpload(path, "image") //Adding file
//                    .addParameter("caption", caption) //Adding text parameter to the request
//                    .setNotificationConfig(new UploadNotificationConfig())
//                    .setMaxRetries(2)
//                    .startUpload(); //Starting the upload
//        } catch (Exception exc) {
//            Toast.makeText(this, exc.getMessage()+"hhh", Toast.LENGTH_SHORT).show();
//        }
//    }
//    private void capture() {
//
//        mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
//
//            @Override
//            public void onPictureTaken(byte[] data, Camera camera) {
//                Toast.makeText(getApplicationContext(), "Picture Taken",
//                        Toast.LENGTH_SHORT).show();
//                log("Picture Taken");
//                Intent intent = new Intent();
//                intent.putExtra("image_arr", data);
//               // Bitmap bmp = BitmapFactory.decodeByteArray(data,0,data.length);
//                try {
//                    craeteImagefile();
//                    Toast.makeText(getApplicationContext(),mCrrentPhotoPath,Toast.LENGTH_LONG).show();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    FileOutputStream fos=new FileOutputStream(mCrrentPhotoPath);
//                    fos.write(data);
//                    fos.close();
//                }
//                catch (java.io.IOException e) {
//                }
//                uploadMultipart();
//               log("path:"+mCrrentPhotoPath);
//                setResult(RESULT_OK, intent);
//                camera.stopPreview();
//                if (camera != null) {
//                    camera.release();
//                    mCamera = null;
//                }
//                // finish();
//            }
//        });
//    }

//    private File craeteImagefile()throws IOException{
//        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String Imagefilename="JPEG_"+timeStamp+"_";
//        File storegDir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        File image= File.createTempFile(Imagefilename,".jpg",storegDir);
//        mCrrentPhotoPath=image.getAbsolutePath();
//        return image;
//    }
    protected void makeCall(String phonenumber)
    {try {
        globalScreenUpdateTask.cancel();
        geteNewMicValueTimer.purge();
        geteNewMicValueTimer.cancel();
        loudnessCurrentInt = 0;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE)) {

            }
        }
        else{
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phonenumber));
            startActivity(callIntent);
            //  finish();
        }
    }
    catch (Exception e) {
        e.printStackTrace();
//        log("Error in send sms(): " + e.toString());
    }
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
            sendSMSMessage(ph_number, "Baby is Alarm Click in link to show " + URLSHOW + videopathshow);
//            log("Send SMS is Done");

        }
    }

    private void uploadVideo() {
        class UploadVideo extends AsyncTask<Void, Void, String> {

            ProgressDialog uploading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                uploading = ProgressDialog.show(Main3Activity.this, "Uploading File", "Please wait...", false, false);
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
}

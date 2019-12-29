package com.example.haj.babymonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * Created by USER on 15/2/2018.
 */

public class ReciveSms extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Bundle pudsBundle=intent.getExtras();
        Object [] pdus=(Object[])pudsBundle.get("pdus");
        SmsMessage message =SmsMessage.createFromPdu((byte [])pdus[0]);
//       Toast.makeText(context,"okokok",Toast.LENGTH_LONG).show();
        String s=message.getMessageBody();
        String kayStart="Start";
        String kayStop="Stop";
        if(s.equals(kayStart))
        {
            String phonenumber=message.getDisplayOriginatingAddress();
            Intent intent1=new Intent(context,Emergency.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.putExtra("phoneEmergency",phonenumber);
            context.startActivity(intent1);
           // Toast.makeText(context, "SMS Recive From" + message.getDisplayOriginatingAddress() +
                   // "\n" + message.getMessageBody(), Toast.LENGTH_LONG).show();
        }
        else if(s.equals(kayStop))
        {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

}

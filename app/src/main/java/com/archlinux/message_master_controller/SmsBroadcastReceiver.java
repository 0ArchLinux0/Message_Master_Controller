package com.archlinux.message_master_controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * A broadcast receiver who listens for incoming SMS
 */

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsBroadcastReceiver";

    private static Context context;
    private static String serviceProviderNumber;
    private static String serviceProviderSmsCondition;

    private Listener listener;

    public SmsBroadcastReceiver(String serviceProviderNumber, String serviceProviderSmsCondition) {
        //this.context = context;
        this.serviceProviderNumber = serviceProviderNumber;
        this.serviceProviderSmsCondition = serviceProviderSmsCondition;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("OnReceive","???????????");
        //if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
        String smsSender = "";
        String smsBody = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                smsSender = smsMessage.getDisplayOriginatingAddress();
                smsBody += smsMessage.getMessageBody();
            }
        } else {
            Bundle smsBundle = intent.getExtras();
            if (smsBundle != null) {
                Object[] pdus = (Object[]) smsBundle.get("pdus");
                if (pdus == null) {
                    // Display some error to the user
                    Log.e(TAG, "SmsBundle had no pdus key");
                    return;
                }
                SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    smsBody += messages[i].getMessageBody();
                }
                smsSender = messages[0].getOriginatingAddress();
                Log.d("called","!!!!!!!!!!!");
            }
        }

        //if (smsSender.equals(serviceProviderNumber) && smsBody.startsWith(serviceProviderSmsCondition)) {
        if(true){
            if (listener != null) {
                listener.onTextReceived(smsSender, smsBody);
            }
        }
        //}
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        void onTextReceived(String smsSender, String smsBody);
    }
}

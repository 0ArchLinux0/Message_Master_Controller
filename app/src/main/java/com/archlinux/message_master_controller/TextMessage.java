package com.archlinux.message_master_controller;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class TextMessage extends Activity {
    private ListView lv;
    private TextView tv;
    private Button b1;
    private EditText et;

    private String transferer;
    private String origSender;
    private String smsBody;
    private ArrayList<String> numberList = null;
    private ArrayAdapter<String> adapter;
    private String messageToSend;
    static SmsManager smsManager = SmsManager.getDefault();

    public void sendSMS(String number, String text) {
        smsManager.sendTextMessage(number, null, text, null, null);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_message);

        Intent intent = getIntent();
        origSender = intent.getStringExtra("origSender");
        transferer = intent.getStringExtra("transferer");

        lv = (ListView)findViewById(R.id.lv);
        tv = (TextView)findViewById(R.id.tv);
        et =  (EditText)findViewById(R.id.et);
        b1 = (Button)findViewById(R.id.b1);

        numberList = new ArrayList<String>();
        adapter =  new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, numberList);
        lv.setAdapter(adapter);

//        class NewRunnable implements Runnable {
//
//            @Override
//            public void run() {
//                while (true) {
//                    sendSMS(origSender, messageToSend);
//
//                    try {
//                        Thread.sleep(1000) ;
//
//                    } catch (Exception e) {
//                        e.printStackTrace() ;
//                    }
//                }
//            }
//        }
//
//        NewRunnable nr = new NewRunnable() ;
//        Thread t = new Thread(nr) ;


        b1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageToSend = et.getText().toString();
                if(messageToSend.length() == 0){
                    et.setText("");
                    return;
                }
                String messageToDB = "[Me]: " + messageToSend;
                String finalMessage;
                MainActivity.db.execSQL("INSERT INTO smsdata (transferer, origSender, smsBody) " +
                        "Values ('" + transferer + "','" + origSender + "','" + messageToDB + "');");
                adapter.add(messageToDB);
                finalMessage = origSender + "@" + messageToSend;
                sendSMS(transferer, finalMessage);
            }
        });

        Cursor c = MainActivity.db.rawQuery("SELECT * FROM smsdata WHERE transferer = '"
                + transferer +"' AND origSender ='" + origSender + "';", null);
        if(c != null){
            if(c.moveToFirst()){
                do{
                    Boolean isNew = true;
                    smsBody = c.getString(c.getColumnIndex("smsBody"));
                    for(String number: numberList){
                        Log.d("smsBody",smsBody);
                        if(number.equals(smsBody)) isNew = false;
                    }
                    if(isNew) adapter.add(smsBody);
                } while(c.moveToNext());
            }
        }




    }
}

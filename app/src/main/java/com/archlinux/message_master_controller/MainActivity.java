package com.archlinux.message_master_controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button b1;
    private Button b2;
    private TextView tv;
    private ListView lv;
    private Context context;
    private ArrayList<String> numberList = null;
    private ArrayAdapter<String> adapter;

    private SmsBroadcastReceiver smsBroadcastReceiver;
    static SQLiteDatabase db;
    private final String dbName = "message";
    private final String regExpIsNumber = "^[0-9+]+$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences( getString(R.string.pref_key), Context.MODE_PRIVATE);
        final SharedPreferences.Editor prefEditor = sharedPref.edit();

        tv = (TextView) findViewById(R.id.tv);
        lv = (ListView) findViewById(R.id.lv);
        b1 = (Button)  findViewById(R.id.button1);
        b2 = (Button)  findViewById(R.id.button2);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS}, 1000);
        try{
            db =this.openOrCreateDatabase(dbName, MODE_PRIVATE,null);
            db.execSQL("CREATE TABLE IF NOT EXISTS smsdata (" +
                    "transferer VARCHAR(20)," +
                    "origSender VARCHAR(20), " +
                    "smsBody VARCHAR(161) );");
           // db.execSQL("DELETE FROM smsdata");
        } catch(SQLException se){
            Log.e("Sql error", se.getMessage());
        }

        numberList = new ArrayList<String>();
        adapter =  new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, numberList);
        lv.setAdapter(adapter);
        updateLvFromDB();

        smsBroadcastReceiver = new SmsBroadcastReceiver("", "");
        registerReceiver(smsBroadcastReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
        smsBroadcastReceiver.setListener(new SmsBroadcastReceiver.Listener() {
            @Override
            public void onTextReceived(String smsSender, String smsBody) {
                //test
                //String origSender = smsBody.substring(0,11);
//                Log.d("origSender", origSender);
//                String checkSymbol = smsBody.substring(11,12);
//                Log.d("checkSymbol", checkSymbol);
//                String bodyText = smsBody.substring(12);
                if(smsBody.length()<13) return;
//                if(smsBody.indexOf("@")!=0) {
//                    String origSender = smsBody.split("@")[0];
//                    String bodyText = smsBody.split("@")[1];
//                }
                //Production
                if(smsBody.indexOf("@")!=-1){
                    String origSender = smsBody.substring(0,smsBody.indexOf("@"));
                    String bodyText = smsBody.substring(smsBody.indexOf("@") + 1);
                    Log.d("smsbody", bodyText);
                    Log.d("transferer", smsSender);
                    if(origSender.matches(regExpIsNumber)){
                        db.execSQL("INSERT INTO smsdata (transferer, origSender, smsBody) " +
                                "Values ('" + smsSender + "','" + origSender + "','" + bodyText + "');");
                        updateLvFromDB();
                    }
                }
            }
        });

        final boolean permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;

        b1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                  //notifyChange();
                  //adapter.add("clicked");         //study about:!!!!!!!!!!!!!!!calling adapter.notifyall() caused object not locked by thread before notify() error
                  //numberList.add("nice");       //wow this works so I'll use this instead of adapter.add
//                db.execSQL("INSERT INTO smsdata (transferer, origSender, smsBody) " +
//                        "Values ('" + "fuckfuck" + "','" + "test" + "','" + "test" + "');");
                updateLvFromDB();
            }
        });

        b2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                  db.close();
                  context.deleteDatabase(dbName);
                  numberList = new ArrayList<String>();
                  adapter =  new ArrayAdapter<String>(context,
                    android.R.layout.simple_list_item_1, numberList);
                  lv.setAdapter(adapter);
                try{
                    db =context.openOrCreateDatabase(dbName, MODE_PRIVATE,null);
                    db.execSQL("CREATE TABLE IF NOT EXISTS smsdata (" +
                            "transferer VARCHAR(20)," +
                            "origSender VARCHAR(20), " +
                            "smsBody VARCHAR(161) );");
                    // db.execSQL("DELETE FROM smsdata");
                } catch(SQLException se){
                    Log.e("Sql error", se.getMessage());
                }
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String transferer = numberList.get(position);

                Toast.makeText(MainActivity.this, numberList.get(position) ,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), SmsBody.class);
                intent.putExtra("transferer", transferer);
                startActivity(intent);
            }
        });
    }
//    public void notifyChange(MainActivity.adapter){
//        ListAdapter.notifyAll();                    // study about:Study about syncronized in Android
//    }

    public void updateLvFromDB(){
        Cursor c = db.rawQuery("SELECT transferer FROM smsdata", null);
        if(c != null){
            if(c.moveToFirst()){
                do{
                    Boolean isNew = true;
                    String phoneNumber = c.getString(c.getColumnIndex("transferer"));
                    for(String number: numberList){
                        if(number.equals(phoneNumber)) isNew = false;
                    }
                    if(isNew) adapter.add(phoneNumber);
                } while(c.moveToNext());
            }
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(smsBroadcastReceiver);
        super.onDestroy();
    }
}

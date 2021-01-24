package com.archlinux.message_master_controller;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class SmsBody extends Activity {

    private ListView lv;
    private TextView tv;
    private Cursor  c;

    private String transferer;
    private String origSender;
    private String smsBody;
    private ArrayList<String> numberList = null;
    private ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sms_body);

        Intent intent = getIntent();
        transferer = intent.getStringExtra("transferer");

        lv = (ListView)findViewById(R.id.lv);
        tv = (TextView)findViewById(R.id.tv);

        tv.setText(transferer);

        numberList = new ArrayList<String>();
        adapter =  new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, numberList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String origSender = numberList.get(position);

                Toast.makeText(SmsBody.this, numberList.get(position) ,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), TextMessage.class);

                intent.putExtra("transferer", transferer);
                intent.putExtra("origSender", origSender);
                startActivity(intent);
            }
        });

        if(MainActivity.db!=null){
            c = MainActivity.db.rawQuery("SELECT * FROM smsdata WHERE transferer = '"
                    + transferer +"'", null);
        }
        if(c != null){
            if(c.moveToFirst()){
                do{
                    Boolean isNew = true;
                    origSender = c.getString(c.getColumnIndex("origSender"));
                    for(String number: numberList){
                        if(number.equals(origSender)) isNew = false;
                    }
                    if(isNew) adapter.add(origSender);
                } while(c.moveToNext());
            }
        }
    }
}

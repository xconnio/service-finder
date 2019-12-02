package org.deskconn.servicefinder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class ServiceListView extends AppCompatActivity {

    private ListView listView;
    private ArrayList<DataModel> models;
    private ServiceAdapter myAdapter;
    private UpdateBroadCastListener broadCastListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        listView = findViewById(R.id.listview);
        broadCastListener = new UpdateBroadCastListener();
        ArrayList<Service> items = (ArrayList<Service>) getIntent().getSerializableExtra("items");
        models = new ArrayList<>();
        myAdapter = new ServiceAdapter(ServiceListView.this, items);
        listView.setAdapter(myAdapter);
        registerReceiver(broadCastListener, new IntentFilter("com.update"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(broadCastListener);
        } catch (IllegalArgumentException e) {

        }

    }

    public class UpdateBroadCastListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Service> items = (ArrayList<Service>) intent.getSerializableExtra("items");
            models = new ArrayList<>();
            myAdapter = new ServiceAdapter(ServiceListView.this, items);
            listView.setAdapter(myAdapter);
        }
    }
}

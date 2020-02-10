package org.deskconn.servicefinder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class ServiceListView extends AppCompatActivity {

    private ListView listView;
    private ServiceAdapter myAdapter;
    private UpdateBroadCastListener broadCastListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        listView = findViewById(R.id.listview);
        broadCastListener = new UpdateBroadCastListener();
        ArrayList<Service> items = (ArrayList<Service>) getIntent().getSerializableExtra("items");
        myAdapter = new ServiceAdapter(ServiceListView.this, items);
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Service mdatamodels = items.get(position);
            System.out.println(mdatamodels.mProperties);
            createDialog(mdatamodels);
        });
        registerReceiver(broadCastListener, new IntentFilter("com.update"));
    }

    private void createDialog(Service service) {
        AlertDialog alertDialog = new AlertDialog.Builder(ServiceListView.this).create();
        alertDialog.setTitle("Device Info");
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ").append(service.getHostName()).append("\n");
        builder.append("IP: ").append(service.getHostIP()).append("\n");
        builder.append("Port: ").append(service.getPort()).append("\n");

        service.mProperties.forEach((key, value) -> {
            String fixedCase = key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase();
            builder.append(fixedCase).append(": ").append(value).append("\n");
        });

        alertDialog.setMessage(builder.toString());
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
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
            myAdapter = new ServiceAdapter(ServiceListView.this, items);
            listView.setAdapter(myAdapter);
        }
    }
}

package org.deskconn.servicefinder;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements ServiceFinder.ServiceListener,
        WiFi.StateListener, AdapterView.OnItemClickListener {

    private ServiceFinder mFinder;
    private WiFi mWiFi;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private ListView listView;
    private Map<String, ArrayList<Service>> listHashMap;
    private TypeAdapter myAdapter;
    private ProgressDialog progressDialog;
    private int selectedPosition = -1;
    private boolean foreground = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWiFi = new WiFi(getApplicationContext());
        builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.exit(0);

            }
        });
        dialog = builder.create();
        listView = findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        listHashMap = new HashMap<>();
        myAdapter = new TypeAdapter(this, listHashMap);
        listView.setAdapter(myAdapter);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        foreground = true;
        selectedPosition = -1;
    }

    @Override
    protected void onPause() {
        super.onPause();
        foreground = false;
    }

    @Override
    public void onFound(String type, Service service) {
        System.out.println("Found: " + type + ": " + service.getHostName());
        ArrayList<Service> services = listHashMap.getOrDefault(type, new ArrayList<>());
        services.add(service);
        progressDialog.dismiss();
        listHashMap.put(type, services);
        if (foreground) {
            myAdapter.notifyDataSetChanged();
        }
        if (selectedPosition != -1) {
            ArrayList<Service> serviceArrayList = listHashMap.get(listHashMap.keySet().toArray()[selectedPosition]);
            Intent intent1 = new Intent("com.update");
            intent1.putExtra("items", serviceArrayList);
            sendBroadcast(intent1);
        }
    }

    @Override
    public void onLost(String type, String name) {
        Log.i("TAG", " on lost");
        ArrayList<Service> services = listHashMap.get(type);
        List<Service> toRemove = new ArrayList<>();
        for (Service service : services) {
            if (service.getHostName().equals(name)) {
                toRemove.add(service);
            }
        }
        for (Service service : toRemove) {
            services.remove(service);
        }
        listHashMap.put(type, services);
        myAdapter.notifyDataSetChanged();
        if (selectedPosition != -1) {
            ArrayList<Service> serviceArrayList = listHashMap.get(listHashMap.keySet().toArray()[selectedPosition]);
            Intent intent1 = new Intent("com.update");
            intent1.putExtra("items", serviceArrayList);
            sendBroadcast(intent1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);
        listHashMap.clear();
        myAdapter.notifyDataSetChanged();
        mFinder = new ServiceFinder(getApplicationContext());
        mFinder.addServiceListener(this);
        mFinder.discoverAll();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(wifiStateReceiver);
            mFinder.removeServiceListener(this);
            mFinder.cleanup();
        } catch (Exception e) {

        }
        super.onDestroy();

    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);

            switch (wifiStateExtra) {
                case WifiManager.WIFI_STATE_ENABLED:
                    dialog.dismiss();
                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    dialog.show();
                    dialog.setCancelable(false);
                    break;
            }
        }
    };

    @Override
    public void onConnect(String ip) {
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedPosition = position;
        ArrayList<Service> services = listHashMap.get(listHashMap.keySet().toArray()[selectedPosition]);
        Intent intent = new Intent(MainActivity.this, ServiceListView.class);
        intent.putExtra("items", services);
        startActivity(intent);
    }
}

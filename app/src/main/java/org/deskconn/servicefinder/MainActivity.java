package org.deskconn.servicefinder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWiFi = new WiFi(getApplicationContext());
        builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
        dialog = builder.create();
        listView = findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        listHashMap = new HashMap<>();
        myAdapter = new TypeAdapter(this, listHashMap);
        listView.setAdapter(myAdapter);
    }

    @Override
    public void onFound(String type, Service service) {
        System.out.println("Found: " + type + ": " + service.getHostName());
        ArrayList<Service> services = listHashMap.getOrDefault(type, new ArrayList<>());
        services.add(service);
        listHashMap.put(type, services);
        myAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLost(String type, String name) {
        System.out.println("Lost: " + type + ": " + name);
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
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
        unregisterReceiver(wifiStateReceiver);
        mFinder.removeServiceListener(this);
        mFinder.cleanup();
    }

    private BroadcastReceiver wifiStateReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);

            switch (wifiStateExtra) {
                case WifiManager.WIFI_STATE_ENABLED:
                    dialog.dismiss();
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    dialog.show();
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
        ArrayList<Service> services = listHashMap.get(listHashMap.keySet().toArray()[position]);
        Intent intent = new Intent(MainActivity.this, ServiceListView.class);
        intent.putExtra("items" , services);
        startActivity(intent);
    }
}

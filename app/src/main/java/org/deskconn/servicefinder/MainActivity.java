package org.deskconn.servicefinder;

import android.app.ProgressDialog;
import android.content.Intent;
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
    private AlertDialog mDialog;
    private ListView mListView;
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
        mWiFi.addStateListener(this);
        mWiFi.trackState();
        mDialog = createDialog();
        mListView = findViewById(R.id.listview);
        mListView.setOnItemClickListener(this);
        listHashMap = new HashMap<>();
        myAdapter = new TypeAdapter(this, listHashMap);
        mListView.setAdapter(myAdapter);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Looking for services...");
        mFinder = new ServiceFinder(getApplicationContext());
        mFinder.addServiceListener(this);
        mFinder.discoverAll();
    }

    private AlertDialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
        builder.setNegativeButton(R.string.ok, (dialog, id) -> finish());
        return builder.create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mWiFi.hasIP()) {
            mDialog.show();
        }
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
            ArrayList<Service> serviceArrayList = listHashMap.get(
                    listHashMap.keySet().toArray()[selectedPosition]);
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
            ArrayList<Service> serviceArrayList = listHashMap.get(
                    listHashMap.keySet().toArray()[selectedPosition]);
            Intent intent1 = new Intent("com.update");
            intent1.putExtra("items", serviceArrayList);
            sendBroadcast(intent1);
        }
    }

    @Override
    protected void onDestroy() {
        mFinder.removeServiceListener(this);
        mFinder.cleanup();
        super.onDestroy();
    }

    @Override
    public void onConnect(String ip) {
        mDialog.dismiss();
    }

    @Override
    public void onDisconnect() {
        mDialog.show();
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

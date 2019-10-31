package org.deskconn.servicefinder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements ServiceFinder.ServiceListener {

    private ServiceFinder mFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFinder = new ServiceFinder(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFinder.addServiceListener(this);
        mFinder.discoverAll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFinder.removeServiceListener(this);
    }

    @Override
    public void onFound(String type, Service service) {
        System.out.println("Found: " + type + ": " + service.getHostName());
        Toast.makeText(this, service.getHostIP(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLost(String type, String name) {
        System.out.println("Lost: " + type + ": " + name);
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
    }
}

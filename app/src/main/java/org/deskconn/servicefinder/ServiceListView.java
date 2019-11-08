package org.deskconn.servicefinder;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;


public class ServiceListView extends Activity {

    private ListView listView;
    private String mItemPosition;
    private ArrayList<DataModel> models;
    private ServiceAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        listView = findViewById(R.id.listview_tatti);
        ArrayList<Service> items = (ArrayList<Service>) getIntent().getSerializableExtra("items");
        models = new ArrayList<>();
        myAdapter = new ServiceAdapter(ServiceListView.this, items);
        listView.setAdapter(myAdapter);


//        for (Service service: items) {
//            items.add(service);
//            System.out.println(service.getHostName());
//
//        }



    }
}

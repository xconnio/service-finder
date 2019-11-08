package org.deskconn.servicefinder;


import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;


public class ServiceAdapter extends ArrayAdapter<DataModel> {


    private Activity mActivity;
    private ViewHolder viewHolder;
    private ArrayList<Service> dataModels;

    public ServiceAdapter(Activity mActivity, ArrayList<Service> dataModels) {
        super(mActivity.getApplicationContext(), R.layout.services_list);
        this.mActivity = mActivity;
        this.dataModels = dataModels;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.services_list, parent,
                    false);
            viewHolder = new ViewHolder();
            viewHolder.hostname = convertView.findViewById(R.id.service_host);
            viewHolder.type = convertView.findViewById(R.id.type);
            convertView.setTag(viewHolder);
            Log.i("TAG", " creating new");
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            Log.i("TAG", " using old one");
        }
        Service mdatamodels = dataModels.get(position);
        viewHolder.hostname.setText(mdatamodels.getHostName());
        viewHolder.type.setText("deskcon " + mdatamodels.getHostIP());

        Animation animation = AnimationUtils.loadAnimation(mActivity.getApplicationContext(), R.anim.fade_in);
        convertView.startAnimation(animation);

        return convertView;
    }

    @Override
    public int getCount() {
        return dataModels.size();
    }

    class ViewHolder {
        TextView hostname;
        TextView type;
    }
}

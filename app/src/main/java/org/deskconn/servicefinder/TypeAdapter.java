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
import java.util.Map;

public class TypeAdapter extends ArrayAdapter<String> {

    private Activity mActivity;
    private ViewHolder viewHolder;
    private Map<String, ArrayList<Service>> dataModels;

    public TypeAdapter(Activity mActivity, Map<String, ArrayList<Service>> dataModels) {
        super(mActivity.getApplicationContext(), R.layout.types_group);
        this.mActivity = mActivity;
        this.dataModels = dataModels;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.types_group, parent,
                    false);
            viewHolder = new ViewHolder();
            viewHolder.totalTypes = convertView.findViewById(R.id.type_total);
            viewHolder.type = convertView.findViewById(R.id.type);
            convertView.setTag(viewHolder);
            Log.i("TAG", " creating new");
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            Log.i("TAG", " using old one");
        }
        viewHolder.totalTypes.setText("devices " + dataModels.get(getItem(position)).size());
        viewHolder.type.setText(getItem(position));

        Animation animation = AnimationUtils.loadAnimation(mActivity.getApplicationContext(), R.anim.slide_left);
        convertView.startAnimation(animation);

        convertView.setFocusable(false);
        return convertView;
    }

    @Override
    public int getCount() {
        return dataModels.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return (String) dataModels.keySet().toArray()[position];
    }

    class ViewHolder{
        TextView type;
        TextView totalTypes;
    }
}

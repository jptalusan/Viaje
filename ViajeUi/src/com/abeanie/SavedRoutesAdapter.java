package com.abeanie;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.viaje.main.R;

public class SavedRoutesAdapter extends BaseAdapter {
    private Context context;

    private List<SavedRoutes> listPhonebook;

    public SavedRoutesAdapter(Context context, List<SavedRoutes> listPhonebook) {
        this.context = context;
        this.listPhonebook = listPhonebook;
    }

    public int getCount() {
        return listPhonebook.size();
    }

    public Object getItem(int position) {
        return listPhonebook.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
    	SavedRoutes entry = listPhonebook.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.saved_list_custom_adapter, null);
        }
        TextView to = (TextView) convertView.findViewById(R.id.toText);
        to.setText(entry.getFrom());

        TextView from = (TextView) convertView.findViewById(R.id.fromText);
        from.setText(entry.getTo());

        TextView cost = (TextView) convertView.findViewById(R.id.costText);
        cost.setText(entry.getCost());
        TextView distance = (TextView) convertView.findViewById(R.id.distanceText);
        distance.setText(entry.getDistance());
        TextView traffic = (TextView) convertView.findViewById(R.id.trafficText);
        traffic.setText(entry.getTraffic());
        return convertView;
    }
}

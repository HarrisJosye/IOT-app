package it.ex.routex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ex on 30/08/16.
 */
public class ServiceSpinnerAdapter extends ArrayAdapter {

    Context context;

    /* Command list used to populate the spinner */
    ArrayList<DeviceService> services;

    public ServiceSpinnerAdapter(Context context, int resource, ArrayList<DeviceService> objects) {
        super(context, resource, objects);
        this.context = context;
        this.services = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View row = inflater.inflate(R.layout.service_spinner_element, parent, false);

        TextView dev = (TextView) row.findViewById(R.id.service_spinner_element_device);
        TextView serv = (TextView) row.findViewById(R.id.service_spinner_element_service);

        dev.setText(services.get(position).getDevice().getName());
        serv.setText(services.get(position).getName());

        return row;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View row = inflater.inflate(R.layout.service_spinner_element, parent, false);

        TextView dev = (TextView) row.findViewById(R.id.service_spinner_element_device);
        TextView serv = (TextView) row.findViewById(R.id.service_spinner_element_service);

        dev.setText(services.get(position).getDevice().getName());
        serv.setText(services.get(position).getName());

        return row;
    }
}

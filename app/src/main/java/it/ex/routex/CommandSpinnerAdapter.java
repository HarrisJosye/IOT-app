package it.ex.routex;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex on 05/07/16.
 */
public class CommandSpinnerAdapter extends ArrayAdapter {

    Context context;

    /* Command list used to populate the spinner */
    ArrayList<TriggerElement> commands;

    public CommandSpinnerAdapter(Context context, int resource, ArrayList<TriggerElement> objects) {
        super(context, resource, objects);
        this.context = context;
        this.commands = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View row = inflater.inflate(R.layout.command_spinner_element, parent, false);

        TextView dev = (TextView) row.findViewById(R.id.command_spinner_element_device);
        TextView serv = (TextView) row.findViewById(R.id.command_spinner_element_service);
        TextView com = (TextView) row.findViewById(R.id.command_spinner_element_command);

        dev.setText(commands.get(position).getDeviceName());
        serv.setText(commands.get(position).getServiceName());
        com.setText(commands.get(position).getCommandName());

        return row;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View row = inflater.inflate(R.layout.command_spinner_element, parent, false);

        TextView dev = (TextView) row.findViewById(R.id.command_spinner_element_device);
        TextView serv = (TextView) row.findViewById(R.id.command_spinner_element_service);
        TextView com = (TextView) row.findViewById(R.id.command_spinner_element_command);

        dev.setText(commands.get(position).getDeviceName());
        serv.setText(commands.get(position).getServiceName());
        com.setText(commands.get(position).getCommandName());

        return row;
    }
}

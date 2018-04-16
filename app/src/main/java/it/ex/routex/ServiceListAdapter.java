package it.ex.routex;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ex on 23/06/16.
 */
public class ServiceListAdapter extends RecyclerView.Adapter {

    ArrayList<DeviceService> services;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nameView;
        TextView typeView;

        public MyViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.list_service_name_text);
            typeView = (TextView) itemView.findViewById(R.id.list_service_type_text);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.replaceFrag(new TabbedServiceFragment(services.get(getPosition())));
                }
            });
        }

    }

    public ServiceListAdapter(ArrayList<DeviceService> s, Context c) {
        services = s;
        context = c;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.service_list_element, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MyViewHolder)holder).nameView.setText(services.get(position).getName());

        /* Switch case, depending on service type */
        if (services.get(position).getType().matches("Status"))
            ((MyViewHolder)holder).typeView.setText(context.getResources().getString(R.string.attuator));

        else if (services.get(position).getType().matches("Text"))
            ((MyViewHolder)holder).typeView.setText(context.getResources().getString(R.string.sensor_text));

        else if (services.get(position).getType().matches("Number"))
            ((MyViewHolder)holder).typeView.setText(context.getResources().getString(R.string.sensor_number));
    }


    @Override
    public int getItemCount() {
        return services.size();
    }
}

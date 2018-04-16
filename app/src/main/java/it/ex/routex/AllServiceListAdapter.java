package it.ex.routex;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ex on 17/08/16.
 */
public class AllServiceListAdapter extends RecyclerView.Adapter {

    /* Devices used to poulate the list */
    ArrayList<DeviceService> services;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView servNameView;
        TextView devNameView;
        TextView lastValueView;
        Handler handler;

        public MyViewHolder(View itemView) {
            super(itemView);
            servNameView = (TextView) itemView.findViewById(R.id.all_service_el_serv_name);
            devNameView = (TextView) itemView.findViewById(R.id.all_service_el_dev_name);
            lastValueView = (TextView) itemView.findViewById(R.id.all_service_el_last_value);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.replaceFrag(new TabbedServiceFragment(services.get(getPosition())));
                }
            });

            /* Setting Handler for Asyncronous Requests */
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch(msg.what) {
                        case RouterManager.LAST_VALUE:
                            try {
                                JSONObject jo = (JSONObject) msg.obj;
                                boolean available = jo.getBoolean(Utility.Available);
                                if (available) {
                                    lastValueView.setText(jo.getString(Utility.Value) + " (" + jo.getString(Utility.Timestamp) + ")");
                                }
                            } catch (JSONException e) {

                            }
                        default:
                            break;
                    }
                }
            };

        }

    }

    public AllServiceListAdapter(ArrayList<DeviceService> s, Context c) {
        services = s;
        context = c;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_service_list_element, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MyViewHolder)holder).servNameView.setText(services.get(position).getName());
        ((MyViewHolder)holder).devNameView.setText(services.get(position).getDevice().getName());

        new RouterManager(context).getLastValue(((MyViewHolder)holder).handler, services.get(position));
    }


    @Override
    public int getItemCount() {
        return services.size();
    }
}

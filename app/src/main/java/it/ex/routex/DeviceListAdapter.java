package it.ex.routex;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ex on 21/06/16.
 */
public class DeviceListAdapter extends RecyclerView.Adapter {

    /* Devices used to poulate the list */
    ArrayList<Device> devices;
    Context context;
    Handler handler;


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nameView;
        TextView alwaysOnView;
        TextView technologyView;

        public MyViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.list_device_name_text);
            alwaysOnView = (TextView) itemView.findViewById(R.id.list_always_on_text);
            technologyView = (TextView) itemView.findViewById(R.id.list_technology_text);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.replaceFrag(new DeviceFragment(devices.get(getPosition())));
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    RelativeLayout linearLayout = new RelativeLayout(v.getContext());

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
                    RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

                    linearLayout.setLayoutParams(params);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());
                    alertDialogBuilder.setMessage(v.getResources().getString(R.string.remove_device));
                    alertDialogBuilder.setView(linearLayout);
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton(v.getResources().getString(R.string.remove),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            Device d = devices.get(getPosition());
                                            new RouterManager(v.getContext()).removeDevice(handler, d);
                                            //MainActivity.triggerFragment.triggerLoadTrigger();
                                        }
                                    })
                            .setNegativeButton(v.getResources().getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    return false;
                }
            });
        }

    }

    public DeviceListAdapter(ArrayList<Device> d, Handler h, Context c) {
        devices = d;
        handler = h;
        context = c;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_list_element, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MyViewHolder)holder).nameView.setText(devices.get(position).getName());
        if (devices.get(position).isAlwaysOn()) ((MyViewHolder)holder).alwaysOnView.setText(context.getResources().getString(R.string.str_true));
        else ((MyViewHolder)holder).alwaysOnView.setText(context.getResources().getString(R.string.str_false));
        ((MyViewHolder)holder).technologyView.setText(devices.get(position).getTechnology());
    }


    @Override
    public int getItemCount() {
        return devices.size();
    }
}

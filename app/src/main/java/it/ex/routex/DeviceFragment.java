package it.ex.routex;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class DeviceFragment extends Fragment {

    Device device;
    TextView nameView;
    TextView alwaysOnView;
    TextView technlogyView;

    ProgressDialog progressDialog;

    Handler handler;

    private ArrayList<DeviceService> services;
    private RecyclerView listView;
    private ServiceListAdapter adapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;

    public DeviceFragment() {}

    @SuppressLint("ValidFragment")
    public DeviceFragment(Device d) {
        this.device = d;
    }

    @Override
    public void onResume() {
        MainActivity.deviceFragment = this;
        MainActivity.showing = MainActivity.DEVICE;
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_device, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameView = (TextView) v.findViewById(R.id.fragment_device_name_text);
        alwaysOnView = (TextView) v.findViewById(R.id.fragment_device_always_on);
        technlogyView = (TextView) v.findViewById(R.id.fragment_device_technology);

        nameView.setText(device.getName());

        if (device.isAlwaysOn()) alwaysOnView.setText(getResources().getString(R.string.str_true));
        else alwaysOnView.setText(getResources().getString(R.string.str_false));

        technlogyView.setText(device.getTechnology());

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case RouterManager.SERVICES_UPDATE:
                        /* Update current service list variable */
                        services = JsonManager.getServ
                         ices((JSONArray)msg.obj);
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        device.setServices(services);
                        updateServices(services);
                        break;

                    case RouterManager.CONNECTION_ERROR:
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        MainActivity.toErrorFrag((String)msg.obj);
                        break;
                    default:
                        break;
                }
            }
        };

        /* Setting Dialog Info */
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.getting_services));

        /* Init device service list */
        listView = (RecyclerView) v.findViewById(R.id.device_services_rec_view);
        recyclerLayoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(recyclerLayoutManager);

        /* Update Service List View */
        if (services != null) updateServices(services);

        /* Get Available Service of Selected Device */
        if (services == null) triggerLoadServices();

        return v;
    }

    /* Get Available Service of Selected Device */
    public void triggerLoadServices() {
        if (progressDialog != null && !progressDialog.isShowing()) progressDialog.show();
        RouterManager rm = new RouterManager(getActivity());
        rm.getDeviceInfo(handler, device);
    }

    /* Update Service List View */
    public void updateServices(ArrayList<DeviceService> s) {
        services = (ArrayList<DeviceService>) s.clone();

        adapter = new ServiceListAdapter(services, getActivity());
        listView.setAdapter(adapter);
     }

}

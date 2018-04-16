package it.ex.routex;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.TestMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by ex on 21/06/16.
 */
public class DeviceListFragment extends Fragment {

    ArrayList<Device> availableDevices;
    Handler handler;
    ProgressDialog progressDialog;

    public DeviceListFragment() {}

    private RecyclerView listView;
    private DeviceListAdapter adapter;
    private ArrayList<Device> devices;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    private TextView status;
    private RouterInfo lastRouterInfo;

    @Override
    public void onResume() {
        MainActivity.showing = MainActivity.DEVICE_LIST;
        MainActivity.deviceListFragment = this;
        super.onResume();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_device_list, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        /* Setting Handler for Asyncronous Requests */
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case RouterManager.DEVICES_UPDATE:
                        /* Update current device list variable */
                        availableDevices = JsonManager.getDevices((JSONArray)msg.obj);
                        Log.w("SHOW", "DISMISS");

                        /* Update All Services */
                        AllServiceListFragment.services = new ArrayList<DeviceService>();
                        for (Device device : availableDevices) {
                            for (DeviceService service : device.getServices()) {
                                AllServiceListFragment.services.add(service);
                            }
                        }
                        Log.w("ServiceLen Outside", AllServiceListFragment.services.size()+"");
                        AllServiceListFragment.notifyServices(getActivity());

                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        update(availableDevices);
                        break;
                    case RouterManager.DEVICE_DELETED:
                        boolean successfulDeleted = (boolean)msg.obj;
                        if (successfulDeleted) {
                            Toast.makeText(getContext(), getResources().getString(R.string.device_removed), Toast.LENGTH_SHORT).show();
                            triggerLoadDevices();
                        }
                        break;
                    case RouterManager.ROUTER_STATUS_UPDATE:
                        RouterInfo ri = (RouterInfo)msg.obj;
                        updateRouterInfo(ri);
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
        progressDialog.setMessage(getResources().getString(R.string.getting_devices));

        /* Update Service List View */
        if (devices != null) update(devices);

        /* Get Available Service of Selected Device */
        triggerLoadDevices();

        listView = (RecyclerView) v.findViewById(R.id.device_list_rec_view);
        status = (TextView) v.findViewById(R.id.router_status_text);

        recyclerLayoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(recyclerLayoutManager);

        if (devices != null) update(devices);
        if (lastRouterInfo != null) updateRouterInfo(lastRouterInfo);

        return v;
    }

    /* Get Available Devices from Router */
    protected void triggerLoadDevices() {

        /* Setting Dialog Info */
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.getting_devices));

        if (progressDialog != null && !progressDialog.isShowing()) progressDialog.show();
        RouterManager rm = new RouterManager(getActivity());
        rm.getDevices(handler);
    }

    public void update(ArrayList<Device> d) {
        devices = d;

        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
        adapter = new DeviceListAdapter(devices, handler, getContext());
        listView.setAdapter(adapter);
    }

    public void updateRouterInfo(RouterInfo info) {
        lastRouterInfo = info;
        status.setText(info.getStatusMessage());
    }
}

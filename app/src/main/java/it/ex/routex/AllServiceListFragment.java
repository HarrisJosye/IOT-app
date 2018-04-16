package it.ex.routex;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.formatter.DefaultValueFormatter;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by ex on 17/08/16.
 */
public class AllServiceListFragment extends Fragment {

    public AllServiceListFragment() {
        services = new ArrayList<DeviceService>();
    };

    static RecyclerView recyclerView;
    private static AllServiceListAdapter adapter;
    public static ArrayList<DeviceService> services;
    private static RecyclerView.LayoutManager recyclerLayoutManager;

    FloatingActionButton floatingActionButton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_all_service_list, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        floatingActionButton = (FloatingActionButton) v.findViewById(R.id.graph_fab);

        /*services = new ArrayList<DeviceService>();
        DeviceService ds = new DeviceService("Paolo", "S");
        ds.assignToDevice(new Device("Luciano", "ZigBee", true));
        services.add(ds);*/

        recyclerView = (RecyclerView) v.findViewById(R.id.all_service_list_rec_view);
        recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setAdapter(new AllServiceListAdapter(services, getActivity()));

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.replaceFrag(new AllServiceGraph(services));
            }
        });

        return v;
    }

    public static void notifyServices(Context c) {
        recyclerView.setAdapter(new AllServiceListAdapter(services, c));
        Log.w("ServiceLen", services.size()+"");
    }
}

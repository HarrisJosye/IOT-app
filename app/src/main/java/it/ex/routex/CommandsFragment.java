package it.ex.routex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ex on 23/06/16.
 */
public class CommandsFragment extends Fragment implements TabbedServiceFragment.TabFragmentInterface {

    protected DeviceService service;

    TextView serviceName;
    TextView statusText;
    TextView thingSpeakStatus;

    boolean activeThingSpeak = false;

    Button thingSpeakButton;

    Handler handler;

    public CommandsFragment() {

    }

    @SuppressLint("ValidFragment")
    public CommandsFragment(DeviceService service) {
        this.service = service;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = (RelativeLayout) inflater.inflate(R.layout.fragment_commands, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        serviceName = (TextView) v.findViewById(R.id.fragment_service_name);
        serviceName.setText(service.getName());

        statusText = (TextView) v.findViewById(R.id.request_status);

        thingSpeakStatus = (TextView) v.findViewById(R.id.thingspeak_status);

        LinearLayout ll = (LinearLayout) v.findViewById(R.id.ser_lin_layout);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RouterManager.EXECUTE_RESULT:
                        Log.w("Command executed", ((JSONObject) msg.obj).toString());

                        String result = null;
                        try {
                            JSONObject jo = (JSONObject) msg.obj;
                            result = jo.getString(Utility.Result);
                            Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (result != null) statusText.setText(result.toUpperCase());
                        break;

                    case RouterManager.LAST_VALUE:
                        try {
                            JSONObject jo = (JSONObject) msg.obj;
                            boolean available = jo.getBoolean(Utility.Available);
                            if (available) {
                                statusText.setText(jo.getString(Utility.Value) + " (" + jo.getString(Utility.Timestamp) + ")");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                    case RouterManager.THING_SPEAK:
                        String res = (String) msg.obj;
                        if (res.matches("ON")) {
                            thingSpeakStatus.setText(getResources().getString(R.string.on));
                            activeThingSpeak = true;
                            thingSpeakButton.setText(getResources().getString(R.string.disable));
                        }
                        if (res.matches("OFF")) {
                            thingSpeakStatus.setText(getResources().getString(R.string.off));
                            activeThingSpeak = false;
                            thingSpeakButton.setText(getResources().getString(R.string.enable));
                        }
                        else {
                            /* Else */
                        }

                    case RouterManager.ROUTER_UNAVAILABLE:
                        break;

                    default:
                        break;
                }
            }
        };

        /* For each command of this service add an element to the LinearLayout */
        for (int i = 0; i < service.getCommands().size(); i++) {
            final Command c = service.getCommands().get(i);

            /* Command with no argument, so create a Button */
            if (c.getType().matches("Button")) {
                Button b = new Button(getActivity());
                b.setText(c.getName());
                b.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#46b2f5")));
                b.setTextColor(Color.parseColor("#ffffff"));

                /* Launch corresponding action when clicked */
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new RouterManager(getActivity()).
                                executeCommand(handler, service.getDevice(), service, c.getName(), null);
                    }
                });
                ll.addView(b);
            }

            /* Command with an argument, create Button and EditText */
            else {
                LayoutInflater newinflater =
                        (LayoutInflater)getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                View view = inflater.inflate( R.layout.button_and_edittext, null );

                Button b;
                b = (Button) view.findViewById(R.id.baet_button);
                b.setText(c.getName());
                b.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#46b2f5")));
                b.setTextColor(Color.parseColor("#ffffff"));

                final EditText e = (EditText) view.findViewById(R.id.baet_edittext);

                RelativeLayout rl2 = (RelativeLayout) view.findViewById(R.id.button_and_edittext);

                /* Launch corresponding action when clicked */
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new RouterManager(getActivity()).
                                executeCommand(handler, service.getDevice(), service, c.getName(), e.getText().toString());
                    }
                });
                ll.addView(rl2);
            }
        }

        thingSpeakButton = (Button) v.findViewById(R.id.button_thingspeak);
        thingSpeakButton.setText(getResources().getString(R.string.enable));
        thingSpeakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeThingSpeak) new RouterManager(getActivity()).setThingSpeak(handler, service, "OFF");
                else new RouterManager(getActivity()).setThingSpeak(handler, service, "ON");
            }
        });

        new RouterManager(getActivity()).getLastValue(handler, service);
        new RouterManager(getActivity()).setThingSpeak(handler, service, "GET");

        return v;
    }


    @Override
    /* Called when this fragment is shown */
    public void fragmentBecameVisible() {
        MainActivity.showing = MainActivity.NONE;
    }
}

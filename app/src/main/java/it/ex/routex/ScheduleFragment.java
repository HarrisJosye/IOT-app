package it.ex.routex;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ex on 03/07/16.
 */
public class ScheduleFragment extends Fragment implements TabbedServiceFragment.TabFragmentInterface {

    DeviceService service;
    ArrayList<Command> commands;
    FloatingActionButton fab;
    Handler handler;
    ProgressDialog progressDialog;

    private ArrayList<ScheduleElement> scheduleElements;
    private RecyclerView listView;
    private ScheduleListAdapter adapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;

    public ScheduleFragment() {
    }

    @SuppressLint("ValidFragment")
    public ScheduleFragment(DeviceService s) {
        service = s;
    }


    @Override
    public void onResume() {
        MainActivity.scheduleFragment = this;
        MainActivity.showing = MainActivity.SCHEDULE;
        super.onResume();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule, container, false);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case RouterManager.SCHEDULE_COMMAND_RESULT:
                        /* Once the command is executed, reload schedule */

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                triggerLoadSchedule();
                            }
                        }, 500);

                        break;

                    case RouterManager.SCHEDULE_UPDATE:
                        Log.w("SchedUpdate", ((JSONArray)msg.obj).toString());
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        scheduleElements = JsonManager.getSchedule((JSONArray)msg.obj);
                        updateSchedule(scheduleElements);
                        break;

                    case RouterManager.CONNECTION_ERROR:
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        break;
                }
            }
        };

        commands = service.getCommands();

        /* On click create dialog to add new schedule element */
        fab = (FloatingActionButton) v.findViewById(R.id.schedule_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater newinflater =
                        (LayoutInflater)getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

                View dialogView = newinflater.inflate(R.layout.dialog_timer, null);

                final NumberPicker hPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_frequency_hours);
                final NumberPicker mPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_frequency_minutes);
                final NumberPicker sPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_frequency_seconds);
                final NumberPicker timehPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_time_hours);
                final NumberPicker timemPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_time_minutes);


                /* To Hide and Show */
                final RadioButton freqRadioButton = (RadioButton) dialogView.findViewById(R.id.freq_radio_button);
                final RadioButton timeRadioButton = (RadioButton) dialogView.findViewById(R.id.time_radio_button);
                final TextView freqTitle = (TextView) dialogView.findViewById(R.id.dialog_frequency_title);
                final TextView timeTitle = (TextView) dialogView.findViewById(R.id.dialog_time_title);
                final LinearLayout freqLinLay1 = (LinearLayout) dialogView.findViewById(R.id.dialog_lin_lay_freq_1);
                final LinearLayout freqLinLay2 = (LinearLayout) dialogView.findViewById(R.id.dialog_lin_lay_freq_2);
                final LinearLayout timeLinLay1 = (LinearLayout) dialogView.findViewById(R.id.dialog_lin_lay_time_1);
                final LinearLayout timeLinLay2 = (LinearLayout) dialogView.findViewById(R.id.dialog_lin_lay_time_2);

                freqRadioButton.setChecked(true);
                freqRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            freqLinLay1.setVisibility(View.VISIBLE);
                            freqLinLay2.setVisibility(View.VISIBLE);
                            freqTitle.setVisibility(View.VISIBLE);
                            timeLinLay1.setVisibility(View.GONE);
                            timeLinLay2.setVisibility(View.GONE);
                            timeTitle.setVisibility(View.GONE);
                        }
                        else {
                            freqLinLay1.setVisibility(View.GONE);
                            freqLinLay2.setVisibility(View.GONE);
                            freqTitle.setVisibility(View.GONE);
                            timeLinLay1.setVisibility(View.VISIBLE);
                            timeLinLay2.setVisibility(View.VISIBLE);
                            timeTitle.setVisibility(View.VISIBLE);
                        }
                    }
                });

                final Spinner commandSpinner = (Spinner) dialogView.findViewById(R.id.dialog_schedule_command_spinner);
                final EditText argumentEdit = (EditText) dialogView.findViewById(R.id.dialog_schedule_argument_edit);

                final ArrayList<String> commandNames = new ArrayList<String>();

                /* Add each command to the spinner list */
                for (int i = 0; i < commands.size(); i++) {
                        commandNames.add(commands.get(i).getName());
                }

                if (commands.size() > 0)
                    if (commands.get(0).getType().matches("Button"))
                        argumentEdit.setEnabled(false);

                commandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String comType = commands.get(position).getType();
                        if (comType.matches("Button"))
                            argumentEdit.setEnabled(false);
                        else
                            argumentEdit.setEnabled(true);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, commandNames);
                commandSpinner.setAdapter(dataAdapter);

                hPicker.setMaxValue(24);
                hPicker.setMinValue(0);
                hPicker.setValue(0);

                mPicker.setMaxValue(59);
                mPicker.setMinValue(0);
                mPicker.setValue(0);

                sPicker.setMaxValue(59);
                sPicker.setMinValue(0);
                sPicker.setValue(30);

                timehPicker.setMaxValue(23);
                timehPicker.setMinValue(0);
                timehPicker.setValue(10);

                timemPicker.setMaxValue(59);
                timemPicker.setMinValue(0);
                timemPicker.setValue(30);

                dialogBuilder.setView(dialogView);
                dialogBuilder.setTitle(getResources().getString(R.string.add_schedule_element));
                dialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        if (scheduleElements == null) return;
                                        for (int i = 0; i < scheduleElements.size(); i++) {
                                            if (scheduleElements.get(i).commandName.matches(commandSpinner.getSelectedItem().toString())) {
                                                Toast.makeText(getActivity(), getResources().getString(R.string.command_already_scheduled), Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }

                                        int timeInSec = hPicker.getValue() * 3600 + mPicker.getValue() * 60 + sPicker.getValue();
                                        String argument;
                                        if (argumentEdit.isEnabled()) {
                                            argument = argumentEdit.getText().toString();
                                            if (argument == null || argument.matches("")) {
                                                Toast.makeText(getActivity(), getResources().getString(R.string.value_cant_be_empty), Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }
                                        else
                                            argument = null;

                                        /* Add request */
                                        if (freqRadioButton.isChecked()) {
                                            new RouterManager(getActivity()).addScheduleElement(handler, service, commandSpinner.getSelectedItem().toString(),
                                                    'F', timeInSec, null, argument);
                                        }
                                        else {
                                            int h = timehPicker.getValue();
                                            int m = timemPicker.getValue();
                                            String strTime;

                                            if (h <= 9) strTime = "0" + h;
                                            else strTime = "" + h;
                                            strTime = strTime + ":";
                                            if (m <= 9) strTime = strTime + "0" + m;
                                            else strTime = strTime + m;

                                            new RouterManager(getActivity()).addScheduleElement(handler, service, commandSpinner.getSelectedItem().toString(),
                                                    'T', 0, strTime, argument);
                                        }
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog dialog = dialogBuilder.create();

                LayoutInflater inflater = getActivity().getLayoutInflater();

                dialog.show();
            }
        });

        /* Recycler View */
        listView = (RecyclerView) v.findViewById(R.id.schedule_rec_view);
        recyclerLayoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(recyclerLayoutManager);

        /* Setting Dialog Info */
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.getting_schedule));

        triggerLoadSchedule();

        return v;
    }

    /* Get Available Service of Selected Device */
    public void triggerLoadSchedule() {
        /* Show Dialog */
        if (progressDialog != null && !progressDialog.isShowing()) progressDialog.show();

        RouterManager rm = new RouterManager(getActivity());
        rm.getServiceSchedule(handler, service);
    }


    public void updateSchedule(ArrayList<ScheduleElement> s) {
        scheduleElements = (ArrayList<ScheduleElement>) s.clone();

        adapter = new ScheduleListAdapter(scheduleElements, service, handler, getActivity());
        listView.setAdapter(adapter);
    }

    @Override
    /* Called when this fragment is shown */
    public void fragmentBecameVisible() {
        MainActivity.scheduleFragment = this;
        MainActivity.showing = MainActivity.SCHEDULE;
    }
}

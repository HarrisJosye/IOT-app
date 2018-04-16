package it.ex.routex;

import android.annotation.SuppressLint;
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
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;

/**
 * Created by ex on 05/07/16.
 */
public class TriggerFragment extends Fragment implements TabbedServiceFragment.TabFragmentInterface {

    FloatingActionButton fab;
    Handler handler;
    ProgressDialog progressDialog;
    ArrayList<TriggerCondition> triggerConditions;

    private ArrayList<TriggerElement> availableCommands;

    private ArrayList<TriggerElement> triggerElements;
    private RecyclerView listView;
    private TriggerListAdapter adapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;

    public TriggerFragment() {}

    String type;
    String argument;
    String textContent;

    /*@SuppressLint("ValidFragment")
    public TriggerFragment(DeviceService s) {
        service = s;
    }*/

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_trigger, container, false);

        availableCommands = new ArrayList<TriggerElement>();

        fab = (FloatingActionButton) v.findViewById(R.id.trigger_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater newinflater =
                        (LayoutInflater)getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

                View dialogView = newinflater.inflate(R.layout.dialog_trigger_then, null);

                final RadioButton radioCommand = (RadioButton) dialogView.findViewById(R.id.dialog_radio_command);
                final RadioButton radioMail = (RadioButton) dialogView.findViewById(R.id.dialog_radio_mail);
                final RadioButton radioNotification = (RadioButton) dialogView.findViewById(R.id.dialog_radio_notification);

                final EditText argumentEdit = (EditText) dialogView.findViewById(R.id.dialog_trigger_argument_edit);
                final EditText textContentEdit = (EditText) dialogView.findViewById(R.id.dialog_trigger_textcontent_edit);
                textContentEdit.setVisibility(View.GONE);

                final Spinner comSpinner = (Spinner) dialogView.findViewById(R.id.dialog_trigger_command_spinner);
                comSpinner.setAdapter(new CommandSpinnerAdapter(getActivity(), 0, availableCommands));

                radioCommand.setChecked(true);
                radioCommand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            comSpinner.setVisibility(View.VISIBLE);
                            argumentEdit.setVisibility(View.VISIBLE);
                            comSpinner.setEnabled(true);
                            if (!((TriggerElement)comSpinner.getSelectedItem()).getCommandType().matches("Button"))
                                argumentEdit.setEnabled(true);
                            textContentEdit.setVisibility(View.GONE);
                        }

                        else {
                            comSpinner.setVisibility(View.GONE);
                            argumentEdit.setVisibility(View.GONE);
                            comSpinner.setEnabled(false);
                            argumentEdit.setEnabled(false);
                            textContentEdit.setVisibility(View.VISIBLE);
                        }
                    }
                });

                comSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (availableCommands.get(position).getCommandType().matches("Button"))
                            argumentEdit.setEnabled(false);
                        else
                            argumentEdit.setEnabled(true);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                dialogBuilder.setView(dialogView);
                dialogBuilder.setTitle(getResources().getString(R.string.set_action));
                dialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        String condition = new String();

                                        /*if (conditionSpinner.getSelectedItem().toString().matches((getResources().getString(R.string.cond_greater))))
                                            condition = "greater";
                                        else if (conditionSpinner.getSelectedItem().toString().matches((getResources().getString(R.string.cond_equal))))
                                            condition = "equal";
                                        else if (conditionSpinner.getSelectedItem().toString().matches((getResources().getString(R.string.cond_less))))
                                            condition = "less";
                                        else if (conditionSpinner.getSelectedItem().toString().matches((getResources().getString(R.string.cond_matches))))
                                            condition = "matches";
                                        else if (conditionSpinner.getSelectedItem().toString().matches((getResources().getString(R.string.cond_contains))))
                                            condition = "contains";*/

                                        type = "";
                                        if (radioCommand.isChecked()) type = TriggerElement.COMMAND;
                                        else if (radioMail.isChecked()) type = TriggerElement.MAIL;
                                        else if (radioNotification.isChecked()) type = TriggerElement.NOTIFICATION;

                                        Log.w("Action Type", type);

                                        argument = null;
                                        if (argumentEdit.isEnabled()) {
                                            Log.w("Argument", "Command with argument");
                                            if (!argumentEdit.getText().toString().equals(""))
                                                argument = argumentEdit.getText().toString();

                                            else {
                                                Toast.makeText(getActivity(), getResources().getString(R.string.value_cant_be_empty), Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }

                                        if (!radioCommand.isChecked()) {
                                            /* Mail or Notification */
                                            textContent = textContentEdit.getText().toString();
                                            if (textContent == null || textContent.matches("")) {
                                                Toast.makeText(getActivity(), getResources().getString(R.string.value_cant_be_empty), Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }

                                        dialog.dismiss();

                                        /* Second Dialog */
                                        LayoutInflater newinflater2 =
                                                (LayoutInflater)getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                                        AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(getActivity());

                                        View dialogView = newinflater2.inflate(R.layout.dialog_trigger_if, null);

                                        RecyclerView recyclerView = (RecyclerView) dialogView.findViewById(R.id.trigger_dialog_rec_view);
                                        triggerConditions = new ArrayList<TriggerCondition>();
                                        triggerConditions.add(new TriggerCondition("", "", "", ""));
                                        RecyclerView.LayoutManager recyclerLayoutManagerDialog = new LinearLayoutManager(getActivity());
                                        recyclerView.setLayoutManager(recyclerLayoutManagerDialog);
                                        final TriggerDialogListAdapter dialogListAdapter = new TriggerDialogListAdapter(triggerConditions, getActivity());
                                        recyclerView.setAdapter(dialogListAdapter);

                                        dialogBuilder2.setView(dialogView)
                                                    .setTitle(getResources().getString(R.string.set_conditions))
                                                    .setCancelable(false)
                                                    .setPositiveButton(getResources().getString(R.string.ok),
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                for (TriggerCondition cnd: triggerConditions) {
                                                                    if (cnd.value == null || cnd.value.matches("")) {
                                                                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.value_cant_be_empty), Toast.LENGTH_SHORT).show();
                                                                        return;
                                                                    }
                                                                }

                                                                if (type == TriggerElement.COMMAND)
                                                                    new RouterManager(getContext()).addTriggerElement(handler, triggerConditions, (TriggerElement)comSpinner.getSelectedItem(), argument, type);

                                                                else
                                                                    new RouterManager(getContext()).addTriggerElement(handler, triggerConditions, (TriggerElement)comSpinner.getSelectedItem(), textContent, type);

                                                            }
                                                        })
                                                    .setNeutralButton(getResources().getString(R.string.add) , null)
                                                    .setNegativeButton(getResources().getString(R.string.cancel),
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                            }
                                                        });

                                        final AlertDialog dialog2 = dialogBuilder2.create();

                                        //LayoutInflater inflater = getActivity().getLayoutInflater();
                                        dialog2.setOnShowListener(new DialogInterface.OnShowListener() {

                                            @Override
                                            public void onShow(DialogInterface dialog) {

                                                Button b = dialog2.getButton(AlertDialog.BUTTON_NEUTRAL);
                                                b.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        triggerConditions.add(new TriggerCondition("", "", "", ""));
                                                        dialogListAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                            }
                                        });

                                        dialog2.show();

                                        dialog2.getWindow().clearFlags(
                                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                                        |WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                                        dialog2.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case RouterManager.EXECUTE_RESULT:

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                triggerLoadTrigger();
                            }
                        }, 500);

                        break;


                    case RouterManager.AVAILABLE_COMMAND_RESULT:
                        JSONArray content = (JSONArray)msg.obj;
                        Log.w("SchedComResult", content.toString());

                        availableCommands = JsonManager.getAvailableCommands(content);
                        Log.w("Comms", availableCommands.toString());

                        break;

                    case RouterManager.TRIGGER_UPDATE:
                        Log.w("Trigger Update", ((JSONArray)msg.obj).toString());
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        triggerElements = JsonManager.getTriggers((JSONArray)msg.obj);
                        updateTrigger(triggerElements);
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

        /* Recycler View */
        listView = (RecyclerView) v.findViewById(R.id.trigger_rec_view);
        recyclerLayoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(recyclerLayoutManager);

        /* Setting Dialog Info */
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.getting_triggers));

        new RouterManager(getActivity()).getTriggerCommands(handler);
        new RouterManager(getActivity()).getTriggers(handler);

        return v;
    }

    public void triggerLoadTrigger() {
        /* Show Dialog */
        if (progressDialog != null && !progressDialog.isShowing()) progressDialog.show();

        RouterManager rm = new RouterManager(getActivity());
        rm.getTriggers(handler);
    }

    public void updateTrigger(ArrayList<TriggerElement> t) {
        triggerElements = t;

        adapter = new TriggerListAdapter(triggerElements, handler, getActivity());
        listView.setAdapter(adapter);
    }

    @Override
    /* Called when this fragment is shown */
    public void fragmentBecameVisible() {
        MainActivity.triggerFragment = this;
        MainActivity.showing = MainActivity.TRIGGER;
    }
}

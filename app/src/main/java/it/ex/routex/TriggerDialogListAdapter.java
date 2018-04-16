package it.ex.routex;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ex on 30/08/16.
 */
public class TriggerDialogListAdapter extends RecyclerView.Adapter {

    static public ArrayList<DeviceService> services;

    ArrayList<TriggerCondition> triggerConditions = new ArrayList<TriggerCondition>();
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        Spinner serviceSpinner;
        final Spinner conditionSpinner;
        final EditText argumentEditText;

        public MyViewHolder(View itemView) {
            super(itemView);
            final ArrayList<String> conditionNames = new ArrayList<String>();

            services = AllServiceListFragment.services;

            serviceSpinner = (Spinner) itemView.findViewById(R.id.dialog_trigger_service_spinner);
            conditionSpinner = (Spinner) itemView.findViewById(R.id.dialog_trigger_condition_spinner);
            argumentEditText = (EditText) itemView.findViewById(R.id.dialog_trigger_list_argument_edit);

            serviceSpinner.setAdapter(new ServiceSpinnerAdapter(context, R.layout.service_spinner_element, services));

            /*if (service.getType().matches("Text")) {
                conditionNames.add(getResources().getString(R.string.cond_matches));
                conditionNames.add(getResources().getString(R.string.cond_contains));
            }*/

            //else {
            //compareValueEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            conditionNames.add(context.getResources().getString(R.string.cond_greater));
            conditionNames.add(context.getResources().getString(R.string.cond_equal));
            conditionNames.add(context.getResources().getString(R.string.cond_less));
            //}

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, conditionNames);
            conditionSpinner.setAdapter(dataAdapter);
        }

    }

    public TriggerDialogListAdapter(ArrayList<TriggerCondition> t, Context c) {
        triggerConditions = t;
        context = c;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dialog_trigger_list_element, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        ((MyViewHolder)holder).argumentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                triggerConditions.get(position).value = s.toString();
            }
        });

        final EditText argEdTx = ((MyViewHolder)holder).argumentEditText;
        final Spinner tmp = ((MyViewHolder)holder).conditionSpinner;

        ((MyViewHolder)holder).serviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {
                triggerConditions.get(position).serviceName = services.get(position2).getName();
                triggerConditions.get(position).deviceName = services.get(position2).getDevice().getName();


                final ArrayList<String> conditionNames = new ArrayList<String>();

                if (services.get(position2).getType().matches("Status")) {
                    argEdTx.setInputType(InputType.TYPE_CLASS_TEXT);
                    conditionNames.add(context.getResources().getString(R.string.cond_matches));
                    conditionNames.add(context.getResources().getString(R.string.cond_contains));
                }

                else {
                    argEdTx.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    conditionNames.add(context.getResources().getString(R.string.cond_greater));
                    conditionNames.add(context.getResources().getString(R.string.cond_equal));
                    conditionNames.add(context.getResources().getString(R.string.cond_less));
                }

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, conditionNames);
                tmp.setAdapter(dataAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ((MyViewHolder)holder).conditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position2, long id) {

                String string = tmp.getSelectedItem().toString();

                if (string.matches((context.getResources().getString(R.string.cond_greater))))
                    triggerConditions.get(position).condition = "greater";
                else if (string.matches((context.getResources().getString(R.string.cond_equal))))
                    triggerConditions.get(position).condition = "equal";
                else if (string.matches((context.getResources().getString(R.string.cond_less))))
                    triggerConditions.get(position).condition = "less";
                else if (string.matches((context.getResources().getString(R.string.cond_matches))))
                    triggerConditions.get(position).condition = "matches";
                else if (string.matches((context.getResources().getString(R.string.cond_contains))))
                    triggerConditions.get(position).condition = "contains";

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return triggerConditions.size();
    }

}

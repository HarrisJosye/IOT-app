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
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ex on 03/07/16.
 */
public class ScheduleListAdapter extends RecyclerView.Adapter {

    ArrayList<ScheduleElement> scheduleElements;
    DeviceService service;
    Handler handler;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView commandView;
        TextView frequencyView;
        TextView timeLabel;

        public MyViewHolder(View itemView) {
            super(itemView);
            commandView = (TextView) itemView.findViewById(R.id.list_schedule_command_name);
            frequencyView = (TextView) itemView.findViewById(R.id.list_schedule_frequency);
            timeLabel = (TextView) itemView.findViewById(R.id.schedule_frequency_label);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {

                    RelativeLayout linearLayout = new RelativeLayout(v.getContext());

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
                    RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

                    linearLayout.setLayoutParams(params);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());
                    alertDialogBuilder.setMessage(v.getResources().getString(R.string.remove_element_from_schedule));
                    alertDialogBuilder.setView(linearLayout);
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton(v.getResources().getString(R.string.remove),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            ScheduleElement e = scheduleElements.get(getPosition());
                                            new RouterManager(v.getContext()).delScheduleElement(handler, service, e.getCommandName(),
                                                                                                    scheduleElements.get(getPosition()).getId());
                                            MainActivity.scheduleFragment.triggerLoadSchedule();
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

    public ScheduleListAdapter(ArrayList<ScheduleElement> s, DeviceService ser, Handler h, Context c) {
        scheduleElements = s;
        service = ser;
        handler = h;
        context = c;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.schedule_list_element, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (scheduleElements.get(position).type == ScheduleElement.TIME) {
            ((MyViewHolder)holder).timeLabel.setText(context.getString(R.string.le_time));
        }

        if (scheduleElements.get(position).getArgument() == null || scheduleElements.get(position).getArgument().matches(""))
            ((MyViewHolder)holder).commandView.setText(scheduleElements.get(position).getCommandName());
        else
            ((MyViewHolder)holder).commandView.setText(scheduleElements.get(position).getCommandName() + " (" + scheduleElements.get(position).getArgument() + ")");

        if (scheduleElements.get(position).type == ScheduleElement.FREQUENCY)
            ((MyViewHolder)holder).frequencyView.setText(secondsToFormattedString(scheduleElements.get(position).getFrequency()));
        else if (scheduleElements.get(position).type == ScheduleElement.TIME)
            ((MyViewHolder)holder).frequencyView.setText(scheduleElements.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return scheduleElements.size();
    }

    public String secondsToFormattedString(int seconds) {
        int h = seconds/3600;
        seconds = seconds%3600;
        int m = seconds/60;
        seconds = seconds%60;

        return "H: " + h + " M: " + m + " S: " + seconds;
    }
}

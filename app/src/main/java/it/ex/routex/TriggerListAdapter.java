package it.ex.routex;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ContentFrameLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ex on 05/07/16.
 */
public class TriggerListAdapter extends RecyclerView.Adapter {

    ArrayList<TriggerElement> triggerElements = new ArrayList<TriggerElement>();
    Handler handler;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView conditionView;
        TextView actionView;

        public MyViewHolder(View itemView) {
            super(itemView);
            conditionView = (TextView) itemView.findViewById(R.id.list_trigger_condition);
            actionView = (TextView) itemView.findViewById(R.id.list_trigger_action);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {

                    RelativeLayout linearLayout = new RelativeLayout(v.getContext());

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
                    RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

                    linearLayout.setLayoutParams(params);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());
                    alertDialogBuilder.setMessage(v.getResources().getString(R.string.remove_trigger));
                    alertDialogBuilder.setView(linearLayout);
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton(v.getResources().getString(R.string.remove),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            TriggerElement e = triggerElements.get(getPosition());
                                            new RouterManager(v.getContext()).delTriggerElement(handler, e.getId());
                                            MainActivity.triggerFragment.triggerLoadTrigger();
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

    public TriggerListAdapter(ArrayList<TriggerElement> t, Handler h, Context c) {
        triggerElements = t;
        handler = h;
        context = c;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trigger_list_element, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ((MyViewHolder)holder).conditionView.setText("");
        String previousText = "";

        for (TriggerCondition condEl: triggerElements.get(position).getConditions()) {

            String cond = condEl.getCondition();
            String condString = "";

            if (cond.matches("greater"))
                condString = context.getResources().getString(R.string.cond_greater);
            else if (cond.matches("equal"))
                condString = context.getResources().getString(R.string.cond_equal);
            else if (cond.matches("less"))
                condString = context.getResources().getString(R.string.cond_less);
            else if (cond.matches("matches"))
                condString = context.getResources().getString(R.string.cond_matches);
            else if (cond.matches("contains"))
                condString = context.getResources().getString(R.string.cond_contains);

            ((MyViewHolder)holder).conditionView.setText(previousText + condEl.getDeviceName() + " - " + condEl.getServiceName() + " - " + condString + " - " + condEl.getValue());

            previousText = ((MyViewHolder)holder).conditionView.getText().toString() + "\n";
        }

        ((MyViewHolder)holder).actionView.setText(triggerElements.get(position).getAction(context));

    }

    @Override
    public int getItemCount() {
        return triggerElements.size();
    }

}

package it.ex.routex;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

import static com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM;

/**
 * Created by ex on 28/06/16.
 */
public class GraphFragment extends Fragment implements TabbedServiceFragment.TabFragmentInterface{

    DeviceService service;

    boolean threadStop;

    ImageButton frequencyButton;
    ImageButton startIntervalButton;
    ImageButton endIntervalButton;
    RadioButton realTimeRadioButton;
    RadioButton intervalRadioButton;

    long lastRequestMillis = System.currentTimeMillis();

    long startMilliseconds = System.currentTimeMillis() - 1000 * 60 * 60 * 24;
    long endMilliseconds = System.currentTimeMillis();

    Switch threadSwitch;

    Handler handler;

    Thread requestThread;

    LineChart chart;
    LineData data;
    ArrayList<ILineDataSet> dataSets;
    ArrayList<Entry> vals;
    ArrayList<String> xVals;

    int index = 1;

    int frequency = 30;

    static String START = "Start";
    static String END = "End";

    public GraphFragment(){}

    @SuppressLint("ValidFragment")
    public GraphFragment(DeviceService s) {
        service = s;
    }

    @Override
    public void onResume() {
        MainActivity.graphFragment = this;
        MainActivity.showing = MainActivity.GRAPH;
        super.onResume();
    }

    @Override
    public void onPause() {
        threadStop = true;
        super.onPause();
    }

    @Override
    public void onStop() {
        threadStop = true;
        super.onStop();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_graph, container, false);

        chart = (LineChart) v.findViewById(R.id.chart);
        chart.setPinchZoom(true);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.setDescription("");

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ChartAxisFormatter(startMilliseconds));
        xAxis.setLabelRotationAngle(45f);
        xAxis.setLabelCount(5);

        frequencyButton = (ImageButton) v.findViewById(R.id.graph_time_replay_button);
        startIntervalButton = (ImageButton) v.findViewById(R.id.graph_start_interval_button);
        endIntervalButton = (ImageButton) v.findViewById(R.id.graph_end_interval_button);
        realTimeRadioButton = (RadioButton) v.findViewById(R.id.radio_real_time);
        intervalRadioButton = (RadioButton) v.findViewById(R.id.radio_fixed_time);
        threadSwitch = (Switch) v.findViewById(R.id.thread_switch);

        startIntervalButton.setEnabled(true);
        endIntervalButton.setEnabled(true);
        //startIntervalButton.setImageAlpha(100);
        //endIntervalButton.setImageAlpha(100);

        frequencyButton.setEnabled(false);

        vals = new ArrayList<Entry>();

        LineDataSet setComp1 = new LineDataSet(vals, service.getName());
        setComp1.setDrawValues(false);
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setColor(Color.parseColor("#46b2f5"));
        setComp1.setCircleColor(Color.parseColor("#46b2f5"));

        // use the interface ILineDataSet
        dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);

        xVals = new ArrayList<String>();

        //data = new LineData(xVals, dataSets);
        data = new LineData();
        chart.setData(data);
        chart.invalidate();

        realTimeRadioButton.setChecked(false);
        intervalRadioButton.setChecked(true);

        /* On click open the dialog to set the request frequency */
        frequencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout linearLayout = new RelativeLayout(getActivity());
                final NumberPicker aNumberPicker = new NumberPicker(getActivity());
                aNumberPicker.setMaxValue(300);
                aNumberPicker.setMinValue(10);
                aNumberPicker.setValue(frequency);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
                RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

                linearLayout.setLayoutParams(params);
                linearLayout.addView(aNumberPicker,numPicerParams);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle(getResources().getString(R.string.seconds_between_data_request));
                alertDialogBuilder.setView(linearLayout);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        /* Update frequency value */
                                        frequency = aNumberPicker.getValue();
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        startIntervalButton.setOnClickListener(createDateTimePicker(START));
        endIntervalButton.setOnClickListener(createDateTimePicker(END));

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RouterManager.EXECUTE_RESULT:
                        Log.w("Graph execute", ((JSONObject) msg.obj).toString());

                        String result = null;
                        try {
                            JSONObject jo = (JSONObject) msg.obj;
                            result = jo.getString(Utility.Result);
                            long timestamp = jo.getLong(Utility.Timestamp);
                            try {
                                addValueToChart(Integer.parseInt(result), timestamp - startMilliseconds);
                            }
                            catch (NumberFormatException e) {
                                Log.w("NoAck", "Not Able to Insert in Graph");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                    case RouterManager.DATA_UPDATE:
                        Log.w("Data update", ((JSONObject) msg.obj).toString());

                        try {
                            JSONObject jo = (JSONObject) msg.obj;
                            String status = jo.getString(Utility.Status);
                            JSONArray jsonValues = jo.getJSONArray(Utility.Values);

                            /* For each value returnd, add it to the graph */
                            for (int i = 0; i < jsonValues.length(); i++) {
                                String v = jsonValues.getJSONObject(i).getString(Utility.Value);
                                try {
                                    addValueToChart(Float.parseFloat(v), /*jsonValues.getJSONObject(i).getString(Utility.Timestamp)*/
                                                    jsonValues.getJSONObject(i).getLong(Utility.Timestamp) - startMilliseconds);
                                }
                                catch (NumberFormatException e) {
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                    case RouterManager.ROUTER_UNAVAILABLE:
                        break;
                    default:
                        break;
                }
            }
        };

        /* Handle thread and button enabling */
        realTimeRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    frequencyButton.setEnabled(true);
                    frequencyButton.setImageAlpha(255);
                    startIntervalButton.setEnabled(false);
                    endIntervalButton.setEnabled(false);
                    startIntervalButton.setImageAlpha(100);
                    endIntervalButton.setImageAlpha(100);

                    threadSwitch.setEnabled(true);
                }

                else {
                    /* Stop RealTime Request Thread */
                    threadStop = true;
                    threadSwitch.setChecked(false);
                    threadSwitch.setEnabled(false);

                    frequencyButton.setEnabled(false);
                    frequencyButton.setImageAlpha(100);
                    startIntervalButton.setEnabled(true);
                    endIntervalButton.setEnabled(true);
                    startIntervalButton.setImageAlpha(255);
                    endIntervalButton.setImageAlpha(255);
                }
            }
        });


        threadSwitch.setChecked(false);
        requestThread = getNewThread();
        threadStop = true;

        threadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    /* Start RealTime request thread */
                    threadStop = false;
                    requestThread = getNewThread();
                    requestThread.start();
                }
                else {
                    threadStop = true;
                }
            }
        });

        reloadGraph();

        return v;
    }

    /* Add new value to the graph */
    private void addValueToChart(float value, long timestamp) {
        //xVals.add(time);
        Entry c1e = new Entry(timestamp, value);
        data.addEntry(c1e, 0);
        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    /* RealTime request thread */
    public Thread getNewThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                RouterManager rm = new RouterManager(getActivity());
                while (true) {
                    if (threadStop) return;
                    rm.getIntervalData(handler, service, lastRequestMillis, System.currentTimeMillis());
                    lastRequestMillis = System.currentTimeMillis();

                    try {
                        Thread.sleep(frequency*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    View.OnClickListener createDateTimePicker(final String type) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout linearLayout = new RelativeLayout(getActivity());
                final DatePicker datePicker = new DatePicker(getActivity());

                /* Set Default Value */
                final Calendar c = Calendar.getInstance();
                if (type.matches(START)) {
                    c.setTimeInMillis(startMilliseconds);
                }
                else if (type.matches(END)) {
                    c.setTimeInMillis(endMilliseconds);
                }
                datePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));


                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
                RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

                linearLayout.addView(datePicker, numPicerParams);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                if (type.matches(START)) alertDialogBuilder.setTitle(getResources().getString(R.string.select_start_time));
                else alertDialogBuilder.setTitle(getResources().getString(R.string.select_end_time));

                alertDialogBuilder.setView(linearLayout);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        RelativeLayout linearLayout = new RelativeLayout(getActivity());
                                        final TimePicker timePicker = new TimePicker(getActivity());

                                        /* Set Default Value */
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            timePicker.setHour(c.get(Calendar.HOUR));
                                            timePicker.setMinute(c.get(Calendar.MINUTE));
                                        }
                                        else {
                                            timePicker.setCurrentHour(c.get(Calendar.HOUR));
                                            timePicker.setCurrentMinute(c.get(Calendar.MINUTE));
                                        }

                                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
                                        RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

                                        linearLayout.addView(timePicker, numPicerParams);

                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                                        if (type.matches(START)) alertDialogBuilder.setTitle(getResources().getString(R.string.select_start_time));
                                        else alertDialogBuilder.setTitle(getResources().getString(R.string.select_end_time));
                                        alertDialogBuilder.setView(linearLayout);
                                        alertDialogBuilder
                                                .setCancelable(false)
                                                .setPositiveButton(getResources().getString(R.string.ok),
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog,
                                                                                int id) {
                                                                Calendar calendar = Calendar.getInstance();
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                                    calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                                                                            timePicker.getHour(), timePicker.getMinute(), 0);
                                                                } else {
                                                                    calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                                                                            timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);
                                                                }

                                                                if (type.matches(START)) {
                                                                    startMilliseconds = calendar.getTimeInMillis();
                                                                }

                                                                if (type.matches(END)) {
                                                                    endMilliseconds = calendar.getTimeInMillis();
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
                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        alertDialog.show();
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        };
    }


    /* Remove every value from the graph */
    public void reloadGraph() {
        index = 0;
        vals = new ArrayList<Entry>();

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ChartAxisFormatter(startMilliseconds));
        xAxis.setLabelRotationAngle(45f);
        xAxis.setLabelCount(5);

        LineDataSet setComp1 = new LineDataSet(vals, service.getName());
        setComp1.setDrawValues(false);
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setColor(Color.parseColor("#46b2f5"));
        setComp1.setCircleColor(Color.parseColor("#46b2f5"));

        dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);

        xVals = new ArrayList<String>();

        //data = new LineData(xVals, dataSets);
        data = new LineData(setComp1);
        chart.setData(data);
        chart.invalidate();

        if (intervalRadioButton.isChecked()) {
            new RouterManager(getActivity()).getIntervalData(handler, service, startMilliseconds, endMilliseconds);
        }
    }

    public static String convertDate(long dateInMilliseconds) {
        return DateFormat.format("dd-MM-yyyy hh:mm", dateInMilliseconds).toString();
    }

    public void stopThread() {
        threadStop = true;
    }

    @Override
    /* Called when this fragment is shown */
    public void fragmentBecameVisible() {
        MainActivity.graphFragment = this;
        MainActivity.showing = MainActivity.GRAPH;
    }
}

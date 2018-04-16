package it.ex.routex;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * Created by ex on 17/08/16.
 */
public class AllServiceGraph extends Fragment {

    ArrayList<DeviceService> services;
    boolean[] isServiceSelected;

    ImageButton startIntervalButton;
    ImageButton endIntervalButton;
    Button filterButton;

    long lastRequestMillis = System.currentTimeMillis();
    long startMilliseconds = System.currentTimeMillis() - 1000 * 60 * 60 * 24; /* Yesterday */
    long endMilliseconds = System.currentTimeMillis();

    Handler handler;

    int index = 0;

    int maxGraphValues = 0;

    static String START = "Start";
    static String END = "End";

    LineChart chart;
    LineData data;
    List<ILineDataSet> dataSets;
    ArrayList<Entry> vals;
    ArrayList<String> xVals;


    public AllServiceGraph() {}

    @SuppressLint("ValidFragment")
    public AllServiceGraph(ArrayList<DeviceService> services) {
        this.services = (ArrayList<DeviceService>) services.clone();
    }

    @Override
    public void onResume() {
        MainActivity.allServiceGraphFragment = this;
        MainActivity.showing = MainActivity.ALL_SERVICE_GRAPH;
        super.onResume();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View v = inflater.inflate(R.layout.fragment_all_service_graph, container, false);

        isServiceSelected = new boolean[services.size()];


        chart = (LineChart) v.findViewById(R.id.chart);
        chart.setPinchZoom(true);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.setDescription("");

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ChartAxisFormatter(startMilliseconds));
        xAxis.setLabelRotationAngle(45f);
        xAxis.setLabelCount(5);

        startIntervalButton = (ImageButton) v.findViewById(R.id.all_service_graph_start_interval_button);
        endIntervalButton = (ImageButton) v.findViewById(R.id.all_service_graph_end_interval_button);

        vals = new ArrayList<Entry>();

        LineDataSet setComp1 = new LineDataSet(vals, "ServTest");
        setComp1.setDrawValues(false);
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setColor(Color.parseColor("#46b2f5"));
        setComp1.setCircleColor(Color.parseColor("#46b2f5"));

        dataSets = new ArrayList<ILineDataSet>();

        xVals = new ArrayList<String>();

        //data = new LineData(xVals, dataSets);
        data = new LineData();
        chart.setData(data);
        chart.invalidate();

        filterButton = (Button) v.findViewById(R.id.all_service_filter_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout linearLayout = new LinearLayout(getActivity());
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                for (int i = 0; i < services.size(); i++) {
                    final CheckBox checkBox = new CheckBox(getActivity());
                    checkBox.setText(services.get(i).getName() + " (" + services.get(i).getDevice().getName() + ")");
                    checkBox.setChecked(isServiceSelected[i]);
                    linearLayout.addView(checkBox,numPicerParams);
                    final int z = i;

                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            isServiceSelected[z] = isChecked;
                        }
                    });
                }

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle(getResources().getString(R.string.select_services));
                alertDialogBuilder.setView(linearLayout);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {

                                        //frequency = aNumberPicker.getValue();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RouterManager.DATA_UPDATE:
                        Log.w("Data update", ((JSONObject) msg.obj).toString());

                        try {
                            JSONObject jo = (JSONObject) msg.obj;
                            String status = jo.getString(Utility.Status);
                            JSONArray jsonValues = jo.getJSONArray(Utility.Values);

                            /* For each value returned, add it to the graph */
                            /*for (int i = 0; i < jsonValues.length(); i++) {
                                String v = jsonValues.getJSONObject(i).getString(Utility.Value);
                                try {
                                    addValueToChart(Float.parseFloat(v), jsonValues.getJSONObject(i).getString(Utility.Timestamp));
                                }
                                catch (NumberFormatException e) {
                                }
                            }*/

                            addSetOfData(jo.getString(Utility.ServiceName), jsonValues);

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
        startIntervalButton.setOnClickListener(createDateTimePicker(START));
        endIntervalButton.setOnClickListener(createDateTimePicker(END));

        for (int i = 0; i < services.size(); i++) {
            isServiceSelected[i] = true;
        }


        /* Initial Values */
        for (DeviceService s: services) {
            new RouterManager(getActivity()).getIntervalData(handler, s, startMilliseconds, endMilliseconds);
        }


        return v;
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

    /* Add new value to the graph */
    private void addValueToChart(float value, String time) {
        xVals.add(time);
        Entry c1e = new Entry(value, index++);
        data.addEntry(c1e, 0);
        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private void addSetOfData(String name, JSONArray jsonValues) {


        /*List<Entry> valsComp1 = new ArrayList<Entry>();
        Entry c1e1 = new Entry(1471445672614L/1000, 100000f); // 0 == quarter 1
        valsComp1.add(c1e1);
        Entry c1e2 = new Entry(1471445272614L/1000, 140000f); // 1 == quarter 2 ...
        valsComp1.add(c1e2);
        Entry c1e3 = new Entry(1471545272614L/1000, 140000f); // 1 == quarter 2 ...
        valsComp1.add(c1e3);
        LineDataSet setComp1 = new LineDataSet(valsComp1, "Company 1");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);*/


        List<Entry> valsComp1 = new ArrayList<Entry>();

        for (int i = 0; i < jsonValues.length(); i++) {
            try {
                String v = jsonValues.getJSONObject(i).getString(Utility.Value);
                String timestamp = jsonValues.getJSONObject(i).getString(Utility.Timestamp);
                long seconds = jsonValues.getJSONObject(i).getLong(Utility.Timestamp);

                try {
                    float floatValue = Float.parseFloat(v);
                    Entry c1e1 = new Entry(seconds - startMilliseconds, floatValue); // 0 == quarter 1
                    valsComp1.add(c1e1);
                } catch (NumberFormatException e) {
                    continue;
                }


            } catch (JSONException e) {
            }
        }

        LineDataSet setComp1 = new LineDataSet(valsComp1, name);
        setComp1.setDrawValues(false);
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        setComp1.setColor(color);
        setComp1.setCircleColor(color);

        dataSets.add(setComp1);

        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.invalidate();



        /*dataSets = new LineDataSet(vals, "");
        dataSets.setAxisDependency(YAxis.AxisDependency.LEFT);
        setCom.setColor(Color.parseColor("#46b2f5"));
        setComp1.setCircleColor(Color.parseColor("#46b2f5"));*/
    }

    /* Remove every value from the graph */
    public void reloadGraph() {

        Log.w("Reloading Graph", "Services");

        index = 0;
        vals = new ArrayList<Entry>();

        //LineDataSet setComp1 = new LineDataSet(vals, "");
        //setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

        dataSets = new ArrayList<ILineDataSet>();

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ChartAxisFormatter(startMilliseconds));
        xAxis.setGranularity(10f);
        xAxis.setLabelRotationAngle(45f);
        xAxis.setLabelCount(5);

        //xVals = new ArrayList<String>();

        //data = new LineData(xVals, dataSets);
        data = new LineData(dataSets);
        //chart.setData(data);
        chart.invalidate();

        for (int x = 0; x < services.size(); x++) {
            Log.w("Service", services.get(x).getName());
            if (isServiceSelected[x]) {
                Log.w("Is", "Selected");
                new RouterManager(getActivity()).getIntervalData(handler, services.get(x), startMilliseconds, endMilliseconds);
            }
            else Log.w("Is", "Not Selected");
        }
    }

    public static String convertDate(long dateInMilliseconds) {
        return DateFormat.format("dd-MM-yyyy hh:mm", dateInMilliseconds).toString();
    }
}

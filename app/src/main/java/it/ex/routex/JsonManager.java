package it.ex.routex;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by ex on 21/06/16.
 */
public class JsonManager {

    /* Return list of Devices given the json returned by the server */
    public static ArrayList<Device> getDevices (JSONArray jsonArray){
        ArrayList<Device> devices = new ArrayList<Device>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject o = jsonArray.getJSONObject(i);
                Device d = new Device(o.getString(Utility.DeviceName), o.getString(Utility.Technology), o.getBoolean(Utility.AlwaysOn));

                /* Servs */
                JSONArray servsJson = o.getJSONArray(Utility.Services);
                for (int j = 0; j < servsJson.length(); j++) {
                    DeviceService ds = new DeviceService(servsJson.getJSONObject(j).getString(Utility.ServiceName), servsJson.getJSONObject(j).getString(Utility.ServiceType));
                    ds.assignToDevice(d);
                    d.addService(ds);

                    JSONArray coms = servsJson.getJSONObject(j).getJSONArray(Utility.Commands);
                    for (int z = 0; z < coms.length(); z++) {
                        try {
                            ds.addCommand(new Command(coms.getJSONObject(z).getString(Utility.CommandName),
                                    coms.getJSONObject(z).getString(Utility.CommandType)));
                        }
                        catch (JSONException e) {
                            Log.w("Command", "Error adding command");
                        }
                    }
                }



                devices.add(d);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return devices;
    }

    /* Return list of DeviceServices given the json returned by the server */
    public static ArrayList<DeviceService> getServices (JSONArray jsonArray){
        ArrayList<DeviceService> services = new ArrayList<DeviceService>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject o = jsonArray.getJSONObject(i);
                DeviceService serv = new DeviceService(o.getString(Utility.ServiceName), o.getString(Utility.ServiceType));

                try {
                    JSONArray coms = o.getJSONArray(Utility.Commands);
                    Log.w("Commands",  coms.toString());
                    for (int j = 0; j < coms.length(); j++) {
                        try {
                            serv.addCommand(new Command(coms.getJSONObject(j).getString(Utility.CommandName),
                                    coms.getJSONObject(j).getString(Utility.CommandType)));
                        }
                        catch (JSONException e) {
                            Log.w("Command", "Error adding command");
                        }
                    }
                }
                catch (JSONException e) {
                    Log.w("Service", "No Custom Commands Here");
                }

                services.add(serv);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return services;
    }


    /* Return list of ScheduleElements given the json returned by the server */
    public static ArrayList<ScheduleElement> getSchedule (JSONArray jsonArray){
        ArrayList<ScheduleElement> scheduleElements = new ArrayList<ScheduleElement>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject o = jsonArray.getJSONObject(i);

                /* Frequency Schedule */
                if (o.has(Utility.Frequency)) {
                    if (o.has(Utility.Argument)) {
                        scheduleElements.add(new ScheduleElement(o.getString(Utility.Command),
                                Integer.parseInt(o.getString(Utility.Frequency)),
                                o.getString(Utility.Argument), o.getInt(Utility.ID)));
                    } else
                        scheduleElements.add(new ScheduleElement(o.getString(Utility.Command),
                                Integer.parseInt(o.getString(Utility.Frequency)), o.getInt(Utility.ID)));
                }

                /* Time Schedule */
                if (o.has(Utility.Time)) {
                    if (o.has(Utility.Argument)) {
                        scheduleElements.add(new ScheduleElement(o.getString(Utility.Command),
                                o.getString(Utility.Time),
                                o.getString(Utility.Argument), o.getInt(Utility.ID)));
                    } else
                        scheduleElements.add(new ScheduleElement(o.getString(Utility.Command),
                                o.getString(Utility.Time), o.getInt(Utility.ID)));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return scheduleElements;
    }

    /* Return list of Commands given the json returned by the server */
    public static ArrayList<TriggerElement> getAvailableCommands (JSONArray jsonArray){

        ArrayList<TriggerElement> commands = new ArrayList<TriggerElement>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject devJson = jsonArray.getJSONObject(i);
                String devName = devJson.getString(Utility.DeviceName);
                JSONArray serArrayJson = devJson.getJSONArray(Utility.Services);
                for (int j = 0; j < serArrayJson.length(); j++) {
                    String servName = serArrayJson.getJSONObject(j).getString(Utility.ServiceName);
                    JSONArray comArrayJson = serArrayJson.getJSONObject(j).getJSONArray(Utility.Commands);
                    for (int z = 0; z < comArrayJson.length(); z++) {
                        String comName = comArrayJson.getJSONObject(z).getString(Utility.CommandName);
                        String comType = comArrayJson.getJSONObject(z).getString(Utility.CommandType);
                        commands.add(new TriggerElement(devName, servName, comName, comType));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return commands;
    }

    /* Return list of TriggerElements given the json returned by the server */
    public static ArrayList<TriggerElement> getTriggers (JSONArray jsonArray){
        ArrayList<TriggerElement> triggerElements = new ArrayList<TriggerElement>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject o = jsonArray.getJSONObject(i);
                JSONArray conditionArrayJson = o.getJSONArray(Utility.Conditions);
                JSONObject then = o.getJSONObject(Utility.Then);
                String type = then.getString(Utility.Type);

                TriggerElement te;

                if (type.matches(TriggerElement.COMMAND)) {
                    if (then.has(Utility.Argument)) {
                        te = new TriggerElement(then.getString(Utility.DeviceName), then.getString(Utility.ServiceName),
                                then.getString(Utility.Command), then.getString(Utility.Argument), o.getInt(Utility.ID));
                    }
                    else {
                        te = new TriggerElement(then.getString(Utility.DeviceName), then.getString(Utility.ServiceName),
                                then.getString(Utility.Command), o.getInt(Utility.ID));
                    }
                }

                else {
                    te = new TriggerElement(o.getInt(Utility.ID), type);
                }

                for (int j = 0; j < conditionArrayJson.length(); j++) {
                    JSONObject cJson = conditionArrayJson.getJSONObject(j);
                    te.addCondition(new TriggerCondition(cJson.getString(Utility.DeviceName), cJson.getString(Utility.ServiceName),
                                            cJson.getString(Utility.Condition), cJson.getString(Utility.Value)));
                }

                triggerElements.add(te);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return triggerElements;
    }
}

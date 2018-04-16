package it.ex.routex;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Created by ex on 21/06/16.
 */
public class RouterManager {

    /* Handler Message Type */
    public static final int DEVICES_UPDATE = 0;
    public static final int ROUTER_STATUS_UPDATE = 1;
    public static final int ROUTER_UNAVAILABLE = 2;
    public static final int SERVICES_UPDATE = 3;
    public static final int EXECUTE_RESULT = 4;
    public static final int DATA_UPDATE = 5;
    public static final int SCHEDULE_UPDATE = 6;
    public static final int SCHEDULE_COMMAND_RESULT = 7;
    public static final int AVAILABLE_COMMAND_RESULT = 8;
    public static final int TRIGGER_UPDATE = 9;
    public static final int CONNECTION_ERROR = 10;
    public static final int LAST_VALUE = 11;
    public static final int THING_SPEAK = 12;
    public static final int DEVICE_DELETED = 13;

    /* Requests URLs */
    public static final String defaultURL =             "http://192.168.1.13/RouteX";
    public static final String getDevicesURL =          "/getDevices.py";
    public static final String removeDeviceURL =       "/removeDevice.py";
    public static final String getDeviceInfoURL =       "/getDeviceInfo.py";
    public static final String executeCommandURL =      "/executeCommand.py";
    public static final String getServiceDataURL =      "/getServiceData.py";
    public static final String getScheduleURL =         "/getSchedule.py";
    public static final String handleScheduleURL =      "/handleSchedule.py";
    public static final String handleTriggerURL =       "/handleTrigger.py";
    public static final String getLastValueURL =       "/getLastValue.py";
    public static final String thingSpeakURL =       "/setThingSpeak.py";

    Context context;
    JSONArray devices;
    String errorMsg;

    RouterManager(Context context) {
        this.context = context;
    }

    /* Request available devices to router */
    public void getDevices(final Handler handler) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String gdu = serverUrl + getDevicesURL;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (com.android.volley.Request.Method.GET, gdu, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.w("Json Response", response.toString());

                        try {
                            /* Send Message to Handler with Devices */
                            devices = response.getJSONArray(Utility.Devices);
                            Log.w("Devices", devices.toString());
                            Message messageToSend = handler.obtainMessage();
                            messageToSend.what = DEVICES_UPDATE;
                            messageToSend.obj = devices;
                            handler.sendMessage(messageToSend);
                        } catch (JSONException e) {
                            Log.w("ERROR", "Catch");
                            e.printStackTrace();
                        }

                        try {
                            /* Send Message to Handler with Router Info */
                            String str = response.getString(Utility.Status);
                            Log.w("Status", str);
                            Message messageToSend = handler.obtainMessage();
                            messageToSend.what = ROUTER_STATUS_UPDATE;
                            messageToSend.obj = new RouterInfo(str);
                            handler.sendMessage(messageToSend);
                        } catch (JSONException e) {
                            Log.w("ERROR", "Catch");
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;

                        String statusCode = new String(context.getResources().getString(R.string.server_unreachable));
                        errorMsg = new String(context.getResources().getString(R.string.server_unreachable));
                        if (error.networkResponse != null) {
                            statusCode = String.valueOf(error.networkResponse.statusCode);
                            if (error.networkResponse.data != null) {
                                try {
                                    errorMsg = new String(error.networkResponse.data, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        messageToSend.obj = errorMsg;
                        Log.w("ErrorMessage",errorMsg);
                        handler.sendMessage(messageToSend);
                    }
                });

        queue.add(jsObjRequest);
    }


    /* Delete device */
    public void removeDevice(final Handler handler, final Device device) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String rd = serverUrl + removeDeviceURL;

        JSONObject params = new JSONObject();
        try {
            params.put(Utility.DeviceName, device.getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, rd, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject jo = response;
                            Message messageToSend = handler.obtainMessage();
                            messageToSend.what = DEVICE_DELETED;
                            messageToSend.obj = jo.getBoolean(Utility.Result);
                            handler.sendMessage(messageToSend);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;
                        int statusCode = -1;
                        if (error != null && error.networkResponse != null) statusCode = error.networkResponse.statusCode;
                        if (statusCode != -1) messageToSend.obj = statusCode + "  -  " + error.toString();
                        else messageToSend.obj = error.toString();
                        handler.sendMessage(messageToSend);
                    }
                })  {

            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 0/* Retries*/ , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }


    /* Request service and info of selected device */
    public void getDeviceInfo(final Handler handler, final Device device) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String gdi = serverUrl + getDeviceInfoURL;

        JSONObject params = new JSONObject();
        try {
            params.put(Utility.DeviceName, device.getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, gdi, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject jo = response;
                            Log.w("ServicesResponse", response.toString());
                            // Send Message to Handler with Services
                            JSONArray services = jo.getJSONArray(Utility.Services);
                            Log.w("Services", services.toString());
                            Message messageToSend = handler.obtainMessage();
                            messageToSend.what = SERVICES_UPDATE;
                            messageToSend.obj = services;
                            handler.sendMessage(messageToSend);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;
                        int statusCode = -1;
                        if (error != null && error.networkResponse != null) statusCode = error.networkResponse.statusCode;
                        if (statusCode != -1) messageToSend.obj = statusCode + "  -  " + error.toString();
                        else messageToSend.obj = error.toString();
                        handler.sendMessage(messageToSend);
                    }
                })  {

            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 0/* Retries*/ , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    /* Request the execution of a command */
    public void executeCommand(final Handler handler, final Device device, final DeviceService service,
                                    final String command, final String argument) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String ec = serverUrl + executeCommandURL;

        JSONObject params = new JSONObject();
        try {
            params.put(Utility.DeviceName, device.getName());
            params.put(Utility.ServiceName, service.getName());
            params.put(Utility.Command, command);
            if (argument != null) {
                params.put(Utility.Argument, argument);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, ec, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject jo = response;
                        Log.w("ExecuteResponse", response.toString());
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = EXECUTE_RESULT;
                        messageToSend.obj = jo;
                        handler.sendMessage(messageToSend);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;
                        String statusCode = null;
                        if (error.networkResponse != null) {
                            statusCode = String.valueOf(error.networkResponse.statusCode);
                            if (error.networkResponse.data != null) {
                                try {
                                    errorMsg = new String(error.networkResponse.data, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (statusCode != null)
                            messageToSend.obj = error.networkResponse.statusCode + "  -  " + error.toString();
                        else
                            messageToSend.obj = "";
                        handler.sendMessage(messageToSend);
                    }
                }) {
            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }

        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 0/* Retries*/ , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    /* Request values of a sensor */
    public void getIntervalData(final Handler handler, final DeviceService service, final long start, final long end) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String gsd = serverUrl + getServiceDataURL;
        Log.w("URL", gsd);

        JSONObject params = new JSONObject();
        try {
            params.put(Utility.DeviceName, service.getDevice().getName());
            params.put(Utility.ServiceName, service.getName());
            params.put(Utility.StartTime, start + "");
            params.put(Utility.EndTime, end + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, gsd, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject jo = response;
                        Log.w("DataResponse", response.toString());
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = DATA_UPDATE;
                        messageToSend.obj = jo;
                        handler.sendMessage(messageToSend);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;
                        messageToSend.obj = error.networkResponse.statusCode + "  -  " + error.toString();
                        handler.sendMessage(messageToSend);
                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 0 /* Retries*/ , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    /* Request schedule of selected service */
    public void getServiceSchedule(final Handler handler, final DeviceService service) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String gs = serverUrl + getScheduleURL;
        Log.w("URL", gs);

        JSONObject params = new JSONObject();
        try {
            params.put(Utility.DeviceName, service.getDevice().getName());
            params.put(Utility.ServiceName, service.getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, gs, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray jo = null;
                        try {
                            jo = response.getJSONArray(Utility.Schedule);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.w("ScheduleResponse", response.toString());
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = SCHEDULE_UPDATE;
                        messageToSend.obj = jo;
                        handler.sendMessage(messageToSend);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;
                        messageToSend.obj = error.networkResponse.statusCode + "  -  " + error.toString();
                        handler.sendMessage(messageToSend);
                    }
                }) {
            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 0 /* Retries*/ , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    /* Add new event to the schedule */
    public void addScheduleElement(final Handler handler, final DeviceService service, final String commandName, final char timeType,
                                   final long frequency, final String time,  final String argument) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String hs = serverUrl + handleScheduleURL;
        Log.w("URL", hs);

        JSONObject params = new JSONObject();
        try {
            params.put(Utility.ScheduleCommand, Utility.ScheduleAdd);
            params.put(Utility.DeviceName, service.getDevice().getName());
            params.put(Utility.ServiceName, service.getName());
            params.put(Utility.Command, commandName);
            if (timeType == ScheduleElement.FREQUENCY) params.put(Utility.Frequency, frequency + "");
            if (timeType == ScheduleElement.TIME) params.put(Utility.Time, time + "");
            if (argument != null) params.put(Utility.Argument, argument);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, hs, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject jo = response;
                        Log.w("ScheduleResponse", response.toString());
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = SCHEDULE_COMMAND_RESULT;
                        messageToSend.obj = jo;
                        handler.sendMessage(messageToSend);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;
                        messageToSend.obj = error.networkResponse.statusCode + "  -  " + error.toString();
                        handler.sendMessage(messageToSend);
                    }
                }) {
            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
        };

        queue.add(stringRequest);
    }

    /* Delete element of schedule */
    public void delScheduleElement(final Handler handler, final DeviceService service, final String commandName, int id) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String hs = serverUrl + handleScheduleURL;
        Log.w("URL", hs);

        JSONObject params = new JSONObject();
        try {
            params.put(Utility.ScheduleCommand, Utility.ScheduleRemove);
            params.put(Utility.DeviceName, service.getDevice().getName());
            params.put(Utility.ServiceName, service.getName());
            params.put(Utility.Command, commandName);
            params.put(Utility.ID, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, hs, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject jo = response;
                        Log.w("ScheduleResponse", response.toString());
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = SCHEDULE_COMMAND_RESULT;
                        messageToSend.obj = jo;
                        handler.sendMessage(messageToSend);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;
                        messageToSend.obj = error.networkResponse.statusCode + "  -  " + error.toString();
                        handler.sendMessage(messageToSend);
                    }
                }) {
            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 0 /* Retries*/ , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    /* Request commands */
    public void getTriggerCommands(final Handler handler) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String ht = serverUrl + handleTriggerURL;
        Log.w("URL", ht);

        JSONObject params = new JSONObject();
        try {
            params.put(Utility.Request, Utility.Commands);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, ht, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray jo = response.getJSONArray(Utility.Commands);
                            Log.w("CommandResponse", response.toString());
                            // Send Message to Handler with Services
                            Message messageToSend = handler.obtainMessage();
                            messageToSend.what = AVAILABLE_COMMAND_RESULT;
                            messageToSend.obj = jo;
                            handler.sendMessage(messageToSend);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;
                        messageToSend.obj = error.toString();
                        handler.sendMessage(messageToSend);
                    }
                }) {
            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 0 /* Retries*/ , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    /* Request active triggers of selected service */
    public void getTriggers(final Handler handler) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String ht = serverUrl + handleTriggerURL;
        Log.w("URL", ht);

        JSONObject params = new JSONObject();
        try {
            params.put(Utility.Request, Utility.Triggers);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, ht, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray jo = response.getJSONArray(Utility.Triggers);
                            Log.w("TriggerResponse", response.toString());
                            Message messageToSend = handler.obtainMessage();
                            messageToSend.what = TRIGGER_UPDATE;
                            messageToSend.obj = jo;
                            handler.sendMessage(messageToSend);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;
                        messageToSend.obj = error.toString();
                        handler.sendMessage(messageToSend);
                    }
                }) {
            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 0 /* Retries*/ , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    /* Add new trigger */
    public void addTriggerElement(final Handler handler, final ArrayList<TriggerCondition> conditions,
                                  final TriggerElement triggerElement, final String argument, final String type) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String ht = serverUrl + handleTriggerURL;
        Log.w("URL", ht);

        JSONObject params = new JSONObject();
        JSONObject jo = new JSONObject();
        JSONArray condsJson = new JSONArray();

        try {
            params.put(Utility.Request, Utility.TriggerAdd);
            //params.put(Utility.DeviceName, service.getDevice().getName());
            //params.put(Utility.ServiceName, service.getName());
            //params.put(Utility.Condition, condition);
            //params.put(Utility.Value, value);

            for (int i = 0; i < conditions.size(); i++) {
                JSONObject condJson = new JSONObject();
                condJson.put(Utility.Condition, conditions.get(i).getCondition());
                condJson.put(Utility.Value, conditions.get(i).getValue());
                condJson.put(Utility.DeviceName, conditions.get(i).getDeviceName());
                condJson.put(Utility.ServiceName, conditions.get(i).getServiceName());
                condsJson.put(condJson);
            }

            jo.put(Utility.Type, type);
            if (type.matches(TriggerElement.COMMAND)) {
                jo.put(Utility.DeviceName, triggerElement.getDeviceName());
                jo.put(Utility.ServiceName, triggerElement.getServiceName());
                jo.put(Utility.Command, triggerElement.getCommandName());
                if (argument != null)
                    jo.put(Utility.Argument, argument);
            }
            if (type.matches(TriggerElement.MAIL)) {
                jo.put(Utility.MailAddress, PreferenceManager.getDefaultSharedPreferences(context).getString("dest_mail", ""));
                jo.put(Utility.TextContent, argument);
            }
            if (type.matches(TriggerElement.NOTIFICATION)) {
                jo.put(Utility.NotificationId, MainActivity.gcm_id);
                jo.put(Utility.TextContent, argument);
            }

            params.put(Utility.Conditions, condsJson);
            params.put(Utility.Then, jo.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, ht, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject jo = response;
                        Log.w("CommandResponse", response.toString());
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = EXECUTE_RESULT;
                        messageToSend.obj = jo;
                        handler.sendMessage(messageToSend);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;
                        messageToSend.obj = error.networkResponse.statusCode + "  -  " + error.toString();
                        handler.sendMessage(messageToSend);
                    }
                }) {
            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
            /*@Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                JSONObject jo = new JSONObject();

                params.put(Utility.Request, Utility.TriggerAdd);
                params.put(Utility.DeviceName, service.getDevice().getName());
                params.put(Utility.ServiceName, service.getName());
                params.put(Utility.Condition, condition);
                params.put(Utility.Value, value);

                try {
                    jo.put(Utility.Type, type);
                    if (type.matches(TriggerElement.COMMAND)) {
                        jo.put(Utility.DeviceName, triggerElement.getDeviceName());
                        jo.put(Utility.ServiceName, triggerElement.getServiceName());
                        jo.put(Utility.Command, triggerElement.getCommandName());
                        if (argument != null)
                            jo.put(Utility.Argument, argument);
                    }
                    if (type.matches(TriggerElement.MAIL)) {
                        jo.put(Utility.MailAddress, PreferenceManager.getDefaultSharedPreferences(context).getString("dest_mail", ""));
                    }
                    if (type.matches(TriggerElement.NOTIFICATION)) {
                        jo.put(Utility.NotificationId, MainActivity.gcm_id);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                params.put(Utility.Then, jo.toString());
                return params;
            };*/
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 0 /* Retries*/ , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    /* Delete trigger */
    public void delTriggerElement(final Handler handler, final int id) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String ht = serverUrl + handleTriggerURL;
        Log.w("URL", ht);

        JSONObject params = new JSONObject();
        try {
            params.put(Utility.Request, Utility.TriggerRemove);
            params.put(Utility.ID, id + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, ht, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.w("CommandResponse", response.toString());
                        JSONObject jo = response;
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = EXECUTE_RESULT;
                        messageToSend.obj = jo;
                        handler.sendMessage(messageToSend);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;
                        messageToSend.obj = error.networkResponse.statusCode + "  -  " + error.toString();
                        handler.sendMessage(messageToSend);
                    }
                }) {
            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 0 /* Retries*/ , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }


    /* Get last value of service */
    public void getLastValue(final Handler handler, DeviceService service) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String glv = serverUrl + getLastValueURL;
        Log.w("URL", glv);

        JSONObject params = new JSONObject();
        try {
            params.put(Utility.DeviceName, service.getDevice().getName());
            params.put(Utility.ServiceName, service.getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, glv, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.w("CommandResponse", response.toString());
                        JSONObject jo = response;
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = LAST_VALUE;
                        messageToSend.obj = jo;
                        handler.sendMessage(messageToSend);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;
                        messageToSend.obj = error.networkResponse.statusCode + "  -  " + error.toString();
                        handler.sendMessage(messageToSend);
                    }
                }) {
            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 0 /* Retries*/ , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }


    /* ThingSpeak Handling */
    public void setThingSpeak(final Handler handler, DeviceService service, String command) {
        RequestQueue queue = Volley.newRequestQueue(context);
        PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", "null");
        Log.w("URL", serverUrl);
        if (serverUrl.matches("null")) serverUrl = defaultURL+"";
        String ts = serverUrl + thingSpeakURL;
        Log.w("URL", ts);

        JSONObject params = new JSONObject();
        try {
            params.put(Utility.DeviceName, service.getDevice().getName());
            params.put(Utility.ServiceName, service.getName());
            params.put(Utility.Command, command);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, ts, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.w("CommandResponse", response.toString());
                        JSONObject jo = response;
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = THING_SPEAK;
                        try {
                            messageToSend.obj = jo.getString(Utility.Result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        handler.sendMessage(messageToSend);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Message messageToSend = handler.obtainMessage();
                        messageToSend.what = CONNECTION_ERROR;
                        messageToSend.obj = error.networkResponse.statusCode + "  -  " + error.toString();
                        handler.sendMessage(messageToSend);
                    }
                }) {
            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 0 /* Retries*/ , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }
}

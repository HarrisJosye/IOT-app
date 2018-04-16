package it.ex.routex;

/**
 * Created by ex on 22/06/16.
 */
public class Utility {

    /* Technologies Values */
    public static final String WiFi =      "WiFi";
    public static final String nRF24 =     "nRF24";
    public static final String Bluetooth = "Bluetooth";
    public static final String ZigBee =    "ZigBee";
    public static final String RFLink =    "RFLink";

    /* Hardware Values */
    public static final String Raspberry =    "Raspberry";
    public static final String Arduino =      "Arduino";
    public static final String Esp8266 =      "Esp8266";
    public static final String NodeMcu =      "NodeMcu";

    /* Json Values*/
    public static final String Name =           "name";
    public static final String Hardware =       "hardware";
    public static final String Technology =     "technology";
    public static final String Status =         "status";
    public static final String Devices =        "devices";
    public static final String DeviceName =     "device_name";
    public static final String Services =       "services";
    public static final String ServiceName =    "service_name";
    public static final String ServiceType =    "service_type";
    public static final String Get =            "Get";
    public static final String Command =        "command";
    public static final String Commands =       "commands";
    public static final String CommandName =    "command_name";
    public static final String CommandType =    "command_type";
    public static final String Argument =       "argument";
    public static final String Result =         "result";
    public static final String Timestamp =      "timestamp";
    public static final String StartTime =      "start_time";
    public static final String EndTime =        "end_time";
    public static final String Values =         "values";
    public static final String Value =          "value";
    public static final String Frequency =      "frequency";
    public static final String Time =           "time";
    public static final String Schedule =       "schedule";
    public static final String ScheduleCommand ="schedule_command";
    public static final String ScheduleAdd =    "A";
    public static final String ScheduleRemove = "R";
    public static final String TriggerAdd =     "A";
    public static final String TriggerRemove =  "R";
    public static final String Request =        "request";
    public static final String Condition =      "condition";
    public static final String Conditions =      "conditions";
    public static final String Then =           "then";
    public static final String Type =           "type";
    public static final String ID =             "id";
    public static final String Triggers =       "triggers";
    public static final String MailAddress =    "mail_address";
    public static final String NotificationId = "notification_id";
    public static final String AlwaysOn =       "always_on";
    public static final String Available =      "available";
    public static final String TextContent =    "text_content";


    /* Return the resource ID corresponding to the string passed as argument */
    public static int getIdFromValue(String v) {
        switch (v) {
            case Raspberry:
                return R.drawable.raspi;
            case Arduino:
                return R.drawable.arduino;
            case Esp8266:
                return R.drawable.esp8266;
            case NodeMcu:
                return R.drawable.nodemcu;
            case Bluetooth:
                return R.drawable.bluetooth;
            case WiFi:
                return R.drawable.wifi;
            case ZigBee:
                return R.drawable.zigbee;
            case RFLink:
                return R.drawable.rflink_rx;
            case nRF24:
                return R.drawable.nrf24;
            default:
                return R.drawable.icon_router_orange;
        }
    }
}

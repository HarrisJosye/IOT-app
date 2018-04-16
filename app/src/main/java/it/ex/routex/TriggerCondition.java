package it.ex.routex;

/**
 * Created by ex on 30/08/16.
 */
public class TriggerCondition {
    String deviceName;
    String serviceName;
    String condition;
    String value;

    public TriggerCondition(String dName, String sName, String cond, String val) {
        deviceName = dName;
        serviceName = sName;
        condition = cond;
        value = val;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getValue() {
        return value;
    }

    public String getCondition() {
        return condition;
    }
}

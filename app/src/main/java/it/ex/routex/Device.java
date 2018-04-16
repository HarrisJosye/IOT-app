package it.ex.routex;

import java.util.ArrayList;

/**
 * Created by ex on 21/06/16.
 */
public class Device {

    /* Device info */
    protected String name;
    //protected String hardware;
    protected boolean alwaysOn;
    protected String technology;

    /* Device Services */
    protected ArrayList<DeviceService> services;

    Device(String name, String technology, /*String hardware*/ boolean alwaysOn) {
        this.name = name;
        //this.hardware = hardware;
        this.alwaysOn = alwaysOn;
        this.technology = technology;
        this.services = new ArrayList<DeviceService>();
    }

    public String getName() {
        return name;
    }

    public String getTechnology() {
        return technology;
    }

    /*public String getHardware() {
        return hardware;
    }*/

    public boolean isAlwaysOn() {
        return alwaysOn;
    }

    public ArrayList<DeviceService> getServices() {
        return services;
    }

    public void addService(DeviceService s) {
        services.add(s);
    }

    public void setServices (ArrayList<DeviceService> s) {
        services = (ArrayList<DeviceService>) s.clone();
        for (int i = 0; i < services.size(); i++) services.get(i).assignToDevice(this);
    }


}

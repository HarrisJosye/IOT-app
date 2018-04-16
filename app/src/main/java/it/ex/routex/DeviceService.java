package it.ex.routex;

import java.util.ArrayList;

/**
 * Created by ex on 23/06/16.
 */
public class DeviceService {

    protected String name;
    protected String type;
    protected Device device;

    //protected boolean isSelectedInGraph;

    protected ArrayList<Command> commands;

    public DeviceService(String name, String type) {
        this.name = name;
        this.type = type;
        commands = new ArrayList<Command>();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void assignToDevice(Device d) {
        device = d;
    }

    public Device getDevice() {
        return this.device;
    }

    public void addCommand(Command c) {
        commands.add(c);
    }

    public ArrayList<Command> getCommands() {
        return this.commands;
    }
}

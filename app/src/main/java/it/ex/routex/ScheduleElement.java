package it.ex.routex;

/**
 * Created by ex on 03/07/16.
 */
public class ScheduleElement {

    public static char FREQUENCY = 'F';
    public static char TIME = 'T';

    protected String commandName;
    protected int frequency;
    protected String time;
    protected String argument;
    protected char type;
    protected int id;

    public ScheduleElement(String c, int f, int id) {
        commandName = c;
        frequency = f;
        argument = null;
        this.id = id;
        type = FREQUENCY;
    }

    public ScheduleElement(String c, String time, int id) {
        commandName = c;
        this.time = time;
        argument = null;
        this.id = id;
        type = TIME;
    }

    public ScheduleElement(String c, int f, String arg, int id) {
        commandName = c;
        frequency = f;
        argument = arg;
        this.id = id;
        type = FREQUENCY;
    }

    public ScheduleElement(String c, String time, String arg, int id) {
        commandName = c;
        this.time = time;
        argument = arg;
        this.id = id;
        type = TIME;
    }

    public String getCommandName() {
        return commandName;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getTime() {
        return time;
    }

    public int getId() {
        return id;
    }

    public String getArgument() {
        return argument;
    }
}

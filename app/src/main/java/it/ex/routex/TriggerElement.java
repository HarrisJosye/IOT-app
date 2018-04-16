package it.ex.routex;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by ex on 05/07/16.
 */
public class TriggerElement {

    public final static String COMMAND = "command";
    public final static String MAIL = "mail";
    public final static String NOTIFICATION = "notification";

    String reactDeviceName;
    String reactServiceName;
    String reactCommandName;
    String reactCommandType;

    ArrayList<TriggerCondition> conditions;

    String argument;
    String type;
    int id;

    /* Constructor for command list */
    public TriggerElement(String d, String s, String c, String t) {
        reactDeviceName = d;
        reactServiceName = s;
        reactCommandName = c;
        reactCommandType = t;
    }

    /* Constructor for Command Trigger */
    public TriggerElement(String d, String s, String c, int id) {
        reactDeviceName = d;
        reactServiceName = s;
        reactCommandName = c;
        conditions = new ArrayList<TriggerCondition>();

        this.id = id;

        argument = null;

        this.type = COMMAND;
    }

    /* Constructor for Command Trigger */
    public TriggerElement(String d, String s, String c, String arg, int id) {
        reactDeviceName = d;
        reactServiceName = s;
        reactCommandName = c;
        conditions = new ArrayList<TriggerCondition>();

        this.id = id;

        argument = arg;

        this.type = COMMAND;
    }

    /* Constructor for Mail & Notification Trigger */
    public TriggerElement(int id, String type) {
        this.id = id;
        conditions = new ArrayList<TriggerCondition>();

        this.type = type;
    }

    public void addCondition(TriggerCondition cond) {
        this.conditions.add(cond);
    }

    public ArrayList<TriggerCondition> getConditions() {
        return conditions;
    }

    public String getDeviceName() {
        return reactDeviceName;
    }

    public String getServiceName() {
        return reactServiceName;
    }

    public String getCommandName() {
        return reactCommandName;
    }

    public String getCommandType() {
        return reactCommandType;
    }

    public int getId() {
        return id;
    }

    public String getAction(Context c) {
        switch (this.type) {
            case MAIL:
                return c.getResources().getString(R.string.mail);
            case NOTIFICATION:
                return c.getResources().getString(R.string.notification);
            case COMMAND:
                if (argument == null || argument.matches(""))
                    return getDeviceName() + " - " + getServiceName() + " - " + getCommandName();
                else
                    return getDeviceName() + " - " + getServiceName() + " - " + getCommandName() + " (" + argument + ")";
            default:
                return "";
        }
    }

}

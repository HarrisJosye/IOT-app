package it.ex.routex;

public class Command {

    protected String type;
    protected String name;

    public Command(String name, String type) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}

package com.tilled.service;

import com.tilled.data.Command;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry implements Service<Command> {

    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry() {}

    public void register(Command command) {
        commands.put(command.name().toLowerCase(), command);
    }

    public Command get(String name) {
        if (name == null) {
            return null;
        }

        return commands.get(name.toLowerCase());
    }

    public boolean contains(String name) {
        return name != null && commands.containsKey(name.toLowerCase());
    }

    public void unregister(String name) {
        if (name != null) {
            commands.remove(name.toLowerCase());
        }
    }

    public void clear() {
        commands.clear();
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    public int size() {
        return commands.size();
    }
}
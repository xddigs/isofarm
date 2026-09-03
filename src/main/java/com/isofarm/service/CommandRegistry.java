package com.isofarm.service;

import com.isofarm.data.Command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry implements Service<Command> {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry() {
    }

    public void register(Command command) {
        if (command == null || command.name() == null) {
            return;
        }

        commands.put(normalize(command.name()), command);
    }

    public Command get(String name) {
        if (name == null) {
            return null;
        }

        return commands.get(normalize(name));
    }

    public boolean contains(String name) {
        return name != null && commands.containsKey(normalize(name));
    }

    public void unregister(String name) {
        if (name != null) {
            commands.remove(normalize(name));
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

    public Map<String, Command> getCommands() {
        return commands;
    }

    public List<String> getNames() {
        return commands.keySet()
                .stream()
                .sorted()
                .toList();
    }

    private String normalize(String name) {
        return name.startsWith("/")
                ? name.substring(1).toLowerCase()
                : name.toLowerCase();
    }
}
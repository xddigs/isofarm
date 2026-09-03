package com.isofarm.service;

import com.isofarm.data.Command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides command registry behavior.
 */
public class CommandRegistry implements Service<Command> {
    private final Map<String, Command> commands = new HashMap<>();

    /**
     * Creates a new {@code CommandRegistry} instance.
     */
    public CommandRegistry() {
    }

    /**
     * Performs the register operation.
     * @param command the command value
     */
    public void register(Command command) {
        if (command == null || command.name() == null) {
            return;
        }

        commands.put(normalize(command.name()), command);
    }

    /**
     * Returns get.
     * @param name the name value
     * @return the get result
     */
    public Command get(String name) {
        if (name == null) {
            return null;
        }

        return commands.get(normalize(name));
    }

    /**
     * Performs the contains operation.
     * @param name the name value
     * @return the contains result
     */
    public boolean contains(String name) {
        return name != null && commands.containsKey(normalize(name));
    }

    /**
     * Performs the unregister operation.
     * @param name the name value
     */
    public void unregister(String name) {
        if (name != null) {
            commands.remove(normalize(name));
        }
    }

    /**
     * Removes clear.
     */
    public void clear() {
        commands.clear();
    }

    /**
     * Checks whether the empty condition is met.
     * @return {@code true} if empty; otherwise {@code false}
     */
    public boolean isEmpty() {
        return commands.isEmpty();
    }

    /**
     * Performs the size operation.
     * @return the size result
     */
    public int size() {
        return commands.size();
    }

    /**
     * Returns the commands.
     * @return the commands
     */
    public Map<String, Command> getCommands() {
        return commands;
    }

    /**
     * Returns the names.
     * @return the names
     */
    public List<String> getNames() {
        return commands.keySet()
                .stream()
                .sorted()
                .toList();
    }

    /**
     * Performs the normalize operation.
     * @param name the name value
     * @return the normalize result
     */
    private String normalize(String name) {
        return name.startsWith("/")
                ? name.substring(1).toLowerCase()
                : name.toLowerCase();
    }
}
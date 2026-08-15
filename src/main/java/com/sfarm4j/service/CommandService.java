package com.sfarm4j.service;

import com.sfarm4j.data.Command;

public class CommandService implements Service<Command> {

    private final CommandRegistry registry;

    public CommandService(CommandRegistry registry) {
        this.registry = registry;
    }

    public void execute(String input) {
        if (input == null || input.isBlank()) {
            return;
        }

        input = input.trim();
        if (input.startsWith("/")) {
            input = input.substring(1);
        }

        String[] tokens = input.split("\\s+");
        if (tokens.length == 0) {
            return;
        }

        String commandName = tokens[0];
        Command command = registry.get(commandName);
        if (command == null) {
            return;
        }

        String[] args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, args, 0, args.length);
        command.action().accept(args);
    }
}
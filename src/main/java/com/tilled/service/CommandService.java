package com.tilled.service;

import com.tilled.data.Command;

public class CommandService implements Service<Command> {
    private final CommandRegistry registry;
    private final ToastService toastService;

    public CommandService(CommandRegistry registry, ToastService toastService) {
        this.registry = registry;
        this.toastService = toastService;
    }

    public void execute(String input) {
        if (input == null || input.isBlank()) {
            return;
        }

        input = input.trim();
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
        toastService.success("You ran= " + commandName + " " +
                String.join(" ", args));
    }
}
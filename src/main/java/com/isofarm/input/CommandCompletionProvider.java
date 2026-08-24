package com.isofarm.input;

import com.isofarm.data.Command;
import com.isofarm.data.CommandArgument;
import com.isofarm.data.CompletionProvider;
import com.isofarm.service.CommandRegistry;

import java.util.ArrayList;
import java.util.List;

public class CommandCompletionProvider implements CompletionProvider {

    private final CommandRegistry commandRegistry;

    public CommandCompletionProvider(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    @Override
    public List<String> complete(String text, int cursorPosition) {
        if (text == null || cursorPosition <= 0) {
            return List.of();
        }
        String beforeCursor = text.substring(0, cursorPosition);
        if (!beforeCursor.contains(" ")) {
            return completeCommand(beforeCursor);
        }
        return completeArgument(beforeCursor);
    }

    private List<String> completeCommand(String text) {
        if (!text.startsWith("/")) {
            return List.of();
        }

        String prefix = text.substring(1).toLowerCase();
        List<String> result = new ArrayList<>();
        for (String commandName : commandRegistry.getCommands().keySet()) {
            String normalized = commandName.startsWith("/") ? commandName.substring(1) : commandName;
            if (normalized.toLowerCase().startsWith(prefix)) {
                result.add("/" + normalized);
            }
        }
        result.sort(String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    private List<String> completeArgument(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }

        String commandName = trimmed.split("\\s+", 2)[0];
        Command command = commandRegistry.get(commandName);
        if (command == null) {
            return List.of();
        }

        String argumentPart = text.substring(commandName.length());

        if (argumentPart.startsWith(" ")) {
            argumentPart = argumentPart.substring(1);
        }

        String[] arguments = argumentPart.isEmpty() ? new String[]{""} : argumentPart.split("\\s+", -1);
        int argumentIndex = arguments.length - 1;
        if (text.endsWith(" ")) {
            argumentIndex = arguments.length - 1;
            if (arguments.length > 0 && arguments[arguments.length - 1].isEmpty()) {
                argumentIndex = arguments.length - 1;
            }
        }

        if (argumentIndex < 0 || argumentIndex >= command.args().length) {
            return List.of();
        }

        CommandArgument argument = command.args()[argumentIndex];
        if (argument == null || !argument.hasCompletion()) {
            return List.of();
        }

        String input = arguments[argumentIndex];
        return argument.complete(input, argumentIndex);
    }
}
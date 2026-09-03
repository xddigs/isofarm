package com.isofarm.input;

import com.isofarm.data.Command;
import com.isofarm.data.CommandArgument;
import com.isofarm.data.CompletionProvider;
import com.isofarm.service.CommandRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides command completion provider behavior.
 */
public class CommandCompletionProvider implements CompletionProvider {
    private final CommandRegistry commandRegistry;
    /**
     * Creates a new {@code CommandCompletionProvider} instance.
     * @param commandRegistry the command registry value
     */
    public CommandCompletionProvider(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    /**
     * Performs the complete operation.
     * @param text the text value
     * @param cursorPosition the cursor position value
     * @return the complete result
     */
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

    /**
     * Performs the complete command operation.
     * @param text the text value
     * @return the complete command result
     */
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

    /**
     * Performs the complete argument operation.
     * @param text the text value
     * @return the complete argument result
     */
    private List<String> completeArgument(String text) {
        String trimmedLead = text.stripLeading();
        if (trimmedLead.isEmpty()) {
            return List.of();
        }

        String[] parts = trimmedLead.split("\\s+");
        String commandName = parts[0];
        Command command = commandRegistry.get(commandName);
        if (command == null) {
            return List.of();
        }

        int firstSpaceIndex = text.indexOf(' ');
        if (firstSpaceIndex == -1) {
            return List.of();
        }

        String argsSubstring = text.substring(firstSpaceIndex + 1);
        String[] args = argsSubstring.split("\\s+", -1);
        int argumentIndex = args.length - 1;

        if (argumentIndex < 0 || argumentIndex >= command.args().length) {
            return List.of();
        }

        CommandArgument argument = command.args()[argumentIndex];
        if (argument == null || !argument.hasCompletion()) {
            return List.of();
        }

        String inputPrefix = args[argumentIndex];
        return argument.complete(inputPrefix, argumentIndex);
    }
}
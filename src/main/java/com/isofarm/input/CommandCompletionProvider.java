package com.isofarm.input;

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
        String token = getCurrentToken(text, cursorPosition);
        if (!token.startsWith("/")) {
            return List.of();
        }

        String prefix = token.substring(1).toLowerCase();
        List<String> result = new ArrayList<>();
        for (String command : commandRegistry.getCommands().keySet()) {
            if (command.startsWith(prefix)) {
                result.add("/" + command);
            }
        }

        result.sort(String.CASE_INSENSITIVE_ORDER);

        return result;
    }

    private String getCurrentToken(String text, int cursorPosition) {
        int start = cursorPosition;
        while (start > 0 && !Character.isWhitespace(text.charAt(start - 1))) {
            start--;
        }
        return text.substring(start, cursorPosition);
    }
}
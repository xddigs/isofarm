package com.isofarm.data;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Stores command argument data.
 */
@DataClass
public record CommandArgument(String name, BiFunction<String, Integer, List<String>> completion) {

    /**
     * Creates a new {@code CommandArgument} instance.
     * @param name the name value
     */
    public CommandArgument(String name) {
        this(name, null);
    }

    /**
     * Performs the of operation.
     * @param name the name value
     * @param completion the completion value
     * @return the of result
     */
    public static CommandArgument of(String name, BiFunction<String, Integer, List<String>> completion) {
        return new CommandArgument(name, completion);
    }

    /**
     * Checks whether the completion condition is met.
     * @return {@code true} if completion; otherwise {@code false}
     */
    public boolean hasCompletion() {
        return completion != null;
    }

    /**
     * Performs the complete operation.
     * @param text the text value
     * @param cursorPosition the cursor position value
     * @return the complete result
     */
    public List<String> complete(String text, int cursorPosition) {
        if (completion == null) {
            return List.of();
        }

        if (text == null) {
            text = "";
        }

        cursorPosition = Math.clamp(cursorPosition, 0, text.length());
        return completion.apply(text, cursorPosition);
    }
}
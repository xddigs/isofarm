package com.isofarm.data;

import java.util.List;
import java.util.function.BiFunction;

@DataClass
public record CommandArgument(String name, BiFunction<String, Integer, List<String>> completion) {

    public CommandArgument(String name) {
        this(name, null);
    }

    public static CommandArgument of(String name, BiFunction<String, Integer, List<String>> completion) {
        return new CommandArgument(name, completion);
    }

    public boolean hasCompletion() {
        return completion != null;
    }

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
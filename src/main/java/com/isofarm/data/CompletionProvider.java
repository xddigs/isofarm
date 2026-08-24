package com.isofarm.data;

import java.util.List;

@DataClass
@FunctionalInterface
public interface CompletionProvider {
    List<String> complete(String text, int cursorPosition);
}
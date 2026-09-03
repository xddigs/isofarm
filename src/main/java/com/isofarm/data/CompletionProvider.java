package com.isofarm.data;

import java.util.List;

/**
 * Defines the completion provider contract.
 */
@DataClass
@FunctionalInterface
public interface CompletionProvider {
    /**
     * Performs the complete operation.
     * @param text the text value
     * @param cursorPosition the cursor position value
     * @return the complete result
     */
    List<String> complete(String text, int cursorPosition);
}
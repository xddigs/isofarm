package com.isofarm.data;

import java.util.function.Consumer;

/**
 * Stores command data.
 */
@DataClass
public record Command(
        String name,
        CommandArgument[] args,
        Consumer<String[]> action) {}
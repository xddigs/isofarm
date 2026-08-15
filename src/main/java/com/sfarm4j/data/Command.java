package com.sfarm4j.data;

import java.util.function.Consumer;

@DataClass
public record Command(
        String name,
        String[] args,
        Consumer<String[]> action
) {}
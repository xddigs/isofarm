package com.isofarm.data;

import java.util.function.Consumer;

@DataClass
public record Command(
        String name,
        CommandArgument[] args,
        Consumer<String[]> action) {}
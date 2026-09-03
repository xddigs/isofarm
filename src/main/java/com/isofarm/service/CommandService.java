package com.isofarm.service;

import com.isofarm.data.Command;
import com.isofarm.gui.GameUIService;
import com.isofarm.utils.ToastFactory;

/**
 * Provides command service behavior.
 */
public class CommandService implements Service<Command> {
    private final CommandRegistry registry;
    private GameUIService gameUIService;

    /**
     * Creates a new {@code CommandService} instance.
     * @param registry the registry value
     */
    public CommandService(CommandRegistry registry) {
        this.registry = registry;
    }

    /**
     * Sets the game uiservice.
     * @param gameUIService the game uiservice value
     */
    public void setGameUIService(GameUIService gameUIService) {
        this.gameUIService = gameUIService;
    }

    /**
     * Performs the execute operation.
     * @param input the input value
     */
    public void execute(String input) {
        if (input == null || input.isBlank()) return;
        if (!input.startsWith("/")) return;

        input = input.trim();
        String[] tokens = input.split("\\s+");
        if (tokens.length == 0) return;

        String commandName = tokens[0];
        Command command = registry.get(commandName);
        if (command == null) {
            if (gameUIService != null) {
                ToastFactory.error("Command not found: " + commandName);
            }
            return;
        }

        String[] args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, args, 0, args.length);
        command.action().accept(args);
    }
}
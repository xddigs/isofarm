package com.isofarm.service;

import com.isofarm.data.Command;
import com.isofarm.gui.GameUIService;

public class CommandService implements Service<Command> {
    private final CommandRegistry registry;
    private GameUIService gameUIService;

    public CommandService(CommandRegistry registry) {
        this.registry = registry;
    }

    public void setGameUIService(GameUIService gameUIService) {
        this.gameUIService = gameUIService;
    }

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
                gameUIService.addChatMessage("Command not found: " + commandName);
            }
            return;
        }

        String[] args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, args, 0, args.length);
        command.action().accept(args);
        
        String result = "You ran: " + commandName + " " + String.join(" ", args);
        if (gameUIService != null) {
            gameUIService.addChatMessage(result);
        }
    }
}
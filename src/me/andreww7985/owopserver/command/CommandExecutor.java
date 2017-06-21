package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.server.Player;

public interface CommandExecutor {
	public CommandResult execute(String name, String[] parameters, Player player);
}

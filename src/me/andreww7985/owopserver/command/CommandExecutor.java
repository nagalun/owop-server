package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.server.Player;

public interface CommandExecutor {
	public void execute(String name, String[] parameters, Player player);
}

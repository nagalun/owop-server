package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.game.Player;

public interface CommandExecutor {
	public CommandResult execute(String name, String[] arguments, Player sender);
}

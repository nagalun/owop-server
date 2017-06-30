package me.andreww7985.owopserver.server;

import java.util.HashMap;

import me.andreww7985.owopserver.command.Command;
import me.andreww7985.owopserver.command.CommandExecutor;
import me.andreww7985.owopserver.command.CommandResult;
import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.helper.ChatHelper;

public class CommandManager {
	private final HashMap<String, CommandExecutor> executors = new HashMap<String, CommandExecutor>();
	private final HashMap<String, Command> commands = new HashMap<String, Command>();

	public void executeCommand(final String name, final String[] parameters, final Player player) {
		if (executors.containsKey(name)) {
			if (executors.get(name).execute(name, parameters, player) == CommandResult.WRONG_ARGUMENTS) {
				player.sendMessage(ChatHelper.RED + "Usage: "
						+ OWOPServer.getInstance().getCommandManager().getCommand(name).getUsage());
			}
		} else {
			player.sendMessage(ChatHelper.RED + "Unknown command! Type '/help' for a list of commands.");
		}
	}

	public Command getCommand(final String command) {
		return commands.get(command);
	}

	public void registerCommand(final Command command) {
		if (executors.containsKey(command.getName()) || commands.containsKey(command.getName())) {
			OWOPServer.getInstance().getLogManager().err("Command already registered : " + command.getName());
		} else {
			commands.put(command.getName(), command);
			executors.put(command.getName(), command);
		}
	}

	public Object[] getCommands() {
		return commands.values().toArray();
	}
}

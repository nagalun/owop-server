package me.andreww7985.owopserver.server;

import java.util.HashMap;

import me.andreww7985.owopserver.command.Command;
import me.andreww7985.owopserver.command.CommandExecutor;

public class CommandManager {
	private final HashMap<String, CommandExecutor> executors = new HashMap<String, CommandExecutor>();
	private final HashMap<String, Command> commands = new HashMap<String, Command>();

	public void executeCommand(final String name, final String[] parameters, final Player player) {
		if (executors.containsKey(name)) {
			executors.get(name).execute(name, parameters, player);
		} else {
			OWOPServer.getInstance().getLogger().err("Command not found : " + name);
		}
	}

	public Command getCommand(final String command) {
		return commands.get(command);
	}

	public void registerCommand(final Command command) {
		if (executors.containsKey(command.getName()) || commands.containsKey(command.getName())) {
			OWOPServer.getInstance().getLogger().err("Command already registered : " + command.getName());
		} else {
			commands.put(command.getName(), command);
			executors.put(command.getName(), command);
		}
	}

	public Object[] getCommands() {
		return commands.values().toArray();
	}
}

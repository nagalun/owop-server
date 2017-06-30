package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.server.OWOPServer;

public class ShutdownCommand extends Command {

	public ShutdownCommand() {
		super("Shutdown server", "/shutdown", "shutdown");
	}

	@Override
	public CommandResult execute(final String name, final String[] arguments, final Player sender) {
		if (sender.isAdmin()) {
			OWOPServer.getInstance().shutdown();
		}
		return CommandResult.OK;
	}
}

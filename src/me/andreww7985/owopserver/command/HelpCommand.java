package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.server.OWOPServer;
import me.andreww7985.owopserver.server.Player;

public class HelpCommand extends Command {

	public HelpCommand() {
		super("Show list of commands", "/help", "help");
	}

	@Override
	public CommandResult execute(final String name, final String[] parameters, final Player player) {
		if (player.isAdmin()) {
			for (final Command command : OWOPServer.getInstance().getCommandManager().getCommands()) {
				player.sendMessage(ChatHelper.LIME + "/" + command.getName() + " - " + command.getDescription());
			}
		}
		return CommandResult.OK;
	}
}

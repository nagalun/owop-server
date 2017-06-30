package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.server.OWOPServer;

public class HelpCommand extends Command {

	public HelpCommand() {
		super("Show list of commands", "/help OR /help <command>", "help");
	}

	@Override
	public CommandResult execute(final String name, final String[] arguments, final Player sender) {
		if (sender.isAdmin()) {
			if (arguments.length > 0) {
				final Command command = OWOPServer.getInstance().getCommandManager().getCommand(arguments[0]);
				if (command == null) {
					sender.sendMessage(ChatHelper.RED + "Can't find that command!");
					return CommandResult.OK;
				}
				sender.sendMessage(ChatHelper.BLUE + ChatHelper.CENTER + "Help - " + command.getName());
				sender.sendMessage(ChatHelper.BLUE + "Description: " + command.getDescription());
				sender.sendMessage(ChatHelper.BLUE + "Usage: " + command.getUsage());
			} else {
				sender.sendMessage(ChatHelper.BLUE + ChatHelper.CENTER + "Help");
				for (final Object command : OWOPServer.getInstance().getCommandManager().getCommands()) {
					sender.sendMessage(ChatHelper.BLUE + "/" + ((Command) command).getName() + " - "
							+ ((Command) command).getDescription());
				}
			}
		}
		return CommandResult.OK;
	}
}

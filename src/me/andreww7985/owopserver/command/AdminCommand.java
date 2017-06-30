package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.server.OWOPServer;

public class AdminCommand extends Command {

	public AdminCommand() {
		super("Enable admin mode", "/admin <password>", "admin");
	}

	@Override
	public CommandResult execute(final String name, final String[] arguments, final Player sender) {
		if (arguments.length == 1) {
			if (arguments[0].equals(OWOPServer.getInstance().getAdminPassword())) {
				sender.setAdmin(true);
				sender.sendMessage(ChatHelper.LIME + "Admin mode enabled! Type '/help' for a list of commands.");
			} else {
				sender.kick();
			}
		}
		return CommandResult.OK;
	}
}

package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.server.ChatHelper;
import me.andreww7985.owopserver.server.OWOPServer;
import me.andreww7985.owopserver.server.Player;

public class AdminCommand extends Command {

	public AdminCommand() {
		super("Enable admin mode", "/admin <password>", "admin");
	}

	@Override
	public CommandResult execute(final String name, final String[] parameters, final Player player) {
		if (parameters.length == 1) {
			if (parameters[0].equals(OWOPServer.getInstance().getAdminPassword())) {
				player.setAdmin(true);
				player.sendMessage(ChatHelper.LIME + "Admin mode enabled! Type '/help' for a list of commands.");
			} else {
				player.kick();
			}
		}
		return CommandResult.OK;
	}
}

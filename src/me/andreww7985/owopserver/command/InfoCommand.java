package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.server.OWOPServer;
import me.andreww7985.owopserver.server.Player;

public class InfoCommand extends Command {

	public InfoCommand() {
		super("Show server information", "/info", "info");
	}

	@Override
	public CommandResult execute(final String name, final String[] parameters, final Player player) {
		if (player.isAdmin()) {
			player.sendMessage(ChatHelper.LIME + "Total online: " + OWOPServer.getInstance().getTotalOnline());
			player.sendMessage(
					ChatHelper.LIME + "Total chunks loaded: " + OWOPServer.getInstance().getTotalChunksLoaded());
			player.sendMessage(ChatHelper.LIME + "Memory used: "
					+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MB");
		}
		return CommandResult.OK;
	}
}

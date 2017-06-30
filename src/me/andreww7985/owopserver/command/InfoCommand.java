package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.server.OWOPServer;

public class InfoCommand extends Command {

	public InfoCommand() {
		super("Show server information", "/info", "info");
	}

	@Override
	public CommandResult execute(final String name, final String[] arguments, final Player sender) {
		if (sender.isAdmin()) {
			sender.sendMessage(ChatHelper.BLUE + "Total online: " + OWOPServer.getInstance().getTotalOnline());
			sender.sendMessage(
					ChatHelper.BLUE + "Total chunks loaded: " + OWOPServer.getInstance().getTotalChunksLoaded());
			sender.sendMessage(ChatHelper.BLUE + "Memory used: "
					+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MB");
		}
		return CommandResult.OK;
	}
}

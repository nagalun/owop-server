package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.server.OWOPServer;
import me.andreww7985.owopserver.server.Player;

public class KickCommand extends Command {

	public KickCommand() {
		super("Kick player", "/kick <ID>", "kick");
	}

	@Override
	public CommandResult execute(final String name, final String[] parameters, final Player player) {
		if (player.isAdmin()) {
			if (parameters.length == 1) {
				int id;
				try {
					id = Integer.parseInt(parameters[0]);
				} catch (final Exception e) {
					return CommandResult.WRONG_ARGUMENTS;
				}
				final Player toPlayer = OWOPServer.getInstance().getPlayer(id);
				if (toPlayer == null) {
					player.sendMessage(ChatHelper.RED + "Can't find that player!");
					return CommandResult.OK;
				}
				toPlayer.kick();
			} else {
				return CommandResult.WRONG_ARGUMENTS;
			}
		}
		return CommandResult.OK;
	}
}

package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.server.ChatHelper;
import me.andreww7985.owopserver.server.OWOPServer;
import me.andreww7985.owopserver.server.Player;

public class TeleportCommand extends Command {

	public TeleportCommand() {
		super("Teleport to player or coordinates", "/tp <X> <Y> OR /tp <ID>", "tp");
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
				player.teleport(toPlayer.getX() >> 4, toPlayer.getY() >> 4);
			} else if (parameters.length == 2) {
				int x, y;
				try {
					x = Integer.parseInt(parameters[0]);
					y = Integer.parseInt(parameters[1]);
				} catch (final Exception e) {
					return CommandResult.WRONG_ARGUMENTS;
				}
				player.teleport(x, y);
			} else {
				return CommandResult.WRONG_ARGUMENTS;
			}
		}
		return CommandResult.OK;
	}
}

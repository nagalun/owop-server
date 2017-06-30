package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.helper.ChatHelper;

public class TeleportCommand extends Command {

	public TeleportCommand() {
		super("Teleport to player or coordinates", "/tp <X> <Y> OR /tp <ID>", "tp");
	}

	@Override
	public CommandResult execute(final String name, final String[] arguments, final Player sender) {
		if (sender.isAdmin()) {
			if (arguments.length == 1) {
				int id;
				try {
					id = Integer.parseInt(arguments[0]);
				} catch (final Exception e) {
					return CommandResult.WRONG_ARGUMENTS;
				}
				final Player toPlayer = sender.getWorld().getPlayer(id);
				if (toPlayer == null) {
					sender.sendMessage(ChatHelper.RED + "Can't find that player!");
					return CommandResult.OK;
				}
				sender.teleport(toPlayer.getX() >> 4, toPlayer.getY() >> 4);
			} else if (arguments.length == 2) {
				int x, y;
				try {
					x = Integer.parseInt(arguments[0]);
					y = Integer.parseInt(arguments[1]);
				} catch (final Exception e) {
					return CommandResult.WRONG_ARGUMENTS;
				}
				sender.teleport(x, y);
			} else {
				return CommandResult.WRONG_ARGUMENTS;
			}
		}
		return CommandResult.OK;
	}
}

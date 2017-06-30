package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.helper.ChatHelper;

public class KickCommand extends Command {

	public KickCommand() {
		super("Kick player", "/kick <ID>", "kick");
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
				final Player player = sender.getWorld().getPlayer(id);
				if (player == null) {
					sender.sendMessage(ChatHelper.RED + "Can't find that player!");
					return CommandResult.OK;
				}
				player.sendMessage(ChatHelper.RED + "You were kicked by admin!");
				player.kick();
			} else {
				return CommandResult.WRONG_ARGUMENTS;
			}
		}
		return CommandResult.OK;
	}
}

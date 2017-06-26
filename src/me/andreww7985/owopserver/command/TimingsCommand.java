package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.helper.TimingsHelper;
import me.andreww7985.owopserver.helper.TimingsHelper.TimingsEntry;
import me.andreww7985.owopserver.server.Player;

public class TimingsCommand extends Command {

	public TimingsCommand() {
		super("View what loads CPU and how", "/timings OR /timings <name>", "timings");
	}

	@Override
	public CommandResult execute(final String name, final String[] parameters, final Player player) {
		if (player.isAdmin()) {
			if (parameters.length > 0) {
				final TimingsEntry entry = TimingsHelper.getEntry(parameters[0]);
				if (entry.getMinimal() == Long.MAX_VALUE) {
					return CommandResult.WRONG_ARGUMENTS;
				}
				player.sendMessage(ChatHelper.LIME + ChatHelper.CENTER + entry.getName());
				player.sendMessage(ChatHelper.LIME + "Total time - " + entry.getTotal() + " ms");
				player.sendMessage(ChatHelper.LIME + "Maximal time - " + entry.getMaximal() + " ms");
				player.sendMessage(ChatHelper.LIME + "Minimal time - " + entry.getMinimal() + " ms");
				player.sendMessage(ChatHelper.LIME + "Average time - " + entry.getAverage() + " ms");
			} else {
				for (final TimingsEntry entry : TimingsHelper.getEntries()) {
					player.sendMessage(ChatHelper.LIME + entry.getName() + " - " + entry.getTotal() + " ms");
				}
			}
		}
		return CommandResult.OK;
	}
}

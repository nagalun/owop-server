package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.server.OWOPServer;
import me.andreww7985.owopserver.timings.TimingsEntry;

public class TimingsCommand extends Command {

	public TimingsCommand() {
		super("View what uses CPU and how", "/timings OR /timings <name>", "timings");
	}

	@Override
	public CommandResult execute(final String name, final String[] arguments, final Player sender) {
		if (sender.isAdmin()) {
			if (arguments.length > 0) {
				final TimingsEntry entry = OWOPServer.getInstance().getTimingsManager().getEntry(arguments[0]);
				if (entry.getMinimal() == Long.MAX_VALUE) {
					sender.sendMessage(ChatHelper.RED + "Can't find that entry!");
					return CommandResult.OK;
				}
				sender.sendMessage(ChatHelper.BLUE + ChatHelper.CENTER + "Timings - " + entry.getName());
				sender.sendMessage(ChatHelper.BLUE + "Total time  - " + entry.getTotal() + " ms");
				sender.sendMessage(ChatHelper.BLUE + "Maximal time - " + entry.getMaximal() + " ms");
				sender.sendMessage(ChatHelper.BLUE + "Minimal time - " + entry.getMinimal() + " ms");
				sender.sendMessage(ChatHelper.BLUE + "Average time - " + entry.getAverage() + " ms");
			} else {
				sender.sendMessage(ChatHelper.BLUE + ChatHelper.CENTER + "Timings");
				for (final Object entry : OWOPServer.getInstance().getTimingsManager().getEntries()) {
					sender.sendMessage(ChatHelper.BLUE + ((TimingsEntry) entry).getName() + " - "
							+ ((TimingsEntry) entry).getTotal() + " ms");
				}
			}
		}
		return CommandResult.OK;
	}
}

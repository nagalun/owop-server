package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.server.ChatHelper;
import me.andreww7985.owopserver.server.OWOPServer;
import me.andreww7985.owopserver.server.Player;

public class TeleportCommand extends Command {

	public TeleportCommand() {
		super("Teleport to player or coordinates", "/tp <X> <Y> OR /tp <ID>", "tp");
	}

	@Override
	public void execute(final String name, final String[] parameters, final Player player) {
		for (final Object command : OWOPServer.getInstance().getCommandManager().getCommands()) {
			player.sendMessage(ChatHelper.LIME + "/" + ((Command) command).getName() + " - "
					+ ((Command) command).getDescription());
		}
	}
}

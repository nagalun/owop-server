package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.server.OWOPServer;
import me.andreww7985.owopserver.server.Player;

public class ACommand extends Command {

	public ACommand() {
		super("Say something only to admins", "/a <text>", "a");
	}

	@Override
	public CommandResult execute(final String name, final String[] parameters, final Player player) {
		if (parameters.length > 0) {
			String text = ChatHelper.ORANGE + "[A] " + player.getID() + ": ";
			for (final String word : parameters) {
				text += word + " ";
			}
			OWOPServer.getInstance().broadcast(text, true);
		} else {
			return CommandResult.WRONG_ARGUMENTS;
		}
		return CommandResult.OK;
	}
}

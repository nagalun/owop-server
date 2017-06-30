package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.server.OWOPServer;

public class ACommand extends Command {

	public ACommand() {
		super("Say something only to admins", "/a <text>", "a");
	}

	@Override
	public CommandResult execute(final String name, final String[] arguments, final Player sender) {
		if (arguments.length > 0) {
			String text = ChatHelper.ORANGE + "[A] " + sender.getID() + ": ";
			for (final String word : arguments) {
				text += word + " ";
			}
			OWOPServer.getInstance().broadcast(text, true);
		} else {
			return CommandResult.WRONG_ARGUMENTS;
		}
		return CommandResult.OK;
	}
}

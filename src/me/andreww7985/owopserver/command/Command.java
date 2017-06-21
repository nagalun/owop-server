package me.andreww7985.owopserver.command;

import me.andreww7985.owopserver.server.ChatHelper;

public abstract class Command implements CommandExecutor {
	private final String description, usage, name;

	public Command(final String description, final String usage, final String name) {
		this.description = ChatHelper.format(description);
		this.usage = ChatHelper.format(usage);
		this.name = ChatHelper.format(name);
	}

	public String getDescription() {
		return description;
	}

	public String getUsage() {
		return usage;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name + " - " + description + " - " + usage;
	}
}

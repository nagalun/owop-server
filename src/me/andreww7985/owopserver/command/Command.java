package me.andreww7985.owopserver.command;

public abstract class Command implements CommandExecutor {
	private final String description, usage, name;

	public Command(final String description, final String usage, final String name) {
		this.description = description;
		this.usage = usage;
		this.name = name;
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

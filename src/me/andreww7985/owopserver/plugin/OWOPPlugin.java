package me.andreww7985.owopserver.plugin;

public abstract class OWOPPlugin {
	private final String name, version, description;

	public OWOPPlugin(final String name, final String version, final String description) {
		this.name = name;
		this.version = version;
		this.description = description;
	}

	abstract void onEnable();

	abstract void onDisable();

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getDescription() {
		return description;
	}
}

package me.andreww7985.owopserver.event;

public abstract class Event {
	private boolean cancelled = false;

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(final boolean value) {
		cancelled = value;
	}
}

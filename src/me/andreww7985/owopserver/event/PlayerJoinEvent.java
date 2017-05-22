package me.andreww7985.owopserver.event;

import me.andreww7985.owopserver.server.Player;

public class PlayerJoinEvent extends Event {
	private final Player player;

	public PlayerJoinEvent(final Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}
}